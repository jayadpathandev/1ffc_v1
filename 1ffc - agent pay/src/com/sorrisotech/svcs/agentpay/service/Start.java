package com.sorrisotech.svcs.agentpay.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.fffc.agent.pay.ApiPayDao;
import com.sorrisotech.fffc.agent.pay.Fffc;
import com.sorrisotech.svcs.agentpay.api.IApiAgentPay;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

public class Start extends StartBase {

	// ************************************************************************
	private static final long serialVersionUID = 4670798356384007647L;
	
	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(Start.class);
	
	// ************************************************************************
	private ApiPayDao mDao = Fffc.apiPay();

	// ************************************************************************
	private void populateScheduled(
				final BigDecimal       userId,
				final String           accountId,
				final IRequestInternal op
			) {
		final var bill = mDao.findBill(userId, accountId);
		
		final var data = mDao.scheduledSummary(
			userId, 
			accountId,
			bill != null ? bill.dueDate : null
		);
		
		if (data != null) {
			op.set(IApiAgentPay.Start.scheduledCount, data.count.toPlainString());
			op.set(IApiAgentPay.Start.scheduledDate,  data.date);
			op.set(IApiAgentPay.Start.scheduledTotal, data.total.toPlainString());
		} else {
			op.set(IApiAgentPay.Start.scheduledCount, BigDecimal.ZERO.toPlainString());
			op.set(IApiAgentPay.Start.scheduledDate, "");
			op.set(IApiAgentPay.Start.scheduledTotal, BigDecimal.ZERO.toPlainString());
		}
		
		op.set(
			IApiAgentPay.Start.invoice, 
			bill != null ? bill.invoice : ""
		);
	}

	// ************************************************************************
	private void populateAuto(
				final BigDecimal       userId,
				final String           accountId,
				final String           configChange,
				final IRequestInternal op
			) {
		final var data = mDao.autoPay(userId, accountId, configChange);
		
		if (data != null) {
			op.set(IApiAgentPay.Start.automaticEnabled, true);
			op.set(IApiAgentPay.Start.automaticDate,    data.when);
			op.set(IApiAgentPay.Start.automaticAmount,  data.amount);
			op.set(IApiAgentPay.Start.automaticCount,   data.stop);
		} else {
			op.set(IApiAgentPay.Start.automaticEnabled, false);
			op.set(IApiAgentPay.Start.automaticDate,    "");
			op.set(IApiAgentPay.Start.automaticAmount,  "0.00");
			op.set(IApiAgentPay.Start.automaticCount,   "0");
		}
	}
	
	
	// ************************************************************************
	@Override
	protected ServiceAPIErrorCode processInternal(
				final IRequestInternal op
			) {
		final String customerId = op.getString(IApiAgentPay.Start.customerId);
		final String accountId  = op.getString(IApiAgentPay.Start.accountId);
		final String configChange = op.getString(IApiAgentPay.Start.configChange);
		
		op.setToResponse();
		
		var user = mDao.user(customerId);
		
		if (user == null) {
			LOG.info("Could not find user with customerId [" + customerId + "].");
			op.set(IApiAgentPay.Start.userid, "");
			op.set(IApiAgentPay.Start.userName, "");
			op.set(IApiAgentPay.Start.companyId, "");
			op.set(IApiAgentPay.Start.accountNumber, "");
			op.set(IApiAgentPay.Start.payGroup, "");

			op.set(IApiAgentPay.Start.invoice, "");
			op.set(IApiAgentPay.Start.scheduledCount, "");
			op.set(IApiAgentPay.Start.scheduledDate, "");
			op.set(IApiAgentPay.Start.scheduledTotal, "");

			op.set(IApiAgentPay.Start.automaticEnabled, "");
			op.set(IApiAgentPay.Start.automaticDate, "");
			op.set(IApiAgentPay.Start.automaticAmount, "");
			op.set(IApiAgentPay.Start.automaticCount, "");
			
			op.setRequestStatus(ServiceAPIErrorCode.Success);
			return ServiceAPIErrorCode.Success;
		}
		
		final var account = mDao.lookupAccount(customerId, accountId);
		
		if (account == null) {
			LOG.warn("Could not find account for customerId [" + customerId + "] and accountId [" + accountId + "].");
			op.setRequestStatus(ServiceAPIErrorCode.Failure);
			return ServiceAPIErrorCode.Failure;			
		}
		
		op.set(IApiAgentPay.Start.userid, user.userId);
		op.set(IApiAgentPay.Start.userName, user.username);
		op.set(IApiAgentPay.Start.companyId, user.companyId.toPlainString());
		op.set(IApiAgentPay.Start.accountNumber, account.externalNumber);
		op.set(IApiAgentPay.Start.payGroup, account.payGroup);
		
		populateScheduled(user.userId, accountId, op);
		populateAuto(user.userId, accountId, configChange, op);
		
		op.setRequestStatus(ServiceAPIErrorCode.Success);
		return ServiceAPIErrorCode.Success;
	}
	

}
