package com.sorrisotech.fffc.agent.pay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Fffc {
	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(Fffc.class);
	
	//*************************************************************************
	// The SPRING context loaded from the 1ffc.xml file.
	private static final ClassPathXmlApplicationContext mContext;
	
	static {	
		ClassPathXmlApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("agent_pay.xml");
		}
		catch (Throwable e) {
			final String msg ="Could not load agent_pay.xml"; 
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	    mContext = context;
	}
	
	//*************************************************************************
	// Get a bean from the 1ffc.xml file.
	private static <TYPE> TYPE getBean(
				final String      name,
				final Class<TYPE> clazz
			) {
		TYPE retval = null;
		
		try {
			retval = mContext.getBean(name, clazz);
		} catch(BeansException e) {
			final String msg ="Could not find bean [" + name + "] in 1ffc.xml."; 
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		return retval;		
	}			

	//*************************************************************************
	// Get the ApiPayDao instance from spring.
	public static ApiPayDao apiPay() {
		return getBean("ApiPayDao", ApiPayDao.class);
	}

}
