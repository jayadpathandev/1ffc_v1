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
package com.sorrisotech.client.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/******************************************************************************
 * The response POJO for add document endpoint.
 * 
 * @author Rohit Singh
 */
public class AddDocumentResponse {
	
	/******************************************************************************
	 * List of document indexes per document added to the session.
	 */
    @JsonProperty("OriginalDocumentIndexList") 
    public List<Integer> originalDocumentIndexList;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AddDocumentResponse [originalDocumentIndexList=");
		builder.append(originalDocumentIndexList);
		builder.append("]");
		return builder.toString();
	}
    
}
