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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import com.sorrisotech.batch.fffc.transaction.loader.Constants.FileStatus;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaData;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaDataDao;

/******************************************************************************
 * The class updates the database for newly received files by the saas system
 * and if files have to be moved to a new destination directory, the class
 * moves the files.
 * 
 * @author Mariana Jbantova
 *
 */
public class FileSystemWriter implements ItemWriter<File>, InitializingBean {


    /**************************************************************************
     * Development level logging.
     */
    private static final Logger mLog = LoggerFactory.getLogger(FileSystemWriter.class);

    /**************************************************************************
     * The destination directory to which new files will be moved if 
     * <code>mMove</code> set to true.
     */
    private String mDestinationDirPath = null;
    
    /**************************************************************************
     * The error directory to which duplicate files will be moved if 
     * <code>mAllowDUplicateFilename</code> set to false.
     */
    private String mErrorDirPath = null;
    
    /**************************************************************************
     * A boolean flag indicating if new bill files will be moved to a provided
     * destination directory. If set to FALSE then new files will be left
     * in the source directory. Database will be updated to record the 
     * information for the new files.
     */
    private boolean mMove = true;
    
    /**************************************************************************
     * A boolean flag indicating if duplicate files (files with the same name)
     * will be moved and considered for loading by the next batch job.
     */
    private boolean mAllowDuplicateFilename = false;
    
    /**************************************************************************
     * A String indicating how files should be moved to the destination
     * directory. Possible values are: COPY_AND_DELETE or RENAME.
     */
    private String mMoveMethod = null;
    
    /**************************************************************************
     * Status the bill file will be set to, when the job finishes successfully.
     */
    private FileStatus mSuccessStatus = null;
    
    /**************************************************************************
     * Status the bill file will be set to, if the job fails.
     */
    private FileStatus mFailStatus = null;
    
    /**************************************************************************
     * Status a new bill file will be set to before the job processes it.
     */
    private FileStatus mNewStatus = null;
    
    
    /**************************************************************************
     * Status a duplicate file will be set to if duplicate files are not 
     * allowed.
     */
    private FileStatus mErrorStatus = FileStatus.ERROR;

    /**************************************************************************
     * Dao class for querying and updating the bill file meta data table.
     */
    private IFileMetaDataDao mFileMetaDataDao = null;
    
    /**************************************************************************
     * @return mDestinationDirPath
     */
    public String getDestinationDirPath() {
        return mDestinationDirPath;
    }

    /**************************************************************************
     * @param szDestinationDirPath  mDestinationDirPath is set to this path
     */
    public void setDestinationDirPath(final String szDestinationDirPath) {
        mDestinationDirPath = szDestinationDirPath;
    }

    
    /**************************************************************************
     * Should files be moved?
     * 
     * @return mMove            true if files should be moved to a new dir
     */
    public boolean isMove() {
        return mMove;
    }

    /**************************************************************************
     * @param bMove            mMove will be set to it 
     */
    public void setMove(final boolean bMove) {
        mMove = bMove;
    }

    
    /**************************************************************************
     * @return mStatusSuccess
     */
    public String getMoveMethod() {
        return mMoveMethod;
    }

    /**************************************************************************
     * @param szMoveMethod          mMoveMethod will be set to it
     */
    public void setMoveMethod(final String szMoveMethod) {
        mMoveMethod = szMoveMethod;
    }

    /**************************************************************************
     * @return mStatusSuccess
     */
    public FileStatus getSuccessStatus() {
        return mSuccessStatus;
    }

    /**************************************************************************
     * Sets the value which will indicate what status a file should be set to
     * if successfully processed.
     * 
     * @param szSuccessStatus       mSuccessStatus will be set to it
     */
    public void setSuccessStatus(final FileStatus szSuccessStatus) {
        mSuccessStatus = szSuccessStatus;
    }

    /**************************************************************************
     * @return mFailStatus          mFailStatus will be set to it
     */
    public FileStatus getFailStatus() {
        return mFailStatus;
    }

    /**************************************************************************
     * @param szFailStatus        mFailStatus will be set to it
     */
    public void setFailStatus(final FileStatus szFailStatus) {
        mFailStatus = szFailStatus;
    }

    /**************************************************************************
     * @return mNewStatus
     */
    public FileStatus getNewStatus() {
        return mNewStatus;
    }

    /**************************************************************************
     * @param szNewStatus               mNewStatus will be set to it.
     */
    public void setNewStatus(final FileStatus szNewStatus) {
        mNewStatus = szNewStatus;
    }

