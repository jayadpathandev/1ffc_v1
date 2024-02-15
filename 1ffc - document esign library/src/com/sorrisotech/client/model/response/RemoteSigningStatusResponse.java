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
 * The response POJO for remote sign status endpoint.
 * 
 * @author Rohit Singh
 */
public class RemoteSigningStatusResponse {
	
	/******************************************************************************
	 * One of these ['RemoteSessionComplete', 'InProcess', 'Expired', 'Aborted']
	 */
    @JsonProperty("RemoteStatus") 
    public String remoteStatus;
    
    /******************************************************************************
	 * An array of all participants whose role identifies them as signers or viewers
	 */
    @JsonProperty("Signers") 
    public List<Signer> signers;
    
    /******************************************************************************
	 * The email of the next signer
	 */
    @JsonProperty("Email") 
    public String email;
    
    /******************************************************************************
	 * The URL the host system should use to display the document to the next signer for signing.
	 */
    @JsonProperty("URL") 
    public String uRL;
    
    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RemoteSigningStatusResponse [remoteStatus=");
		builder.append(remoteStatus);
		builder.append(", signers=");
		builder.append(signers);
		builder.append(", email=");
		builder.append(email);
		builder.append(", uRL=");
		builder.append(uRL);
		builder.append("]");
		return builder.toString();
	}

	public static class Signer{
		
		/******************************************************************************
		 * Signer Name
		 */
		@JsonProperty("Name") 
		public String name;
		
		/******************************************************************************
		 * Signer Email
		 */
		@JsonProperty("Email") 
		public String email;
	 
		/******************************************************************************
		 * Signer Status one of these = ['SIGNED', 'COMPLETED', 'ARCHIVED', 'EXPIRED', 
		 * 'DOCUMENTS_NOT_YET_PROCESSED', 'WAITING_FOR_VERIFICATION', 'IN_PROCESS', 
		 * 'WAITING_FOR_OTHERS', 'OUT_FOR_SIGNATURE', 'OUT_FOR_APPROVAL', 'WAITING_FOR_REVIEW', 
		 * 'WAITING_FOR_MY_SIGNATURE', 'WAITING_FOR_MY_APPROVAL', 'WAITING_FOR_MY_ACCEPTANCE', 
		 * 'WAITING_FOR_MY_ACKNOWLEDGEMENT', 'HIDDEN', 'NOT_YET_VISIBLE', 'STATUS_ERROR', 
		 * 'UNKNOWN', 'RECALLED', 'ABORTED', 'CANCELLED']
		 */
		@JsonProperty("Status") 
		public String status;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Signer [name=");
			builder.append(name);
			builder.append(", email=");
			builder.append(email);
			builder.append(", status=");
			builder.append(status);
			builder.append("]");
			return builder.toString();
		}
	}

}
