package com.sorrisotech.fffc.documentesign.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.sorrisotech.fffc.documentesign.service.WalletInfo.WalletInfoWrapper;

public class Dao extends JdbcDaoSupport {
	
	private String getWalletInfoSql;
	
	private static Dao instance = null;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Dao.class);
	
	private Dao() {}
	
	@SuppressWarnings("resource")
	public static Dao getInstance() {
		if (instance != null) return instance;

		ApplicationContext context = new ClassPathXmlApplicationContext("documentEsignServiceContext.xml");
		instance = (Dao) context.getBean("documentEsignDao");
		return instance;
	}
	
	public WalletInfo getWalletInfo(String sourceId) {
		try {
			return getJdbcTemplate().queryForObject(
					getWalletInfoSql, 
					new WalletInfoWrapper(), 
					sourceId
			);
		} catch (Exception ex) {
			LOGGER.error("Error occured while executing query : {} exception : {}", getWalletInfoSql, ex);
			return null;
		}
	}
	
	// All setters goes here
	
	public void setGetWalletInfoSql(String getWalletInfoSql) {
		this.getWalletInfoSql = getWalletInfoSql;
	}
}
