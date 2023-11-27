/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.client.main.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/******************************************************************************
 * Holds the information payment made of the NLS branch.
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class PaymentMade {
	
	/**********************************************************************************************
	 * The payment id.
	 */
	@JsonProperty("paymentID")
	private String m_szPaymentId;
	
	/**********************************************************************************************
	 * Date on which payment done.
	 */
	@JsonProperty("datePaid")
	private Date m_cDatePaid;
	
	/**********************************************************************************************
	 * Amount of payment made.
	 */
	@JsonProperty("amountPaid")
	private Double m_dAmountPaid;
	
	/**********************************************************************************************
	 * Constructor for creating the object.
	 */
	public PaymentMade() {
		super();
	}
	
	/**********************************************************************************************
	 * Constructor for creating the object with all fields.
	 */
	public PaymentMade(
			String paymentId, 
			Date datePaid, 
			Double amountPaid) {
		super();
		this.m_szPaymentId = paymentId;
		this.m_cDatePaid = datePaid;
		this.m_dAmountPaid = amountPaid;
	}
	
	/**********************************************************************************************
	 * Getter for payment id of payment made.
	 * 
	 * @return String payment id
	 */
	public String getPaymentID() {
		return m_szPaymentId;
	}
	
	/**********************************************************************************************
	 * Getter for date of payment made.
	 * 
	 * @return Date payment paid date.
	 */
	public Date getDatePaid() {
		return m_cDatePaid;
	}
	
	/**********************************************************************************************
	 * Getter for amount of payment made.
	 * 
	 * @return Double amount paid
	 */
	public Double getAmountPaid() {
		return m_dAmountPaid;
	}
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "PaymentMade [paymentId=" + m_szPaymentId + ", datePaid=" + m_cDatePaid
		        + ", amountPaid=" + m_dAmountPaid + "]";
	}
	
}
