package com.sorrisotech.fffc.migration;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Quick and dirty property file object
 */
public class Config {
	
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	static ResourceBundle m_resource = null;
	
	final static String cszPropertyFileBase = "migration"; 
	
	static String get(final String cszName) {
		if (null == m_resource) {
			try {
				m_resource = ResourceBundle.getBundle(cszPropertyFileBase);
			} catch (MissingResourceException e) {
				LOG.error("Confg:get -- failure getting resource bundle {}.", cszPropertyFileBase);
				return null;
			}
		}
		return m_resource.getString(cszName);
	}

}
 