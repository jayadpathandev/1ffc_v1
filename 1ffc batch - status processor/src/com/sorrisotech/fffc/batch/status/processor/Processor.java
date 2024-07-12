/* (c) Copyright 2016-2024 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park,
 * Suite 1725-184,
 * Woburn, MA 01801, USA
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
package com.sorrisotech.fffc.batch.status.processor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.sorrisotech.fffc.batch.status.processor.bean.BillFileMetadata;
import com.sorrisotech.fffc.batch.status.processor.bean.Record;
import com.sorrisotech.fffc.batch.status.processor.mapper.RecordMapper;

/**************************************************************************************************
 * Implementation class for ItemProcessor.
 * 
 * @author Rohit Singh
 * 
 */
public class Processor extends NamedParameterJdbcDaoSupport implements ItemProcessor<BillFileMetadata, List<Record>> {

	/**************************************************************************
	 * Development level logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

	/**************************************************************************
	 * SQL query to fetch internal account numbers based on bill file id.
	 */
	private String m_szGetInternalAccNumberFromBillFileId = null;

	/**************************************************************************
	 * BillFileId mapper for record model class.
	 */
	private RecordMapper m_cRecordMapper;
	
	public Processor() {
		m_cRecordMapper = new RecordMapper();
	}
	
	@Override
	public List<Record> process(BillFileMetadata billFileId) throws Exception {

		LOG.info("Processing Bill file id : {}", billFileId.getBillFiledId());
		
		var params = new MapSqlParameterSource();
		
		params.addValue("bill_file_id_processing", billFileId.getBillFiledId());
		
		List<Record> recordList = getNamedParameterJdbcTemplate().query(
				m_szGetInternalAccNumberFromBillFileId, 
				params, 
				m_cRecordMapper
		);
		
		return recordList;
	}

	public void setGetInternalAccNumberFromBillFileId(String szGetInternalAccNumberFromBillFileId) {
		this.m_szGetInternalAccNumberFromBillFileId = szGetInternalAccNumberFromBillFileId;
	}
	
}