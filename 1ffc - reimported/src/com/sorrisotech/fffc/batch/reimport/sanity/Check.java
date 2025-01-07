/* (c) Copyright 2016-2025 Sorriso Technologies, Inc(r), All Rights Reserved, 
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
package com.sorrisotech.fffc.batch.reimport.sanity;

import java.math.BigDecimal;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public class Check extends NamedParameterJdbcDaoSupport {
	private static final Logger LOG = LoggerFactory.getLogger(Check.class);
	private String mQueryTmAccount;
	private String mFixTmAccount;
	private String mQueryProfCompanyAccount;
	private String mFixProfCompanyAccount;
	private String mQueryProfCompanyOrgId;
	private String mFixProfCompanyOrgId;
	
	private void fixUpTmAccount(
			final String     orgId,
			final BigDecimal companyId
			) {
		var params  = new HashMap<String, Object>();
		params.put("newOrgId", orgId);
		var records = this.getNamedParameterJdbcTemplate().query(
				mQueryTmAccount,
				params,
				new TmAccount()
				);
		for(var rec : records) {
			var fix = false;
			if (rec.endDate != null) {
				LOG.info("Fixing TM_ACCOUNT [" + rec.id + "] end_date is set.");
				fix = true;
			}
			if (rec.companyid == null) {
				LOG.info("Fixing TM_ACCOUNT [" + rec.id + "] company_id is not set.");
				fix = true;			
			}
			
			if (fix) {
				var fixParams  = new HashMap<String, Object>();
				fixParams.put("companyId", companyId);
				fixParams.put("accountId", rec.id);
				this.getNamedParameterJdbcTemplate().update(
					mFixTmAccount,
					fixParams
					);
			}
		}	
	}

	private void fixUpProfCompanyAccount(
			final BigDecimal companyId
			) {
		var params  = new HashMap<String, Object>();
		params.put("companyId", companyId);
		var records = this.getNamedParameterJdbcTemplate().query(
				mQueryProfCompanyAccount,
				params,
				new ProfCompanyAccount()
				);
		for(var rec : records) {
			var fix = false;
			if (rec.endDate != null) {
				LOG.info("Fixing PROF_COMPANY_ACCOUNT [" + rec.id + "] end_date is set.");
				fix = true;
			}
			
			if (fix) {
				var fixParams  = new HashMap<String, Object>();
				fixParams.put("id", rec.id);
				fixParams.put("startDate", rec.startDate);
				this.getNamedParameterJdbcTemplate().update(
					mFixProfCompanyAccount,
					fixParams
					);
			}
		}	
	}

	private void fixUpProfCompanyOrgId(
			final String     orgId,
			final BigDecimal companyId
			) {
		var params  = new HashMap<String, Object>();
		params.put("companyId", companyId);
		var records = this.getNamedParameterJdbcTemplate().queryForList(
				mQueryProfCompanyOrgId,
				params,
				String.class
				);
		if (records.size() == 1) {
			if (records.get(0).equals(orgId) == false) {
				LOG.info(
					"Company [+ " + companyId + "] has wrong org ID [" + records.get(0) + 
					"] updating to [" + orgId + "]");
				var fixParams  = new HashMap<String, Object>();
				fixParams.put("newOrgId", orgId);
				fixParams.put("companyId", companyId);
				this.getNamedParameterJdbcTemplate().update(
					mFixProfCompanyOrgId,
					fixParams
					);				
			}
		} else {
			LOG.error(
				"Company [" + companyId + "] has " + records.size() + " org IDs assigned!"
				);
		}
	}
	
	public void verifyCompany(
			final String     orgId,
			final BigDecimal companyId
			) {
		fixUpTmAccount(orgId, companyId);
		fixUpProfCompanyAccount(companyId);
		fixUpProfCompanyOrgId(orgId, companyId);
	}
	
	public void setQueryTmAccount(
			final String sql
			) {
		mQueryTmAccount = sql;
	}

	public void setFixTmAccount(
			final String sql
			) {
		mFixTmAccount = sql;
	}

	public void setQueryProfCompanyAccount(
			final String sql
			) {
		mQueryProfCompanyAccount = sql;
	}
	
	public void setFixProfCompanyAccount(
			final String sql
			) {
		mFixProfCompanyAccount = sql;
	}

	public void setQueryProfCompanyOrgId(
			final String sql
			) {
		mQueryProfCompanyOrgId = sql;
	}
	
	public void setFixProfCompanyOrgId(
			final String sql
			) {
		mFixProfCompanyOrgId = sql;
	}
	
}
