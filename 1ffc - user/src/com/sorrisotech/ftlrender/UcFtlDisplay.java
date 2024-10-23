package com.sorrisotech.ftlrender;

import com.sorrisotech.app.billutils.BillCache;
import com.sorrisotech.app.common.utils.I18n;
import com.sorrisotech.app.utils.Freemarker;
import com.sorrisotech.app.utils.Session;
import com.sorrisotech.app.utils.freemarker.FormatUtils;
import com.sorrisotech.common.CurrencySymbol;
import com.sorrisotech.common.DateFormat;
import com.sorrisotech.common.NumberFormatter;
import com.sorrisotech.common.app.Format;
import com.sorrisotech.persona.bill.api.IBillInfo;
import com.sorrisotech.persona.bill.api.exception.BillDbException;
import com.sorrisotech.persona.bill.api.exception.NoBillDataFoundException;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

import freemarker.ext.dom.NodeModel;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * UcFtlDisplay -- provides a simple way of rendering an FTL template. Takes
 * 	a bunch of elements to be placed in the template and then renders the template
 *  into a tag (string).  KISS since this doesn't really call a database, external 
 *  service or manage a model on top of that... simple java calls from a use case
 *  is the best way to get it done with little overhead.
 *  
 *  This class is capable of managing multiple templates simultaneously... so
 *  the use case/session can be building the data for several templates and then render
 *  them all at the same time in a single xslt screen.
 *  
 *  Version 2023-Dec-08 jak	Initial version for use with simple bill screens only
 *  Version 2024-Jan-04 jak	Updated to use throughout the application for all templates.
 *  						Added code specific to bills and handling the loading of
 *  						Legacy root level information just like the old templates
 *  						manager in payment.
 *  
 * @author John Kowalonek
 * @since  2023-Dec-08
 * @version 2023-Dec-08
 * 
 */
public class UcFtlDisplay implements IExternalReuse {



	private static final long serialVersionUID = -183659323036448071L;

	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger m_cLog = LoggerFactory.getLogger(UcFtlDisplay.class);

	/**
	 * private class to hold all groups and items for a template that's being
	 * managed by this class --
	 */
	private class FtlTemplate {

		UUID m_TemplateId					= null;
		String m_szTemplateName 			= null;
		String m_szPayGroup 				= null;
		CurrencySymbol m_CurrencySymbol		= null;
		DateFormat m_DateFormat 			= null;
		NumberFormatter m_NumberFormatter 	= null;
		String m_szSorrisoLanguage 			= null;
		String m_szSorrisoCountry 			= null;
		IUserData m_UserData				= null;		// -- locale
		IServiceLocator2 m_ServiceLocator	= null;		// -- available services

		Map<String, GroupItems> m_Groups = new HashMap<String, GroupItems>();
		
		/**
		 * private class used by FtlTemplate to hold all items for a specific group inside
		 * a FtlTemplate instance.
		 */
		private class GroupItems {
				private String m_szGroupName = null;
				Map<String, Object> m_GroupMap = new HashMap<String, Object>();
			
			/**
			 * Constructor
			 * 
			 * @param cszGroupName -- name of group being managed
			 */
			GroupItems(final String cszGroupName) {
				m_szGroupName = cszGroupName;
			}
			
			/**
			 * put an item into the group
			 * 
			 * @param cszItemName -- name of item to be stored
			 * @param cszItemObject -- object to be indexed by name
			 */
			void put (final String cszItemName, final Object cszItemObject) {
				m_GroupMap.put(cszItemName, cszItemObject);
			}
			
			/**
			 * returns a map of name to object that higher level can add to
			 * the template
			 * 
			 * @return -- All the items associated with this group
			 */
			Map<String, Object> getGroupItems() {
				return m_GroupMap;
			}
			
			/**
			 * returns the name of the group associated with this GroupItems
			 * object
			 * 
			 * @return -- name of the group
			 */
			String getGroupName() {
				return m_szGroupName;
			}
		} // end of class GroupItems --
		
		/**
		 * constructor
		 * 
		 * @param cszTemplateId -- unique identifier for this template
		 * @param cszTemplateName -- name of the template associated with this FtlTemplate object
		 * @param cszPayGroup -- name of the payment group for this template
		 * @param cszSorrisoLanguage -- language code
		 * @param cszSorrisoCountry -- country code
		 * @param cCurrencySymbol -- currency symbol translation object
		 * @param cDateFormat -- date formatter for locale
		 * @param cNumberFormatter -- number formatter for locale
		 */
		FtlTemplate (UUID cTemplateId,
					 String cszTemplateName,
					 String cszPayGroup,
 					 IUserData cUserData,				// -- locale
					 IServiceLocator2 cServiceLocator,	// -- available services
					 String cszSorrisoLanguage,
					 String cszSorrisoCountry,
					 CurrencySymbol cCurrencySymbol,
					 DateFormat cDateFormat,
					 NumberFormatter cNumberFormatter) {
			m_TemplateId = cTemplateId;
			m_szTemplateName = cszTemplateName;
			m_szPayGroup = cszPayGroup;
			m_UserData = cUserData;
			m_ServiceLocator = cServiceLocator;
			m_szSorrisoLanguage = cszSorrisoLanguage;
			m_szSorrisoCountry = cszSorrisoCountry;
			m_CurrencySymbol = cCurrencySymbol;
			m_DateFormat = cDateFormat;
			m_NumberFormatter = cNumberFormatter;

		}
		
