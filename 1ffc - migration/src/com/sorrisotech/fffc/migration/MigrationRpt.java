/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 	  Creates a comma separated report file with headers
 * 		showing the records migrated and their status.
 * 	@author johnk
 *  @since	2024-Jul-25
 *  @version 2024-Jul-25 jak	First version.
 */
public class MigrationRpt {

	private static final Logger LOG = LoggerFactory.getLogger(MigrationRpt.class);
	
	private static MigrationRpt report = null;
	
	FileWriter m_Writer = null;
	
	

	
	/**
	 * Writes a record associated with migration
	 * 
	 * @param rcd
	 */
	static void reportItem(final MigrateRecord rcd) {
		
		// -- initialize if its not there --
		if (null == report) {
			report = new MigrationRpt();
			if (!report.getWriter()) return;
			if (!report.writeHeader()) return;
		}
		
		// -- format the output data --
		if (null == rcd) return;
		
		String szToWrite = null;
		szToWrite = report.addItem(szToWrite, rcd.migrationStatus, false);
		szToWrite = report.addItem(szToWrite, rcd.validated, false);
		szToWrite = report.addItem(szToWrite, rcd.pmtType, false);
		szToWrite = report.addItem(szToWrite, rcd.customerId, false);
		szToWrite = report.addItem(szToWrite, rcd.displayAcct, false);
		szToWrite = report.addItem(szToWrite, rcd.internalAcct, false);
		szToWrite = report.addItem(szToWrite, rcd.schedPmtId, false);
		szToWrite = report.addItem(szToWrite, rcd.schedDate, false);
		szToWrite = report.addItem(szToWrite, rcd.schedAmt, false);
		szToWrite = report.addItem(szToWrite, rcd.recurDay, false);
		szToWrite = report.addItem(szToWrite, rcd.failReason, true);
		try {
			report.m_Writer.write(szToWrite);
		} catch (IOException e) {
			LOG.error("MigrationReport:reportItem -- failed to write item.", e);
			e.printStackTrace();
		}
	}

	static void flushReport() {
		if (null == report) return;
		
		try {
			if (null == report.m_Writer) return;
			report.m_Writer.flush();
		} catch (IOException e) {
			LOG.error("MigrationReport:closeWriter -- failed to flush writer.", e);
			e.printStackTrace();
		}
		
	}
	/**
	 * Flushes and closes the report file
	 */
	static void closeReport() {
		if (null == report) return;
		
		try {
			if (null == report.m_Writer) return;
			report.m_Writer.flush();
			report.m_Writer.close();
		} catch (IOException e) {
			LOG.error("MigrationReport:closeWriter -- failed to flush/close writer.", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds an item to the list.
	 * 
	 * @param base
	 * @param cszItem
	 * @param cbIsLast
	 * @return
	 */
	private String addItem(String base, final String cszItem, final Boolean cbIsLast ) {
		String szAdd = "--";
		String szRet = null;
		
		// -- not null... add it in --
		if (null != cszItem) {
			szAdd = cszItem;
		}
		
		// -- add separator --
		if (!cbIsLast) 
			szAdd = szAdd.concat(",");
		else
			szAdd = szAdd.concat("\n");

		// -- add to base --
		if (null != base) {
			base = base.concat(szAdd);
			szRet = base;
		} else {
			szRet = szAdd;
		}
		
		return szRet;
	}
	
	/**
	 * writes the header when starting 
	 * 
	 * @return
	 */
	private Boolean writeHeader() {
		Boolean bRet = true;
	
		final String cszHeader = "Status, Validated, Type, Customer Id, Loan Id, " + ""
				+ "Account Id, Sched-PmtId, Sched-Date, Sched-Amt, Recur-DayOfMonth, Fail-Reason\n";
		
		try {
			m_Writer.write(cszHeader);
		} catch (IOException e) {
			LOG.error("MigrationReport:getWriter -- failed to write header.", e);
			bRet = false;
		}
		return bRet;
			
	}

	/**
	 * gets the writer object
	 * 
	 * @return
	 */
	private Boolean getWriter() {
		
		Boolean bRet = true;
		
		// -- filepath + date/time + .csv suitable for reading into excel --
		String cszFilePath = Config.get("report.filePathBase");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cszFilePath = cszFilePath + " " + sdf.format(cal.getTime()) + ".csv";
		
		// -- create the file writer --
		try {
			m_Writer = new FileWriter(cszFilePath);
		} catch (IOException e) {
			LOG.error("MigrationReport:getWriter -- failed to create or open File writer {}", e);
			bRet = false;
		}
		return bRet;
	}
	
	

}