    /**************************************************************************
     * @return mBillFileMetaDataDao
     */
    public IFileMetaDataDao getFileMetaDataDao() {
        return mFileMetaDataDao;
    }

    /**************************************************************************
     * @param cBillFileMetaDataDao          the billFileMetaDataDao to set
     */
    public void setFileMetaDataDao(
        final IFileMetaDataDao cBillFileMetaDataDao) {
        mFileMetaDataDao = cBillFileMetaDataDao;
    }

    
    /**************************************************************************
     * Checks if the properties for this class are correctly initialized
     * 
     * (non-Javadoc).
     * @see org.springframework.beans.factory.InitializingBean#
     * afterPropertiesSet()
     * 
     * @throws Exception    if properties set to illegal values.           
     */
    public void afterPropertiesSet() throws Exception {
        
        Assert.notNull(mDestinationDirPath, 
            "destination dir path for transferring "
            + "newly received files is not initialized.");
        
        /*
         * Validate that the destination directory exists and it has correct
         * write permissions.
         */ 
        validateDestinationDir(new File(mDestinationDirPath));
        
        // If files have to be moved, then the move method has to be defined
        if(mMove) {
            Assert.notNull(mMoveMethod, 
                "method type how to move newly received "
                + "files is not initialized.");
        }        
        
        Assert.notNull(mSuccessStatus, "status a bill file will be set to "
            + "when the job finishes successfully is not initialzied.");
        
        Assert.notNull(mFailStatus, "status a bill file will be set to "
            + "if the job fails is not initialzied.");
        
        Assert.notNull(mNewStatus, "status a new bill file will be set to "
            + "before the job processes it is not initialzied.");
        
        Assert.notNull(mFileMetaDataDao, "BillFileMetaDataDao instance " 
            + "used for querying the database is not initialized.");
        
        // if duplicate files not allowed
        if(!mAllowDuplicateFilename) {
            Assert.notNull(
                mErrorDirPath, 
                    "Duplicate files are not allowed so error directory "
                        + " cannot be null.");
            /*
             * Validate that the error directory exists and it has correct
             * write permissions.
             */ 
            validateDestinationDir(new File(mErrorDirPath));
        }

    }
    
