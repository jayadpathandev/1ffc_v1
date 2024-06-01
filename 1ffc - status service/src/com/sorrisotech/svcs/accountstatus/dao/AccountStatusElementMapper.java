package com.sorrisotech.svcs.accountstatus.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AcctStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AchEnabled;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AutoPmtStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.ContPrefsStats;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.PayEnabled;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.ViewAcct;

/**
 * Map a row in the result set into an AccountStatusElement for use in 
 * the cache
 * 
 * @author - John A. Kowalonek
 * @since - 09-Oct-2023
 * @version - 22-Jan-2024  jak added current amount due and convenience fee
 * @version - 1-Feb-2024   jak added account_balance
 * @version - 30-May-2024  jak added payment due date
 */
public class AccountStatusElementMapper implements RowMapper<AccountStatusElement>{
	
	private static final Logger LOG = LoggerFactory.getLogger(AccountStatusElementMapper.class);

	@Override
	public AccountStatusElement mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		AccountStatusElement lAcctStats = new AccountStatusElement();

		// -- lots can break if these are null in the database, so we will trust them --
		lAcctStats.setStatusGroupId(arg0.getString("status_group"));    
		lAcctStats.setPaymentGroupId(arg0.getString("payment_group"));  
		lAcctStats.setCustomerId(arg0.getString("customer_id"));		
		lAcctStats.setAccountId(arg0.getString("account_number"));		
		lAcctStats.setPaymentDueDate(getSafeBigDecimal(arg0.getString("due_date"), "due_date", lAcctStats));
		
		LOG.debug("mapRow for status group {}, customer id {}, account number {}.",
				lAcctStats.getStatusGroupId(), lAcctStats.getCustomerId(), lAcctStats.getAccountId());
		
		// -- need to retrieve these "safely" since they are flex fields and we need to note
		//		any problems as errors and set default values --
		lAcctStats.setFinalBill( getSafeBooleanDbValue ( arg0.getString("final_bill"), false, "final_bill", lAcctStats));
		lAcctStats.setAcctClosed( getSafeBooleanDbValue ( arg0.getString("account_closed"), false, "account_closed", lAcctStats));
		lAcctStats.setPayEnabled( getSafePaymentDisabledReason(arg0.getString("payment_disabled_reason"), lAcctStats));
		lAcctStats.setAchEnabled( getSafeAchDisabledReason(arg0.getString("ach_disabled_reason"), lAcctStats));
		lAcctStats.setViewAccount( getSafeViewDisabledReason(arg0.getString("portal_access_disabled_reason"), lAcctStats));
		lAcctStats.setOrigLoanAmt(getSafeBigDecimal(arg0.getString("original_loan_amount"), "original_loan_amount", lAcctStats));
		lAcctStats.setMonthlyPayment(getSafeBigDecimal(arg0.getString("monthly_payment_amount"), "monthly_payment_amount", lAcctStats));
		lAcctStats.setTotalNumPayments(getSafeInteger(arg0.getString("total_num_payments"), "total_num_payments", lAcctStats));
		lAcctStats.setRemainingNumPayments(getSafeInteger(arg0.getString("num_payments_remaining"), "num_payments_remaining", lAcctStats));
		lAcctStats.setMostRecentUpdate(arg0.getInt("most_recent")); // date stored as numeric
		
		// -- this one is a little odd because we "infer the status based on query results returned --
		if (lAcctStats.isAcctClosed())
			lAcctStats.setAcctStatus(AcctStatus.closedAccount);
		else if (arg0.getString("total_num_payments").equalsIgnoreCase(arg0.getString("num_payments_remaining")))
			 // this doesn't tell us there are no bills, but tells us there aren't any payments yet. We need to check for bills in the use case. --
			lAcctStats.setAcctStatus(AcctStatus.newAccount);  // this doesn't tell us there are no bills, but tells us there aren't any payments yet.
		else
			lAcctStats.setAcctStatus(AcctStatus.activeAccount);
		
