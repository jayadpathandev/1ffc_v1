/*
 * (c) Copyright 2017-2024 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.user;

import java.math.BigDecimal;
import java.util.Date;

import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

/************************************************************************************************
 * This class contains helper methods of usecase.
 * 
 * @author Asrar Saloda.
 */
public class UcHelper {
	
	/***********************************************************************************
	 * This methods compares dates. Sets true if eSignConsentLastUpdatedProfile is
	 * greater than eSignConsentLastUpdatedStatus otherwise false.
	 * 
	 * @param eSignConsentLastUpdatedProfile E-consent last updated time_stamp from
	 *                                       auth_user_profile table.
	 * @param eSignConsentLastUpdatedStatus  E-consent last updated time_stamp from
	 *                                       status API.
	 */
	public static void isConsentUpdatedRecently(
	        final BigDecimal eSignConsentLastUpdatedProfile,
	        final BigDecimal eSignConsentLastUpdatedStatus,
	        IStringData sReturnValue) throws MargaritaDataException {
		
		String returnValue = "false";
		
		if (eSignConsentLastUpdatedProfile.compareTo(eSignConsentLastUpdatedStatus) > 0) {
			returnValue = "true";
		}
		
		sReturnValue.putValue(returnValue);
	}
	
	/***********************************************************************************
	 * This method returns current UNIX epoch time stamp.
	 */
	public static String getCurrentEpochTime() {
		return String.valueOf(new Date().getTime());
	}
}
