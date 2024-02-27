package com.sorrisotech.svcs.agentpay.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
				) throws JsonProcessingException 
	{
			
		final List<ScheduledPaymentBean> data = mDao.scheduledPayments(userId, accountId);
		final ObjectMapper mapper = new ObjectMapper();
		
		final ObjectNode bld = mapper.createObjectNode();
		final ArrayNode abld = bld.withArray("scheduledPayments");
		
		if (!((null == data) || (0 == data.size()))) {
			// -- create a builder, add each item in the list to the JsonArray --
			ListIterator<ScheduledPaymentBean> it = data.listIterator();
			while (it.hasNext()) {
				ScheduledPaymentBean pmt = it.next();
				final ObjectNode bObj = mapper.createObjectNode();
				bObj.put("paymentId", pmt.paymentId);
				{
					SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
					String sDate = date.format(pmt.paymentDate);
					bObj.put("paymentDate", sDate);
				}
				bObj.put("paymentDate", pmt.paymentDate.toString());

				// -- need to format payment numbers for the json --
				DecimalFormat dFmt = new DecimalFormat("#0.00");
				
				bObj.put("paymentAmount", dFmt.format(pmt.paymentAmount));
				bObj.put("paymentSurcharge", dFmt.format(pmt.paymentSurcharge));
				bObj.put("paymentTotalAmount",dFmt.format(pmt.paymentTotalAmount));
				bObj.put("paymentCategory", pmt.paymentCategory);
				bObj.put("paymentStatus", pmt.paymentStatus);
				bObj.put("paymentAccountNickname", pmt.paymentAccountNickname);
				bObj.put("paymentAccountMasked", pmt.paymentAccountMasked);
				bObj.put("paymentAccountType", pmt.paymentAccountType);
				abld.add(bObj);
			}
		}		
		// -- add the array into this json object we are building, not the array may be empty --
		op.set(IApiAgentPay.GetScheduledPaymentsAsJson.scheduledPayments, mapper.writeValueAsString(bld));

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

	try {
		getScheduledPaymentsFormatted(user.userId, accountId, op);
	} catch (JsonProcessingException e) {
		LOG.error("Error creating the JSON:", e);
		return ServiceAPIErrorCode.Failure;
	}

	op.setRequestStatus(ServiceAPIErrorCode.Success);
	return ServiceAPIErrorCode.Success;
	}

}
