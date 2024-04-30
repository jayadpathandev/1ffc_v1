package com.sorrisotech.svcs.accountstatus.cache;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AcctStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AchEnabled;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.AutoPmtStatus;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.ContPrefsStats;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.PayEnabled;
import com.sorrisotech.svcs.accountstatus.cache.EnumConst.ViewAcct;
import com.sorrisotech.svcs.accountstatus.dao.AccountStatusDaoImpl;
import com.sorrisotech.svcs.accountstatus.dao.AccountStatusElement;



/**
 * Contains status information on all the accounts associated with a UserId.<p>
 * 
 * Implements Interfaces:<p>
 * 
 * -- IUserStatusCacheItem is the interface that the service objects use to get information
 * 		on an account. There are some shortcuts and some consolidation based on business
 * 		rules, so use the "shortcut" if you can... but the underlying object for a 
 * 		given account is available as well if a deeper dig or necessary.<p>
 *  
 * -- IUserStatusItemUpdate is the interface used by the cache itself to get the 
 * 		user information into the cache via the DAO and to remove the item from the 
 * 		cache if it hasn't been used in awhile.<p>
 * 
 *  @author John A. Kowalonek
 *  @since 	2023-Oct-10
 *  @version 2024-Jan-22	jak added new fields (current amount due and convenience fee)
 * 	@version 2024-Feb-01	jak added currentbalance
 *  @version 2024-Mar-17 	jak added getMonthlyPayment	
 *  @version 2024-Apr-24	jak	updated portal access call for proper view status
 */
public class UserStatusCacheItem implements IUserStatusCacheItem, IUserStatusItemUpdate {

	private static final Logger LOG = LoggerFactory.getLogger(UserStatusCacheItem.class);

	// -- a cache element is basically a HashMap of AccountStatusElements organized by
	//		account --
	private HashMap< AccountKey, AccountStatusElement> AccountStatusMap = new HashMap<AccountKey, AccountStatusElement>();
	private Instant m_LastTimeAccessed = Instant.now();
	
	/**
	 * Object that's used as the key to the AccountStatusMap. Accounts are ONLY unique
	 * in our system by the combination of payment group and account identifier. <p>
	 * 
	 * Override hashCode and equals to ensure that it behaves properly as a key to a
	 * hash map.
	 * 
	 */
	private final class AccountKey extends Object {
		private String  m_szPaymentGroup = null;
		private String  m_szAccountId = null;
		
		AccountKey(final String cszPaymentGroup, final String cszAccountId) {
			m_szPaymentGroup = cszPaymentGroup;
			m_szAccountId = cszAccountId;
		}
	
		@Override
		public int hashCode() {
			return new String(m_szPaymentGroup + m_szAccountId).hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
		    if (this == o) return true;
		    if (o == null || getClass() != o.getClass()) return false;
		    AccountKey that = (AccountKey) o;
		    return m_szPaymentGroup.equals(that.m_szPaymentGroup) &&
		    		m_szAccountId.equals(that.m_szAccountId);
		}
	} // -- end AccountKey
	
	private void timeStamp() {
		m_LastTimeAccessed = Instant.now();
	}
	