		/**
		 * Adds a name/value pair for a specific group in this FtlTemplate
		 * 
		 * @param cszGroupName -- group name
		 * @param cszItemName -- item name
		 * @param cszObject -- object to store
		 */
		private void put (final String cszGroupName, final String cszItemName, final Object cszObject) {
			
			GroupItems lGroupEntry = null;
			if (m_Groups.containsKey(cszGroupName)) {
				lGroupEntry = m_Groups.get(cszGroupName);
			} else {
				lGroupEntry = new GroupItems(cszGroupName);
				m_Groups.put(cszGroupName, lGroupEntry);
			}
			
			lGroupEntry.put(cszItemName, cszObject);
		} // -- end of raw object put
		
		/**
		 * Adds a string to the map associated with this template
		 * 
		 * @param cszGroupName -- group name for string
		 * @param cszItemName -- item name for string
		 * @param cszObject -- string to be stored
		 * @return -- true
		 */
		boolean putString (final String cszGroupName, final String cszItemName, final String cszObject) {
			put (cszGroupName, cszItemName, cszObject);
			return true;
		} // -- end of putString
		
		/**
		 * Puts a time variable (date plus time) into the template as a date object
		 * 
		 * @param cszGroupName
		 * @param cszItemName
		 * @param cszObject
		 * @return
		 */
		boolean putTimeDbFromString (final String cszGroupName, final String cszItemName, final String cszObject) {
			
			Boolean rVal = false;
			try {
				BigDecimal ldBackToNumber = new BigDecimal(cszObject);
				Date lDate = new Date(ldBackToNumber.longValue());
				put (cszGroupName, cszItemName, lDate);
				rVal = true;
			} catch (NumberFormatException e) {
				m_cLog.error("UcFtlDisplay:FtlTemplate:putTimeDbFromString -- invalid number format for " + 
						"template id {}, template name {}, type specification {}, input string {}.", 
						m_TemplateId.toString(), m_szTemplateName, "dateDb", cszObject);
				m_cLog.error("UcFtlDisplay:FtlTemplate:putTimeDbFromString", e);
			}
			
			return rVal;
		} // -- end of putTimeDbFromString

		/**
		 * puts the date formatted as YYYYMMDD into a date object and adds that into template
		 * 
		 * @param cszGroupName
		 * @param cszItemName
		 * @param cszObject
		 * @return
		 */
		boolean putDateDb (final String cszGroupName, final String cszItemName, final String cszObject) {
			
			Boolean rVal = false;
			SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyyMMdd");
			
			try {
				Date lDate = lDateFormat.parse(cszObject);
				put (cszGroupName, cszItemName, lDate);
				rVal = true;
			} catch (ParseException e) {
				m_cLog.debug("UcFtlDisplay:FtlTemplate:putDateDb -- invalid date format for " + 
						"template id {}, template name {}, type specification {}, input string {}.", 
						m_TemplateId.toString(), m_szTemplateName, "dateDb", cszObject);
				try {
					put (cszGroupName, cszItemName, lDateFormat.parse("19700101"));
					rVal = true;
				} catch (ParseException e1) {
				}
			}
			
			return rVal;
		} // -- end of putDateDb
		
		/**
		 * Takes a number as a string, validates that its a number, and then places it in the template.
		 * 
		 * @param cszGroupName
		 * @param cszItemName
		 * @param cszObject
		 * @return true if its a number, false otherwise.
		 */
		boolean putNumberString (final String cszGroupName, final String cszItemName, final String cszObject) {
			
			boolean bRet = false;
			// -- convert to number-- 
			try {
				
				BigDecimal ldTestConversion = new BigDecimal(cszObject);
				Object lItem = ldTestConversion;
				put (cszGroupName, cszItemName, lItem);
				bRet = true;
			} catch (NumberFormatException e) {
				m_cLog.error("UcFtlDisplay:FtlTemplate:putNumber -- invalid number format for " + 
						"template id {}, template name {}, type specification {}, input string {}.", 
						m_TemplateId.toString(), m_szTemplateName, "number", cszObject);
				m_cLog.error("UcFtlDisplay:FtlTemplate:putNumber", e);
			}
			return bRet;
		} // -- end of putNumberString
		
