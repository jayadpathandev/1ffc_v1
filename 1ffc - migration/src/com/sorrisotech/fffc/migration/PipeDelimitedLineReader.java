/**
 * 
 */
package com.sorrisotech.fffc.migration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *   Reads and splits lines with pipe delimited records
 *   
 *   @author johnk
 *   @since 2024-Jul-17
 *   @version 2024-Jul-17	jak	first version
 */
public class PipeDelimitedLineReader {

	
	private static final Logger LOG = LoggerFactory.getLogger(PipeDelimitedLineReader.class);
	
	BufferedReader m_in = null;
	String m_szPath = null;
	Integer m_iLineCount = 0;
	
	/**
	 * Opens the file specified in path and creates the reader
	 * 
	 * @param cszPathName
	 * @return
	 */
	public Boolean openFile(final String cszPathName) {
		Boolean lbReturn = false;
		try {
			m_in = new BufferedReader(new FileReader(cszPathName));
			LOG.info("PipeDelimitedLineReader:openFile -- opened file {}.", cszPathName);
			m_szPath = cszPathName;
			lbReturn = true;
		} catch (FileNotFoundException e) {
			LOG.error("PipeDelimitedLineReader:openFile -- FileNotFound {}, message: {}.", cszPathName, e.getMessage());
			return lbReturn;
		}
		return lbReturn;
	}
	
	/**
	 * Returns an array of fields from the line that is split by the
	 * pipe | delimiter or null if there was an error or we reached end 
	 * of file
	 * 
	 * @return
	 */
	public String[] getSplitLine() {
		String[] lszRetArray = null;
		String lszInLine = new String();
		try {
			lszInLine = m_in.readLine();
			if (null != lszInLine) {
				lszRetArray = lszInLine.split("\\Q|\\E", 0);
				if ((lszRetArray == null) || (lszRetArray.length == 0)) 
					return null;
				else
					m_iLineCount++;
			}
			else {
				m_in.close();
				System.out.println();
				LOG.info("PipeDelimitedLineReader:getSplitLine -- file {} closed, lines read: {}.", m_szPath, m_iLineCount);
			}
		} catch (IOException e) {
			System.out.println();
			LOG.error("PipeDelimitedLineReader:getSplitLine -- IOException {}. message: {}", m_szPath, e.getMessage());
			return null;
		}
		return lszRetArray;
	}
}