    /**************************************************************************
     * For every file in <code>cFilesList</code> if
     * 1) move = true a new entry is inserted in the database and the file
     *    is moved to a new directory <code>mDestinationDirPath</code>.
     * 2) move = false a new entry is inserted in the database for this file
     *    if entries for files with such a name do not already exist.
     *    
     * If duplicate entries for files with such entries already exist in the
     * database, log a warning message.
     *  
     * (non-Javadoc)
     * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
     * 
     * @param cFilesList        list of Files to be processed by the write 
     *                          method
     * 
     * @throws Exception        if anything goes wrong
     *                           
     */
    public void write(final List< ? extends File> cFilesList) 
    throws Exception {
        
        if (cFilesList != null) {         
            for (File e : cFilesList) {
                final long lBillFileId = mFileMetaDataDao.nextId();
                
                /**************************************************************
                 * If mMove = true, then new bill files should be 
                 * moved to a new destination directory. 
                 */
                final boolean bIsDuplicateFile = logDuplicateFiles(e.getName());
                
                if(!isAllowDuplicateFilename() && bIsDuplicateFile) {
                    if (mLog.isDebugEnabled()) {
                        mLog.debug("File: " + e.getName() 
                            + " is a duplicate file and duplicate files are " 
                                + "not allowed. File moved to an error dir.");
                    }
                    processDuplicateFile(e,Long.valueOf(lBillFileId));
                } else {
                    if (mMove) {                    
                        mFileMetaDataDao.insertNewFile(
                            Long.valueOf(lBillFileId), 
                                e.getName(), 
                                    mDestinationDirPath, 
                                        mNewStatus
                                            );

                        final boolean bIsMoveSuccessFull = 
                            moveFile(e,mDestinationDirPath);
                        
                        if (bIsMoveSuccessFull) {
                            
                            if (mLog.isDebugEnabled()) {
                                mLog.debug("File: " + e.getName() 
                                    + " was successfully moved to new directory :" 
                                        + mDestinationDirPath
                                            + " using method: " + mMoveMethod);
                            }
                            
                            //update status to mSuccessStatus
                            mFileMetaDataDao.updateFileStatus(
                              lBillFileId, mSuccessStatus);
                        } else {
                            
                            if (mLog.isDebugEnabled()) {
                                mLog.debug("File: " + e.getName() 
                                    + " could not be moved to new directory :" 
                                        + mDestinationDirPath
                                            + " using method: " + mMoveMethod);
                            }
                            //update status to mFailStatus
                            mFileMetaDataDao.updateFileStatus(
                                lBillFileId, mFailStatus);
                        }
                        
                    } else {
                        /*
                         * New bill file should not be moved to new dest directory. 
                         * Update the database for this file only if a file with 
                         * such a name has not been processed yet.
                         */                    
                        if(!bIsDuplicateFile) {
                            mFileMetaDataDao.insertNewFile(
                                Long.valueOf(lBillFileId), 
                                    e.getName(), 
                                        e.getParent(), 
                                            mSuccessStatus
                                                );
                        } 
                    } 
                }
                
                //
            }                
        }
    }
    
                            
    /**************************************************************************
     * Checks if files with the same name have not been already 
     * received and processed by saas system. If duplicate files
     * exist log in a warning message and return true, else
     * return false and do not log in any messages. 
     *
     * @param szFileName        name of most recently received file
     * 
     * @return boolean          true if a file with szFileName already exists 
     *                          in the bill_file_meta_data table
     */
    private boolean logDuplicateFiles(
        final String       szFileName
    ) {
        final List< ? extends IFileMetaData> cDuplicateFilesList = 
            mFileMetaDataDao.findDuplicateFilesByFileName(szFileName);
        
        final StringBuffer cFileTransferDates = new StringBuffer();
        final SimpleDateFormat cDateFormatter = 
            new SimpleDateFormat("yyyyMMdd");
        
        for (IFileMetaData e : cDuplicateFilesList) {
            cFileTransferDates
                .append(cDateFormatter.format(e.getCreatedDate()))
                .append("  ");
        }
        
        final boolean bIsDuplicateFile = cDuplicateFilesList.size() > 0;
        
        if (bIsDuplicateFile) {
            mLog.warn("A file(s) with the same name was transferred "
                + "on these dates: " + cFileTransferDates.toString() + ". "
                + "If this is being done intentionally, "
                + "then ignore this warning. However, if the file "
                + "got transferred over multiple times in error, "
                + "then please take appropriaate actions. "
                + "If the job is configured to ALLOW the processing "
                + "of duplicate files, then take appropriaate action "  
                + "on the duplicate file by rejecting one of the files. "
                + "If the job is configured to DISALLOW the processing "
                + "of duplicate files no actions need to be taken. "
                + "The duplicate file will be moved to an error directory "
                + "and its status in the database will be set to: "
                + getErrorStatus());
        }
        
        return bIsDuplicateFile;
    }

    
    /**************************************************************************
     * The method moves a bill file to a destination directory by either
     * renaming the file or copying and deleting the file. 
     * <code>mMoveMethod</code> determines the way a file will be moved.
     * If <code>mMoveMethod</code> has values other than 'rename' or
     * 'copy_and_delete' an Exception: move method can't be recognized.
     * 
     * @param cInputFile            file to be moved
     * @param szDestinationDir      directory path to which file will be moved
     * 
     * @return bIsMoveSuccessFull   true if file was successfully moved
     * 
     * @throws Exception            thrown if move method not recognized
     */
    private boolean moveFile(
        final File      cInputFile,
        final String    szDestinationDir)   
    throws Exception 
    {
        
        boolean bIsMoveSuccessFull = false;
        
        final String szDestinationFileName =
            szDestinationDir + File.separator
            + cInputFile.getName();
        
        if (mMoveMethod.equalsIgnoreCase("rename")) {
             bIsMoveSuccessFull = 
                 cInputFile.renameTo(new File(szDestinationFileName));
        } else if (mMoveMethod.equalsIgnoreCase("copy_and_delete")) {
            
            try {
                
                FileCopyUtils.copy(cInputFile, new File(szDestinationFileName));
               
                if (cInputFile.delete()) {
                    if (mLog.isDebugEnabled()) {
                       mLog.debug("File: " + cInputFile.getName()
                           + " successfully deleted after moving.");
                   }
                } else {
                    if (mLog.isDebugEnabled()) {
                        mLog.debug("File: " + cInputFile.getName()
                            + " could not be deleted after moving.");
                    }

                }
                bIsMoveSuccessFull = true;
           
           } catch (IOException cIOException) {
               
               mLog.error("Exception occurred while trying to copy "
                   + " the bill file: " + cInputFile.getName() 
                   + " to the destination directory: " 
                   + mDestinationDirPath
               );
               bIsMoveSuccessFull = false;
           }   
        } else {
            // create a new exception type
            throw new Exception("Illegal move method specified");
        }
        
        return bIsMoveSuccessFull;
    }
    