		/**
		 * Putting a currency string into the template is more than just a simple currency query. It involves
		 * locale, the currency associated with the payment group (which will need to be fixed someday) and the
		 * appropriate formatting. This takes a String, makes certain its a number we can use, then formats
		 * that number in the appropriate currency, and finally stores it in the template.
		 * 
		 * @param cszGroupName
		 * @param cszItemName
		 * @param cszObject
		 * @return true if succeeded, false if not.
		 */
		boolean putCurrencyString (final String cszGroupName, final String cszItemName, final String cszObject) {

			boolean rVal = false;
			String  lCurrencySymbol = null;
			boolean lSymBefore = true; 
			Integer lSignificantDigits = 0;
			String lFormattedString = null;
			try {
				// -- Is this a number? 
				BigDecimal lAmount = new BigDecimal(cszObject);
				
				// -- if we got here it is a number obtain info for the conversion --
				lCurrencySymbol = m_CurrencySymbol.forPayGroup(m_szPayGroup);
				lSymBefore = m_CurrencySymbol.before();
				lSignificantDigits = m_CurrencySymbol.significant();
				
				// -- now convert it to a currency string (doesn't deal with the case where a locale is different than
				//		the paygroup currency and with things like $ you need to specify exactly what KIND of $ it is AFTER the
				//		currency amount is displayed... so there's something before and after.. this will change someday - I hope). 
				if (lSymBefore) {
					lFormattedString = lCurrencySymbol.toString() + m_NumberFormatter.formatDecimal(lAmount, lSignificantDigits);
				} else {
					lFormattedString = m_NumberFormatter.formatDecimal(lAmount, lSignificantDigits) + lCurrencySymbol.toString();
				}
				
				// -- store it --
				put (cszGroupName, cszItemName, lFormattedString);
				rVal = true;
				
			} catch (NumberFormatException e) {
				m_cLog.error("UcFtlDisplay:FtlTemplate:putCurrency -- invalid number format for " + 
						"template id {}, template name {}, type specification {}, input string {}.", 
						m_TemplateId.toString(), m_szTemplateName, "currency", cszObject);
				m_cLog.error("UcFtlDisplay:FtlTemplate:putCurrency", e);
			}
			return rVal;		
		} // -- end of putCurrencyString
		
		/**
		 * Puts a boolean value into the freemarker tempalte given a string containing "true"/"false or "y"/"n".
		 * Conversion is case insensitive.
		 * 
		 * @param cszGroupName
		 * @param cszItemName
		 * @param cszObject
		 * @return true if a valid value, false if not.
		 */
		boolean putBoolean(final String cszGroupName, final String cszItemName, final String cszObject) {
			
			Boolean rVal = false;
			
			if (cszObject.equalsIgnoreCase("true") || cszObject.equalsIgnoreCase("y")) {
				put (cszGroupName, cszItemName, Boolean.TRUE.booleanValue());
				rVal = true;
			} else if (cszObject.equalsIgnoreCase("false") || cszObject.equalsIgnoreCase("n")) {
				put (cszGroupName, cszItemName, Boolean.FALSE.booleanValue());
				rVal = true;
			} else {
				m_cLog.error("UcFtlDisplay:FtlTemplate:putBooleanString -- invalid boolean or Y/N value for " + 
						"template id {}, template name {}, type specification {}, input string {}.", 
						m_TemplateId.toString(), m_szTemplateName, "boolean", cszObject);
			}
			
			return rVal;
		} // -- end of putBoolean
		
		/**
		 * Renders the template as a String which is converted to a Persona tag.
		 * 
		 * @return
		 */
		String renderTemplate() {
			
			Map<String, Object> cRoot = new HashMap<String, Object>();
			String szResult = "";
			String lRoot = "root";

			// -- important utility items for template rendering --
			cRoot.put("languageCode", m_szSorrisoLanguage);
			cRoot.put("countryCode", m_szSorrisoCountry);
			cRoot.put("i18n", new I18n());
			cRoot.put("serviceLocator", m_ServiceLocator);
			cRoot.put("userData", m_UserData);
			try {
				cRoot.put("formatUtils", new FormatUtils(m_ServiceLocator, m_UserData, m_szPayGroup));
			}
			catch (MargaritaDataException e) {
				m_cLog.error("ucFtlDisplay:FtlTemplate:renderTemplate -- can't create FormatUtils object.", e );
				return szResult;
			}
			
			// -- add all the data we have in this object, group by group --
			m_Groups.forEach((name, map) -> {
				if (name.equalsIgnoreCase(lRoot)) {
					// -- special case for group "root" --
					map.getGroupItems().forEach((itemname, itemvalue) -> {
						cRoot.put(itemname,  itemvalue);
					});
				} else {
					cRoot.put(map.getGroupName(), map.getGroupItems());
				}
				m_cLog.debug("getPaymentSummaryTemplate -- added group {} to template.", name);
			});

			try {
			szResult = Freemarker.resolveFreemarkerTemplate(m_ServiceLocator, m_UserData, m_szTemplateName, cRoot,
					m_szPayGroup, m_szSorrisoLanguage, m_szSorrisoCountry);
			} catch (Exception e) {
				m_cLog.error("ucFtlDisplay:FtlTemplate:renderTemplate -- failed to resolve FreeMarker Template.", e );
				return szResult;
				
			}
			
			return szResult;
		} // -- end of renderTemplate
		
