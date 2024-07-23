/**
 * 
 */
package com.sorrisotech.fffc.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ACIToken class supports two capabilities:
 * 
 * 	1. Static method that creates a map of the external tokens that ACI has supplied, reading
 * 		the migration file and creating the map of these objects from that. 
 * 
 *  2. The IExternalToken interface on an instance of this object that retrieves the attributes
 *  	of that object.
 * 
 * @author johnk
 * @since 2024-Jul-17
 * @version 2024-Jul-18  Everything except reading the latest file.
 * @version 2024-Jul-21  jak changed to use token map object when creating the map of tokens.
 * 
 */
public class ACIToken implements IExternalToken {

	private static final Logger LOG = LoggerFactory.getLogger(ACIToken.class);

	private String m_sACIToken = null;
	private String m_sExprationDate = null;
	private String m_slast4CardNumber = null;
	private String m_sAccountId = null;
	private String m_sCardHolderName = null;
	
	/**
	 * Reads an ACI token file for use in the application. Creates a list
	 * of ACIToken objects accessible through the IExternalToken interface
	 * 
	 * @param czFilePath
	 * @return null if it failed, a map of IExternalToken interface objects if it succeeds
	 */
	static public TokenMap createACITokenMap(String czFilePath) {
		
		final Integer ciRecordTypePos = 0;
		final Integer ciStatusPos = 1;
		final Integer ciFailureReaonPos = 2;
		final Integer ciAccountNumberPos = 3;
		final Integer ciTokenValuePos = 5;
		final Integer ciMaskedValuePos = 6;
		final Integer ciHolderNamePos = 7;
		final Integer ciExpireDatePos = 8;
		final String  cszTokenRecordType = "576503";
		final String  cszTokenSuccess = "SUCCESS";

		TokenMap lTokenMap = new TokenMap();
		PipeDelimitedLineReader input = new PipeDelimitedLineReader();
		String [] record = null;
		
		if (!input.openFile(czFilePath)) {
			LOG.error("ACIToken:createACITokenMap -- Failed to open file: ().", czFilePath);
			return null;
		}
		
		LOG.info("ACIToken:createACITokenMap -- processing token records.");
		Integer iLoopCount = 0;
		
		do {
			record = input.getSplitLine();
			if ((null != record) && record[ciRecordTypePos].equals(cszTokenRecordType)) {
				if (record[ciStatusPos].equals(cszTokenSuccess)) {
					// -- convert expiration date MMYYYY into MM/YYYY what we need --
					String szExpireDate = record[ciExpireDatePos].substring(0,2) + "/" +
											record[ciExpireDatePos].substring(2);
					String szMask = record[ciMaskedValuePos];
					String szLast4 = szMask.substring(szMask.length()-4);
					IExternalToken token = new ACIToken( record[ciTokenValuePos],
												   szExpireDate,
												   szLast4,
												   record[ciAccountNumberPos],
												   record[ciHolderNamePos]);
					lTokenMap.put(token);
					LOG.info("ACIToken:createACITokenMap -- Created token for account {}.", record[ciAccountNumberPos]);
					iLoopCount++;
					if (0 == Integer.remainderUnsigned(iLoopCount, 10000)) {
						System.out.print(".");
					}
				} else {
					LOG.info("ACIToken:createACITokenMap -- Invalid token for account: {}" +
								" Reason: {}", record[ciAccountNumberPos], record[ciFailureReaonPos]);
				}
			}
		} while (record != null);
		System.out.println();
		LOG.info("ACIToken:createACITokenMap -- finished processing file: {}, {} token objects created.", 
				czFilePath, lTokenMap.size());
		return lTokenMap;
	}
	
	/**
	 * creates an ACIToken object
	 * 
	 * @param cszToken
	 * @param cszExpiratonDate
	 * @param cszLastFour
	 * @param cszAccountId
	 * @param cszCardHolderName
	 */
	private ACIToken( 
			final String cszToken, 
			final String cszExpiratonDate, 
			final String cszLastFour, 
			final String cszAccountId,
			final String cszCardHolderName) {
		m_sACIToken = cszToken;
		m_sExprationDate = cszExpiratonDate;
		m_slast4CardNumber = cszLastFour;
		m_sAccountId = cszAccountId;
		m_sCardHolderName = cszCardHolderName;
	}
	
	@Override
	public String getPipedToken() {
		
		String l_sPipedToken = "ACITOKEN|" + m_sACIToken;
		return l_sPipedToken;
	}

	@Override
	public String getExpirationDate() {
		return m_sExprationDate;
	}

	@Override
	public String getFriendlyName() {
		String lszFriendlyName = "Migrated Debit Card ending in " + m_slast4CardNumber;
		return lszFriendlyName;
	}

	@Override
	public String getMaskedName() {
		String lszMaskedName = "************" + m_slast4CardNumber;
		return lszMaskedName;
	}

	@Override
	public String getAccountId() {
		return m_sAccountId;
	}

	@Override
	public String getInfoAsString() {
		String lszInfo = "Values Account: " + m_sAccountId +", Token: " + m_sACIToken + 
				", Card holder: " + m_sCardHolderName  + ", Last 4 digits: " + m_slast4CardNumber +
				", Expiration: " + m_sExprationDate;
		return lszInfo;
	}

	@Override
	public String getAccountHolderName() {

		return m_sCardHolderName;
	}

	@Override
	public String getToken() {
		
		return m_sACIToken;
	}

	@Override
	public Object getLast4() {
		return m_slast4CardNumber;
	}

}
