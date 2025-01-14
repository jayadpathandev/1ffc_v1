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
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

/************************************************************************************************
 * This class contains helper methods of usecase.
 * 
 * @author Asrar Saloda.
 */
public class UcHelper {
	
	/********************************************************************************************
	 * Determines the consent status based on profile consent, status consent, and
	 * their update timestamps.
	 *
	 * @param szConsenProfile                String representing the profile consent
	 *                                       status ("true" or "false").
	 * @param szConsentStatus                String representing the status consent
	 *                                       status ("true" or "false").
	 * @param eSignConsentLastUpdatedProfile BigDecimal representing the last update
	 *                                       time of the profile consent (epoch
	 *                                       seconds).
	 * @param eSignConsentLastUpdatedStatus  BigDecimal representing the last update
	 *                                       time of the status consent (epoch
	 *                                       seconds).
	 * @param sReturnValue                   The object to store the result ("true"
	 *                                       or "false").
	 * @throws MargaritaDataException If an error occurs during processing.
	 */
	public static void checkConsentStatus(
	        final String szConsenProfile,
	        final String szConsentStatus,
	        final BigDecimal eSignConsentLastUpdatedProfile,
	        final BigDecimal eSignConsentLastUpdatedStatus,
	        IStringData sReturnValue) throws MargaritaDataException {
		
		boolean bIsProfileConsentEnabled = Boolean.parseBoolean(szConsenProfile);
		boolean bIsStatusConsentEnabled  = Boolean.parseBoolean(szConsentStatus);
		
		// ------------------------------------------------------------------------------------
		// Case 1: If profile consent is disabled and status consent is enabled.
		if (!bIsProfileConsentEnabled && bIsStatusConsentEnabled) {
			sReturnValue.putValue("true");
			return;
		}
		
		// ------------------------------------------------------------------------------------
		// Case 2: If both profile consent and status consent are enabled.
		if (bIsProfileConsentEnabled && bIsStatusConsentEnabled) {
			sReturnValue.putValue("false");
			return;
		}
		
		// ------------------------------------------------------------------------------------
		// Case 3: If profile consent is enabled and status consent is disabled.
		if (bIsProfileConsentEnabled && !bIsStatusConsentEnabled) {
			// --------------------------------------------------------------------------------
			// Converting timestamps from BigDecimal to Instant
			var profileUpdateTime = Instant
			        .ofEpochMilli(eSignConsentLastUpdatedProfile.longValue());
			var statusUpdateTime  = Instant
			        .ofEpochMilli(eSignConsentLastUpdatedStatus.longValue());
			
			// --------------------------------------------------------------------------------
			// Calculating the time difference between the profile and status updates
			var timeDifference = Duration.between(profileUpdateTime, statusUpdateTime);
			
			// ---------------------------------------------------------------------------------
			// Checking if the time difference is greater than 1 hour.
			if (timeDifference.toHours() > 1) {
				// -----------------------------------------------------------------------------
				// Return true (Show consent to user) if status timestamp is more than 1 hour
				// after profile update.
				sReturnValue.putValue("true");
			} else {
				// -----------------------------------------------------------------------------
				// Return false (not show consent to user) otherwise (if status timestamp is
				// within 1 hour after profile update.)
				sReturnValue.putValue("false");
			}
			return;
		}
		// -------------------------------------------------------------------------------------
		// Default case: If no conditions are met, return true as a fallback and show
		// consent to user. (Unreachable if all conditions above goes well)
		sReturnValue.putValue("true");
	}
    
    /***********************************************************************************
	 * This method returns current UNIX epoch time stamp.
	 */
	public static String getCurrentEpochTime() {
		return String.valueOf(new Date().getTime());
	}
}
