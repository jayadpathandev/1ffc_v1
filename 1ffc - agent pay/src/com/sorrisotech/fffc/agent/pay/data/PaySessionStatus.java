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
package com.sorrisotech.fffc.agent.pay.data;

public enum PaySessionStatus {
	created("created"),
	started("started"),
	error("error"),
	pmtAccountChosen("pmtAccountChosen"),
	oneTimePmtInProgress("oneTimePmtInProgress"),
	automaticPmtRuleInProgress("automaticPmtRuleInProgress"),
	transactionComplete("transactionComplete"),
	transactionIdExpired("transactionIdExpired"),
	transactionIdNotFound("transactionIdNotFound");
	
	private String mDbFormat;
	
	PaySessionStatus(
			final String dbFormat
			) {
		mDbFormat = dbFormat;
	}
	
	public String dbFormat() {
		return mDbFormat;
	}
	
	public static PaySessionStatus lookup(
			final String value
			) {
		switch(value) {
		case "created":                    return created;
		case "started":                    return started;
		case "error":                      return error;
		case "pmtAccountChosen":           return pmtAccountChosen;
		case "oneTimePmtInProgress":       return oneTimePmtInProgress;
		case "automaticPmtRuleInProgress": return automaticPmtRuleInProgress;
		case "transactionComplete":        return transactionComplete;
		case "transactionIdExpired":       return transactionIdExpired;
		case "transactionIdNotFound":      return transactionIdNotFound;
		}
		return error;
	}
}
