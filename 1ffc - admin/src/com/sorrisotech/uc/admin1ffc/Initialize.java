/*
 * (c) Copyright 2017-2023 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 400 West Cummings Park, Suite 1725-184, Woburn, MA
 * 01801, USA +1.978.635.3900
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
package com.sorrisotech.uc.admin1ffc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.library.nls.NLSClient;
import com.sorrisotech.utils.AppConfig;

/*********************************************************************************
 * This class is used to initialization.
 * 
 * @author Asrar Saloda
 */
public class Initialize {
	
	private static final Logger LOG = LoggerFactory.getLogger(Initialize.class);
	
	private static boolean m_bInitialized = false;
	
	/*********************************************************************************
	 * This method is used to initialize NLS library.
	 * 
	 */
	public static synchronized void init() {
		if (!m_bInitialized) {
			
			m_bInitialized = NLSClient.init(AppConfig.get("nls.api.base.url"),
			        AppConfig.get("nls.api.client.id"),
			        AppConfig.get("nls.api.client.secret"),
			        AppConfig.get("nls.api.version"));
			
			if (!m_bInitialized) {
				LOG.debug("Initialize:init() ...... Failed to inilialize NLS API Library");
			}
			
		}
	}
	
}
