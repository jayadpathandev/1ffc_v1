/*
 * (c) Copyright 2024 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
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

public class EsignHelper {
	
	public static String getDueDateStatement(String value) {
		String count = "";
		
		if (value.equals("1")) {
			count = "1 st";
		} else if (value.equals("2")) {
			count = "2 nd";
		} else if (value.equals("3")) {
			count = "3 rd";
		} else {
			count = value + " th";
		}

		return count + " of every month";
	}
	
	public static String getPriorDaysStatement(String value) {
		if (value.equals("1")) {
			return value + " day prior to 'Due Date'";
		} else {
			return value + " days prior to 'Due Date'";
		}
	}

	public static String getExpiryDateStatement(String value) {
		return "effective until " + value;
	}
	
	public static String getPayCountStatement(String value) {
		return "for " + value + " payments";
	}
	
	public static String getFullName(String firstName, String lastName) {
		return firstName + " " + lastName;
	}
}