		lAcctStats.setPortalEligible(getSafeBooleanDbValue(arg0.getString("eligible_for_portal"), true, "eligible_for_portal", lAcctStats));
		lAcctStats.setAutoPaymentStatus(getSafeAutoPaymentStatus(arg0.getString("automatic_payment_status"), lAcctStats));
		lAcctStats.setMaxPaymentAmount(getSafeBigDecimal(arg0.getString("maximum_payment_amount"), "maximum_payment_amount", lAcctStats));
		lAcctStats.setMinimumPaymentDue(getSafeBigDecimal(arg0.getString("minimum_payment_amount"), "minimum_payment_amount", lAcctStats));
		lAcctStats.setContactPrefsStatus(getSafeContactPrefStatus(arg0.getString("contact_preferences_status"), lAcctStats));
		lAcctStats.setCurrentAmountDue(getSafeBigDecimal(arg0.getString("current_amount_due"),"current_amount_due",lAcctStats));
		lAcctStats.setDebitConvenienceFee(getSafeBigDecimal(arg0.getString("debit_convenience_fee"),"debit_convenience_fee",lAcctStats));
		lAcctStats.setAccountBalance(getSafeBigDecimal(arg0.getString("account_balance"), "account_balance", lAcctStats));
		
		// -- get the timestamp --
		Timestamp ltimestamp = arg0.getTimestamp("last_updated");
		BigDecimal ldtime = new BigDecimal(ltimestamp.getTime());
		lAcctStats.setLastUpdate(ldtime);
		
