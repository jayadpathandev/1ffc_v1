package com.sorrisotech.svcs.accountstatus.service;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sorrisotech.svcs.accountstatus.api.IApiAccountStatus;
import com.sorrisotech.svcs.accountstatus.dao.EligibleRegisteredAcctElement;
import com.sorrisotech.svcs.accountstatus.dao.EligibleRegisteredAcctsDaoImpl;
import com.sorrisotech.svcs.accountstatus.dao.IEligibleRegisteredAccountsDao;
import com.sorrisotech.svcs.serviceapi.api.IRequestInternal;
import com.sorrisotech.svcs.serviceapi.api.ServiceAPIErrorCode;

/**
 * Returns the accounts assigned to a user that are eligible for online access
 * 
 * @author john kowalonek
 * @since 2024-Mar-24
 * @version 2024-Mar-24 jak First version
 */
public class GetEligibleAssignedAccounts extends GetEligibleAssignedAccountsBase {

	private static final long serialVersionUID = 2957155766169105776L;
	private static final Logger LOG = LoggerFactory.getLogger(GetEligibleAssignedAccounts.class);
	
	@Override
	protected ServiceAPIErrorCode processInternal(IRequestInternal request) {

		ServiceAPIErrorCode eReturnCode = ServiceAPIErrorCode.Failure;

		final String sStatusPaymentGroup = request.getString(IApiAccountStatus.GetEligibleAssignedAccounts.statusPaymentGroup);
		final String sUserId = request.getString(IApiAccountStatus.GetEligibleAssignedAccounts.user);
		final String sBillPayGroup = request.getString(IApiAccountStatus.GetEligibleAssignedAccounts.billPaymentGroup);
		request.setToResponse();
	
		// -- retrieve the accounts to be registered --
		IEligibleRegisteredAccountsDao DAO = new EligibleRegisteredAcctsDaoImpl();
		List<EligibleRegisteredAcctElement> lRegisteredAccts = null;
		try {
			lRegisteredAccts = DAO.getEligibleRegisteredAccts(sUserId, sStatusPaymentGroup);
		}
		catch (Exception e) {
			LOG.error("GetEligibleAssignedAccounts:processInternal -- exception thrown getting list", e);
			return eReturnCode;
		}
		
		if ((lRegisteredAccts == null) || (lRegisteredAccts.size() == 0)) {
			LOG.debug("GetEligibleAssignedAccounts:processInternal -- no eligible accounts");
			return eReturnCode;
		}		
		
		LOG.debug("GetAccountsForRegistration:processInternal -- found {} eligible accounts for pmt group {}, userId {}",
				lRegisteredAccts.size(), sStatusPaymentGroup, sUserId);
		
		String szRtnString = getAccountsAsJsonArray( lRegisteredAccts, sBillPayGroup );
		request.set(IApiAccountStatus.GetEligibleAssignedAccounts.accountsAsJsonArray, szRtnString);	
		eReturnCode = ServiceAPIErrorCode.Success;
		request.setStatus(eReturnCode);

		
		return eReturnCode;

	}
	
	/** 
	 * Serializes the list of accounts into a JSON array
	 * 
	 * @param accounts
	 * @param cszBillPayGroup
	 * @return
	 */
	private String getAccountsAsJsonArray(List<EligibleRegisteredAcctElement> accounts, String cszBillPayGroup) {

		String szRetValue = null;
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode abld = mapper.createArrayNode();
		
		if (!((null == accounts) || (0 == accounts.size()))) {
			// -- create a builder, add each item in the list to the JsonArray --
			ListIterator<EligibleRegisteredAcctElement> it = accounts.listIterator();
			while (it.hasNext()) {
				EligibleRegisteredAcctElement acct = it.next();
				final ObjectNode bObj = mapper.createObjectNode();
				bObj.put("internalAccountId", acct.m_szInternalAccountId);
				bObj.put("externalAccountId", acct.m_szDisplayAccountId);
				bObj.put("payGroup", cszBillPayGroup);
				abld.add(bObj);
			}
		}
		try {
			szRetValue = mapper.writeValueAsString(abld);
		} catch (JsonProcessingException e) {
			LOG.error("getAccountsAsJsonArray -- failed to write out Json object");
			e.printStackTrace();
		}
		return szRetValue;
	}


}
