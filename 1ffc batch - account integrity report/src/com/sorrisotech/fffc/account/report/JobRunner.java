/* (c) Copyright 2025 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc.
 * 40 Nagog Park
 * Acton, MA 01720
 * +1.978.635.3900
 *
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc.  "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.account.report;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;

/**************************************************************************************************
 * Start the loader job.
 * 
 * @author Asrar Saloda
 * 
 */
public class JobRunner {

	/**********************************************************************************************
	 * Logger.
	 */
	private static Logger LOG = LoggerFactory.getLogger(JobRunner.class);
	
	/**********************************************************************************************
	 * Creates a thread to log the shutdown.
	 */
	static class Shutdown extends Thread {
	
		/******************************************************************************************
		 * Called when the system shuts down.  Log the shutdown time.
		 */
		@Override
		public void run() {
			LOG.info("Finished at: " + new Date());
		}
	}
	
	/**********************************************************************************************
	 * Main entry point for the application.
	 * 
	 * @param aArgs  Not used.
	 */
	public static void main(
		final String aArgs[]
		) {
	
		Runtime.getRuntime().addShutdownHook(new Shutdown());
		
		String szXml = "accountReport.xml";
		String szJobName = "accountReportJob";
		String szTstamp = "tstamp=" + String.valueOf(System.currentTimeMillis());
		
		try {
			LOG.info("Started at: " + new Date());

			CommandLineJobRunner.main(new String[]{
					szXml,
					szJobName,
					szTstamp
			});
		} catch (Exception e) {
			LOG.error("Failed at: " + new Date(), e);
		}
	}
}
