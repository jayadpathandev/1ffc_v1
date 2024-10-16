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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.common.LocalizedFormat;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;

public class EsignHelper {
	
	private static final Logger mLog = LoggerFactory.getLogger(EsignHelper.class);
	
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
	
	public static String formatEftDate(String inputDateString) {
		try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(inputDateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return null; 
        }
	}
	
	public static String getFormattedPayDay(String inputDateString) {
		try {
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(inputDateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
            return outputFormat.format(date);
		} catch( Exception ex) {
			mLog.error("EsignHelper...getFormattedPayDay()...exception occured : {}", ex);
			return null;
		}
	}
	
	/**
     * Checks if date1 is after date2.
     *
     * @param date1 in format yyyy-MM-dd
     * @param date2 in format yyyyMMdd
     * @return "true" if date1 is after date2, "false" otherwise
     */
    public static String dateIsGreater(String date1, String date2) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");

        try {
            // Parse the input strings into Date objects
            Date parsedDate1 = sdf1.parse(date1);
            Date parsedDate2 = sdf2.parse(date2);

            // Compare the Date objects
            if (parsedDate1.after(parsedDate2)) {
                return "true";
            } else {
                return "false";
            }
        } catch (ParseException e) {
            // Handle the ParseException if the date strings are in incorrect format
        	mLog.error("EsignHelper...dateIsGreater()...exception occured while parsing : {}", e);
            return "false"; // Or return an appropriate error indication
        }
    }
    
    /**
     * Formats the amount to it's local format.
     * 
     * @param locator
     * @param data
     * @param cszPayGroup
     * @param amount
     * @return
     */
    public static String formatAmount(
    		IServiceLocator2 	locator, 
			IUserData           data,
			final String 		cszPayGroup,
			final String amount) {
    	String lszRetVal = null;
    	
    	try {
			final LocalizedFormat format = new LocalizedFormat(locator, data.getLocale());
			
			final BigDecimal amountInBigDcimal = new BigDecimal(amount);
			
			lszRetVal = format.formatAmount(cszPayGroup, amountInBigDcimal);
			
		} catch (Exception e) {
			mLog.error("EsignHelper...formatAmount() - An exception was thrown", e);
		}
    	
    	if (lszRetVal != null) {
            lszRetVal = "\\" + lszRetVal;
        }

		return lszRetVal ;
    }
    
    /**
     * Formats the date string to the desired format.
     *
     * @param date          the input date string
     * @param inputPattern  the format of the input date string (e.g., "yyyy-MM-dd")
     * @param outputPattern the desired format of the output date string (e.g., "dd/MM/yyyy")
     * @return formatted date string in the output pattern
     */
    public static String formatDate(String date, String inputPattern, String outputPattern) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        try {
            // Parse the input date string into a Date object
            Date parsedDate = inputFormat.parse(date);

            // Format the parsed Date object into the desired output format
            String formattedDate = outputFormat.format(parsedDate);

            return formattedDate;
        } catch (ParseException e) {
            // Handle the ParseException if the input date string is in incorrect format
        	mLog.error("EsignHelper...formatAmount() - An exception was thrown", e);
            return null; // Or return an appropriate error indication
        }
    }
}
