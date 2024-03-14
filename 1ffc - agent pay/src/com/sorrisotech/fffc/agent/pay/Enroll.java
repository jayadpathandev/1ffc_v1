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
package com.sorrisotech.fffc.agent.pay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.registration.UcUserAssignmentAction;
import com.sorrisotech.persona.comgmt.api.CompanyManagementFactory;
import com.sorrisotech.persona.comgmt.api.ICompany;
import com.sorrisotech.persona.usercompanylink.api.UserCompanyLinkFactory;
import com.sorrisotech.saas.orgid.api.OrgIdFactory;
import com.sorrisotech.svcs.agentpay.api.IApiAgentPay;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.aaa.CredentialNames;
import com.sorrisotech.svcs.itfc.aaa.IAAAClassFactory;
import com.sorrisotech.svcs.itfc.aaa.api.ICredential;
import com.sorrisotech.svcs.itfc.aaa.credMgmt.ICredentialManager;
import com.sorrisotech.svcs.itfc.exceptions.DuplicateEntityException;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaCredentialException;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;
import com.sorrisotech.utils.AppConfig;

public class Enroll {

	// ************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(Enroll.class);  

	// ************************************************************************	
	final static ApiPayDao mDao = Fffc.apiPay();
	
	// ************************************************************************	
	private static ICredential create_user(
				final IServiceLocator2 services,
				final String           customerId
			) {
		final var                username = "-- " + customerId;
		final IAAAClassFactory   factory = services.findService(IAAAClassFactory.class);
		final ICredentialManager manager = factory.createCredentialManager();
			
		final ICredential user = factory.createCredential();
		user.setType(CredentialNames.USER);
		user.setValue(username);
		
		try {
			user.addEnclosingCred(manager.createTemporary(CredentialNames.PASSWORD, ""));
			user.addEnclosingCred(manager.getByValue(CredentialNames.NAMESPACE, "saas.user.namespace"));
			user.addEnclosingCred(manager.getByValue(CredentialNames.ROLE, "Role_Consumer_EndUser"));
		} catch(MargaritaCredentialException e) {
			LOG.error("Could not enroll the new user.", e);
			return null;
		}
		user.addAttribute("appType", "b2c");
		user.addAttribute("registrationStatus", "agentpay");
		user.addAttribute("accountStatus", "agentpay");
		user.addAttribute("fffcCustomerId", customerId);
		
		try {
			manager.addCredential(user);
		} catch(DuplicateEntityException e) {
			LOG.error("User name [" + username + "] already exists", e);
			return null;			
		} catch(MargaritaCredentialException e) {
			LOG.error("Could not enroll the new user.", e);
			return null;
		}
			
		return user;		
	}
	
	// ************************************************************************	
	public static BigDecimal create_company(
				final BigDecimal user,
				final String     customerId
			) {
		
        // ------------------------------------------------------------------------------------
        // Create the "company" for the user. 
    	final ICompany cCompany = CompanyManagementFactory.createCompany();                      
        cCompany.setName("B2CUSER:" + user.toPlainString());
        cCompany.setDescription(null);
        cCompany.setCreatedBy(user);
        cCompany.setType("b2c");
        
        
        return CompanyManagementFactory.getCompanyRepository().create(cCompany);
	}

	// ************************************************************************	
	public static boolean assign_accounts(
				final BigDecimal       userId,
				final BigDecimal       companyId,
				final String           customerId,
				final List<BigDecimal> accounts
			) {
        
        // ------------------------------------------------------------------------------------
        // Add the OrgId to the company.
		ArrayList<String> orgIds = new ArrayList<String>();
		orgIds.add(customerId);			
		OrgIdFactory.getOrgIdRepository().setOrgIds(
			companyId, 
            orgIds
        );			
		
        // ------------------------------------------------------------------------------------
        // Add the OrgId(s) to the company.
        final UcUserAssignmentAction standard = new UcUserAssignmentAction();
        
        for(final BigDecimal id : accounts) {
        	final var result = standard.assignAccountToCompany(
        		id.toPlainString(), 
        		companyId.toPlainString()
        	);
        	if (!result.equals("success")) return false;
        }
        
        // ------------------------------------------------------------------------------------
        // Add the OrgId(s) to the company.
        UserCompanyLinkFactory.getUserCompanyLinkRepository().linkUserToCompany(
        	userId, 
            companyId
        );
        
        
        return true;
	}
	
	// ************************************************************************	
	private static void delete_user(
				final IServiceLocator2 services,
				final ICredential      user,
				final BigDecimal       companyId
			) {
		final IAAAClassFactory   factory = services.findService(IAAAClassFactory.class);
		final ICredentialManager manager = factory.createCredentialManager();
		
		try {
			manager.removeCredential(user);
		} catch(MargaritaCredentialException e) {
			LOG.error("Could not delete enrolled user.", e);
		}
		
		if (companyId != null) { 
			CompanyManagementFactory.getCompanyRepository().delete(companyId);
		}
	}
			
	// ************************************************************************	
	public static String create_account(
				final IServiceLocator2 services,
				final String           customerId,
				final String           accountId
			) {
		// --------------------------------------------------------------------
		final var verify = mDao.accountsForOrg(customerId);
		
		if (verify == null) return "invalid";

		var accounts = new ArrayList<BigDecimal>();
		var found    = false;
		
        final String ignore = AppConfig.get("1ffc.ignore.group");
		for(var bean : verify) {
			if (bean.payGroup.equals(ignore) == false) {
				accounts.add(bean.id);
			}
			if (bean.account.equals(accountId)) {
				found = true;
			}
		}
		
		if (found == false) {
			return "invalid";
		}
		
		// --------------------------------------------------------------------
		final var user = create_user(services, customerId);
		
		if (user == null) return "error";		
		// --------------------------------------------------------------------
		BigDecimal companyId = null;
		try {
			companyId = create_company(
				new BigDecimal(user.getId()), 
				customerId
			);
		} catch(Throwable e) {
			LOG.error("Error creating the company.", e);
		}
		
		if (companyId == null) {
			delete_user(services, user, null);
			return "error";			
		}

		// --------------------------------------------------------------------
		final var retval = assign_accounts(
			new BigDecimal(user.getId()), 
			companyId, 
			customerId, 
			accounts
		);
		
		if (retval == false) {
			delete_user(services, user, companyId);
			return "error";			
		}
			
		return "success";
	}
	
	public static String isUserAlreadyRegistered(
			final IServiceLocator2 services,
			final String szCustomerId
			) {
		
		String szStatus = "not_registered";
		
		try {
			
			LOG.debug("Enroll:isUserAlreadyRegistered() ..... entered method for customer id: {}",
			        szCustomerId);
			
			// ************************************************************************************
			// Checking for existence of user.
			final var user = mDao.user(szCustomerId);
			
			// ************************************************************************************
			// If user not null it means that user already created.
			if (null != user) {
				szStatus = "registered";
			}
			
		} catch (Exception e) {
			szStatus = "error";
			LOG.error(
			        "Enroll:isUserAlreadyRegistered() ..... failed to check existance of user for customer id: {}",
			        szCustomerId, e, e);
		}
		return szStatus;
	}
}
