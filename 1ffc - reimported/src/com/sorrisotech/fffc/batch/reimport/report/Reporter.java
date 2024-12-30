package com.sorrisotech.fffc.batch.reimport.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public class Reporter extends NamedParameterJdbcDaoSupport {
	private static final Logger LOG = LoggerFactory.getLogger(Reporter.class);

	// For both.
	private String mProfCompanyOrgid;
	private String mProfCompanyAccount;
	private String mTmAccount;
	
	// Only account changes.
	private String mPmtGrouping;
	private String mPmtAutomaticGrouping;
	private String mPmtAutomaticDocuments;
	private String mBill;
	
	private String toList(
			Collection<String> ids
			) {
		final StringBuilder retval = new StringBuilder();
		
		for(var id : ids) {
			 if (retval.length() > 0) {
				 retval.append(',');
			 }
			 retval.append('\'');
			 retval.append(id);
			 retval.append('\'');
		}			
		
		return retval.toString();
	}
	
	private void profCompanyOrgId(
			final Collection<String> orgIds
			) {
		final var ids = toList(orgIds);

		final var pco = this.getNamedParameterJdbcTemplate().query(
			mProfCompanyOrgid.replace("@VALUES@", ids),
			new HashMap<String, Object>(),
			new ProfCompanyOrgid()
		);
		LOG.info("PROF_COMPANY_ORGID - " + pco.size() + " records.");
		LOG.info(
			String.format("%10s %10s", "COMPANYID", "ORGID")
		);
		for(final var rec : pco) {
			LOG.info(
				String.format("%10s %10s", rec.companyid, rec.orgId)
			);			
		}	
	}
	
	private void profCompanyAccount(
			final BigDecimal companyId
			) {
		if (companyId != null) {
			final var params = new HashMap<String, Object>();
			params.put("companyId", companyId);
			
			final var pca = this.getNamedParameterJdbcTemplate().query(
				mProfCompanyAccount,
				params,
				new ProfCompanyAccount()
			);
			LOG.info("PROF_COMPANY_ACCOUNT - " + pca.size() + " records.");
			LOG.info(
				String.format("%10s %10s %8s %8s", "COMPANY_ID", "ACCOUNT_ID", "START_DATE", "END_DATE")
			);
			for(final var rec : pca) {
				LOG.info(
					String.format("%10s %10s %8s %8s", rec.companyid, rec.accountid, rec.startDate, rec.endDate)
				);			
			}
		}	
	}
	
	private void tmAccount(
			final Collection<String> orgIds
			) {
		final var ids = toList(orgIds);
		
		final var ta = this.getNamedParameterJdbcTemplate().query(
			mTmAccount.replace("@VALUES@", ids),
			new HashMap<String, Object>(),
			new TmAccount()
		);
		LOG.info("TM_ACCOUNT - " + ta.size() + " records.");
		LOG.info(
			String.format(
				"%20s %22s %8s %8s %20s %10s %10s", 
				"ACCOUNT_NUMBER", "ACCOUNT_NUMBER_DISPLAY", "START_DATE", "END_DATE", "ACCOUNT_NAME", "COMPANY_ID", "ORG_ID"
			)
		);
		for(final var rec : ta) {
			LOG.info(
				String.format(
					"%20s %22s %8s %8s %20s %10s %10s", 
					rec.accountNumber, rec.accountNumberDisplay, rec.startDate, rec.endDate,
					rec.accountName, rec.companyId, rec.orgId
				)
			);			
		}			
	}
	
	private void pmtGrouping(
			final String display
			) {
		final var params= new HashMap<String, Object>();
		params.put("display", display);
		
		final var pg = this.getNamedParameterJdbcTemplate().query(
			mPmtGrouping,
			params,
			new PmtGrouping()
		);
		LOG.info("PMT_GROUPING - " + pg.size() + " records.");
		LOG.info(
			String.format("%15s %23s %22s", "ONLINE_TRANS_ID", "INTERNAL_ACCOUNT_NUMBER", "DISPLAY_ACCOUNT_NUMBER")
		);
		for(final var rec : pg) {
			LOG.info(
				String.format(
					"%15s %23s %22s", 
					rec.onlineTransId, 
					rec.internalAccountNum, 
					rec.displayAccountNum
				)
			);			
		}
	}

	private void pmtAutomaticGrouping(
			final String display
			) {
		final var params= new HashMap<String, Object>();
		params.put("display", display);
		
		final var pag = this.getNamedParameterJdbcTemplate().query(
			mPmtAutomaticGrouping,
			params,
			new PmtAutomaticGrouping()
		);
		LOG.info("PMT_AUTOMATIC_GROUPING - " + pag.size() + " records.");
		LOG.info(
			String.format("%15s %23s %22s", "AUTOMATIC_ID", "INTERNAL_ACCOUNT_NUMBER", "DISPLAY_ACCOUNT_NUMBER")
		);
		for(final var rec : pag) {
			LOG.info(
				String.format(
					"%15d %23s %22s", 
					rec.automaticId, 
					rec.internalAccountNum, 
					rec.displayAccountNum
				)
			);			
		}
	}
	
	private void pmtAutomaticDocuments(
			final Collection<String> accountIds
			) {
		final var ids = toList(accountIds);
		
		final var pad = this.getNamedParameterJdbcTemplate().query(
			mPmtAutomaticDocuments.replace("@VALUES@", ids),
			new HashMap<String, Object>(),
			new PmtAutomaticDocuments()
		);
		LOG.info("PMT_AUTOMATIC_DOCUMENTS - " + pad.size() + " records.");
		LOG.info(
			String.format("%15s %23s %22s", "INTERNAL_ACCOUNT_NUMBER", "DOCUMENT_NUMBER", "STATUS")
		);
		for(final var rec : pad) {
			LOG.info(
				String.format(
					"%15s %23s %22s", 
					rec.internalAccountNum, 
					rec.documentNumber, 
					rec.status
				)
			);			
		}
	}

	private void bill(
			final Collection<String> accountIds
			) {
		final var ids = toList(accountIds);
		
		final var b = this.getNamedParameterJdbcTemplate().query(
			mBill.replace("@VALUES@", ids),
			new HashMap<String, Object>(),
			new Bill()
		);
		LOG.info("BILL - " + b.size() + " records.");
		LOG.info(
			String.format("%20s %23s %9s %14s", "BILL_ID", "ACCOUNT", "BILL_DATE", "INVOICE_NUMBER")
		);
		for(final var rec : b) {
			LOG.info(
				String.format(
					"%20s %23s %9s %14s", 
					rec.billId, 
					rec.account, 
					rec.billDate,
					rec.invoiceNumber
				)
			);			
		}
	}
	
	public void beforeOrgChange(
				final BigDecimal companyId,
				final String     oldOrgId,
				final String     newOrgId
			) {
		final var orgIds = new ArrayList<String>();
		orgIds.add(oldOrgId);
		orgIds.add(newOrgId);
		
		LOG.info("Before reimporting ORG ID " + oldOrgId + " => " + newOrgId);
		profCompanyOrgId(orgIds);
		profCompanyAccount(companyId);
		tmAccount(orgIds);
	}

	public void afterOrgChange(
				final BigDecimal companyId,
				final String     oldOrgId,
				final String     newOrgId
			) {
		final var orgIds = new ArrayList<String>();
		orgIds.add(oldOrgId);
		orgIds.add(newOrgId);
		
		LOG.info("After reimporting ORG ID " + oldOrgId + " => " + newOrgId);
		profCompanyOrgId(orgIds);
		profCompanyAccount(companyId);
		tmAccount(orgIds);
	}

	public void beforeAccountChange(
			final BigDecimal         companyId,
			final Collection<String> oldOrgId,
			final String             newOrgId,
			final String             displayAccount,
			final Collection<String> oldAccountId,
			final String             newAccountId
			) {
		final var orgIds = new ArrayList<String>(oldOrgId);
		orgIds.add(newOrgId);
		final var accountIds = new ArrayList<String>(oldAccountId);
		accountIds.add(newAccountId);

		LOG.info("Before reimporting ORG ID " + oldOrgId + " => " + newOrgId);
		profCompanyOrgId(orgIds);
		profCompanyAccount(companyId);
		tmAccount(orgIds);
		pmtGrouping(displayAccount);
		pmtAutomaticGrouping(displayAccount);
		pmtAutomaticDocuments(accountIds);
		bill(accountIds);
	}
	
	public void afterAccountChange(
				final BigDecimal         companyId,
				final Collection<String> oldOrgId,
				final String             newOrgId,
				final String             displayAccount,
				final Collection<String> oldAccountId,
				final String             newAccountId
			) {
		final var orgIds = new ArrayList<String>(oldOrgId);
		orgIds.add(newOrgId);
		final var accountIds = new ArrayList<String>(oldAccountId);
		accountIds.add(newAccountId);
		
		LOG.info("After reimporting ORG ID " + oldOrgId + " => " + newOrgId);
		profCompanyOrgId(orgIds);
		profCompanyAccount(companyId);
		tmAccount(orgIds);
		pmtGrouping(displayAccount);
		pmtAutomaticGrouping(displayAccount);
		pmtAutomaticDocuments(accountIds);
		bill(accountIds);
	}
	
	public void setProfCompanyOrgid(
			final String sql
			) {
		mProfCompanyOrgid = sql;
	}

	public void setProfCompanyAccount(
			final String sql
			) {
		mProfCompanyAccount = sql;
	}

	public void setTmAccount(
			final String sql
			) {
		mTmAccount = sql;
	}
	
	public void setPmtGrouping(
			final String sql
			) {
		mPmtGrouping = sql;
	}

	public void setPmtAutomaticGrouping(
			final String sql
			) {
		mPmtAutomaticGrouping = sql;
	}

	public void setPmtAutomaticDocuments(
			final String sql
			) {
		mPmtAutomaticDocuments = sql;
	}
	
	public void setBill(
			final String sql
			) {
		mBill = sql;
	}	
}
