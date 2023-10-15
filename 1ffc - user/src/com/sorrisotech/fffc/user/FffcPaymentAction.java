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
package com.sorrisotech.fffc.user;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sorrisotech.app.billutils.BillCache;
import com.sorrisotech.app.common.utils.I18n;
import com.sorrisotech.app.utils.Freemarker;
import com.sorrisotech.app.utils.Session;
import com.sorrisotech.app.utils.freemarker.FormatUtils;
import com.sorrisotech.common.DateFormat;
import com.sorrisotech.common.LocalizedFormat;
import com.sorrisotech.common.app.Format;
import com.sorrisotech.common.app.RestResponseUtil;
import com.sorrisotech.common.rest.Result;
import com.sorrisotech.persona.bill.api.IBillInfo;
import com.sorrisotech.persona.bill.api.exception.NoBillDataFoundException;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IListData;
import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.data.ITable2Data;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.data.table2.ITable2ColumnData;
import com.sorrisotech.svcs.itfc.data.table2.ITable2RowData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;
import com.sorrisotech.svcs.payment.dao.PaymentAutomaticDao;
import com.sorrisotech.svcs.payment.dao.PaymentAutomaticGroupingDao;
import com.sorrisotech.svcs.payment.dao.PaymentHistoryDao;
import com.sorrisotech.svcs.payment.dao.PaymentScheduleDao;
import com.sorrisotech.svcs.payment.dao.PaymentWalletDao;
import com.sorrisotech.svcs.payment.model.PaymentAutomaticFields;
import com.sorrisotech.svcs.payment.model.PaymentAutomaticGroupingFields;
import com.sorrisotech.svcs.payment.model.PaymentScheduleFields;
import com.sorrisotech.svcs.payment.model.PaymentWalletFields;
import com.sorrisotech.svcs.payment.util.RequestTokenUtil;
import com.sorrisotech.svcs.payment.util.RequestTransactionIdUtil;
import com.sorrisotech.uc.payment.UcPaymentAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sorrisotech.utils.AppConfig;
import com.sorrisotech.utils.Config;
import com.sorrisotech.utils.ConfigConstants;
import com.sorrisotech.utils.DbConfig;
import com.sorrisotech.utils.Spring;

import freemarker.core.ParseException;
import freemarker.ext.dom.NodeModel;

/******************************************************************************
 * This class deals with the payment transactions.
 * 
 * @author Maybelle Johnsy Kanjirapallil.
 *
 */
public class FffcPaymentAction extends UcPaymentAction{	

	
	/**************************************************************************
	 * This method returns a 2D array of payment due values.
	 * 
	 * @param cData         The user data.
	 * @param cLocator      The service locator.
	 * @param szCurrent     The current balance.
	 * @param szStatement   The statement balance.
	 * @param szMinimum     The minimum due.
	 * 
	 * @return A 2D array of payment due.
	 */
	public String[][] getPayAmountDropdown(		
		IUserData cData,
		IServiceLocator2 cLocator,			
		String szCurrentDisplay,
		String szStatementDisplay,
		String szMinimumDisplay
		) {
				
		String szStatementText = szStatementDisplay + " - " + I18n.translate(cLocator, cData, "paymentOneTime_sStatementBalText");				
		String szOther         = I18n.translate(cLocator, cData, "paymentOneTime_sOther");
		
		return new String [][] {
			{ "statement", szStatementText },
			{ "other", szOther }				
		};
	}	
	

}