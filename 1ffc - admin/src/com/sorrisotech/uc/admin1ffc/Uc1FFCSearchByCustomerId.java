package com.sorrisotech.uc.admin1ffc;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.sorrisotech.app.utils.Session;
import com.sorrisotech.common.app.NotifUtil;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.uc.admin1ffc.dao.CustomerElement1FFC;
import com.sorrisotech.uc.admin1ffc.dao.SearchByCustomerIdDaoImpl;

/**
 * @author john kowalonek
 * @since 2024-jan-11
 * @version 2024-Jan-11
 * @implNote 2024-Jan-11 jak -- first version.
 * 
 * class Uc1FFCSearchByCustomerId is an extension of the core product agent search class:
 * 		UcConsumerSearchAction.  This class returns the same structure of search results
 * 		as the base class, but based on a search using the 1ffc specific customer id.
 * 		It defines an alternate search method using the customer id. 
 *      
 *      For simplicity and to avoid a potential rabbit hole of code and queries that have not
 *      been used before, this class doesn't use the search repository function
 * 		
 */
public class Uc1FFCSearchByCustomerId extends com.sorrisotech.uc.consumer.search.UcConsumerSearchAction implements IExternalReuse {

	/**************************************************************************
     * The UID for this class.
     */
	private static final long serialVersionUID = 2350237605904724342L;

	/**************************************************************************
     * Logger for development issues.
     */
    private static final Logger LOG =  LoggerFactory.getLogger(Uc1FFCSearchByCustomerId.class);

	@Override
	public int getReuse() {
		return IExternalReuse.REUSE_USECASE;
	}

	/**
	 * Check for null, empty, or blank string
	 * 
	 * @param cszCustomerId
	 * @return
	 */
	public String verifyInputData( final String cszCustomerId ) {
		String lszReturn = "error";
		
		if ((null != cszCustomerId) && (!cszCustomerId.isEmpty()) && (!cszCustomerId.isBlank())) {
			lszReturn = "success";
		}
		return lszReturn;
	}
	
	/**
	 * Performs a wild card or exact match search based on the value of the 
     * customerId supplied.
     * 
	 * @param cData
	 * @param cszCustomerId
	 * @return "success"        if the search found (appropriate number of) results.
     *         "noResults"      if there is no match.
     *         "tooManyResults" if there are more than 99 rows that match the search criteria.
     *         "error"          if any internal error occurred.
	 */
	public String performSearch ( final IUserData cData, final String cszCustomerId ) {
	
    	LOG.debug("Uc1FFCSearchByCustomerId:performSearch() -- Search on customer id: " + cszCustomerId);
   
    	final int ciQueryLimit = 99; // -- actual query limits to 100
    	String szResult = "success";
                
        try {
            final Session cSession = cData.getJavaObj(Session.class);            
            List<CustomerElement1FFC> cRows  = null;
            
            // -- get results of this search --
        	cRows = getSearchResults(cszCustomerId);        	
            if (cRows.isEmpty()) {
            	szResult = "noResults"; // -- no results --
            	return szResult;
            }
            else if (cRows.size() > ciQueryLimit) {
            	szResult = "tooManyResults"; // -- need to restrict search more --
            	return szResult;
            }
        	
            int iRow = 0;
			 
            ArrayList<Map<String, String>> base_Rows = new ArrayList<Map<String, String>>();
			for (final CustomerElement1FFC cElement : cRows) {			 
				 
				 final Map<String, String> cMap   = new HashMap<String, String>();
				 
				 String szUserId = cElement.getId();
				 cMap.put("id", String.valueOf(iRow));					 
				 cMap.put("userid", szUserId);
				 cMap.put("username", cElement.getUserName());
				 cMap.put("name", cElement.getFullName());
				 cMap.put("email", cElement.getEmail());
				 cMap.put("emailMasked", NotifUtil.getMaskedEmail(cElement.getEmail()));	
				 cMap.put("billingUrl", injectImpersonationUrl(cSession, szUserId, "billing"));
				 cMap.put("correspondenceUrl", injectImpersonationUrl(cSession, szUserId, "correspondence"));
				 cMap.put("notificationsUrl", injectImpersonationUrl(cSession,szUserId, "notifications"));
				 cMap.put("paymentUrl", injectImpersonationUrl(cSession, szUserId, "payment"));
				 cMap.put("profileUrl", injectImpersonationUrl(cSession, szUserId, "profile"));
				 cMap.put("impersonateUrl", injectImpersonationUrl(cSession, szUserId, "loginGotoDashboard"));
				 cMap.put("billClass", cSession.areBillsEnabled() == "true" ? "" : "visually-hidden");
				 cMap.put("docClass", cSession.areDocsEnabled() == "true" ? "" : "visually-hidden");
				 cMap.put("paymentClass", cSession.arePaymentsEnabled() == "true" ? "" : "visually-hidden");

				 // -- this is unique to 1ffc --
				 cMap.put("customerId", cElement.getCustomerId());
				 
				 base_Rows.add(cMap);				 
				 iRow = iRow + 1;
			 }
			 setRows(base_Rows);
        }catch(Exception e) {
        	szResult = "error";
	    	LOG.error("Uc1FFCSearchByCustomerId:performSearch()...An exception was thrown", e);          
	    }
        
        LOG.debug("Uc1FFCSearchByCustomerId:performSearch()...Result: " +szResult);
        return szResult;
    }
        
	/**
	 * Returns search results based on 1FFC customer identifier
	 * 
	 * @param cszCustomerId
	 * @return
	 */
    private List<CustomerElement1FFC> getSearchResults(final String cszCustomerId) {
    	
    	SearchByCustomerIdDaoImpl searchDao = new SearchByCustomerIdDaoImpl();
    	
    	return searchDao.getCustomerElementsForCustomerId(cszCustomerId);
    	
    }
}
