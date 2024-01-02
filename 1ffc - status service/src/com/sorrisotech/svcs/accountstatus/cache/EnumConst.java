package com.sorrisotech.svcs.accountstatus.cache;

/**
 * 	@author John A. Kowalonek
 *  @version 01-Nov-2023
 *  
 *  Provides enumerator types for use in the account status service. This class is never
 *  instantiated.
 */
public final class EnumConst {

	/**
	 * Values for <code>accountStatus</code> returned using <code>toString()</code>.
	 */
	public enum AcctStatus {
		newAccount, closedAccount, activeAccount
	}
	
	/**
	 * Values for >code>paymentStatus</code> returned using <code>toString()</code>.
	 */
	public enum PayEnabled {
		enabled, disabledLegal, disabledRepo, disabledLastPayment, 
		disabledAccountDispute, disabledPendingRetraction, disableDQ, disabledOther
	}
	
	/**
	 * Values for <code>achEnabled</code> returned using <code>toString()</code>.
	 */
	public enum AchEnabled {
		enabled, disabledNSF, disabledStopACH, disabledChargeOff
	}
	
	/**
	 * Values for <code>viewAccount</code> returned using <code>toString()</code>.
	 */
	public enum ViewAcct {
		enabled, disabledAccount, disabledPortal, disabledFraud, disabledBankruptcy
	}
	
	/**
	 * Values for <code>recurringPaymentStatus</code> returned using <code>toString()</code>.
	 */
	public enum AutoPmtStatus {
		eligible, enrolled, disabled, disabledUntilCurrent
	}

	/**
	 * Values for <code>contactPreferencesStatus</code> returned using <code>toString()</code>.
	 */
	public enum ContPrefsStats {
		enabled, disabled
	}

}
