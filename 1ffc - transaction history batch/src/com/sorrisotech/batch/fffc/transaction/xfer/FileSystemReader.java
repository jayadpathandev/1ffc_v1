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
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/******************************************************************************
 * The class reads a list of files from a provided input directory.
 * It then filters out the files that do not pass a user defined
 * <code>FileFilter</code> rules and returns the rest of the files 
 * one at a time in the <code>read</code> method.
 * 
 * @author Mariana Jbantova
 *
 */
public class FileSystemReader implements ItemReader<Object>, InitializingBean {

    /**************************************************************************
     * Development level logging.
     */
    private static final Logger mLog = LoggerFactory.getLogger(FileSystemReader.class);
    
    /**************************************************************************
     * The source directory which will be scanned for new bill files.
     */
    private String mSourceDirPath = null;
    
    /**************************************************************************
     * A file filter which identifies bill files. 
     */
    private FileFilter mFileFilter = null;
    
    
    /**************************************************************************
     * A file filter which identifies bill files. 
     */
    private LinkedList<File> mFileList = null;
   
    /**************************************************************************
     * The number of seconds the file should be unchanged before we move it.
     */
    private int m_iModifyDelay = 60;
    
    /**************************************************************************
     * Returns the source directory which will be scanned for new bill files.
     * 
     * @return mSourceDirPath
     */
    public String getSourceDirPath() {
        return mSourceDirPath;
    }

    
    /**************************************************************************
     * Sets the source directory which will be scanned for new bill files.
     * 
     * @param szSourceDirPath       source directory path from which files
     *                              will be read
     */
    public void setSourceDirPath(final String szSourceDirPath) {
        mSourceDirPath = szSourceDirPath;
    }

    
    /**************************************************************************
     * (non-Javadoc).
     * @see org.springframework.beans.factory.InitializingBean#
     * afterPropertiesSet()
     * 
     * @throws Exception        if illegal property values used
     */
    public void afterPropertiesSet() throws Exception {
        
        Assert.notNull(mSourceDirPath, 
            "the dir path of where to look for new received files is " 
            + "not initialized.");
        Assert.notNull(mFileFilter, 
            "the file filter pattern for finding bill files is " 
            + "not initialized.");
        final File cInputDir = new File(mSourceDirPath);
        
        Assert.isTrue(cInputDir.exists(), 
            "Invalid input directory. Directory: " 
            + mSourceDirPath + " does not exist"
            );
        
        Assert.isTrue(!cInputDir.isFile(), 
            "The provided source dir path: " 
            + mSourceDirPath + " points to a file not a directory"
            );
        
        // Get a list of files from the input dir that need to be processed
        initFilesList(cInputDir);
    }

    /************************************************************************** 
     * Returns the next file in the directory that machines the file filter
     * and has not been modified in the last n seconds.  Returns null if
     * there are no more files that match the criteria.
     * 
     * @return  The next file to process.
     */
    public Object read() 
    {        
        File cFile = null;
        
        while(mFileList.size() > 0)
        {
            cFile = mFileList.removeFirst();
            
            final Date cNow = new Date();
            
            long lLastModifed = cFile.lastModified();
            long lNow         = cNow.getTime();
            long lDiff        = lNow - lLastModifed;
            
            if (lLastModifed != 0L && lDiff > (long) m_iModifyDelay)
            {
                break;
            }
            cFile = null;
        }
        
        if (cFile == null)
        {
            mLog.debug("No more files to process.");
        }
        
        return cFile;
    }

            
    /**************************************************************************
     * Returns the file filter used to identify bill files
     * 
     * @return FileFilter
     */
    public FileFilter getFileFilter() {
        return mFileFilter;
    }

    /**************************************************************************
     * Sets the file filter for identifying bill files.
     * 
     * @param cFileFilter
     */
    public void setFileFilter(final FileFilter cFileFilter) {
        mFileFilter = cFileFilter;
    }

    /**************************************************************************
     * Sets the number of seconds the file needs to be unchanged before we 
     * pick it up.
     * 
     * @param iSeconds  The number of seconds the file needs to be unchanged
     *                  before it is picked up.
     */
    public void setModifyDelay(
        final int iSeconds
        )
    {
        m_iModifyDelay = iSeconds;
    }
    
    /**************************************************************************
     * Returns the number of seconds the file needs to be unchanged before we
     * pick it up.
     * 
     * @return  The number of seconds the file needs tob e unchanged before
     *          it is picked up.
     */
    public int getModifyDelay()
    {
        return(m_iModifyDelay);
    }
    
    /**************************************************************************
     * The method initializes the <code>mFileList</code> list with the files  
     * located in the input directory <code>cInputDir</code> which  match
     * the <code>FileFilter</code>
     * 
     * @param cInputDir         the directory to read files from
     */
    private void initFilesList(final File cInputDir) {        
        
        // Read the files that match the file pattern
        final File[] billFiles = cInputDir.listFiles(mFileFilter);
            
        if (mLog.isDebugEnabled()) {
            mLog.debug("Number of new bill files found in source dir: " 
                +  mSourceDirPath + " is: " + billFiles.length );
        }
        
        if (billFiles == null) {
            mLog.error("An error occurred while reading from source dir: "
                + mSourceDirPath);
            throw new UnexpectedInputException(
            "An error occurred while reading from source dir.");
        }

        mFileList = new LinkedList<File>(Arrays.asList(billFiles));
    }
}
