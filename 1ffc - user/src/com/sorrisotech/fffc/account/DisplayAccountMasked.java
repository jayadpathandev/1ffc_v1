package com.sorrisotech.fffc.account;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sorrisotech.app.common.utils.I18n;
import com.sorrisotech.fffc.account.dao.AccountDao;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.ITable2Data;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.data.table2.ITable2ColumnData;
import com.sorrisotech.svcs.itfc.data.table2.ITable2RowData;
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
			
			Arrays.stream(GroupJsonArray).forEach(group -> group.setsDisplayAccountNumber(
			        maskDisplayAccount(group.getsDisplayAccountNumber())));
			
			sGroupJsonString = m_cObjectMapper.writeValueAsString(GroupJsonArray);
			
		} catch (JsonProcessingException e) {
			LOG.debug("DisplayAccountMasked.....escapeGroupingJson()...exception thrown: " + e, e);
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
	
	// TODO Will have to move this method to right place when working for nickname.
	public void setAccountDataTableFromPaymentAutomaticData(IServiceLocator2 locator,
	        IUserData userData, ITable2Data cDataTable, ITable2Data cDisplayTable) {
		try {
			
			cDisplayTable.empty();
			
			for (int i = 0; i < cDataTable.getTotalRows(); i++) {
				ITable2RowData      cRow  = cDataTable.getRow(i);
				Map<Object, Object> mData = cRow.getDataValues();
				
				cRow.setDataValue("GRP_DISP_ACC",
				        maskDisplayAccount(mData.get("GRP_DISP_ACC").toString()));
				
				cDisplayTable.saveRow(cRow);
			}
			
		} catch (Exception e) {
			LOG.error("setAccountDataTableFromPaymentAutomaticData() An exception was thrown", e);
		}
	}
	
	// TODO Will have to move this method to right place when working for nickname.
	public void setAccountDataTableFromPaymentHistoryDataMaskedAccount(IServiceLocator2 locator,
	        IUserData userData, ITable2Data cDataTable, ITable2Data cDisplayTable,
	        String configColumns) {
		try {
			
			final HashSet<String> columns = new HashSet<String>();
			for (final String column : configColumns.split("\\|")) {
				columns.add(column);
			}
			
			cDisplayTable.empty();
			
			for (int i = 0; i < cDataTable.getTotalRows(); i++) {
				ITable2RowData      cRow       = cDataTable.getRow(i);
				Map<Object, Object> mData      = cRow.getDataValues();
				ArrayNode           cJsonArray = (ArrayNode) m_cObjectMapper
				        .readTree((String) mData.get("GROUPING_JSON"));
				
				String szSourceName = cRow.getDataValue("PAY_FROM_ACCOUNT");
				String szSourceType = cRow.getDataValue("PAY_SOURCE_TYPE");
				
				// i18n Payment Type
				if (szSourceType != null && szSourceType.equalsIgnoreCase("bank")) {
					cRow.setDataValue("PAY_SOURCE_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_type_bank"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("credit")) {
					cRow.setDataValue("PAY_SOURCE_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_type_credit"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("debit")) {
					cRow.setDataValue("PAY_SOURCE_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_type_debit"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("sepa")) {
					cRow.setDataValue("PAY_SOURCE_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_type_sepa"));
				}
				
				// i18n Payment Name
				String szText = "";
				if (szSourceName != null && szSourceName.equalsIgnoreCase("unsaved")) {
					if (szSourceType.equalsIgnoreCase("bank")) {
						szText = I18n.translate(locator, userData,
						        "paymentHistory_unsavedBankAcct");
					} else if (szSourceType.equalsIgnoreCase("credit")) {
						szText = I18n.translate(locator, userData,
						        "paymentHistory_unsavedCreditCard");
					} else if (szSourceType.equalsIgnoreCase("debit")) {
						szText = I18n.translate(locator, userData,
						        "paymentHistory_unsavedDebitCard");
					} else if (szSourceType.equalsIgnoreCase("sepa")) {
						szText = I18n.translate(locator, userData,
						        "paymentHistory_unsavedSepaAcct");
					}
					
					cRow.setDataValue("PAY_FROM_ACCOUNT", szText);
				}
				
				boolean bFound = false;
				
				for (JsonNode cGroupingNode : cJsonArray) {
					if (bFound) {
						szText = "";
						szText = I18n.translate(locator, userData,
						        "paymentHistory_sMultipleAccounts");
						cRow.setDataValue("BILLING_ACCOUNT_ID", szText);
						break; // Breaking because we only care about the first element in the array
					} else {
						cRow.setDataValue("BILLING_ACCOUNT_ID", maskDisplayAccount(
						        cGroupingNode.get("displayAccountNumber").textValue()));
						bFound = true;
					}
				}
				
				cDisplayTable.saveRow(cRow);
			}
			
			List<ITable2ColumnData> tblCols = cDisplayTable.getColumns();
			
			for (ITable2ColumnData tblCol : tblCols) {
				String tblColId = tblCol.getId();
				
				if (columns.contains(tblColId)) {
					tblCol.removeDisplayTag("visually-hidden");
				}
			}
			
		} catch (Exception e) {
			LOG.error("setAccountDataTableFromPaymentData() An exception was thrown", e);
		}
	}
	
	// TODO Will have to move this method to right place when working for nickname.
	public void setAccountDataTableFromPaymentDataMaskedAccount(IServiceLocator2 locator, IUserData userData, ITable2Data cDataTable,
			ITable2Data cDisplayTable) {
		try {
			cDisplayTable.empty();

			for (int i = 0; i < cDataTable.getTotalRows(); i++) {
				ITable2RowData cRow = cDataTable.getRow(i);
				Map<Object, Object> mData = cRow.getDataValues();
				ArrayNode cJsonArray = (ArrayNode) m_cObjectMapper.readTree((String) mData.get("GROUPING_JSON"));

				String szSourceName = cRow.getDataValue("SOURCE_NAME");
				String szSourceType = cRow.getDataValue("SOURCE_TYPE");

				// i18n Payment Type
				if (szSourceType != null && szSourceType.equalsIgnoreCase("bank")) {
					cRow.setDataValue("SOURCE_TYPE", I18n.translate(locator, userData, "paymentHistory_type_bank"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("credit")) {
					cRow.setDataValue("SOURCE_TYPE", I18n.translate(locator, userData, "paymentHistory_type_credit"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("debit")) {
					cRow.setDataValue("SOURCE_TYPE", I18n.translate(locator, userData, "paymentHistory_type_debit"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("sepa")) {
					cRow.setDataValue("SOURCE_TYPE", I18n.translate(locator, userData, "paymentHistory_type_sepa"));
				}

				// i18n Payment Name
				String szText = "";
				if (szSourceName != null && szSourceName.equalsIgnoreCase("unsaved")) {
					if (szSourceType.equalsIgnoreCase("bank")) {
						szText = I18n.translate(locator, userData, "paymentHistory_unsavedBankAcct");
					} else if (szSourceType.equalsIgnoreCase("credit")) {
						szText = I18n.translate(locator, userData, "paymentHistory_unsavedCreditCard");
					} else if (szSourceType.equalsIgnoreCase("debit")) {
						szText = I18n.translate(locator, userData, "paymentHistory_unsavedDebitCard");
					} else if (szSourceType.equalsIgnoreCase("sepa")) {
						szText = I18n.translate(locator, userData, "paymentHistory_unsavedSepaAcct");
					}
					cRow.setDataValue("SOURCE_NAME", szText);
				}

				boolean bFound = false;

				for (JsonNode cGroupingNode : cJsonArray) {
					if (bFound) {
						szText = "";
						szText = I18n.translate(locator, userData, "paymentHistory_sMultipleAccounts");
						cRow.setDataValue("BILLING_ACCOUNT_ID", maskDisplayAccount(szText));
						break; // Breaking because we only care about the first element in the array
					} else {
						cRow.setDataValue("BILLING_ACCOUNT_ID", maskDisplayAccount(cGroupingNode.get("displayAccountNumber").textValue()));
						bFound = true;
					}
				}

				cDisplayTable.saveRow(cRow);

			}
		} catch (Exception e) {
			LOG.error("setAccountDataTableFromPaymentData() An exception was thrown", e);
		}
	}
}
