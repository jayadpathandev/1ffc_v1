/*
 * (c) Copyright 2017-2023 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
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
package com.sorrisotech.svcs.fffcnotify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.svcs.fffcnotify.api.IApiFffcNotify;
import com.sorrisotech.svcs.fffcnotify.dao.FffcNotifyDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/************************************************************************************************
 * This class can be used to fetch userId by using customerId (orgId) from
 * database.
 * 
 * @author Asrar Saloda.
 */
public class FetchUserId extends FetchUserIdBase {
	
	/**********************************************************************************************
	 * The UID for this class.
	 */
	private static final long serialVersionUID = 3188790023648971168L;
	
	/**********************************************************************************************
	 * Logger for debug messages.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FetchUserId.class);
	
	/**********************************************************************************************
	 * Dao instance for accessing the database.
	 */
	private static final FffcNotifyDao m_cDao = FffcNotifyDao.get();
	
	/**********************************************************************************************
	 * Attribute name in auth_user_profile for customerId or orgId
	 */
	private static final String FFFC_CUSTOMER_ID = "fffcCustomerId";
	
	/**************************************************************************
	 * 1. Turn the request around. 2. Insert all the configuration parameters. 3.
	 * Return the request with success.
	 */
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {
		
		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;
		
		final String szCustomerId = request.getString(IApiFffcNotify.FetchUserId.customerId);
		
		LOG.debug("FetchUserId:processInternal ..... entered method for customer id: {}",
		        szCustomerId);
		
		request.setToResponse();
		
		try {
			
			// --------------------------------------------------------------------------------------
			// Fetching userId from auth_user_profile table by using attrName as
			// fffcCustomerId and attrValue as customerId received from request.
			final String userId = m_cDao.queryUserId(FFFC_CUSTOMER_ID, szCustomerId);
			
			if (null != userId) {
				// --------------------------------------------------------------------------------------
				// Setting userId to response.
				request.set(IApiFffcNotify.FetchUserId.userid, userId);
				
				eReturnCode = ServiceAPIErrorCode.Success;
				
				LOG.debug("FetchUserId:processInternal ..... success for userId: {}" + userId);
			}
		} catch (Exception e) {
			LOG.error(
			        "FetchUserId:processInternal ..... An exception was thrown while fetching userId for customer: {}",
			        szCustomerId, e, e);
		}
		
		request.setStatus(eReturnCode);
		
		return eReturnCode;
	}
	
}