		LOG.debug("AccountStatusElement mapping complete ", lAcctStats);
		
		
		return lAcctStats;
	}

	/**
	 * Gets a "safe" boolean value from the database to use in account status. Handles situation where
	 * 		the value may be invalid or null by setting it to the default value passed in.
	 * @param cszDbValue
	 * @param cbDefaultVal
	 * @param cszDbColumn -- for log messages
	 * @param cAcctStats -- 
	 * @return
	 */
	private Boolean getSafeBooleanDbValue ( final String cszDbValue, final Boolean cbDefaultVal,
										final String cszDbColumn, final AccountStatusElement cAcctStats) {
		Boolean lbRetValue = cbDefaultVal;
		
		if (null != cszDbValue) {
			if (0 == cszDbValue.compareToIgnoreCase("Y") || 0 == cszDbValue.compareToIgnoreCase("true")) {
				lbRetValue = true;
			}
			else if (0 == cszDbValue.compareToIgnoreCase ("N") || 0 == cszDbValue.compareToIgnoreCase("false")) {
				lbRetValue = false;
			}
			else {
				LOG.error("The boolean column {} is {} (invalid) in db, setting to {} for customer ID {}, status group {}, account id {}.",
						cszDbColumn, cszDbValue, lbRetValue, cAcctStats.getCustomerId(), 
						cAcctStats.getStatusGroupId(), cAcctStats.getAccountId());
			}
		}
		else {
			LOG.debug("The boolean column {} is null in db, setting to {} for customer ID {}, status group {}, account id {}.",
					cszDbColumn, lbRetValue, cAcctStats.getCustomerId(), 
					cAcctStats.getStatusGroupId(), cAcctStats.getAccountId());
		}
		return lbRetValue;
	}
	
	/**
	 * Retrieves a "safe" payment disabled reason since the db can possibly set it to null or an
	 * invalid value.  If its null or invalid then we log the issue and set it to "enabled".
	 * 
	 * @param cszPayDisabledReason -- reason returned from db (could be null)
	 * @param cAcctStats -- used for logging if error
	 * @return
	 */
	private PayEnabled getSafePaymentDisabledReason (final String cszPayDisabledReason, final AccountStatusElement cAcctStats) {
				
		PayEnabled lRetVal = PayEnabled.enabled;
		try {
			lRetVal = PayEnabled.valueOf(cszPayDisabledReason);
		}
		catch (Exception e) {
			// -- conversion didn't work... note this in the log and enable payment --
			String lszDisabledValue = cszPayDisabledReason;
			if (null == lszDisabledValue) lszDisabledValue = "null";
			LOG.error("Payment disabled reason in db invalid for customer ID {}, status group {}, account id {}, value {}.",
					cAcctStats.getCustomerId(), cAcctStats.getStatusGroupId(), cAcctStats.getAccountId(),
					lszDisabledValue);
			lRetVal = PayEnabled.disabledOther;
		}
		return lRetVal;
	}
	
	/**
	 * Returns a "safe" ACH disabled reason since the db can possibly set it to null or an
	 * invalid value. If its null or invalid then we log the issue and set it to "enabled".
	 * 
	 * @param cszAchDisabledReason
	 * @param cAcctStats
	 * @return
	 */
	private AchEnabled getSafeAchDisabledReason (final String cszAchDisabledReason, final AccountStatusElement cAcctStats) {
		
		AchEnabled lRetVal = AchEnabled.enabled;
		try {
			lRetVal = AchEnabled.valueOf(cszAchDisabledReason);
		}
		catch (Exception e) {
			// -- conversion didn't work... note this in the log and enable ach --
			String lszDisabledValue = cszAchDisabledReason;
			if (null == lszDisabledValue) lszDisabledValue = "null";
			LOG.error("Payment disabled reason in db invalid for customer ID {}, status group {}, account id {}, value {}.",
					cAcctStats.getCustomerId(), cAcctStats.getStatusGroupId(), cAcctStats.getAccountId(),
					lszDisabledValue);
			lRetVal = AchEnabled.enabled;
		}
		return lRetVal;
	}

	/**
	 * Returns a "safe" portal disabled reason since the db can possibly set it to null or an
	 * invalid value. If its null or invalid then we log the issue and set it to "enabled".
	 * 
	 * @param cszPortalDisabledReason
	 * @param cAcctStats
	 * @return
	 */
	private ViewAcct getSafeViewDisabledReason (final String cszPortalDisabledReason, final AccountStatusElement cAcctStats) {
		
		ViewAcct lRetVal = ViewAcct.enabled;
		try {
			lRetVal = ViewAcct.valueOf(cszPortalDisabledReason);
		}
		catch (Exception e) {
			// -- conversion didn't work... note this in the log and enable portal --
			String lszDisabledValue = cszPortalDisabledReason;
			if (null == lszDisabledValue) lszDisabledValue = "null";
			LOG.error("Portal disabled reason in db invalid for customer ID {}, status group {}, account id {}, value {}.",
					cAcctStats.getCustomerId(), cAcctStats.getStatusGroupId(), cAcctStats.getAccountId(),
					lszDisabledValue);
			lRetVal = ViewAcct.enabled;
		}
		return lRetVal;
	}
	
	/**
	 * Returns a "safe" autopayment status since the db can possibly set it to null or an
	 * invalid value. If its null or invalid then we log the issue and set it to "disbled".
	 * 
	 * @param cszAutoPaymentStatus
	 * @param cAcctStats
	 * @return
	 */
	private AutoPmtStatus getSafeAutoPaymentStatus (final String cszAutoPaymentStatus, final AccountStatusElement cAcctStats) {
		
		AutoPmtStatus lRetVal = AutoPmtStatus.eligible;
		try {
			lRetVal = AutoPmtStatus.valueOf(cszAutoPaymentStatus);
		}
		catch (Exception e) {
			// -- conversion didn't work... note this in the log and disable auto payment --
			String lszDisabledValue = cszAutoPaymentStatus;
			if (null == lszDisabledValue) lszDisabledValue = "null";
			LOG.error("AutoPayment status in db invalid for customer ID {}, status group {}, account id {}, value {}.",
					cAcctStats.getCustomerId(), cAcctStats.getStatusGroupId(), cAcctStats.getAccountId(),
					lszDisabledValue);
			lRetVal = AutoPmtStatus.disabled;
		}
		return lRetVal;
	}

	/**
	 * Returns a "safe" contact preferences status since the db can possibly set it to null or an
	 * invalid value. If its null or invalid then we log the issue and set it to "enabled".
	 * 
	 * @param cszContPrefsStatus
	 * @param cAcctStats
	 * @return
	 */
	private ContPrefsStats getSafeContactPrefStatus (final String cszContPrefsStatus, final AccountStatusElement cAcctStats) {
		
		ContPrefsStats lRetVal = ContPrefsStats.enabled;
		try {
			lRetVal = ContPrefsStats.valueOf(cszContPrefsStatus);
		}
		catch (Exception e) {
			// -- conversion didn't work... note this in the log and disable auto payment --
			String lszDisabledValue = cszContPrefsStatus;
			if (null == lszDisabledValue) lszDisabledValue = "null";
			LOG.error("Contact Preferences status in db invalid for customer ID {}, status group {}, account id {}, value {}.",
					cAcctStats.getCustomerId(), cAcctStats.getStatusGroupId(), cAcctStats.getAccountId(),
					lszDisabledValue);
			lRetVal = ContPrefsStats.enabled;
		}
		return lRetVal;
	}


	/**
	 * Returns a safe BigDecimal value for a field read from the database.  Returns 0 if there was a failure
	 * either because it was an invalid number or because the field was null
	 * 
	 * @param cszFieldValue
	 * @param cszFieldName
	 * @param cAcctStats
	 * @return
	 */
	private	BigDecimal getSafeBigDecimal (final String cszFieldValue, final String cszFieldName, final AccountStatusElement cAcctStats) {
		BigDecimal lnRetVal = new BigDecimal(0);
		
		if (null != cszFieldValue) {
			try {
				lnRetVal = new BigDecimal(cszFieldValue);
			} catch (NumberFormatException e) {
				LOG.error("Number format exception converting field {} to BigDecimal from {}, customer ID {} status group {} account id {}.",
						cszFieldName, cszFieldValue,
						cAcctStats.getCustomerId(), 
						cAcctStats.getStatusGroupId(), 
						cAcctStats.getAccountId());
			}
		}
		else {
			LOG.error("Null value when number expected, field {}, customer ID {}, status group {}, account id {}.",
					cszFieldName, cAcctStats.getCustomerId(),
					cAcctStats.getStatusGroupId(), 
					cAcctStats.getAccountId());
		}
		return lnRetVal;
	}
	
	/**
	 * Returns a safe Integer value for a field read from the database.  Returns 0 if there was a failure
	 * either because it was an invalid number or because the field was null
	 * 
	 * @param cszFieldValue
	 * @param cszFieldName
	 * @param cAcctStats
	 * @return
	 */
	private	Integer getSafeInteger (final String cszFieldValue, final String cszFieldName, final AccountStatusElement cAcctStats) {
		Integer lnRetVal = Integer.valueOf(0);
		
		if (null != cszFieldValue) {
			try {
				lnRetVal = Integer.valueOf(cszFieldValue);
			} catch (NumberFormatException e) {
				LOG.error("Number format exception converting field {} to integer from {}, customer ID {} status group {} account id {}.",
						cszFieldName, cszFieldValue,
						cAcctStats.getCustomerId(), 
						cAcctStats.getStatusGroupId(),
						cAcctStats.getAccountId());
			}
		}
		else {
			LOG.error("Null value when number expected, field {}, customer ID {}, status group {}, account id {}.",
					cszFieldName, cAcctStats.getCustomerId(), 
					cAcctStats.getStatusGroupId(),
					cAcctStats.getAccountId());
		}
		return lnRetVal;
	}
}

