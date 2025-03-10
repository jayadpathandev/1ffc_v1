package com.sorrisotech.svcs.agentpay.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.fffc.agent.pay.ApiPayDao;
import com.sorrisotech.fffc.agent.pay.Fffc;
import com.sorrisotech.svcs.agentpay.api.IApiAgentPay;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

public class GetAutoPay extends GetAutoPayBase {

	private static final long serialVersionUID = 4919296960704136840L;

	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(Start.class);
	
	// ************************************************************************
	private ApiPayDao mDao = Fffc.apiPay();

	// ************************************************************************
	private void populateAuto(
				final BigDecimal       userId,
				final String           accountId,
				final String           configChange,
				final IRequestInternal op
			) {
		final var data = mDao.autoPay(userId, accountId, configChange);
		
		if (data != null) {
			op.set(IApiAgentPay.GetAutoPay.automaticEnabled, true);
			op.set(IApiAgentPay.GetAutoPay.automaticDate,    data.when);
			op.set(IApiAgentPay.GetAutoPay.automaticAmount,  data.amount);
			op.set(IApiAgentPay.GetAutoPay.automaticCount,   data.stop);
			op.set(IApiAgentPay.GetAutoPay.automaticPaymentId, data.id);
			op.set(IApiAgentPay.GetAutoPay.automaticSourceId, data.sourceId);
			op.set(IApiAgentPay.GetAutoPay.userId, data.userId);
		} else {
			op.set(IApiAgentPay.GetAutoPay.automaticEnabled, false);
			op.set(IApiAgentPay.GetAutoPay.automaticDate,    "");
			op.set(IApiAgentPay.GetAutoPay.automaticAmount,  "0.00");
			op.set(IApiAgentPay.GetAutoPay.automaticCount,   "0");
			op.set(IApiAgentPay.GetAutoPay.automaticPaymentId, "0");
			op.set(IApiAgentPay.GetAutoPay.automaticSourceId, "");
			op.set(IApiAgentPay.GetAutoPay.userId,            "");
		}
	}

	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal op) {

		final String customerId   = op.getString(IApiAgentPay.Start.customerId);
		final String accountId    = op.getString(IApiAgentPay.Start.accountId);
		final String configChange = op.getString(IApiAgentPay.Start.configChange);
		
		op.setToResponse();
		
		final var user = mDao.user(customerId);
		
		if (user == null) {
			LOG.warn("GetAutoPay:processInternal -- Could not find user with customerId [" + customerId + "].");
			op.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;
		}
		
		final var account = mDao.lookupAccount(customerId, accountId);
		
		if (account == null) {
			LOG.warn("GetAutoPay:processInternal -- could not find account for customerId [" + customerId + "] and accountId [" + accountId + "].");
			op.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;			
		}
		
		populateAuto(user.userId, accountId, configChange, op);
		
		op.setRequestStatus(ServiceAPIErrorCode.Success);
		return ServiceAPIErrorCode.Success;
	}

}
