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
package com.sorrisotech.fffc.auth;

import com.sorrisotech.app.registration.UcUserAssignmentAction;
import com.sorrisotech.persona.comgmt.api.CompanyManagementFactory;
import com.sorrisotech.persona.comgmt.api.ICompany;
import com.sorrisotech.persona.usercompanylink.api.UserCompanyLinkFactory;
import com.sorrisotech.saas.orgid.api.OrgIdFactory;
import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.utils.AppConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ****************************************************************************
// Special registration for 1st Franklin Financial.
public class FffcRegistration {
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FffcRegistration.class);	  
				
    // ************************************************************************
	public static void hijack_user(
				final String      accountId,
				final IStringData retval
			) {
		try {
			retval.putValue("");
			
			final var userId = Queries.lookupUser(new BigDecimal(accountId));
			
			if (userId != null) {
				retval.putValue(userId.toPlainString());
			}			
		} catch(Throwable e) {
			LOG.error("Error tracking down user.", e);
		}
	}

	/**************************************************************************
     * Assign user to an account with a new company.
     * 
     * @param szAccountId The account id.
     * @param szUserId    The user id.
     * 
     * @return "success" if the user assigned to company successfully. 
     *         "error"   if any internal error occurred. 
     */    
	public static String assignUserToAccountWithNewCompany(
		        final String accountId,
		        final String userId,
		        final IStringData saveOrgId
	        ) {		
		// ----------------------------------------------------------------------------------------
		// Grab the information we need for 1ffc IF this is a 1ffc status payment group.
		final String orgId = Queries.lookupOrgId(
    		new BigDecimal(accountId)
    	);
		
		// ----------------------------------------------------------------------------------------
		// Continue the normal registration.
        String szResult = "error";
        
        try {
        	
        	// ----------------------------------------------------------------------------------------
    		// Setting(saving) orgId(customerId) 
        	saveOrgId.putValue(orgId);
        	
    		// ----------------------------------------------------------------------------------------
    		// Make sure we are not hijacking an account.
    		if (Queries.lookupCompany(new BigDecimal(userId)) != null) { 
    			return "success";
    		}			
        	
            // ------------------------------------------------------------------------------------
            // Create the "company" for the user. 
        	final ICompany cCompany = CompanyManagementFactory.createCompany();                      
            cCompany.setName("B2CUSER:" + userId);
            cCompany.setDescription(null);
            cCompany.setCreatedBy(new BigDecimal(userId));
            cCompany.setType("b2c");
            
            final BigDecimal companyId = CompanyManagementFactory.getCompanyRepository().create(cCompany);            
            
            // ------------------------------------------------------------------------------------
            // Add the user to the company.
            UserCompanyLinkFactory.getUserCompanyLinkRepository().linkUserToCompany(
                    new BigDecimal(userId), 
                    companyId
                );            	
            
            // ------------------------------------------------------------------------------------
            // Add the OrgId to the company.
			ArrayList<String> orgIds = new ArrayList<String>();
			orgIds.add(orgId);			
			OrgIdFactory.getOrgIdRepository().setOrgIds(
				companyId, 
	            orgIds
	        );			

            // ------------------------------------------------------------------------------------
            // Add the OrgId(s) to the company.
            final UcUserAssignmentAction standard = new UcUserAssignmentAction();
            final String                 ignore   = AppConfig.get("1ffc.ignore.group");
            final List<BigDecimal>       accounts = Queries.findAccounts(orgId, ignore); 
            
            for(final BigDecimal id : accounts) {
            	szResult = standard.assignAccountToCompany(id.toPlainString(), companyId.toPlainString());
            	if (!szResult.equals("success")) break;
            }
            if (accounts.size() == 0) {
            	szResult = "success";
            }
            
        } catch(Exception e) {
        	LOG.error("FffcRegistration.....assignUserToAccountWithNewCompany()...An exception was thrown", e);          
        }
        LOG.debug("FffcRegistration.....assignUserToAccountWithNewCompany()...Result: " +szResult);
        return szResult;
    }

	// ************************************************************************
	public static void delete_users_company(
		        final String userId
	        ) {
		// ----------------------------------------------------------------------------------------
		final var companyId = Queries.lookupCompany(new BigDecimal(userId)); 
		
		if (companyId != null) {
			CompanyManagementFactory.getCompanyRepository().delete(companyId);
		}
    }
	
	// ************************************************************************
	// This method returns current UNIX epoch time stamp.
	public static String getCurrentEpochTime() {
        return String.valueOf(new Date().getTime());
    }
	
}
