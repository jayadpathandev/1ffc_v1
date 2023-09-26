/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 *
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.batch.fffc.transaction.loader.transformer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;

import com.sorrisotech.batch.fffc.transaction.loader.beans.TransactionDetailsBean;
import com.sorrisotech.batch.fffc.transaction.loader.model.TransactionsData;
import com.sorrisotech.batch.fffc.transaction.loader.model.TransactionsData.TransactionRecord;

/******************************************************************************
 * Converts the {@link XmlObject} to {@link TransactionRecord}.
 *
 * @author Asrar Saloda
 *
 */
public class XmlObjectToTransactionTransformer
        implements ItemProcessor<TransactionsData.TransactionRecord, TransactionDetailsBean>,
        StepExecutionListener {
	
	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger mLog = LoggerFactory
	        .getLogger(XmlObjectToTransactionTransformer.class);
	
	/**************************************************************************
	 * Job execution context
	 */
	@SuppressWarnings("unused")
	private ExecutionContext mExecutionContext;
	
	@SuppressWarnings("rawtypes")
	protected ItemWriter postProcessItemWriter = null;
	
	@SuppressWarnings("unchecked")
	public ItemWriter<List<TransactionDetailsBean>> getPostProcessItemWriter() {
		return postProcessItemWriter;
	}
	
	public void setPostProcessItemWriter(
	        ItemWriter<List<TransactionDetailsBean>> postProcessItemWriter) {
		this.postProcessItemWriter = postProcessItemWriter;
	}
	
	/**************************************************************************
	 * (non-Javadoc).
	 * 
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked" })
	public TransactionDetailsBean process(
	        final TransactionsData.TransactionRecord cTransactionRecord) throws Exception {
		//-------------------------------------------------------------------------------------
		// The date in XML file is in time stamp format.
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		
		LocalDateTime cPayDate = LocalDateTime.parse(cTransactionRecord.getDate(), inputFormatter);
		
		//-------------------------------------------------------------------------------------
		// Converted date to yyyyMMdd format and storing in numeric format.
		
		Integer formattedDate =Integer.valueOf(DateTimeFormatter.ofPattern("yyyyMMdd").format(cPayDate));
		
		TransactionDetailsBean cTransactionDetails = new TransactionDetailsBean(
		        cTransactionRecord.getOnlineId(), formattedDate, cTransactionRecord.getAccount(),
		        cTransactionRecord.getTransactionType(), cTransactionRecord.getDescription(),
		        cTransactionRecord.getAmount());
		
		if (postProcessItemWriter != null && cTransactionDetails != null) {
			List<TransactionDetailsBean> wrapperList = new ArrayList<TransactionDetailsBean>(1);
			wrapperList.add(cTransactionDetails);
			postProcessItemWriter.write(wrapperList);
		}
		
		return cTransactionDetails;
	}
	
	/**************************************************************************
	 * (non-Javadoc).
	 * 
	 * @see org.springframework.batch.core.StepExecutionListener
	 *      #afterStep(org.springframework.batch.core.StepExecution)
	 */
	public ExitStatus afterStep(final StepExecution cStepExecution) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("afterStep: " + cStepExecution.getExitStatus());
		}
		return null;
	}
	
	/**************************************************************************
	 * (non-Javadoc).
	 * 
	 * @see org.springframework.batch.core.StepExecutionListener
	 *      #beforeStep(org.springframework.batch.core.StepExecution)
	 */
	public void beforeStep(final StepExecution cStepExecution) {
		mExecutionContext = cStepExecution.getJobExecution().getExecutionContext();
	}
}
