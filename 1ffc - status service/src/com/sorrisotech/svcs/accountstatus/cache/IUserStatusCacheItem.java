/**
 * 
 */
package com.sorrisotech.svcs.accountstatus.cache;

import java.math.BigDecimal;

import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AutoPmtStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.ContPrefsStats;

/**
 * Interface to the result set that can be queried by calls
 * from the use case. The idea here is that generally, this
 * element provides account status for all the accounts a 
 * user has access to and is results in a single query to 
 * the database for the entire session. 
 * <p>
 * It will get pushed out of the cache if its been sitting
 * unattended for a preset timeout period (defined in the 
 * cache). If it gets pushed out, the cache/factory will 
 * do the query again and create a new one next time it is
 * accessed.
 * 
 * @author John A. Kowalonek
 * @since 25-Sep-2023
 * @version 22-Jan-2024 jak Added new fields current amount due and convenience fee
 * @version 01-Feb-2024 jak	Added account balance
 * @version 17-Mar-2024 jak Added getMonthlyPayment
 * @version 03-May-2024 jak	Added getLastUpdate
 */
public interface IUserStatusCacheItem {
	
	/**
	 * General Account Information that doesn't control
	 * application behavior, but may be used in presentation. 
	 */
	public record GeneralAccountInfo (
		String		statusGroupId,
		String		paymentGroupId,
		String		customerId,
		String 	 	accountId,
		BigDecimal	origLoanAmt,
		BigDecimal	monthlyPayment,
		Integer		totalNumPayments,
		Integer		remainingNumPayments,
		BigDecimal  currentAmountDue,
		BigDecimal  debitConvenienceFee,
		Integer		mostRecentUpdate
		)  {}
	
	public enum EnumPortalAccess {
		enabled,					// user has access
		disabledUser,				// user is disabled
		disabledEconsent			// user disabled needs to consent
	}
	
	/**
	 * Return data for monthly minimum
	 */
	public class MinimumPaymentData {
		
		Boolean mbMinimumRequired = false;
		BigDecimal mdMinimumAmount = new BigDecimal(0);
		
		public MinimumPaymentData (Boolean cbMinimumRequired, BigDecimal cdMinimumAmount) {
			mbMinimumRequired = cbMinimumRequired;
			mdMinimumAmount = cdMinimumAmount;
		}
		
		public Boolean getIsMinimumRequired () {
			return mbMinimumRequired;
		}
		
		public BigDecimal getMinimumAmount () {
			return mdMinimumAmount;
		}
	}
	
