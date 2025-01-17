package com.sorrisotech.fffc.account.report.bean;

import java.sql.Timestamp;

/***********************************************************************************
 * Represents an account record with detailed information about a loan or
 * account. This class encapsulates the loan number, reference number, most
 * recent record date, file name from which it was loaded, record type, and a
 * description of any missing records.
 */
public final class AccountRecord {
	
	private String    loanNum;
	private String    refNum;
	private Timestamp latestRecordDate;
	private String    sourceFile;
	private String    recordType;
	private String    missing;
	
	/**
	 * 
	 */
	public AccountRecord() {
		super();
	}

	/**
	 * @return the loanNum
	 */
	public String getLoanNum() {
		return loanNum;
	}

	/**
	 * @param loanNum the loanNum to set
	 */
	public void setLoanNum(String loanNum) {
		this.loanNum = loanNum;
	}

	/**
	 * @return the refNum
	 */
	public String getRefNum() {
		return refNum;
	}

	/**
	 * @param refNum the refNum to set
	 */
	public void setRefNum(String refNum) {
		this.refNum = refNum;
	}

	/**
	 * @return the latestRecordDate
	 */
	public Timestamp getLatestRecordDate() {
		return latestRecordDate;
	}

	/**
	 * @param latestRecordDate the latestRecordDate to set
	 */
	public void setLatestRecordDate(Timestamp latestRecordDate) {
		this.latestRecordDate = latestRecordDate;
	}

	/**
	 * @return the sourceFile
	 */
	public String getSourceFile() {
		return sourceFile;
	}

	/**
	 * @param sourceFile the sourceFile to set
	 */
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	/**
	 * @return the recordType
	 */
	public String getRecordType() {
		return recordType;
	}

	/**
	 * @param recordType the recordType to set
	 */
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	/**
	 * @return the missingRecordDescription
	 */
	public String getMissing() {
		return missing;
	}

	/**
	 * @param missingRecordDescription the missingRecordDescription to set
	 */
	public void setMissing(String missingRecordDescription) {
		this.missing = missingRecordDescription;
	}

	/***********************************************************************************
	 * Returns a string representation of the AccountRecord object. This is useful
	 * for logging or debugging.
	 * 
	 * @return A string representing the AccountRecord object.
	 */
	@Override
	public String toString() {
		return "AccountRecord{" + "loanNumber='" + loanNum + '\'' + ", referenceNumber='"
		        + refNum + '\'' + ", dateOfMostRecentRecord=" + latestRecordDate
		        + ", filename='" + sourceFile + '\'' + ", recordType='" + recordType + '\''
		        + ", missingRecordDescription='" + missing + '\'' + '}';
	}
}
