package com.sorrisotech.svcs.agentpay.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ListIterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.fffc.agent.pay.ApiPayDao;
import com.sorrisotech.fffc.agent.pay.Fffc;
import com.sorrisotech.fffc.agent.pay.data.ScheduledPaymentBean;
import com.sorrisotech.svcs.agentpay.api.IApiAgentPay;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

public class GetScheduledPaymentsAsJson extends GetScheduledPaymentsAsJsonBase {

	private static final long serialVersionUID = -720322122417219938L;

	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(GetScheduledPaymentsAsJson.class);
	
	// ************************************************************************
	private ApiPayDao mDao = Fffc.apiPay();

	private void getScheduledPaymentsFormatted (	
					final BigDecimal       userId,
					final String           accountId,
					final IRequestInternal op
				) 
	{
			
		final List<ScheduledPaymentBean> data = mDao.scheduledPayments(userId, accountId);

		JsonObjectBuilder bld = Json.createObjectBuilder();
		JsonArrayBuilder abld = Json.createArrayBuilder();
		
		if (!((null == data) || (0 == data.size()))) {
			// -- create a builder, add each item in the list to the JsonArray --
			ListIterator<ScheduledPaymentBean> it = data.listIterator();
			while (it.hasNext()) {
				ScheduledPaymentBean pmt = it.next();
				JsonObjectBuilder bObj = Json.createObjectBuilder();
				bObj.add("paymentId", pmt.paymentId);
				{
					SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
					String sDate = date.format(pmt.paymentDate);
					bObj.add("paymentDate", sDate);
				}
				bObj.add("paymentDate", pmt.paymentDate.toString());

			
				DecimalFormat dFmt = new DecimalFormat("#0.00");
				
				bObj.add("paymentAmount", dFmt.format(pmt.paymentAmount));
				bObj.add("paymentSurcharge", dFmt.format(pmt.paymentSurcharge));
				bObj.add("paymentTotalAmount",dFmt.format(pmt.paymentTotalAmount));
				bObj.add("paymentCategory", pmt.paymentCategory);
				bObj.add("paymentStatus", pmt.paymentStatus);
				bObj.add("paymentAccountNickname", pmt.paymentAccountNickname);
				bObj.add("paymentAccountMasked", pmt.paymentAccountMasked);
				bObj.add("paymentAccountType", pmt.paymentAccountType);
				abld.add(bObj.build());
			}
		}		
		// -- add the array into this json object we are building, not the array may be empty --
		bld.add("scheduledPayments",  abld.build()); 
		op.set(IApiAgentPay.GetScheduledPaymentsAsJson.scheduledPayments, bld.build().toString());

	}
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal op) {

	final String customerId = op.getString(IApiAgentPay.Start.customerId);
	final String accountId  = op.getString(IApiAgentPay.Start.accountId);
	
	op.setToResponse();
	
	final var user = mDao.user(customerId);
	
	if (user == null) {
		LOG.warn("GetScheduledPaymentsAsJson:processInternal -- Could not find user with customerId [" + customerId + "].");
		op.setRequestStatus(ServiceAPIErrorCode.Failure);
		return ServiceAPIErrorCode.Failure;
	}
	
	final var account = mDao.lookupAccount(user.userId, accountId);
	
	if (account == null) {
		LOG.warn("GetScheduledPaymentsAsJson:processInternal -- could not find account for customerId [" + customerId + "] and accountId [" + accountId + "].");
		op.setRequestStatus(ServiceAPIErrorCode.Failure);
		return ServiceAPIErrorCode.Failure;			
	}

	getScheduledPaymentsFormatted(user.userId, accountId, op);

	op.setRequestStatus(ServiceAPIErrorCode.Success);
	return ServiceAPIErrorCode.Success;
	}

}
