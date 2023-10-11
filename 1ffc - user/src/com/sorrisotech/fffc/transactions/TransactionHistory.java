package com.sorrisotech.fffc.transactions;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sorrisotech.common.LocalizedFormat;
import com.sorrisotech.svcs.external.IServiceLocator2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionHistory {

	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TransactionHistory.class);	  

	private static class Transaction {
		int mDate;
		String mType 	   = null;
		String mDesc 	   = null;
		BigDecimal mAmount = BigDecimal.ZERO;				
	
    
	private Transaction( int iDate, String szType, String szDesc, BigDecimal bdAmount) {
		setDate(iDate);
		setType(szType);
		setDesc(szDesc);
		setAmount(bdAmount);
	}
	
	public void setDate(Integer iDate) {
		mDate = iDate;
	}
	
	public int getDate() {
		return mDate;
	}
	
	public void setType(String szType) {
		if (szType != null)
			mType = szType;
	}
	
	public String getType() {
		return mType;
	}
	
	public void setDesc(String szDesc) {
		if (szDesc != null)
			mDesc = szDesc;
	}
	
	public String getDesc() {
		return mDesc;
	}
	
	public void setAmount(BigDecimal amount) {
		mAmount = amount;
	}
	
	public BigDecimal getAmount() {
		return mAmount;
	}	
  }

	/**************************************************************************
	 * Context for 1ffc.xml.
	 */
	private static TransactionHistory mDao = null;
	static {	
		try {
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("1ffc.xml");
			mDao = context.getBean("transactionHistoryDao", TransactionHistory.class);
			context.close();			
		}
		catch (Throwable e) {
			LOG.error("Could not load 1ffc.xml", e); 
		}
		
	    if (mDao == null) throw new RuntimeException("Could not load 1ffc.xml.");
	}

	/**********************************************************************************************
     * Query is the query to get the list of transaction history.
     */	
	@Autowired
	@Qualifier("viewTransactionHistorySql")  
	private String mViewTransactionHistorySql;
	
	/***************************************************************************
	 * Spring object to make the JDBC calls.
	 */
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")	
	private NamedParameterJdbcTemplate mJdbc = null;
		
	/***************************************************************************
	 * GetHistory would query the database and return the List<Transactions> object.
	 */
	private List<Transaction> getHistory(String szAccountNum, String szPayGroup) {
		
		List<Transaction> lResultList = null;

		try {
			final HashMap<String, Object> cParams = new HashMap<String, Object>();    	
			
			cParams.put("account", szAccountNum);
			cParams.put("pay_group", szPayGroup);
    	
			lResultList = mJdbc.query(mViewTransactionHistorySql, cParams, new viewMapper());

		} catch (Exception e)	{
			LOG.error("TransactionHistory.....GetHistory.....An exception was thrown", e);
		}    	
		
		return lResultList;
	}

	@SuppressWarnings("unchecked")
	static public Map<String, String>[] historyFor(
												final IServiceLocator2 locator,
												final Locale cLocale,
												final String szAccountNum,
												final String szPayGroup) {
		
		ArrayList<Map<String, String>> rMap = new ArrayList<Map<String, String>>();
		List<Transaction> cResultList = null;

		try{	
			final LocalizedFormat format = new LocalizedFormat(locator, cLocale);
			cResultList = mDao.getHistory(szAccountNum, szPayGroup);	
						
			for (final Transaction info : cResultList) {
				HashMap<String, String> row = new HashMap<String, String>();

				row.put("t_date", format.date().numeric(info.getDate()));
				row.put("t_date_num", Integer.toString(info.getDate()));				
				row.put("t_type", info.getType());
				row.put("t_desc", info.getDesc());
				
				if (info.getAmount() != null) {
					row.put("t_amount", format.formatAmount(szPayGroup, info.getAmount()));
				}
				else {
					row.put("t_amount", "");
				}
				
				rMap.add(row);
			}
		} catch(Exception e){
			LOG.error("TransactionHistory.....historyFor()...An exception was thrown", e);
		}		

		return rMap.toArray(new Map[rMap.size()]);
		
	}
	
	
	/***************************************************************************
	 * This class is used by the method ViewTransactionHistory()
	 * to map each row of data in the ResultSet.
	 */
	static public class viewMapper implements RowMapper<Transaction> {
		@Override
		public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			return new Transaction(
								rs.getInt(1),
								rs.getString(2),
								rs.getString(3),
								rs.getBigDecimal(4));
		}
	}

	

	

}