		/** gets the template name for this FtlTemplate
		 * 
		 * @return -- name of template
		 */
		String getTemplateName() {
			return m_szTemplateName;
		}
	} // -- end of class FtlTemplate
	
	// -- map of templates we are currently managing organized by identifier --
	Map <UUID, FtlTemplate> m_Templates = new HashMap<UUID, FtlTemplate>();
	Boolean m_bInitialized 				= false;

	
	@Override
	public int getReuse() {
		return IExternalReuse.REUSE_SESSION_SINGLETON;
	}
	
	/**
	 * constructor
	 */
	public UcFtlDisplay() {
		m_bInitialized = true;
	}
	
	/**
	 * Sets the template name based on language and country and clears any data
	 * elements associated with that template name.
	 * 
	 * @param cUserData -- locale data
	 * @param cServiceLocator -- available services
	 * @param cszTemplateBaseName -- base name of template (localized by Persona)
	 * @return -- Template identifier string if success, "false" if failed to create
	 * 
	 */
	public String initializeTemplate(
			IUserData cUserData,				// -- locale
			IServiceLocator2 cServiceLocator,	// -- available services
			String cszSorrisoLanguage,			// -- language code
			String cszSorrisoCountry,			// -- country code
			String cszTemplateBaseName,			// -- base name of template (localized by Persona)
			String cszPayGroup)	{				// -- Payment group used to select templates
	
		String rVal = "false";
		
		if (!m_bInitialized) return rVal;
		
		// -- need names for these arguments --
		if (((null == cszTemplateBaseName) || (null == cszPayGroup) || (null == cUserData) || (null == cServiceLocator) ||
				(null == cszSorrisoLanguage) || (null == cszSorrisoCountry)) ||
			((0 == cszTemplateBaseName.length()) || (0 == cszPayGroup.length()) ||
				(0 == cszSorrisoLanguage.length()) || (0 == cszSorrisoCountry.length()))) {
			m_cLog.error("UcFtlDisplay:InitializeTemplate -- one or more arguments are null or empty");
			return rVal;
		}
	
		try {
			// -- need formatting data for the template --
			CurrencySymbol lCurrency = new CurrencySymbol(cServiceLocator, cUserData.getLocale());
			DateFormat lDateFormat = new DateFormat(cServiceLocator, cUserData.getLocale());
			NumberFormatter lNumberFormatter = new NumberFormatter(cUserData.getLocale());
			if ((null == lCurrency) || (null == lDateFormat) || (null == lNumberFormatter)) {
				m_cLog.error("UcFtlDisplay:InitializeTemplate -- failed to create one or more local based formatter objects.");
				return rVal;
			}

			// -- create the unique template id --
			UUID lTemplateId = UUID.randomUUID();

			// -- create a new template map --
			FtlTemplate lTemplate = new FtlTemplate(lTemplateId,
													cszTemplateBaseName, 
													cszPayGroup,
													cUserData,				// -- locale
													cServiceLocator,	// -- available services
													cszSorrisoLanguage,
													cszSorrisoCountry,
													lCurrency,
													lDateFormat,
													lNumberFormatter);
			// -- add to the map in this class --
			m_Templates.put(lTemplateId, lTemplate);
			rVal = lTemplateId.toString();
		
		} catch (MargaritaDataException e) {
			m_cLog.error("UcFtlDisplay:InitializeTemplate -- Exception  creating one or more local based formatter objects.", e);
			return rVal;
		}
		
		return rVal;
	} // -- end of initializeTemplate
	
