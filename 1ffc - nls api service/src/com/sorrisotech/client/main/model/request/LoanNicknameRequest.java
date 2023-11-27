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
package com.sorrisotech.client.main.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/******************************************************************************
 * Holds the information of the update borrower nickname request
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class LoanNicknameRequest {
	
	/**************************************************************************
	 * Loan Id of borrower.
	 */
	@NotEmpty(message = "Loan Id can not be null or empty")
	@JsonProperty("loanId")
	private String m_szLoanId;
	
	/**************************************************************************
	 * Borrowers nickname to be updated. Note: An empty string ("") is accepted at
	 * NLS side so validating only for null.
	 */
	@NotNull(message = "Borrowers nickname can not be null or empty")
	@JsonProperty("nickname")
	private String m_szNickname;
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "LoanNicknameRequest [m_szLoanId=" + m_szLoanId + ", m_szNickname=" + m_szNickname
		        + "]";
	}
	
	/**************************************************************************
	 * Default constructor
	 */
	public LoanNicknameRequest() {
		super();
	}
	
	/**************************************************************************
	 * All argument constructor.
	 */
	public LoanNicknameRequest(
	        @NotEmpty(message = "Loan Id can not be null or empty") String m_szLoanId,
	        @NotNull(message = "Borrowers nickname can not be null or empty") String m_szNickname) {
		super();
		this.m_szLoanId = m_szLoanId;
		this.m_szNickname = m_szNickname;
	}
	
	/**************************************************************************
	 * Getter method for loan id.
	 * 
	 * @return String loan id
	 */
	public String getLoanId() {
		return m_szLoanId;
	}
	
	/**************************************************************************
	 * Getter method for borrowers nickname to be updated
	 * 
	 * @return String Borrowers nickname to be updated
	 */
	public String getNickname() {
		return m_szNickname;
	}
}
