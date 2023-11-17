/* (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 40 Nagog Park
 * Acton, MA 01720
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
package com.sorrisotech.batch.fffc.transaction.xfer;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/******************************************************************************
 */
public class FileFilter implements java.io.FileFilter, InitializingBean {
    
    /**************************************************************************
     * Development level logging.
     */
    private static final Logger mLog = LoggerFactory.getLogger(FileFilter.class);

    /**************************************************************************
     * A String pattern used for the initialization of the file filter.
     */
    private String mFilePattern = null;
        
    /**************************************************************************
     * A regular expression String pattern used for the initialization of the
     * file filter. This variable is set to mFilePattern with special 
     * characters escaped and "*" replaced with ".*".
     */
    private String mRegExpPattern = null;

    /**************************************************************************
     * Indicates the type of <code>File</code> the file filter should return
     * - files only, directories only or both.
     */
    private String mFileType = "file";


    /**************************************************************************
     * A set of regular expression special characters that must be escaped.
     */
    private static final Set<Character> SPECIAL_CHARS_SET = new HashSet<Character>();
    static {
        SPECIAL_CHARS_SET.add('.');
    }
    
    /************************************************************************** 
     * If fileType = 'file', the method will return only files matching     
     *   the provided filePattern                                        
     * If fileType = 'dir', the method will return only Directories          
     *   whose names match the provided filePattern                      
     * If fileType = any value different than 'file' and 'dir', the method will   
     *   return both files and directory whose names match the provided  
     *   filePattern.
     *   
     * (non-Javadoc)
     * @see java.io.FileFilter#accept(java.io.File)
     * 
     * @param   cFile       input File to be matches against the file pattern 
     * 
     * @return  boolean     true if file matches file pattern, false otherwise
     */
    public boolean accept(final File cFile) {
        boolean bAccepted = false;
        
        if (mFileType.equalsIgnoreCase("file")) {
            bAccepted = Pattern.matches(mRegExpPattern, cFile.getName())
                && cFile.isFile();
            if (mLog.isDebugEnabled()) {
                mLog.debug("accept(final File cFile): return files only.");
            }
        } else if (mFileType.equalsIgnoreCase("dir")) {
            bAccepted = Pattern.matches(mRegExpPattern, cFile.getName())
                && cFile.isDirectory();
            if (mLog.isDebugEnabled()) {
                mLog.debug("accept(final File cFile): return directories only.");
            }
        } else {
            bAccepted = Pattern.matches(mRegExpPattern, cFile.getName());
            if (mLog.isDebugEnabled()) {
                mLog.debug("accept(final File cFile): "
                    + "return both: files and directories.");
            }
        }
            
        return bAccepted;
    }

    /**************************************************************************
     * Sets the pattern for file names to the provided String. 
     * 
     * @param szFilePattern         regular expression 
     */
    public void setFilePattern(final String szFilePattern) {
        mFilePattern = szFilePattern;
    }
        
    /************************************************************************** 
     * Returns the pattern for file names.
     * 
     * @return mFilePattern     the regular expression mFilePattern is set to
     */
    public String getFilePattern() {
        return mFilePattern;
    }

    /**************************************************************************
     * @return mFileType
     */
    public String getFileType() {
        return mFileType;
    }

    /**************************************************************************
     * Sets file type <code>mFileType</code> which defines if files only, 
     * directories only or files and directories should be considered
     * by this file pattern class.
     * 
     * @param szFileType            file, dir or both should           
     */
    public void setFileType(final String szFileType) {
        mFileType = szFileType;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see org.springframework.beans.factory.InitializingBean#
     * afterPropertiesSet()
     * 
     * @throws Exception        if properties not properly initialized
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mFilePattern, "file pattern is not initialzed.");
        mRegExpPattern = convertToRegExpr(mFilePattern);
        }
    
    /**************************************************************************
     * A method that does simple conversion of a String to  regular expression
     * by escaping special characters such as '.' and replacing '*' with '.*'.
     * 
     * @param szFilePattern     file-name search pattern
     * 
     * @return String           a String representing a legal 
     *                          regular expression
     */
    private String convertToRegExpr(final String szFilePattern) {
        
        String szRegExpres = szFilePattern;
        
        // escape all special characters first
        final Iterator<Character > cIterator = SPECIAL_CHARS_SET.iterator();
        while (cIterator.hasNext()) {
            final String szNextChar = cIterator.next().toString();
            szRegExpres = 
                StringUtils.replace(szRegExpres, szNextChar, "\\" + szNextChar);
        }
        if(szRegExpres.indexOf('*') > -1) {
            szRegExpres = StringUtils.replace(szRegExpres, "*", ".*");
        }
        
        if(mLog.isDebugEnabled()) {
            mLog.debug("Reg expression is: " 
                + szRegExpres + " pattern was: " + szFilePattern);
        }
        
        return szRegExpres;
    }
}
