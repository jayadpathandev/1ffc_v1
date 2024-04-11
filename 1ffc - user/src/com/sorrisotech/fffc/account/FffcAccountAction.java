/*
 * (c) Copyright 2017-2024 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 * 
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.fffc.account;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sorrisotech.app.common.utils.I18n;
import com.sorrisotech.common.app.RestResponseUtil;
import com.sorrisotech.common.rest.Result;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.ITable2Data;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.data.table2.ITable2ColumnData;
import com.sorrisotech.svcs.itfc.data.table2.ITable2RowData;
import com.sorrisotech.uc.payment.UcPaymentAction;

public class FffcAccountAction implements IExternalReuse {
	
	/***************************************************************************
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6212872058959542858L;
	
	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(FffcAccountAction.class);
	
	/**************************************************************************
	 * This method always returns IExternalReuse.REUSE_USECASE.
	 * 
	 * @return This method always returns IExternalReuse.REUSE_USECASE.
	 */
	public int getReuse() {
		return IExternalReuse.REUSE_SESSION_SINGLETON;
	}
	
	/**********************************************************************************************
	 * JSON Object Mapper to read and write.
	 */
	private static ObjectMapper m_cObjectMapper = new ObjectMapper();
	
	/************************************************************************************************
	 * This method can be used to set account data table from payment data.
	 * 
	 * @param cDataTable    Payment data table.
	 * @param cDisplayTable Account data display table.
	 * @param szUserId      The user id.
	 * 
	 */
	public void setAccountDataTableFromPaymentData(
	        IServiceLocator2 locator,
	        IUserData userData,
	        ITable2Data cDataTable,
	        ITable2Data cDisplayTable,
	        final String szUserId) {
		try {
			cDisplayTable.empty();
			
			for (int i = 0; i < cDataTable.getTotalRows(); i++) {
				ITable2RowData      cRow       = cDataTable.getRow(i);
				Map<Object, Object> mData      = cRow.getDataValues();
				ArrayNode           cJsonArray = (ArrayNode) m_cObjectMapper
				        .readTree((String) mData.get("GROUPING_JSON"));
				
				String szSourceName = cRow.getDataValue("SOURCE_NAME");
				String szSourceType = cRow.getDataValue("SOURCE_TYPE");
				
				String szPayAmtNum = new BigDecimal(cRow.getDataValue("PAY_AMT_NUM")).setScale(2, RoundingMode.HALF_UP).toString();
				cRow.setDataValue("PAY_AMT_NUM", szPayAmtNum);
				
				// i18n Payment Type
				if (szSourceType != null && szSourceType.equalsIgnoreCase("bank")) {
					cRow.setDataValue("SOURCE_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_type_bank"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("credit")) {
					cRow.setDataValue("SOURCE_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_type_credit"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("debit")) {
					cRow.setDataValue("SOURCE_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_type_debit"));
				} else if (szSourceType != null && szSourceType.equalsIgnoreCase("sepa")) {
					cRow.setDataValue("SOURCE_TYPE",
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
					cRow.setDataValue("SOURCE_NAME", szText);
				}


				// i18n Payment Type
				String szPayType = cRow.getDataValue("PAY_TYPE");
				
				if (szPayType != null && szPayType.equalsIgnoreCase("onetime")) {
					cRow.setDataValue("PAY_TYPE",
					        I18n.translate(locator, userData, "paymentHistory_pay_type"));
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
						
						final String szAccount = cGroupingNode.get("internalAccountNumber")
						        .textValue();
						
						final String szPayGroup = cGroupingNode.get("paymentGroup").textValue();
						
						final String szMaskedDisplayAccount = new DisplayAccountMasked()
						        .displayAccountLookup(locator, szUserId, szAccount, szPayGroup);
						
						cRow.setDataValue("BILLING_ACCOUNT_ID", szMaskedDisplayAccount);
						bFound = true;
					}
				}
				
				cDisplayTable.saveRow(cRow);
				
			}
		} catch (Exception e) {
			LOG.error("setAccountDataTableFromPaymentData() An exception was thrown", e);
		}
	}
	
	/************************************************************************************************
	 * This method can be used to set account data table from payment history data.
	 * 
	 * @param cDataTable    Payment history data table.
	 * @param cDisplayTable Account data display table.
	 * @param szUserId      The user id.
	 * 
	 */
	public void setAccountDataTableFromPaymentHistoryData(
	        IServiceLocator2 locator,
	        IUserData userData,
	        ITable2Data cDataTable,
	        ITable2Data cDisplayTable,
	        String configColumns,
	        final String szUserId) {
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
						
						final String szAccount = cGroupingNode.get("internalAccountNumber")
						        .textValue();
						
						final String szPayGroup = cGroupingNode.get("paymentGroup").textValue();
						
						final String szMaskedDisplayAccount = new DisplayAccountMasked()
						        .displayAccountLookup(locator, szUserId, szAccount, szPayGroup);
						
						cRow.setDataValue("BILLING_ACCOUNT_ID", szMaskedDisplayAccount);
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
	
	/************************************************************************************************
	 * This method can be used to set automatic payment table from payment automatic
	 * data.
	 * 
	 * @param cDataTable    Payment automatic data table.
	 * @param cDisplayTable automatic payment table (Display table)
	 * 
	 */
	public void setAutomaticPaymentsTableFromPaymentAutomaticData(
	        IServiceLocator2 locator,
	        IUserData userData,
	        ITable2Data cDataTable,
	        ITable2Data cDisplayTable) {
		try {
			
			cDisplayTable.empty();
			
			for (int i = 0; i < cDataTable.getTotalRows(); i++) {
				ITable2RowData      cRow  = cDataTable.getRow(i);
				Map<Object, Object> mData = cRow.getDataValues();
				
				final String szUserId = mData.get("USER_ID").toString();
				
				ArrayNode cJsonArray = (ArrayNode) m_cObjectMapper
				        .readTree((String) mData.get("GROUPING_JSON"));
				
				// i18n Payment Name
				String szText = "";
				
				boolean bFound = false;
				
				for (JsonNode cGroupingNode : cJsonArray) {
					if (bFound) {
						szText = "";
						szText = I18n.translate(locator, userData,
						        "paymentAutomatic_sMultipleAccounts");
						cRow.setDataValue("GRP_DISP_ACC", szText);
						break; // Breaking because we only care about the first element in the array
					} else {
						
						final String szAccount = cGroupingNode.get("internalAccountNumber")
						        .textValue();
						
						final String szPayGroup = cGroupingNode.get("paymentGroup").textValue();
						
						final String szMaskedDisplayAccount = new DisplayAccountMasked()
						        .displayAccountLookup(locator, szUserId, szAccount, szPayGroup);
						
						cRow.setDataValue("GRP_DISP_ACC", szMaskedDisplayAccount);
						bFound = true;
					}
				}
				
				cDisplayTable.saveRow(cRow);
			}
			
		} catch (Exception e) {
			LOG.error("setAccountDataTableFromPaymentAutomaticData() An exception was thrown", e);
		}
	}
	
	/************************************************************************************************
	 * This method can be used to mask display account number present in grouping
	 * JSON and returns escape grouping JSON.
	 * 
	 * @param sGroupJson The grouping JSON
	 * @param szUserId   The user id.
	 * 
	 * @return escape grouping JSON in string format.
	 * 
	 */
	public static String escapeGroupingJson(
	        final IServiceLocator2 locator2,
	        final String sGroupJson,
	        final String szUserId) {
		
		final UcPaymentAction ucPaymentAction = new UcPaymentAction();
		
		String sGroupJsonString = null;
		
		try {
			GroupJson[] GroupJsonArray = m_cObjectMapper.readValue(sGroupJson, GroupJson[].class);
			
			Arrays.stream(GroupJsonArray)
			        .forEach(group -> group.setsDisplayAccountNumber(
			                new DisplayAccountMasked().displayAccountLookup(locator2, szUserId,
			                        group.getsInternalAccountNumber(), group.getPaymentGroup())));
			
			sGroupJsonString = m_cObjectMapper.writeValueAsString(GroupJsonArray);
			
		} catch (JsonProcessingException e) {
			LOG.debug("FffcAccountAction.....escapeGroupingJson()...exception thrown: " + e, e);
		}
		return ucPaymentAction.escapeGroupingJson(sGroupJsonString);
	}
	
	/************************************************************************************************
	 * Helper class to convert group JSON to java object
	 */
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
	
	/************************************************************************************************
	 * This method is used to get payment record and convert it in JSON format. It
	 * is also masking display account number.
	 * 
	 * @param httpReq  HTTP request.
	 * @param httpResp HTTP response.
	 * @param locator  Service locator.
	 * @param userData User data.
	 */
	public void getPaymentRecord(
	        HttpServletRequest httpReq,
	        HttpServletResponse httpResp,
	        IServiceLocator2 locator,
	        IUserData userData,
	        String szPayDate,
	        String szPayReqDate,
	        String szPayStatus,
	        String szPayFromAccount,
	        String szPayChannel,
	        BigDecimal bdPayAmt,
	        BigDecimal bdSurcharge,
	        BigDecimal bdPayTotalAmt,
	        String szFlex1,
	        String szFlex2,
	        String szFlex3,
	        String szFlex4,
	        String szFlex5,
	        String szFlex6,
	        String szFlex7,
	        String szFlex8,
	        String szFlex9,
	        String szFlex10,
	        String szFlex11,
	        String szFlex12,
	        String szFlex13,
	        String szFlex14,
	        String szFlex15,
	        String szFlex16,
	        String szFlex17,
	        String szFlex18,
	        String szFlex19,
	        String szFlex20,
	        ITable2Data cTable,
	        String szUserId) {
		
		try {
			HashMap<String, Object> cMap = new HashMap<String, Object>();
			cMap.put("PAY_DATE", szPayDate);
			cMap.put("PAY_REQ_DATE", szPayReqDate);
			cMap.put("PAY_STATUS", szPayStatus);
			cMap.put("PAY_FROM_ACCOUNT", szPayFromAccount);
			cMap.put("PAY_CHANNEL", szPayChannel);
			cMap.put("PAY_AMT", bdPayAmt);
			cMap.put("PAY_SURCHARGE", bdSurcharge);
			cMap.put("PAY_TOTAL_AMT", bdPayTotalAmt);
			cMap.put("FLEX1", szFlex1);
			cMap.put("FLEX2", szFlex2);
			cMap.put("FLEX3", szFlex3);
			cMap.put("FLEX4", szFlex4);
			cMap.put("FLEX5", szFlex5);
			cMap.put("FLEX6", szFlex6);
			cMap.put("FLEX7", szFlex7);
			cMap.put("FLEX8", szFlex8);
			cMap.put("FLEX9", szFlex9);
			cMap.put("FLEX10", szFlex10);
			cMap.put("FLEX11", szFlex11);
			cMap.put("FLEX12", szFlex12);
			cMap.put("FLEX13", szFlex13);
			cMap.put("FLEX14", szFlex14);
			cMap.put("FLEX15", szFlex15);
			cMap.put("FLEX16", szFlex16);
			cMap.put("FLEX17", szFlex17);
			cMap.put("FLEX18", szFlex18);
			cMap.put("FLEX19", szFlex19);
			cMap.put("FLEX20", szFlex20);
			
			List<HashMap<Object, Object>> lPayments = new ArrayList<HashMap<Object, Object>>();
			
			for (int i = 0; i < cTable.getTotalRows(); i++) {
				
				ITable2RowData cRow = cTable.getRow(i);
				
				final var szAccount = cRow.getDataValue("INTERNAL_ACCOUNT_NUMBER");
				
				final var szPayGroup = cRow.getDataValue("PAYMENT_GROUP");
				
				final var szMaskedDisplayAccount = new DisplayAccountMasked()
				        .displayAccountLookup(locator, szUserId, szAccount, szPayGroup);
				
				cRow.setDataValue("DISPLAY_ACCOUNT_NUMBER", szMaskedDisplayAccount);
				
				lPayments.add((HashMap<Object, Object>) cRow.getDataValues());
			}
			
			cMap.put("GROUPING", lPayments);
			
			RestResponseUtil.createJsonResponse(httpResp, Result.ok(cMap));
		} catch (Exception e) {
			RestResponseUtil.createJsonResponse(httpResp, Result.ise());
		}
	}
	
	public static String getGroupingJsonAsString(
	        IServiceLocator2 iServiceLocator2,
	        String szJson,
	        final String szUserId) {
		
		String szDocPayments = "";
		
		try {
			
			JsonNode cJsonNode = m_cObjectMapper.readTree(szJson);
			
			JsonNode cGroupingsNode = cJsonNode.get("grouping");
			
			for (JsonNode cElement : cGroupingsNode) {
				
				final String szAccount = cElement.get("internalAccountNumber").asText();
				
				final String szPayGroup = cJsonNode.get("paymentGroup").asText();
				
				final String szDisplayAccountNickname = new DisplayAccountMasked()
				        .displayAccountLookup(iServiceLocator2, szUserId, szAccount, szPayGroup);
				
				((ObjectNode) cElement).put("displayAccountNumber", szDisplayAccountNickname);
				
			}
			
			szDocPayments = m_cObjectMapper.writeValueAsString(cGroupingsNode);
		} catch (IOException e) {
			LOG.error("getGroupingJsonAsString() An exception was thrown", e);
		}
		
		return szDocPayments;
	}

	/**
	 * This method will convert a date format from MM/DD/YYYY to YYYY-MM-DD so it can be displayed in
	 * the calendar
	 * 
	 * @param szPayDate
	 * @return String
	 * @throws ParseException
	 */
	public static String getCalendarConversion(String szPayDate) throws ParseException {
	        SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyyy");
	        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");

	        // Parse the input date
	        Date date = format1.parse(szPayDate);

	        // Format the date in the desired output format
	        String outputDate = format2.format(date);

	        return outputDate;
	    }
	
}
