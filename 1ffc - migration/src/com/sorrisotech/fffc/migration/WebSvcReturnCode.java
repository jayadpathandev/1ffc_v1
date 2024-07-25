/**
 * 
 */
package com.sorrisotech.fffc.migration;

/**
 *  Return codes from the web service call underlying the 
 *  agent api calls used to sequence through setting up a 
 *  scheduled or recurring payment.
 *  
 */
public class WebSvcReturnCode {
	
	public enum PmtMethod
		{ 	startPayment, 
			requestPaymentStatus,
			cancelPayment,
			makeOneTimePayment,
			createAutomaticPayment,
			getAutomaticPaymentRule,
			unknown
	};
	public PmtMethod payMethod = PmtMethod.unknown;
	public String statusCode = null;
	public Boolean success = true;
	public String payload = null;
	public String error = null;
	public String displayName = null;
	
	public String getFriendlyMessage() {

		return payload;
/*		String lsRet = null;
		if (success)
			lsRet = "Status: " + success.toString() +
					", Account: " + displayName +
					", Payment method: " + payMethod.toString() +
					", Return code: " + statusCode +
					", error: " + error +
					", payload " + payload;
		return lsRet; */
	} 
}
