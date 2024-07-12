/* (c) Copyright 2016-2024 Sorriso Technologies, Inc(r), All Rights Reserved, 
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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.sorrisotech.fffc.account.DisplayAccountMasked;
import com.sorrisotech.fffc.batch.status.processor.bean.Record;
import com.sorrisotech.fffc.batch.status.processor.bean.RecurringPayment;
import com.sorrisotech.fffc.batch.status.processor.bean.ScheduledPayment;
import com.sorrisotech.persona.notification.api.NotificationTypes;

public class WriterNotificationListener extends NamedParameterJdbcDaoSupport
		implements ItemWriteListener<List<Record>> {

	private static final Logger LOG = LoggerFactory.getLogger(WriterNotificationListener.class);

	@Override
	public void afterWrite(List<? extends List<Record>> cRecords) {

		for (List<Record> records : cRecords) {
			// Iterate over the inner list
			for (Record record : records) {
				LOG.info("Sending notificaiton for user : {}", record.getInternalAccNumber());

				LOG.info("Sending notifications for scheduled payments for user : {}", record.getScheduledPayments());

				for (ScheduledPayment cPayment : record.getScheduledPayments()) {
					Map<String, String> cParams = new HashMap<>();

					final String displayAccountNickname = new DisplayAccountMasked().displayAccountLookup(null,
							cPayment.getUserId(), cPayment.getInternalAccount(), cPayment.getPayGroup());

					cParams.put("accountNumber", displayAccountNickname);
					cParams.put("walletNickName", cPayment.getWalletNickName());
					cParams.put("paymentDate", cPayment.getScheduledPaymentDate().toString());
					cParams.put("amount", cPayment.getAmount().setScale(2, RoundingMode.HALF_UP).toString());

					if (!record.isAchDisabled()) {
						LOG.info("Sending payment disabled notification for record : {}", cPayment.getId());

						NotificationDispatcher.dispatchNotification(cParams,
								NotificationTypes.PAYMENT_SCHEDULED_PAYMENT_DISABLED_CANCELLED, cPayment.getUserId());

						LOG.info("Sending notifications for scheduled payments for user : {}", cPayment.getUserId());
					} else {
						LOG.info("Sending ach disabled notification for record : {}", cPayment.getId());

						NotificationDispatcher.dispatchNotification(cParams,
								NotificationTypes.PAYMENT_SCHEDULED_ACH_DISABLED_CANCELLED, cPayment.getUserId());

						LOG.info("Sending notifications for scheduled payments for user : {}", cPayment.getUserId());
					}
				}

				for (RecurringPayment cPayment : record.getRecurringPayments()) {
					Map<String, String> cParams = new HashMap<>();

					final String displayAccountNickname = new DisplayAccountMasked().displayAccountLookup(null,
							cPayment.getUserId(), cPayment.getInternalAccount(), cPayment.getPayGroup());

					cParams.put("accountNumber", displayAccountNickname);
					cParams.put("walletNickName", cPayment.getWalletNickName());

					if (!record.isAchDisabled()) {
						LOG.info("Sending payment disabled notification for record : {}", cPayment.getId());

						NotificationDispatcher.dispatchNotification(cParams,
								NotificationTypes.PAYMENT_RECURRING_PAYMENT_DISABLED_RULE_CANCELLED,
								cPayment.getUserId());

						LOG.info("Sending notifications for recurring payments for user : {}", cPayment.getUserId());
					} else {
						LOG.info("Sending ach disabled notification for record : {}", cPayment.getId());

						NotificationDispatcher.dispatchNotification(cParams,
								NotificationTypes.PAYMENT_RECURRING_ACH_DISABLED_RULE_CANCELLED, cPayment.getUserId());

						LOG.info("Sending notifications for recurring payments for user : {}", cPayment.getUserId());
					}
				}
			}
		}

	}

	@Override
	public void beforeWrite(List<? extends List<Record>> cRecord) {

	}

	@Override
	public void onWriteError(Exception exception, List<? extends List<Record>> cRecord) {

	}
}