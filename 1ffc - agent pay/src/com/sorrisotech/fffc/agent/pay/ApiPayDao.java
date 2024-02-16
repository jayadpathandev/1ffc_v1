package com.sorrisotech.fffc.agent.pay;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sorrisotech.fffc.agent.pay.data.AccountBean;
import com.sorrisotech.fffc.agent.pay.data.AccountMapper;
import com.sorrisotech.fffc.agent.pay.data.ApiPayUser;
import com.sorrisotech.fffc.agent.pay.data.ApiPayUserMapper;
import com.sorrisotech.fffc.agent.pay.data.AutoPayBean;
import com.sorrisotech.fffc.agent.pay.data.AutoPayMapper;
import com.sorrisotech.fffc.agent.pay.data.BillBean;
import com.sorrisotech.fffc.agent.pay.data.BillMapper;
import com.sorrisotech.fffc.agent.pay.data.ScheduledBean;
import com.sorrisotech.fffc.agent.pay.data.ScheduledMapper;

public class ApiPayDao {

	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	/***************************************************************************
	 * SQL to look up a user ID from a customerId.
	 */
	@Autowired
	@Qualifier("api.pay.find.user.id")	
	private String mFindUserId = null;

	/***************************************************************************
	 * SQL to check that a user has access to an account.
	 */
	@Autowired
	@Qualifier("api.pay.verify.account")	
	private String mVerifyAccount = null;
	
	/***************************************************************************
	 * SQL to find the due date of the most recent bill.
	 */
	@Autowired
	@Qualifier("api.pay.find.bill")	
	private String mFindBill = null;

	/***************************************************************************
	 * SQL to calculate the summary of scheduled payments for an account.
	 */
	@Autowired
	@Qualifier("api.pay.scheduled.summary")	
	private String mScheduledSummary = null;

	/***************************************************************************
	 * SQL to calculate the summary of scheduled payments for an account. When
	 * there is no bill.
	 */
	@Autowired
	@Qualifier("api.pay.scheduled.summary.no.date")	
	private String mScheduledSummaryNoDate = null;
	
	/***************************************************************************
	 * SQL to get details about the auto pay schedule set for an account.
	 */
	@Autowired
	@Qualifier("api.pay.auto.pay")	
	private String mAutoPay = null;
	
	/***************************************************************************
	 * Given a customerId look up the ID of the associated user.  If no user is 
	 * registered with that customerId returns null.
	 * 
	 * @param customerId  The 1st Franklin customer ID to look up.
	 * 
	 * @return  The userId of the customer or null if none was registered.
	 */
	public ApiPayUser user(
			final String customerId
			) {
		final var params = new HashMap<String, Object>();
		params.put("customerId", customerId);	
		
		ApiPayUser retval = null;
		
		try {
			retval = mJdbc.queryForObject(mFindUserId, params, new ApiPayUserMapper());
		} catch(IncorrectResultSizeDataAccessException e) {
			// There is no user with that customer ID registered.
		}
		
		return retval;
	}

	/***************************************************************************
	 * Looks up details for the account..
	 * 
	 * @param userId     The ID of the user to who owns the account.
	 * @param accountId  The internal number of the account to look up.
	 * 
	 * @return  Details about the account, or null if there is no matching 
	 *          account.
	 */
	public AccountBean lookupAccount(
			final BigDecimal userId,
			final String     accountId			
			) {
		final var params = new HashMap<String, Object>();
		params.put("userId", userId);	
		params.put("accountId", accountId);
		
		AccountBean retval = null;
		try {
			retval = mJdbc.queryForObject(mVerifyAccount, params, new AccountMapper());
		} catch(IncorrectResultSizeDataAccessException e) {
			// There is no user with that customer ID registered.
		}
		return retval;
	}
	
	/***************************************************************************
	 * Given a userId and accountId looks up the bill.
	 * 
	 * @param userId     The ID of the user that owns the account.
	 * @param accountId  The ID of the account to find the most recent bill.
	 * 
	 * @return  Information about the most recent bill if found, otherwise null.
	 */
	public BillBean findBill(
			final BigDecimal userId,
			final String     accountId
			) {
		final var params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("accountId", accountId);
		
		BillBean retval = null;
		try {
			retval = mJdbc.queryForObject(mFindBill, params, new BillMapper());
		} catch(IncorrectResultSizeDataAccessException e) {
			// There is no user with that customer ID registered.
		}
		return retval;
	}
	
	/***************************************************************************
	 * Gathers a summary of scheduled payments that have yet to be executed.
	 * If there is a bill then only returns scheduled payments set to execute 
	 * before the due date of the current bill.  Otherwise returns all scheduled
	 * payments.
	 * 
	 * If there are no scheduled payments then returns null.
	 * 
	 * @param userId     The ID of the user that owns the account.
	 * @param accountId  The ID of the account to get the scheduled payments for.
	 * @param notAfter   The date to get all scheduled payments before or equal.
	 * 
	 * @return  A ScheduledBean if there are scheduled payments, otherwise null.
	 */
	public ScheduledBean scheduledSummary(
			final BigDecimal userId,
			final String     accountId,
			final Date       notAfter
			) {
		final var params = new HashMap<String, Object>();
		
		params.put("userId", userId.toPlainString());
		params.put("accountId", accountId);
		
		ScheduledBean retval = null;
		
		if (notAfter == null) {
			retval = mJdbc.queryForObject(mScheduledSummaryNoDate, params, new ScheduledMapper());			
		} else {
			params.put("date", notAfter);
			retval = mJdbc.queryForObject(mScheduledSummary, params, new ScheduledMapper());
		}
			
		if (retval.count.equals(BigDecimal.ZERO)) {
			return null;
		}
		
		return retval;
	}

	/***************************************************************************
	 * Gathers information about the auto pay schedule (if any) for an account.
	 * If there is no auto pay schedule set then returns nulls.
	 * 
	 * @param userId     The ID of the user that owns the account.
	 * @param accountId  The ID of the account to get the auto pay settings for.
	 * 
	 * @return  An AutoPayBean if there is a auto payment schedule set up,
	 *          otherwise null.
	 */
	public AutoPayBean autoPay(
			final BigDecimal userId,
			final String     accountId
			) {
		final var params = new HashMap<String, Object>();
		
		params.put("userId", userId.toPlainString());
		params.put("accountId", accountId);
		
		AutoPayBean retval = null;

		try {
			retval = mJdbc.queryForObject(mAutoPay, params, new AutoPayMapper());
		} catch(IncorrectResultSizeDataAccessException e) {
			// The account does not have an auto-pay set up.
		}
		
		return retval;
	}

}