	/**
	 * Returns AccountStatusElement given an account identifier. Null if 
	 * Account is not found. Pulls it from the hashmap.
	 * @param cszPaymentGroup 	
	 * @param cszAcctIdentifier
	 * 
	 * @return AccountStatus -- the item corresponding to the account or null;
	 */
	private AccountStatusElement getStatusElement (String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException {
		
		if (null == cszPaymentGroup || null == cszAcctIdentifier) {
			LOG.error("UserStatusCache:getStatusElement -- null value, one of payment group {} or account identifier {}", 
					     cszPaymentGroup, cszAcctIdentifier);
		}
		AccountStatusElement statusItem = AccountStatusMap.get(new AccountKey(cszPaymentGroup,cszAcctIdentifier));
		timeStamp();
		if (null == statusItem) {
			LOG.error("UserStatusCache:getStatusElement -- Failed to get item for {}", cszAcctIdentifier);
			throw new AccountStatusException();
		}
		return statusItem;
	}

	@Override
	public AcctStatus getAccountStatus(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		return lStatusElement.getAcctStatus();
	}

	@Override
	public PayEnabled getPaymentEnabled(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		return lStatusElement.getPayEnabled();
	}

	@Override
	public AchEnabled getAchEnabled() throws AccountStatusException {

		// -- goes through list of accounts to see if any deny portal access, if one
		//		does then return false otherwise true --
		for (HashMap.Entry< AccountKey, AccountStatusElement> entry :  AccountStatusMap.entrySet()) {
			if (entry.getValue().getAchEnabled() == AchEnabled.disabledNSF) {
				LOG.debug ("UserStatusCacheItem:getAchEnabled ACH disabled for {}", entry.getKey());
				return AchEnabled.disabledNSF;
			}
				
		}
		return AchEnabled.enabled;
	}

	@Override
	public ViewAcct getViewAccount(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		return lStatusElement.getViewAccount();
	}

	@Override
	public Integer setContents(String userId) throws AccountStatusException {
		
		// -- get accounts for this user --
		AccountStatusDaoImpl DAO = new AccountStatusDaoImpl();
		List<AccountStatusElement> elements = DAO.getAccountElementsForUser(userId);;

		// -- iterator across the list, adding them to the Map
		AccountStatusMap.clear();
		ListIterator<AccountStatusElement> iterator = elements.listIterator();
		
		while (iterator.hasNext()) {
			AccountStatusElement element = (AccountStatusElement) iterator.next();
			AccountKey key = new AccountKey(element.getPaymentGroupId(), element.getAccountId());
			AccountStatusMap.put(key, element);
		}
		return AccountStatusMap.size();
	}

	@Override
	public Boolean isBeingUsed(long clTimeoutMilliseconds) {
		
		// -- add timeout milliseconds to last accessed... if its create than the current
		//	time then we're still good... if not, then time to dump this object --
		Instant currentTime = Instant.now();
		return (m_LastTimeAccessed.plusMillis(clTimeoutMilliseconds).compareTo(currentTime) > 0);
	}

	@Override
	public EnumPortalAccess hasPortalAccess() throws AccountStatusException {

		// -- goes through list of accounts to see if any deny portal access.
		//		stop at the first disabledUser account -- one does they all do (even though they should all be marked).
		//		continue through marking disabledUser.  If we find one of those and no disabledUser, we return that, 
		//		disabledUser trumps disabledEconsent. --

		EnumPortalAccess rVal = EnumPortalAccess.enabled;
		Boolean bHasDisabledEconsent = false;
		AccountKey lAcctKeyForDisabledEconsent = null;
		for (HashMap.Entry< AccountKey, AccountStatusElement> entry :  AccountStatusMap.entrySet()) {
			if (entry.getValue().getViewAccount() == ViewAcct.disabledUser) {
				rVal = EnumPortalAccess.disabledUser;
				if (true == bHasDisabledEconsent) {
					// -- this case should NEVER be true, we received bad data --
					LOG.error("UserStatusCache:hasPortalAccess -- Detected inconsistency in status between account id {} " + 
							"disabledUser and account {} (disabledEconsent), returning disabledUser.", 
							entry.getKey(), lAcctKeyForDisabledEconsent );
					bHasDisabledEconsent = false;
					lAcctKeyForDisabledEconsent = null;
				}
				rVal = EnumPortalAccess.disabledUser;
				break;
			} else if (entry.getValue().getViewAccount() == ViewAcct.disabledEconsent) {
				bHasDisabledEconsent = true;
				lAcctKeyForDisabledEconsent = entry.getKey();
			}
		}
		
		// -- if we got this during the course of traversing accounts, then return it --
		if (bHasDisabledEconsent) {
			rVal = EnumPortalAccess.disabledEconsent;
		}
		LOG.debug ("UserStatusCacheItem:hasPortalAccess returning {}", rVal.toString());
		return rVal;
	}

	@Override
	public GeneralAccountInfo getGeneralAccountInfo(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		GeneralAccountInfo lReturnInfo = new GeneralAccountInfo(
				lStatusElement.getStatusGroupId(),
				lStatusElement.getPaymentGroupId(),	
				lStatusElement.getCustomerId(),
				lStatusElement.getAccountId(),
				lStatusElement.getOrigLoanAmt(),
				lStatusElement.getMonthlyPayment(),
				lStatusElement.getTotalNumPayments(),
				lStatusElement.getRemainingNumPayments(),
				lStatusElement.getCurrentAmountDue(), 
				lStatusElement.getDebitConvenienceFee(),
				lStatusElement.getMostRecentUpdate() );
		
		return lReturnInfo;
	}

	@Override
	public Boolean isPaymentEnabled(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException {
		
		// *******************************************************************************
		// -- FIRST FRANKLIN ONLY RULE!  Delinquent still allows payment, but establishes
		//		the minimum as the monthly payment --
		// *******************************************************************************
		PayEnabled payStatus = getPaymentEnabled(cszPaymentGroup,cszAcctIdentifier);
		if ((payStatus == PayEnabled.enabled) || (payStatus == PayEnabled.disableDQ))
			return true;
		else
			return false;
	}

	@Override
	public Boolean isAchEnabled() throws AccountStatusException {
		// -- goes through list of accounts to see if any deny ach payments, if one
		//		does then return false otherwise true --
		for (HashMap.Entry< AccountKey, AccountStatusElement> entry :  AccountStatusMap.entrySet()) {
			if (entry.getValue().getAchEnabled() != AchEnabled.enabled) {
				LOG.debug ("UserStatusCacheItem:isAchEnabled ACH Disabled for account {}", entry.getKey());
				return false;
			}
		}
		return true;
	}

	@Override
	public MinimumPaymentData getMinimumPaymentAmt(String cszPaymentGroup, String cszAcctIdentifier) throws AccountStatusException {
		
		// ******************************************************************************
		// -- FIRST FRANKLIN ONLY RULE!!!
		// 		if payment disabled reason is DQ then payments
		//		are not disabled but must meet the minimum payment requirement..
		//		if payments are disabled for other reason (default) this should never
		//		get called but will act as if its status is DQ --
		// ******************************************************************************
		
		MinimumPaymentData rData = null;
		PayEnabled ePayStatus = getPaymentEnabled(cszPaymentGroup,cszAcctIdentifier);
		BigDecimal dMinPayment = getStatusElement(cszPaymentGroup, cszAcctIdentifier).getCurrentAmountDue();
		
		switch (ePayStatus) {
		case enabled: 
			rData = new MinimumPaymentData(false, new BigDecimal(0));
			break;
		case disableDQ:
		default:
			rData = new MinimumPaymentData(true, dMinPayment);
			break;
		}

		return rData;
	}

	@Override
	public Boolean getEligibleForPortal(String cszPaymentGroup, String cszAcctIdentifier)
			throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		Boolean lbEligible = lStatusElement.getPortalEligible();
		return lbEligible;
	}

	@Override
	public AutoPmtStatus getAutoPaymentStatus(String cszPaymentGroup, String cszAcctIdentifier)
			throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		AutoPmtStatus lAutoPmtStatus = lStatusElement.getAutoPaymentStatus();
		return lAutoPmtStatus;
	}

