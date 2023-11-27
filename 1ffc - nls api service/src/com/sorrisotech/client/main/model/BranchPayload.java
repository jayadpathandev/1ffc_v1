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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/******************************************************************************
 * Holds the information of the NLS branch.
 * 
 * @author Asrar Saloda
 */
@JsonInclude(Include.NON_NULL)
public class BranchPayload {
	
	/**********************************************************************************************
	 * Cif Number of NLS Branch.
	 */
	@JsonProperty("cifnumber")
	private String m_szCifNumber;
	
	/**********************************************************************************************
	 * Branch Name of NLS Branch.
	 */
	@JsonProperty("branchname")
	private String m_szBranchName;
	
	/**********************************************************************************************
	 * Complete address of NLS Branch.
	 */
	@JsonProperty("address")
	private Address m_cAddress;
	
	/**********************************************************************************************
	 * Email id NLS Branch.
	 */
	@JsonProperty("email")
	private String m_szEmail;
	
	/**********************************************************************************************
	 * Phone numbers NLS Branch.
	 */
	@JsonProperty("phoneNumbers")
	private List<String> m_cPhoneNumbers;
	
	/**********************************************************************************************
	 * Constructor for creating the object.
	 */
	public BranchPayload() {
	}
	
	/**********************************************************************************************
	 * Constructor for creating the object with all fields.
	 */
	public BranchPayload(
			String cifNumber, 
			String branchName, 
			Address address, 
			String email,
	        List<String> phoneNumbers) {
		this.m_szCifNumber = cifNumber;
		this.m_szBranchName = branchName;
		this.m_cAddress = address;
		this.m_szEmail = email;
		this.m_cPhoneNumbers = phoneNumbers;
	}
	
	/**********************************************************************************************
	 * Getter for cif number of NLS Branch.
	 * 
	 * @return String cif number
	 */
	public String getCifNumber() {
		return m_szCifNumber;
	}
	
	/**********************************************************************************************
	 * Getter for branch name of NLS Branch.
	 * 
	 * @return String branch name.
	 */
	public String getBranchName() {
		return m_szBranchName;
	}
	
	/**********************************************************************************************
	 * Getter for complete address of NLS Branch.
	 * 
	 * @return Address of NLS Branch
	 */
	public Address getAddress() {
		return m_cAddress;
	}
	
	/**********************************************************************************************
	 * Getter for email id of NLS Branch.
	 * 
	 * @return String email id of NLS Branch
	 */
	public String getEmail() {
		return m_szEmail;
	}
	
	/**********************************************************************************************
	 * Getter for phone numbers of NLS Branch.
	 * 
	 * @return List<String> phone numbers of NLS Branch
	 */
	public List<String> getPhoneNumbers() {
		return m_cPhoneNumbers;
	}
	
	/**************************************************************************
	 * Overridden toString method.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "BranchPayload [cifNumber=" + m_szCifNumber + ", branchName=" + m_szBranchName
		        + ", address=" + m_cAddress + ", email=" + m_szEmail + ", phoneNumbers="
		        + m_cPhoneNumbers + "]";
	}
	
}
