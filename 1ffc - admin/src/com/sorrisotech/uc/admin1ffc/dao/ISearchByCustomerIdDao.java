package com.sorrisotech.uc.admin1ffc.dao;


import java.util.List;

/**
 * Search query for 1st Franklin customers that match a customer Id search query
 * 
 * @author john kowalonek
 * @version 2024-Jan-16 -- initial version
 * @since 2024-Jan-16
 */
public interface ISearchByCustomerIdDao {

		/**
		 * Returns list of CustomerElement1FFCs for the specified customer identifier
		 * which may include wildcards
		 * 
		 * @param cszCustomerId -- 1st Franklin customer identifier
		 * 
		 * @return
		 */
		public List<CustomerElement1FFC>	getCustomerElementsForCustomerId(final String cszCustomerId);
}
