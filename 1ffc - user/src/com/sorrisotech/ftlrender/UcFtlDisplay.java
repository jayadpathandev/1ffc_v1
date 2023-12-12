package com.sorrisotech.ftlrender;

import com.sorrisotech.app.common.utils.I18n;
import com.sorrisotech.app.utils.Freemarker;
import com.sorrisotech.app.utils.freemarker.FormatUtils;
import com.sorrisotech.common.CurrencySymbol;
import com.sorrisotech.common.DateFormat;
import com.sorrisotech.common.NumberFormatter;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		}
		
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
		}
		
		boolean putDateDbAsLocalizedString (final String cszGroupName, final String cszItemName, final String cszObject) {
			
			Boolean rVal = false;
			try {
				
				Calendar lCalendar = DateFormat.parseDb(cszObject);
				String lszDate = m_DateFormat.text(lCalendar);
				put (cszGroupName, cszItemName, lszDate);
				rVal = true;
			} catch (ParseException e) {
				m_cLog.error("UcFtlDisplay:FtlTemplate:putDateNumericAsFormattedString -- invalid date format for " + 
						"template id {}, template name {}, type specification {}, input string {}.", 
						m_TemplateId.toString(), m_szTemplateName, "date", cszObject);
				m_cLog.error("UcFtlDisplay:FtlTemplate:putDateNumericAsFormattedString", e);
			}
			
			return rVal;
		}
		
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
				Integer ldTestConversion = Integer.decode(cszObject);
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
		}
		
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
		}
		
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
				put (cszGroupName, cszItemName, Boolean.TRUE);
				rVal = true;
			} else if (cszObject.equalsIgnoreCase("false") || cszObject.equalsIgnoreCase("n")) {
				put (cszGroupName, cszItemName, Boolean.FALSE);
				rVal = true;
			} else {
				m_cLog.error("UcFtlDisplay:FtlTemplate:putBooleanString -- invalid boolean or Y/N value for " + 
						"template id {}, template name {}, type specification {}, input string {}.", 
						m_TemplateId.toString(), m_szTemplateName, "boolean", cszObject);
			}
			
			return rVal;
		}
		
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
		}
		
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
	}
	
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
			case "dateDb":
				lbSuccess = lTemplate.putDateDbAsLocalizedString(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "boolean":
				lbSuccess = lTemplate.putBoolean(cszItemGroup, cszItemName, cszItemValue);
				break;
			case "object":
				lbSuccess = lTemplate.putBoolean(cszItemGroup, cszItemName, cszItemValue);
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
	}
	
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
	}

}
