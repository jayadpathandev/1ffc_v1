/* (c) Copyright 2016-2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park,
 * Suite 1725-184,
 * Woburn, MA 01801, USA
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
package com.sorrisotech.fffc.batch.status.processor;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;

import com.sorrisotech.fffc.batch.status.processor.bean.RecurringPayment;
import com.sorrisotech.fffc.batch.status.processor.bean.ScheduledPayment;
import com.sorrisotech.fffc.batch.status.processor.bean.User;
import com.sorrisotech.persona.notification.api.NotificationTypes;

/**************************************************************************************************
 * Implementation of ItemProcessListener, which sends notificaiton to user after 
 * deletion of records.
 * 
 * @author Rohit Singh
 * 
 */
public class ProcessListener implements ItemProcessListener<User, User>{
	
	/**************************************************************************
     * Development level logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessListener.class);

	@Override
	public void afterProcess(User cInput, User cOutput) {
		
		LOG.info("Sending notificaiton for user : {}", cOutput.getUserId());
		
		LOG.info("Sending notifications for scheduled payments for user : {}", cOutput.getUserId());
		
		for (ScheduledPayment cPayment : cOutput.getScheduledPayments()) {
			Map<String, String> cParams = new HashMap<>();
			cParams.put("accountNumber", cPayment.getAccounutNumber());
			cParams.put("walletNickName", cPayment.getWalletNickName());
			cParams.put("paymentDate", cPayment.getScheduledPaymentDate().toString());
			cParams.put("amount", cPayment.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
			
			if (!cOutput.isAchDisabled()) {
				LOG.info("Sending payment disabled notification for record : {}", cPayment.getId());
				
				NotificationDispatcher.dispatchNotification(
						cParams,
						NotificationTypes.PAYMENT_SCHEDULED_PAYMENT_DISABLED_CANCELLED, 
						cOutput.getUserId()
				);
			} else {
				LOG.info("Sending ach disabled notification for record : {}", cPayment.getId());
				
				NotificationDispatcher.dispatchNotification(
						cParams,
						NotificationTypes.PAYMENT_SCHEDULED_ACH_DISABLED_CANCELLED, 
						cOutput.getUserId()
				);
			}
		}
		
		LOG.info("Sending notifications for recurring payments for user : {}", cOutput.getUserId());
		
		for (RecurringPayment cPayment : cOutput.getRecurringPayments()) {
			Map<String, String> cParams = new HashMap<>();
			cParams.put("accountNumber", cPayment.getAccounutNumber());
			cParams.put("walletNickName", cPayment.getWalletNickName());

			if (!cOutput.isAchDisabled()) {
				LOG.info("Sending payment disabled notification for record : {}", cPayment.getId());
				
				NotificationDispatcher.dispatchNotification(
						cParams,
						NotificationTypes.PAYMENT_RECURRING_PAYMENT_DISABLED_RULE_CANCELLED, 
						cOutput.getUserId()
				);
			} else {
				LOG.info("Sending ach disabled notification for record : {}", cPayment.getId());
				
				NotificationDispatcher.dispatchNotification(
						cParams,
						NotificationTypes.PAYMENT_RECURRING_ACH_DISABLED_RULE_CANCELLED, 
						cOutput.getUserId()
				);
			}
		}
	}

	@Override
	public void beforeProcess(User input) {}

	@Override
	public void onProcessError(User input, Exception output) {}

}