    /**************************************************************************
     * Checks if the destination directory exists. If it does not, it 
     * tries to create. If directory could not be created, an Exception is 
     * thrown. 
     * If directory exists, write permissions are checked. If the directory
     * cannot be writable, an attempt is made to grant such a write permission.
     * If write permission cannot be granted, an Exception is thrown.
     * This method has to be called before attempts to write to the 
     * destination directory. 
     * 
     * @param cDestinationDir           destination directory
     * 
     * @throws FileNotFoundException    destination directory does not exist
     *                                  and it cannot be created
     */
    private void validateDestinationDir(final File cDestinationDir) 
    throws  FileNotFoundException {
        
        if (!cDestinationDir.exists()) {
            if (!cDestinationDir.mkdirs()) {
                throw new FileNotFoundException(
                    "Destination directory does not exits and it could not be "
                    + "created");
            } else {
                if (mLog.isDebugEnabled()) {
                    mLog.debug("Destination directory successfully created.");
                }
            }
        }
        if (!cDestinationDir.canWrite()) {
            if (!cDestinationDir.setWritable(true)) {
                throw new SecurityException(
                    "Destination directory does not have write persmissions "
                    + "and such persmissions cannot be set");
            } else {
                if (mLog.isDebugEnabled()) {
                    mLog.debug(
                        "Destination directory successfully granted "
                        + "write permissions.");
                }
            }
          
        } else {
            if (mLog.isDebugEnabled()) {
                mLog.debug("Destination directory "
                    + "has sufficient security permissions.");
            }
        }
    }
    
    

    /**************************************************************************
     * Returns the boolean flag indicating if duplicate files will be allowed
     * for loading by the system.
     * 
     * @return mAllowDuplicateFilename
     */
    public boolean isAllowDuplicateFilename() {
        return mAllowDuplicateFilename;
    }

    
    /**************************************************************************
     * Sets the boolean flag indicating if duplicate files will be allowed
     * for loading by the system.
     * 
     * @param cAllowDuplicateFilename
     */
    public void setAllowDuplicateFilename(
        final boolean   cAllowDuplicateFilename)
    {
        mAllowDuplicateFilename = cAllowDuplicateFilename;
    }
    
    
    /**************************************************************************
     * Inserts a new record in the file meta dao table for <code>cFile</code>
     * with an error status and moves the duplicate file <code>cFile</code>
     * to an error directory. If moving fails file is left in the source
     * directory.
     * 
     * @param cFile             duplicate file
     * @param cBillFileId       new file id for the file meta dao table
     */
    protected void processDuplicateFile(
        final File      cFile,
        final Long      cBillFileId)
    throws Exception
    {
        /*
         * Insert the file in the file meta dao table with
         * an error status
         */
        mFileMetaDataDao.insertNewFile(
            cBillFileId,
                cFile.getName(),                
                    getErrorDirPath(), 
                        mErrorStatus
                            );
        /*
         * Move the file to an error directory.
         */
        
        final boolean bIsMoveSuccessFull = 
            moveFile(cFile, getErrorDirPath());
        
        if (bIsMoveSuccessFull) {            
            if (mLog.isDebugEnabled()) {
                mLog.debug("File: " + cFile.getName() 
                    + " was successfully moved to new directory :" 
                        + getErrorDirPath()
                            + " using method: " + mMoveMethod);
            }            
        } else {            
            if (mLog.isDebugEnabled()) {
                mLog.debug("File: " + cFile.getName() 
                    + " could not be moved to new directory :" 
                        + getErrorDirPath()
                            + " using method: " + mMoveMethod + ". "
                                + "File will be left in the same input dir.");
            }
        }
    }

    
    
    /**************************************************************************
     * Returns the status a duplicate file will be set to if duplicated files
     * are not allowed.
     * 
     * @return  mErrorStatus
     */
    public FileStatus getErrorStatus() {
        return mErrorStatus;
    }

    
    /**************************************************************************
     * Sets the error status used for duplicated files, if duplicate
     * files are not allowed.
     * 
     * @param cErrorStatus
     */
    public void setErrorStatus( final FileStatus cErrorStatus) {    
        mErrorStatus = cErrorStatus;
    }

    
    /**************************************************************************
     * Returns the error directory to which a duplicate file will be moved
     * if duplicate files are nor allowed.
     * 
     * @return mErrorDirPath
     */
    public String getErrorDirPath() {
        return mErrorDirPath;
    }

    
    /**************************************************************************
     * Sets the error directory to which a duplicate file will be moved
     * if duplicate files are nor allowed.
     * 
     * @param cErrorDirPath
     */
    public void setErrorDirPath(final String cErrorDirPath) {
        mErrorDirPath = cErrorDirPath;
    }
    
 
}
