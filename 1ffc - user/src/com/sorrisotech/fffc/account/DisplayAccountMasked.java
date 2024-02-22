package com.sorrisotech.fffc.account;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorrisotech.fffc.account.dao.AccountDao;
import com.sorrisotech.uc.payment.UcPaymentAction;

public class DisplayAccountMasked {
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DisplayAccountMasked.class);
	
	/**********************************************************************************************
	 * Dao instance for accessing the database.
	 */
	private static final AccountDao m_cDao = AccountDao.get();
	
	/**********************************************************************************************
	 * JSON Object Mapper to read and write.
	 */
	private static ObjectMapper m_cObjectMapper = new ObjectMapper();
	
	/**************************************************************************
	 * This method returns the masked email address.
	 * 
	 * @param szInternalAccountNumber Internal account number.
	 * 
	 * @return masked display account number or nickname.
	 */
	public String getMaskedDisplayAccount(final String szInternalAccountNumber) {
		
		LOG.debug("DisplayAccountMasked.....getMaskedDisplayAccount()...internalAccountNumber: {}"
		        + szInternalAccountNumber);
		
		var szMaskedDisplayAccount = "";
		
		var displayAccountNumber = m_cDao.queryDisplayAccountNumber(szInternalAccountNumber);
		
		if (null != displayAccountNumber)
			szMaskedDisplayAccount = maskDisplayAccount(displayAccountNumber);
		
		LOG.debug("DisplayAccountMasked.....getMaskedDisplayAccount()...displayAccount: {}"
		        + szMaskedDisplayAccount);
		
		return szMaskedDisplayAccount;
	}
	
	public static String maskDisplayAccount(final String displayAccount) {
		
		var maskedDigits = new StringBuilder("X".repeat(Math.max(0, displayAccount.length() - 4)));
		
		var lastFourDigits = displayAccount.substring(Math.max(0, displayAccount.length() - 4));
		
		return maskedDigits.append(lastFourDigits).toString();
	}
	
	public static String escapeGroupingJson(final String sGroupJson) {
		
		final UcPaymentAction ucPaymentAction = new UcPaymentAction();
		
		String sGroupJsonString = null;
		
		try {
			GroupJson[] GroupJsonArray = m_cObjectMapper.readValue(sGroupJson, GroupJson[].class);
			
			Arrays.stream(GroupJsonArray).forEach(group -> group
			        .setsDisplayAccountNumber(maskDisplayAccount(group.getsDisplayAccountNumber())));
			
			sGroupJsonString = m_cObjectMapper.writeValueAsString(GroupJsonArray);
			
		} catch (JsonProcessingException e) {
			LOG.debug("DisplayAccountMasked.....escapeGroupingJson()...exception thrown: "
			        + e,e);
		}
		return ucPaymentAction.escapeGroupingJson(sGroupJsonString);
	}
	
	@SuppressWarnings("unused")
	private static class GroupJson {
		
		@JsonProperty("internalAccountNumber")
		private String sInternalAccountNumber;
		
		@JsonProperty("displayAccountNumber")
		private String sDisplayAccountNumber;
		
		@JsonProperty("paymentGroup")
		private String paymentGroup;

		public String getsInternalAccountNumber() {
			return sInternalAccountNumber;
		}

		public void setsInternalAccountNumber(String sInternalAccountNumber) {
			this.sInternalAccountNumber = sInternalAccountNumber;
		}

		public String getsDisplayAccountNumber() {
			return sDisplayAccountNumber;
		}

		public void setsDisplayAccountNumber(String sDisplayAccountNumber) {
			this.sDisplayAccountNumber = sDisplayAccountNumber;
		}

		public String getPaymentGroup() {
			return paymentGroup;
		}

		public void setPaymentGroup(String paymentGroup) {
			this.paymentGroup = paymentGroup;
		}
		
	}
	
}
