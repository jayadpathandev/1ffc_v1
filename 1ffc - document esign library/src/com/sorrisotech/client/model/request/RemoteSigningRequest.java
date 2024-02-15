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
package com.sorrisotech.client.model.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/******************************************************************************
 * The request POJO for remote sign endpoint.
 * 
 * @author Rohit Singh
 */
@JsonInclude(value = Include.NON_NULL)
public class RemoteSigningRequest {
	
	/******************************************************************************
	 * [Optional] The redirect URL on which we want to redirect after sign
	 */
	@JsonProperty("RedirectURL")
	public String redirectURL;

	/******************************************************************************
	 * [Optional] Time in seconds before being redirected
	 */
	@JsonProperty("RedirectDelay")
	public String redirectDelay;

	/******************************************************************************
	 * Details of the patry going to sign
	 */
	@JsonProperty("RemotePartyDetails")
	public List<RemotePartyDetail> remotePartyDetails;

	/******************************************************************************
	 * [Optional] The message details.
	 */
	@JsonProperty("MessageDetails")
	public MessageDetails messageDetails;
	
	public RemoteSigningRequest(
			String redirectURL, 
			String redirectDelay, 
			List<RemotePartyDetail> remotePartyDetails) {
		this.redirectURL = redirectURL;
		this.redirectDelay = redirectDelay;
		this.remotePartyDetails = remotePartyDetails;
	}
	
	public RemoteSigningRequest(List<RemotePartyDetail> remotePartyDetails) {
		this.remotePartyDetails = remotePartyDetails;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RemoteSigningRequest [redirectURL=");
		builder.append(redirectURL);
		builder.append(", redirectDelay=");
		builder.append(redirectDelay);
		builder.append(", remotePartyDetails=");
		builder.append(remotePartyDetails);
		builder.append(", messageDetails=");
		builder.append(messageDetails);
		builder.append("]");
		return builder.toString();
	}

	public static class RemotePartyDetail {
		
		/******************************************************************************
		 * The party name.
		 */
		@JsonProperty("FullName")
		public String fullName;

		/******************************************************************************
		 * The party Email
		 */
		@JsonProperty("Email")
		public String email;

		/******************************************************************************
		 * [Optional] The auth type for sign
		 */
		@JsonProperty("RemoteAuthenticationType")
		public String remoteAuthenticationType = "Email";

		/******************************************************************************
		 * [Optional] details for sign
		 */
		@JsonProperty("Details")
		public String details;

		/******************************************************************************
		 * [Optional] The order of the sign
		 */
		@JsonProperty("RemoteSigningOrder")
		public int remoteSigningOrder = 1;

		public RemotePartyDetail(
				String fullName, 
				String email, 
				String remoteAuthenticationType, 
				int remoteSigningOrder) {
			this.fullName = fullName;
			this.email = email;
			this.remoteAuthenticationType = remoteAuthenticationType;
			this.remoteSigningOrder = remoteSigningOrder;
		}
		
		public RemotePartyDetail(String fullName, String email) {
			this.fullName = fullName;
			this.email = email;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RemotePartyDetail [fullName=");
			builder.append(fullName);
			builder.append(", email=");
			builder.append(email);
			builder.append(", remoteAuthenticationType=");
			builder.append(remoteAuthenticationType);
			builder.append(", details=");
			builder.append(details);
			builder.append(", remoteSigningOrder=");
			builder.append(remoteSigningOrder);
			builder.append("]");
			return builder.toString();
		}

	}
	
	public static class MessageDetails {

		/******************************************************************************
		 * [Optional] The subject of the message.
		 */
		@JsonProperty("Subject")
		public String subject;

		/******************************************************************************
		 * [Optional] The message
		 */
		@JsonProperty("Message")
		public String message;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MessageDetails [subject=");
			builder.append(subject);
			builder.append(", message=");
			builder.append(message);
			builder.append("]");
			return builder.toString();
		}
	}
}
