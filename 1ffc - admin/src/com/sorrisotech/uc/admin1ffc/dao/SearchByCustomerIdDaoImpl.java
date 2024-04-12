package com.sorrisotech.uc.admin1ffc.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * class SearchByCustomerIdDaoImpl implements the ISearchByCustomerIdDao interface and
 * searches for a list of users and attributes based on the 1FFC customer identifier.
 * 
 * @author john kowalonek
 * @version 2024-jan-16 -- jak initial version.
 * @since 2024-jan-16
 */
public class SearchByCustomerIdDaoImpl implements ISearchByCustomerIdDao {


	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SearchByCustomerIdDaoImpl.class);	  


	/**************************************************************************
	 * Context for 1ffcStatus.xml.
	 */
	private static SearchByCustomerIdDaoImpl mDao = null;
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffcSearchByCustomerId.xml");
			mDao = context.getBean("searchByCustomerIdDao", SearchByCustomerIdDaoImpl.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("Could not load 1ffcSearchByCustomerId.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("Could not load 1ffcSearchByCustomerId.xml.");
	}
	
	/**********************************************************************************************
     * query definition to get the list of transaction history.
     */	
	@Autowired
	@Qualifier("search1FFCUsersByCustomerIdSql")  
	private String m_GetUsersByCustomerIdQuery;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;

	private List<CustomerElement1FFC> getCustomerElementsInternal (final String cszCustomerId) {
		List<CustomerElement1FFC> lResultList = null;
		try {
			String lszCustomerId = null;
			
			// -- convert wild card * to % if present --
			lszCustomerId = cszCustomerId.replace('*', '%');
			
			// -- add the query parameter(s) --
			final HashMap<String, Object> cParams = new HashMap<String, Object>();
			cParams.put("CUSTID", lszCustomerId);
			
			// -- make query --
			lResultList = mJdbc.query(m_GetUsersByCustomerIdQuery, cParams, new CustomerElementMapper1FFC());
		}
		catch (Exception e) {
			LOG.error("getCustomerElementsInternal -- Exception thrown for query", e);
		}
			
		return lResultList;
	
	}

	
	@Override
	public List<CustomerElement1FFC> getCustomerElementsForCustomerId(String cszCustomerId) {
		return mDao.getCustomerElementsInternal(cszCustomerId);
	}
}
