package com.sorrisotech.fffc.migration;

/**
 * interface to get values of ACI token used in addPaymentSource to payment server
 * 
 * @author johnk
 * @since  2024-July-16
 * @version 2024-July-16	jak	initial version
 */
public interface IExternalToken {

	/**
	 * returns an funding token with a prefix that allows the 
	 * ACI driver to differentiate between that an the need to
	 * obtain a temporary funding token.
	 * 
	 * @return String in the format "ACITOKEN|<funding token id>"
	 */
	public String getPipedToken();
	
	/**
	 * returns a funding token no prefix
	 * 
	 * @return
	 */
	public String getToken();
	
	/**
	 * returns expiration date for the underlying debit card
	 * 
	 * @return String in the format  "YYY-MM-DD"
	 */
	public String getExpirationDate();
	
	/**
	 *	Returns a friendly name for this debit card
	 * 
	 * @return  String in the format of "Migrated debit card <last 4 digits>"
	 */
	public String getFriendlyName();
	
	/**
	 * Returns a masked number for the account
	 * 
	 * @return String in the format of "************<last 4 digits>"
	 */
	public String getMaskedName();
	
	/**
	 * Returns an account id (we believe this is loan number) associated
	 * with the token.
	 * 
	 * @return  String in the format of a 1ffc loan number.
	 */
	public String getAccountId();
	
	/**
	 * Account Holder's Name
	 * 
	 * @return
	 */
	public String getAccountHolderName();
	
	/**
	 * Returns token informaton as a string for use in logging
	 * 
	 * @return
	 */
	public String getInfoAsString();

	/**
	 * Returns last 4 digits of card number
	 * @return
	 */
	public Object getLast4();
	
	
}