	/**
	 * Adds an item variable to the ftl template items will be accessible
	 * as <b>cszItemGroup.cszItemName</b>  if <b>cszItemGroup</b> is <i>"root"</i> they 
	 * are available as just <b>cszItemName</b>. type "string" and "object" don't get interpreted.
	 * 
	 * @param cszItemGroup -- group name for the item 
	 * @param cszTemplateId -- template id returned from InitializeTemplate
	 * @param cszItemName -- name of the item
	 * @param cszItemType -- type of value ("string", "number", "currency", "date", "boolean", "object")
	 * @param cszItemValue -- value to be stored (will be converted from string to specified type)
	 * @return -- "true" if success, "false" if failed
	 * 
	 */
	public String setItemValue (
			String cszTemplateId,				// -- template identifier returned from InitializeTemplate
			String cszItemGroup,				// -- Name of FTL group
			String cszItemName,					// -- Name of FTL value
			String cszItemType,					// -- Type of value ("string", "number", "currency", "date", "date_time", "boolean")
			String cszItemValue ) {				// -- Value passed in as a string
		
		Boolean rVal = false;
		if (!m_bInitialized) return rVal.toString();
		
		// -- validate arguments --
		if (((null == cszTemplateId) || (null == cszItemGroup) || (null == cszItemName) || (null == cszItemType) || (null == cszItemValue)) ||
			((0 == cszTemplateId.length()) || (0 == cszItemGroup.length()) || (0 == cszItemName.length()) || (0 == cszItemType.length()) ||
			 (0 == cszItemValue.length()))) {
			m_cLog.error("UcFtlDisplay:SetItemValue -- null or 0 length argument for template id {}.", 
					cszTemplateId);
			return rVal.toString();
		}
		
		// -- do we have a template for this? --
		FtlTemplate lTemplate = null;
		UUID lTemplateId = UUID.fromString(cszTemplateId);
		if (m_Templates.containsKey(lTemplateId)) {
			lTemplate = m_Templates.get(lTemplateId);
		} else {
			m_cLog.error("UcFtlDisplay:SetItemValue -- invalid template id {}.", cszTemplateId);
			return rVal.toString();
		}
			
		// -- convert data if needed and put in the template --

		Boolean lbSuccess = false;
		if (null != lTemplate) {
			switch (cszItemType) {
			case "string":
				lbSuccess = lTemplate.putString(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "number":
				lbSuccess = lTemplate.putNumberString(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "currency":
				lbSuccess = lTemplate.putCurrencyString(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "timeDb":
				lbSuccess = lTemplate.putTimeDbFromString(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "dateDb":
				lbSuccess = lTemplate.putDateDb(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "boolean":
				lbSuccess = lTemplate.putBoolean(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "object":
				break;
			default:
				m_cLog.error("UcFtlDisplay:SetItemValue -- invalid data type specification for " + 
								"template id {}, template name {}, type specification {}.", 
								cszTemplateId, lTemplate.getTemplateName(), cszItemType);
			}
			
			if (lbSuccess) {
				rVal = true;
				m_cLog.debug("UcFtlDisplay:SetItemValue -- success storing data for " + 
						"template id {}, template name {}, type specification {}.", 
						cszTemplateId, lTemplate.getTemplateName(), cszItemType);;
			} else {
				m_cLog.error("UcFtlDisplay:SetItemValue -- failed storing data for " + 
						"template id {}, template name {}, type specification {}.", 
						cszTemplateId, lTemplate.getTemplateName(), cszItemType);;
			}
		} else {
			m_cLog.error("UcFtlDisplay:SetItemValue -- template for template id {} is null.", cszTemplateId);
		}

		return rVal.toString();
	} // -- end of setItemValue
	
	/**
	 * Renders the current template and clears all template values after rendering
	 * 
	 * @param cszTemplateId -- template identifier returned from InitializeTemplate
	 * @return -- the fully rendered template ready to be placed in a tag
	 * 
	 */
	public String renderTemplate (
			String cszTemplateId) {					// -- template identifier returned from InitializeTemplate
		
		String rVal = "";
		
		// -- do we have a template for this? --
		FtlTemplate lTemplate = null;
		UUID lTemplateId = UUID.fromString(cszTemplateId);
		if (m_Templates.containsKey(lTemplateId)) {
			lTemplate = m_Templates.get(lTemplateId);
		} else {
			m_cLog.error("UcFtlDisplay:RenderTemplate -- invalid template id {}.", cszTemplateId);
			return rVal.toString();
		}
		
		// -- instantiate this template in FreeMarker --
		rVal = lTemplate.renderTemplate();
		
		if (!rVal.equalsIgnoreCase("")) {
			m_cLog.debug("UcFtlDisplay:RenderTemplate -- Template render succeeded for {}.", cszTemplateId);
			m_Templates.remove(lTemplateId); // once rendered... get rid of it.
		} else {
			m_cLog.error("UcFtlDisplay:RenderTemplate -- Template render failed for {}.", cszTemplateId);
		}
		return rVal;
	} // -- end of renderTemplate
	
	/**
	 * Adds all the bill and document information to the template for those templates that require it.
	 * Note that it adds a bunch of legacy information that might be duplicated in bill and status.
	 * 
	 * @param cszTemplateId						- Template Id from Initialize call
	 * @param cszPaymentGroup					- payment group for this bill 
	 * @param cszAccountNumber					- account number for this bill
	 * @param cszBillDate						- bill data
	 * @param cszCurrentAmountDue				- current amount due (based on payments since statement)
	 * @param cszPreviousBillAmountRemaining	- previous amount from previous bill
	 * @param cszDocZeroOneManyInfo				- tells you if there are 0, 1, or many documents
	 * @param cszHasAutomaticPaymentRule		- is there an automatic payment set up for this account
	 * @return
	 */
	public String addDocumentInfo(  final String cszTemplateId,
									final String cszPaymentGroup,
									final String cszAccountNumber,
									final String cszBillDate,
									final String cszCurrentAmountDue,
									final String cszPreviousBillAmountRemaining,
									final String cszDocZeroOneManyInfo,
									final String cszHasAutomaticPaymentRule) {

		Boolean rVal = false;
		if (!m_bInitialized) return rVal.toString();
		
		// -- validate arguments --
		if ((null == cszTemplateId) || (null == cszPaymentGroup) || (null == cszAccountNumber) || (null == cszBillDate)) {
			m_cLog.error("UcFtlDisplay:addBillInfo -- null or 0 length argument for template id {}.", 
					cszTemplateId);
			return rVal.toString();
		}
		
		// -- do we have a template for this? --
		FtlTemplate lTemplate = null;
		UUID lTemplateId = UUID.fromString(cszTemplateId);
		if (m_Templates.containsKey(lTemplateId)) {
			lTemplate = m_Templates.get(lTemplateId);
		} else {
			m_cLog.error("UcFtlDisplay:addBillInfo -- invalid template id {}.", cszTemplateId);
			return rVal.toString();
		}
		
		// -- retrieve data to assign BillInfotoOurList --
		String userId = null;
		String szBillDateAsString = null;
		BillCache cache = null;
		
		try {
			final IUserData cUserData = lTemplate.m_UserData;
			cache = cUserData.getJavaObj(BillCache.class);
			userId = cUserData.getJavaObj(Session.class).getUserId();
			final Date rawDate = DateFormat.parseDb(cszBillDate).getTime();
			szBillDateAsString = Format.formatDateForDb(rawDate);
		}
		catch (ClassNotFoundException e) {
			m_cLog.error("UcFtlDisplay:addBillInfo -- ClassNotFoundException (BillCache or UserId).", e);
			return rVal.toString();
		}
		catch (ParseException e) {
			m_cLog.error("UcFtlDisplay:addBillInfo -- ParseException parsing billDate passed in. {}", cszBillDate, e);
			return rVal.toString();
		}

		// -- Assign the information --
		assignBillInfotoGroupListInternal (lTemplate, userId, cszAccountNumber, szBillDateAsString, cszPaymentGroup, cache);
		assignLegacyDataInternal(lTemplate, userId, cszAccountNumber, szBillDateAsString, cszPaymentGroup, cache, cszCurrentAmountDue,
				cszPreviousBillAmountRemaining, cszDocZeroOneManyInfo, cszHasAutomaticPaymentRule );
		rVal = true;
		return rVal.toString();
	} // end of addDocumentInfo
	
	/**
	 * Internal call to add bill information to the a template. 
	 * 
	 * @implNote This code is mostly taken from the template code in UcPaymentAction
	 * 
	 * @param lTemplate				-- template object for this rendering
	 * @param userId				-- user id
	 * @param szAccountNumber		-- internal account number
	 * @param billDate				-- bill date for this bill
	 * @param cszPaymentGroup		-- payment group for this account
	 * @param cache					-- bill cache object for this user
	 */
	private void assignBillInfotoGroupListInternal(FtlTemplate lTemplate, final String userId, final String szAccountNumber, final String billDate,
			final String cszPaymentGroup, final BillCache cache)  {

		String cszGroupName = "bill";

		// --------------------------------------------------------
		// Grab bill information from the bill cache
		//
		IBillInfo info = null;
		try {
			info = cache.getBillInfoFromCache(userId, szAccountNumber, billDate, cszPaymentGroup);
		} catch (NoBillDataFoundException | BillDbException | java.text.ParseException e) {
			// -- not certain we should have this here... customers without bills are ok, we
			// just don't know that from the top --
			m_cLog.error("UcFtlDisplay:assignBillInfotoGroupList(): no bill information");
			m_cLog.debug("UcFtlDisplay:assignBillInfotoGroupList(): no bill information", e);
		}
		
		// ---------------------------------------------------------
		//	add this information to the template, all raw
		//
		if (null != info) {
			lTemplate.put(cszGroupName, "id", info.getId());
			lTemplate.put(cszGroupName,"invoiceNo", info.getInvoiceNo());
			lTemplate.put(cszGroupName,"internalAccountNo", info.getInternalAccountNo());
			lTemplate.put(cszGroupName,"externalAccountNo", info.getExternalAccountNo());
			lTemplate.put(cszGroupName,"date", info.getBillDate());
			lTemplate.put(cszGroupName,"dateNum", info.getBillDateNum());
			lTemplate.put(cszGroupName,"amount", info.getBillAmount());
			lTemplate.put(cszGroupName,"dueDate", info.getBillDueDate());
			lTemplate.put(cszGroupName,"isFinalBill", info.isFinalBill());
			lTemplate.put(cszGroupName,"isAccountClosed", info.isAccountClosed());
			lTemplate.put(cszGroupName,"isPaymentDisabled", info.isPaymentDisabled());
			lTemplate.put(cszGroupName,"amountDue", info.getAmountDue());
			lTemplate.put(cszGroupName,"minDue", info.getMinimumDue());
			lTemplate.put(cszGroupName,"isBillOverdue", info.isBillOverdue());
			lTemplate.put(cszGroupName,"payGroup", info.getPaymentGroup());
			lTemplate.put(cszGroupName,"stream", info.getBillStream());
			lTemplate.put(cszGroupName,"location", info.getLocation());
			lTemplate.put(cszGroupName,"address1", info.getAddress1());
			lTemplate.put(cszGroupName,"address2", info.getAddress2());
			lTemplate.put(cszGroupName,"address3", info.getAddress3());
			lTemplate.put(cszGroupName,"address4", info.getAddress4());
			lTemplate.put(cszGroupName,"address5", info.getAddress5());
			lTemplate.put(cszGroupName,"extDocId", info.getExternalDocId());
			lTemplate.put(cszGroupName,"version", info.getVersion());

			final String[] flex = info.getFlex();
			if (flex != null) {
				for (int i = 1; i <= flex.length; i++) {
					lTemplate.put(cszGroupName, "flex" + i, flex[i - 1]);
				}
			}

			final String[] selfReg = info.getSelfReg();
			if (selfReg != null) {
				for (int i = 1; i <= selfReg.length; i++) {
					lTemplate.put(cszGroupName, "selfReg" + i, selfReg[i - 1]);
				}
			}
			lTemplate.put(cszGroupName, "bill.billAvailable", true);
			m_cLog.debug("UcFtlDisplay:assignBillInfotoGroupList -- bill info available.");

		} else {
			lTemplate.put(cszGroupName, "bill.billAvailable", false);
			m_cLog.debug("UcFtlDisplay:assignBillInfotoGroupList -- bill info NOT available.");
		}
	} // -- end of assignBillInfotoGroupListInternal

	/**
	 * Assigns the root data that the old template engine used. Makes it compatible with old freemarker templates for
	 * customers upgrading... we need to stop duplicating this data and categorize it like bill and status and then
	 * use those buckets rather than root. Limit root to executable stuff. and Localization definitions.
	 * 
	 * @implNote This code is taken largely from the template code in UcPaymentAction.
	 * 
	 * @param lTemplate							-- template id for this action
	 * @param userId							-- user id
	 * @param cszAccountNumber					-- internal acocunt number
	 * @param billDate							-- date for this bill
	 * @param cszPaymentGroup					-- payment group for this account
	 * @param cache								-- bill cache for this user
	 * @param cszCurrentAmountDue				-- current amount due (calculated in use case based on bill + payments since)
	 * @param cszPreviousBillAmountRemaining	-- amount remaining
	 * @param cszDocZeroOneManyInfo				-- 0, 1, or many docs
	 * @param cszHasAutomaticPaymentRule		-- is an automatic payment rule setup
	 */
	@SuppressWarnings("deprecation")
	private void assignLegacyDataInternal (FtlTemplate lTemplate, final String userId, final String cszAccountNumber, final String billDate, 
			final String cszPaymentGroup, final BillCache cache, final String cszCurrentAmountDue,
			final String cszPreviousBillAmountRemaining, final String cszDocZeroOneManyInfo, final String cszHasAutomaticPaymentRule ) {
		try {
			IUserData cUserData = lTemplate.m_UserData;
			String lszBillStorageLocation = "";
			final String lcszRoot = "root";
			IBillInfo lBillInfo = null;
			//----------------------------------------------------
			// Grab bill storage location from bill cache
			//
			try {
				lBillInfo = cache.getBillInfoFromCache(userId, cszAccountNumber, billDate, cszPaymentGroup);
				lszBillStorageLocation = lBillInfo.getLocation();
			} catch (NoBillDataFoundException e) {
				m_cLog.error("UcFtlDisplay:assignLegacyDataInternal: no bill infomation getBillInfoFromCache userId {}, account {}, billDate {}, payment group {} ",
						userId, cszAccountNumber, billDate, cszPaymentGroup);
				m_cLog.debug("UcFtlDisplay:assignLegacyDataInternal: no bill information", e);
			} catch (BillDbException e) {
				m_cLog.error("UcFtlDisplay:assignLegacyDataInternal: bill db exception getBillInfoFromCache userId {}, account {}, billDate {}, payment group {} ",
						userId, cszAccountNumber, billDate, cszPaymentGroup);
				m_cLog.debug("UcFtlDisplay:assignLegacyDataInternal: bill db exception", e);
			} catch (ParseException e) {
				m_cLog.error("UcFtlDisplay:assignLegacyDataInternal: ParseException getBillInfoFromCache userId {}, account {}, billDate {}, payment group {} ",
						userId, cszAccountNumber, billDate, cszPaymentGroup);
				m_cLog.debug("UcFtlDisplay:assignLegacyDataInternal: no bill information", e);
			}
			
			// -- make sure the user can view the bill and add that data --
			if (cUserData.getActors().contains("view_bill") && lszBillStorageLocation .equals("U")) {
		
				byte[] aData = null;
				try {
					aData = cache.getBillSummaryFromCache(userId, cszAccountNumber, billDate, cszPaymentGroup,
							lszBillStorageLocation);
				} catch (NoBillDataFoundException e) {
					m_cLog.error("UcFtlDisplay:assignLegacyDataInternal: no bill infomation getBillSummaryFromCache userId {}, account {}, billDate {}, payment group {} ",
							userId, cszAccountNumber, billDate, cszPaymentGroup);
					m_cLog.debug("UcFtlDisplay:assignLegacyDataInternal: no bill information", e);
				} catch (BillDbException e) {
					m_cLog.error("UcFtlDisplay:assignLegacyDataInternal: bill db exception getBillSummaryFromCache userId {}, account {}, billDate {}, payment group {} ",
							userId, cszAccountNumber, billDate, cszPaymentGroup);
					m_cLog.debug("UcFtlDisplay:assignLegacyDataInternal: bill db exception", e);
				} catch (ParseException e) {
					m_cLog.error("UcFtlDisplay:assignLegacyDataInternal: ParseException getBillSummaryFromCache userId {}, account {}, billDate {}, payment group {} ",
							userId, cszAccountNumber, billDate, cszPaymentGroup);
					m_cLog.debug("UcFtlDisplay:assignLegacyDataInternal: no bill information", e);
				}
		
				if (aData != null) {
					
					InputSource cInputSource = new InputSource();
					ByteArrayInputStream cInputStream = new ByteArrayInputStream(aData);
					cInputSource.setEncoding("UTF-8");
					cInputSource.setByteStream(cInputStream);
					lTemplate.put(lcszRoot, "summaryDoc", NodeModel.parse(cInputSource));
					lTemplate.put(lcszRoot, "hasByteData", "true");
				} else {
					lTemplate.put(lcszRoot, "hasByteData", "false");
				}
			} else {
				lTemplate.put(lcszRoot, "hasByteData", "false");
			}
	
			final Session cSession = cUserData.getJavaObj(Session.class);
			String szPaymentOnlyFlag = "false";
			if (cSession.arePaymentsEnabled().equalsIgnoreCase("true")
					&& cSession.areBillsEnabled().equalsIgnoreCase("false")
					&& cSession.areDocsEnabled().equalsIgnoreCase("false")) {
				szPaymentOnlyFlag = "true";
			}
		
			String szPaymentEnabled = cSession.arePaymentsEnabled();
		
			lTemplate.put(lcszRoot, "jumpToOffset", cSession.getAccountOffset(cszAccountNumber, cszPaymentGroup));
			lTemplate.put(lcszRoot, "displayAccount", lBillInfo.getExternalAccountNo());
			lTemplate.put(lcszRoot, "amount", new BigDecimal(cszCurrentAmountDue)); // -- this is current amount due, not statement amount due --
			if (lBillInfo.getBillDueDate() != null) {
				lTemplate.put(lcszRoot, "dueDate", lTemplate.m_DateFormat.numeric(lBillInfo.getBillDueDate()));
			} else {
				lTemplate.put(lcszRoot, "dueDate", "");
			}
					
			lTemplate.put(lcszRoot, "newAmount", lBillInfo.getAmountDue());
			lTemplate.put(lcszRoot, "previousAmount", cszPreviousBillAmountRemaining); // -- needs to be passed down as it is calculated --
			lTemplate.put(lcszRoot, "minAmountDue", lBillInfo.getMinimumDue());
			if (lBillInfo.getBillDate() != null) {
				lTemplate.put(lcszRoot, "statementDate", lTemplate.m_DateFormat.numeric(lBillInfo.getBillDate()));
			} else {
				lTemplate.put(lcszRoot, "statementDate", "");
			}
			lTemplate.put(lcszRoot, "docNumber", lBillInfo.getInvoiceNo());
			lTemplate.put(lcszRoot, "location", lBillInfo.getLocation());
			lTemplate.put(lcszRoot, "paymentEnabled", szPaymentEnabled);
			lTemplate.put(lcszRoot, "paymentOnlyFlag", szPaymentOnlyFlag);
			lTemplate.put(lcszRoot, "latestDocResult", cszDocZeroOneManyInfo);
			lTemplate.put(lcszRoot, "autopaymentEnabled", cszHasAutomaticPaymentRule);
			lTemplate.put(lcszRoot, "formatUtils", new FormatUtils(lTemplate.m_ServiceLocator, cUserData, cszPaymentGroup));
		
			m_cLog.debug("UcFtlDisplay:assignLegacyDataInternal -- Finished adding all info to template.");
		
		} catch (Exception e) {
			m_cLog.error("UcFtlDisplay:assignLegacyDataInternal -- error see trace", e);
		}
	} // -- end of assignLegacyDataInternal



}