	/**
	 * returns General account information that isn't used to change the behavior of the 
	 * application but may need to be presented.
	 * @param cszPaymentGroup  - payment group this account belongs to
	 * @param cszAcctIdentifier - account for which this information is requested
	 * 
	 * @return GeneralAccountInfo - record containing the account data.
	 * @throws AccountStatusException
	 */
	abstract GeneralAccountInfo getGeneralAccountInfo(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;

	/**
	 * returns the account status value of newAccount, closedAccount, activeAccount
	 * @param cszPaymentGroup - payment group this account belongs to
	 * @param cszAcctIdentifier -- account for which data is requested
	 * 
	 * @return EnumConst.AcctStatus -- result value
	 * @throws AccountStatusException
	 */
	abstract public EnumConst.AcctStatus getAccountStatus(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * returns the payment enabled status value of enabled, disabledLastPayment, disabledDelinquent, 
	 * disabledClosed
	 * @param cszPaymentGroup - payment group this account belongs to
	 * @param cszAcctIdentifier -- account for which data is requested
	 * 
	 * @return EnumConst.PayEnabled -- result value
	 * @throws AccountStatusException
	 */
	abstract public EnumConst.PayEnabled getPaymentEnabled(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * returns true if payment enabled otherwise false
	 * @param cszPaymentGroup - payment group this account belongs to
	 * @param cszAcctIdentifier -- account for which data is requested
	 * 
	 * @return EnumConst.PayEnabled -- result value
	 * @throws AccountStatusException
	 */
	abstract public Boolean isPaymentEnabled(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;

	/**
	 * returns the ACH enabled status value of enabled, disabledNSF.  If one
	 * account is disabled, then all are disabled.
	 * 
	 * @return EnumConst.AchEnabled -- result value
	 * @throws AccountStatusException
	 */
	abstract public EnumConst.AchEnabled getAchEnabled() throws AccountStatusException;

	/**
	 * returns true if ACH enabled otherwise false.  If one
	 * account is disabled, then all are disabled.
	 * 
	 * @return EnumConst.AchEnabled -- result value
	 * @throws AccountStatusException
	 */
	abstract public Boolean isAchEnabled() throws AccountStatusException;

	/**
	 * returns the view account status value of enabled, disableAccount, disablePortal
	 * @param cszPaymentGroup - payment group this account belongs to
	 * @param cszAcctIdentifier -- account for which data is requested
	 * 
	 * @return EnumConst.ViewAcct -- result value
	 * @throws AccountStatusException
	 */
	abstract public EnumConst.ViewAcct getViewAccount(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * returns true if this user has portal access, otherwise false
	 * 
	 * @return EnumPortalAccess access for all accounts associated with this user 
	 * @throws AccountStatusException
	 */
	abstract public EnumPortalAccess hasPortalAccess() throws AccountStatusException;
	
	/**
	 * Returns MonthlyMinimumData object.
	 * 
	 * @return MonthlyMinimumData
	 * @throws AccountStatusException
	 */
	abstract public MinimumPaymentData getMinimumPaymentAmt(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * Returns true if this account is portal eligible
	 * 
	 * @param cszPaymentGroup
	 * @param cszAcctIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public Boolean getEligibleForPortal(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * Returns the current automatic payment status for the specified account
	 * 
	 * @param cszPaymentGroup
	 * @param cszAcctIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public AutoPmtStatus getAutoPaymentStatus(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * Returns the maximum amount that can be paid against this account
	 * 
	 * @param cszPaymentGroup
	 * @param cszAcctIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public BigDecimal getMaxPaymentAmount (String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * Returns status of contact preferences for this user
	 * 
	 * @param cszPaymentGroup
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public ContPrefsStats getContactPreferenceStatus (String cszPaymentGroup) throws AccountStatusException;
	
	/**
	 * Returns the current amount due for this account as of last night's run
	 * 
	 * @param cszPaymentGroup
	 * @param cszAcctIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public BigDecimal getCurrentAmountDue (String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * Returns the debit card convenience fee associated with this account. 0 if there is
	 * no fee.
	 * 
	 * @param cszPaymentGroup
	 * @param cszAcctIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public BigDecimal getDebitConvenienceFee (String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	/**
	 * Returns the integer YYYYMMDD of the most recent update of status info
	 * 
	 * @param cszPaymentGroup
	 * @param cszAcctIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public Integer getMostRecentUpdate  (String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException;
	
	/**
	 * Returns the current balance associated with this account.
	 * 
	 * @param cszPaymentGroup
	 * @param cszAccountIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public BigDecimal getAccountBalance (String cszPaymentGroup, String cszAccountIdentifier) throws AccountStatusException;
	
	/**
	 * Returns the contractual monthly payment amount.
	 * 
	 * @param cszPaymentGroup
	 * @param cszAccountIdentifier
	 * @return
	 * @throws AccountStatusException
	 */
	abstract public BigDecimal getMonthlyPayment (String cszPaymentGroup, String cszAccountIdentifier) throws AccountStatusException;
	

	/** 
	 * Returns oldest time of last update as milliseconds since the UNIX epoch for all the accounts
	 * associated with this user
	 * 
	 * @return BigDecimal -- time since last update in milliseconds
	 * @throws AccountStatusException
	 */
	abstract public BigDecimal getLastUpdate() throws AccountStatusException;
}