	@Override
	public BigDecimal getMaxPaymentAmount(String cszPaymentGroup, String cszAcctIdentifier)
			throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		BigDecimal ldMaxPaymentAmount = lStatusElement.getMaxPaymentAmount();
		return ldMaxPaymentAmount;
	}

	@Override
	public ContPrefsStats getContactPreferenceStatus(String cszPaymentGroup) throws AccountStatusException {

		// -- goes through list of accounts to see if any deny ach payments, if one
		//		does then return false otherwise true --
		for (HashMap.Entry< AccountKey, AccountStatusElement> entry :  AccountStatusMap.entrySet()) {
			if (entry.getValue().getContactPrefsStatus() != ContPrefsStats.enabled) {
				LOG.debug ("UserStatusCacheItem:isAchEnabled ACH Disabled for account {}", entry.getKey());
				return ContPrefsStats.disabled;
			}
		}
		return ContPrefsStats.enabled;
	}

	@Override
	public BigDecimal getCurrentAmountDue(String cszPaymentGroup, String cszAcctIdentifier)
			throws AccountStatusException {

		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		BigDecimal ldCurAmountDue = lStatusElement.getCurrentAmountDue();
		return ldCurAmountDue;
	}

	@Override
	public Integer getMostRecentUpdate(String cszPaymentGroup, String cszAcctIdentifier)
			throws AccountStatusException {
		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		Integer lcMostRecentUpdate = lStatusElement.getMostRecentUpdate();
		return lcMostRecentUpdate;
	}

	@Override
	public BigDecimal getDebitConvenienceFee(String cszPaymentGroup, String cszAcctIdentifier)
			throws AccountStatusException {
		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAcctIdentifier);
		BigDecimal ldDebitConvenienceFee = lStatusElement.getDebitConvenienceFee();
		return ldDebitConvenienceFee;
	}

	@Override
	public BigDecimal getAccountBalance(String cszPaymentGroup, String cszAccountIdentifier)
			throws AccountStatusException {
		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAccountIdentifier);
		BigDecimal ldAccountBalance = lStatusElement.getAccountBalance();
		return ldAccountBalance;
	}

	@Override
	public BigDecimal getMonthlyPayment(String cszPaymentGroup, String cszAccountIdentifier)
			throws AccountStatusException {
		AccountStatusElement lStatusElement = getStatusElement(cszPaymentGroup, cszAccountIdentifier);
		BigDecimal ldMonthlyPaymentAmount = lStatusElement.getMonthlyPayment();
		return ldMonthlyPaymentAmount;
	}
}
