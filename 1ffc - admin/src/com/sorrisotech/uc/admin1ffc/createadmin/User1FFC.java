/* (c) Copyright 2015 Sorriso Technologies, Inc(r), All Rights Reserved,
 * Patents Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc.
 * 40 Nagog Park
 * Acton, MA 01720
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
package com.sorrisotech.uc.admin1ffc.createadmin;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.sorrisotech.app.common.utils.AuthUtil;
import com.sorrisotech.app.utils.Session;
import com.sorrisotech.persona.search.api.IUserSearchRepository;
import com.sorrisotech.persona.search.api.UserSearchFactory;
import com.sorrisotech.persona.search.api.entities.IConsumer;
import com.sorrisotech.persona.search.entities.Consumer;
import com.sorrisotech.persona.search.util.SearchUtils;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.aaa.CredentialNames;
import com.sorrisotech.svcs.itfc.aaa.IAAAClassFactory;
import com.sorrisotech.svcs.itfc.aaa.api.ICredential;
import com.sorrisotech.svcs.itfc.aaa.api.ICredentials;
import com.sorrisotech.svcs.itfc.aaa.credMgmt.ICredentialManager;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.CredentialNotFoundException;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaCredentialException;
import com.sorrisotech.svcs.itfc.i18n.IInternationalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User1FFC
	implements IExternalReuse
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**************************************************************************
     * Logger for this class.
     */
    private static final Logger m_cLog = LoggerFactory.getLogger(User1FFC.class);
	
	private ArrayList < Map <String, String> >   m_aRows = null;
	private String m_aIds[] = null;
	
	private Map<String, Integer>  m_cLevel;
	
	private String m_szUserAppSearchAttribute = null;
	
	/**
	 * Number of records to be displayed on screen
	 */
	public static final Integer m_cUserlistUILimit = 50;
	
	/**
	 * Number of records to be to be fetched from db
	 */
	public static final Integer m_cUserlistDBLimit = m_cUserlistUILimit + 10;
	
	public User1FFC ()
	{
		m_cLevel = new HashMap<String, Integer>();


		 /********************************************************************************************************
		 * There are 3 checks here :
		 *  1. User can always see all users which have the same role (this check covers all 3 roles
		 *  2. Second check is for System Adminstrator, look down till the last one
		 *  3. Third check is for Bus Unit Admin, look up since business user is one level next to business admin
		 */
		
		m_cLevel.put(Role1FFCConstants.ROLE_ADMIN_SYSTEM_ADMIN, 4);
		m_cLevel.put(Role1FFCConstants.ROLE_ADMIN_COMPANY_ADMIN, 3);
		m_cLevel.put(Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN, 2);
		m_cLevel.put(Role1FFCConstants.ROLE_ADMIN_AGENT, 1);

		m_cLevel.put(Role1FFCConstants.ROLE_ADMIN_DOCUMENT_CONTROLLER, 1);
	}

	public void addRole(final String szRole, String pos) {
		
		m_cLevel.put(szRole, Integer.valueOf(pos));		
	}
	
	
	@Override
	public int getReuse() 
	{
		return(IExternalReuse.REUSE_USECASE);
	}
	
	/**
	 * Initializes the userlist based on the application (admin or user)
	 * @param cLocator
	 * @param cData
	 * @param szUserRole
	 * @return
	 * @throws CredentialNotFoundException
	 * @throws MargaritaCredentialException
	 * @throws ClassNotFoundException 
	 */
	public String init(
		final IServiceLocator2 cLocator,
		final IUserData        cData,
		final String		   szUserRole
		) 
		throws CredentialNotFoundException, MargaritaCredentialException, ClassNotFoundException
	{
		
		if (Role1FFCConstants.NAMESPACE_ADMIN_APP.equals(AuthUtil.getAppNameSpace())) {
		
			return initCsrApp(cLocator, cData, szUserRole);
		
		} else {
			return initUserApp(cLocator, cData, szUserRole);
		}
	}

	/**
	 * Initializes the userlist for user app
	 * 
	 * @param cLocator
	 * @param cData
	 * @param szUserRole logged in user role
	 * @return
	 * @throws MargaritaCredentialException
	 * @throws CredentialNotFoundException
	 * @throws ClassNotFoundException 
	 */
	private String initUserApp(final IServiceLocator2 cLocator, final IUserData cData, final String szUserRole)
			throws MargaritaCredentialException, CredentialNotFoundException, ClassNotFoundException {
		
		String szReturn = "no_user";
		
		if (m_aRows == null) {
			
			szReturn = performUserAppSearch(cLocator, cData, szUserRole);
		}
		
		return szReturn;
	}

	/**
	 * Performs the actual search for user app
	 * @param cLocator
	 * @param cData
	 * @param szUserRole
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public String performUserAppSearch(final IServiceLocator2 cLocator, final IUserData cData, final String szUserRole
			 ) throws ClassNotFoundException {
		
		
		int iRow = 0;
		String szUserId;
		List<Consumer> cRows = null;
		List<String> cRoles = new ArrayList<>();
		final IInternationalization cI18n = cLocator.findService(IInternationalization.class);
		String szReturn = "no_user";
		String szStatusActive = cI18n.internationalize(cData, "adminListUsers_Status_Active", "{Active}");
		String szStatusInactive = cI18n.internationalize(cData, "adminListUsers_Status_Inactive", "{Inactive}");
		
		
		try {
			szUserId = cData.getJavaObj(Session.class).getUserId();
			
			// add the role criteria only
			cRoles = new ArrayList<>();
			cRoles.add(Role1FFCConstants.ROLE_BIZ_ADMIN);
			cRoles.add(Role1FFCConstants.ROLE_BIZ_END_USER);
			
			if (Role1FFCConstants.ROLE_BIZ_ORGANIZATION_ADMIN.equals(szUserRole)) {
				
				cRoles.add(Role1FFCConstants.ROLE_BIZ_ORGANIZATION_ADMIN);
			}
			
			// search for the users
			IUserSearchRepository cRepository = UserSearchFactory.getUserSearchRepository();
		    cRows = cRepository.searchCompanyUsersByUserId(new BigDecimal(szUserId), cRoles, m_szUserAppSearchAttribute, BigDecimal.valueOf(m_cUserlistDBLimit));
		    
		    
		    // decide response based on results count.
		    if (cRows.size() > m_cUserlistUILimit) {
		    	//strip the additional records
		    	cRows = cRows.subList(0, m_cUserlistUILimit);
		    	szReturn = "tooManyResults";
			
		    } else if (cRows.size() > 0) {
		    	szReturn = "user";
		    }
		    
		    
		    m_aRows = new ArrayList < Map <String, String > > (cRows.size());
			m_aIds  = new String[cRows.size()];
		    
		    // fill the results in a map
		    Map<String, String> cMap =  null;
		    for (Consumer cUser : cRows) {
				
		    	cMap   = new HashMap<String, String>();
		    	
		    	cMap.put("row", String.valueOf(iRow));
		    	cMap.put("fullname", cUser.getFullName());
				cMap.put("username", cUser.getUserName());
				cMap.put("namespace", Role1FFCConstants.NAMESPACE_USER_APP);
				cMap.put("email", cUser.getEmail() == null ? "N/A" : cUser.getEmail());
				cMap.put("businessunit", "N/A");
				cMap.put("description", "N/A");
				cMap.put("role", User1FFCUtil.getDisplayRole(cLocator, cData, cUser.getRole()));
				cMap.put("status",cUser.getActive() == null ? szStatusActive : (cUser.getActive().equals("false") ? szStatusInactive : szStatusActive));				
				
				m_aIds[iRow] = cUser.getId();
				m_aRows.add(cMap);
				 
				iRow += 1;
			}
		    
		    // reset the search criteria
		    m_szUserAppSearchAttribute = "";
		    
		} catch (ClassNotFoundException e) {
			m_cLog.error("init_user: unable to retrieve Session object.");
			throw e;
		}
		return szReturn;
	}
	
	/**
	 * Retrieves the user's company id
	 * @param cLocator
	 * @param cData
	 * @return
	 * @throws MargaritaCredentialException
	 * @throws CredentialNotFoundException
	 * @throws ClassNotFoundException
	 */
	public String getUserCompanyId(final IServiceLocator2 cLocator, final IUserData cData)
			throws MargaritaCredentialException, CredentialNotFoundException, ClassNotFoundException {

		List<IConsumer> cRows = null;
		String szCompanyId = null;
		String szUserId;

		try {
			szUserId = cData.getJavaObj(Session.class).getUserId();

			// search for the users
			IUserSearchRepository cRepository = UserSearchFactory.getUserSearchRepository();
			cRows = cRepository.searchUsersDetailsByUserId(new BigDecimal(szUserId));

			IConsumer cUser = cRows.get(0);
			szCompanyId = cUser.getCompanyId();

		} catch (ClassNotFoundException e) {
			m_cLog.error("init_user: unable to retrieve Session object.");
			throw e;
		}

		return szCompanyId;
	}
	
	
	/**
	 * Initializes the userlist for admin (csr) app
	 * 
	 * @param cLocator
	 * @param cData
	 * @param szUserRole logged in user role
	 * @return
	 * @throws MargaritaCredentialException
	 * @throws CredentialNotFoundException
	 */
	private String initCsrApp(final IServiceLocator2 cLocator, final IUserData cData, final String szUserRole)
			throws MargaritaCredentialException, CredentialNotFoundException {
		final int iUserLevel = m_cLevel.get(szUserRole);
		
		final IInternationalization cI18n = cLocator.findService(IInternationalization.class);
		String szStatusActive = cI18n.internationalize(cData, "adminListUsers_Status_Active", "{Active}");
		String szStatusInactive = cI18n.internationalize(cData, "adminListUsers_Status_Inactive", "{Inactive}");
		
		if (m_aRows == null)
		{
			final IAAAClassFactory   cFactory = cLocator.findService(IAAAClassFactory.class);
			final ICredentialManager cManager = cFactory.createCredentialManager();
			final String             aAttrs[] = { "emailAddress", "description", "active", "firstName", "lastName"};
			final ICredential cNameSpace = cManager.getByValue(CredentialNames.NAMESPACE, "saas.csr.namespace");
			final ArrayList<ICredential> cFilters = new ArrayList<ICredential> ();
			cFilters.add(cNameSpace);
			
			
			final List<ICredential> cUsers = cManager.searchCredentials(
				CredentialNames.USERNAME,
				cFilters, null, Arrays.asList(aAttrs), -1
				);
			 
			if (cUsers != null)
			{ 
	
			 m_aRows = new ArrayList < Map <String, String > > (cUsers.size());
			 m_aIds  = new String[cUsers.size()];
			 
			 int iRow = 0;
			 
			 for (final ICredential cUser : cUsers)
			 {
				 final ICredentials        cCreds = cUser.getEnclosingCredentials();
				 final Map<String, String> cMap   = new HashMap<String, String>();
				 final Object              cEmail = cUser.getAttribute("emailAddress");
				 final Object              cActive = cUser.getAttribute("active");
				 final Object			   cDescription = cUser.getAttribute("description");
				 final Object			   cFirstName = cUser.getAttribute("firstName");
				 final Object			   cLastName = cUser.getAttribute("lastName");
				 
				 String szRole = "";
				 try {
					 szRole = cCreds.getCredentialByType(CredentialNames.ROLE)[0].getValue().toString();
				 }
				 catch(Throwable eError) {
				 }
				 
				 int iLevel = 10;
				 
				 if (m_cLevel.containsKey(szRole)) iLevel = m_cLevel.get(szRole);
				 
				 szRole = User1FFCUtil.getDisplayRole(cLocator, cData, szRole);
				 
				 /********************************************************************************************************
				 * There are 3 checks here :
				 *  1. User can always see all users which have the same role (this check covers all 3 roles
				 *  2. Second check is for System Adminstrator, look down till the last one
				 *  3. Third check is for Bus Unit Admin, look up since business user is one level next to business admin
				 */
				 
				 if (  iLevel == iUserLevel || 
					 ((iLevel - 1) != 0) && (iLevel - 1 < iUserLevel) || 
						 ( ((iLevel-1) == 0) && (iLevel + 1 == iUserLevel) ))
				 {
					 cMap.put("row", String.valueOf(iRow));
					 cMap.put("fullname", SearchUtils.getFullName(ObjectUtils.getDisplayString(cFirstName), ObjectUtils.getDisplayString(cLastName)));
					 cMap.put("username", cUser.getValue().toString());
					 cMap.put("namespace", cCreds.getCredentialByType(CredentialNames.NAMESPACE)[0].getValue().toString());
					 cMap.put("email", cEmail == null ? "N/A" : cEmail.toString());
					 cMap.put("businessunit", "N/A");
					 cMap.put("description", cDescription == null ? "N/A" : cDescription.toString());
					 cMap.put("role", szRole);
					 cMap.put("status",cActive == null ? szStatusActive : (cActive.toString().equals("false") ? szStatusInactive : szStatusActive));
					 
					 m_aIds[iRow] = cUser.getId();
					 m_aRows.add(cMap);
					 
					 iRow += 1;
				 }
			 }		 
			}
		}
		return (m_aRows == null || m_aRows.size() == 0 ? "no_user" : "user");
	}
	
	@SuppressWarnings("rawtypes")
	public Map[] listUsers(
		final IServiceLocator2 cLocator,
		final IUserData        cData
		) 
		throws MargaritaCredentialException, CredentialNotFoundException
	{
	//	init(cLocator,cData);
		
		return(m_aRows.toArray(new Map[m_aRows.size()]));
	}
	
	public String getId(
		final String szRow
		)
	{
		final int iRow = Integer.parseInt(szRow);
		
		return(m_aIds[iRow]);
	}
	
	
	/**
	 * Sets the search criteria for user list in the user app
	 * @param m_szUserSearchAttribute
	 */
	public void setUserAppSearchAttribute(String m_szUserSearchAttribute) {
		this.m_szUserAppSearchAttribute = m_szUserSearchAttribute;
	}
	
	
	/**************************************************************************
	 * This method checks the if input data is valid or not.
	 * 
	 * @param szUsername The user name.
	 * @param szEmail    The user email.
	 * 
	 * @return "success" if input data are valid.
	 *         "error"   if the input data are not valid.
	 */
	public String verifyInputData (final String szUserAttribute) {
		
		m_cLog.debug("verifyInputData()...szUserAttribute: " +szUserAttribute);
		String szResult = "success";
		
		
		
		if(StringUtils.isEmpty(szUserAttribute) || (szUserAttribute.trim().isEmpty())){
			szResult = "error";
		}
		m_cLog.debug("verifyInputData()...Result: " +szResult);
		return szResult;
	}
	
}
