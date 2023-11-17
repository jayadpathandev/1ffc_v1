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
package com.sorrisotech.batch.fffc.transaction.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.sorrisotech.batch.fffc.transaction.loader.Constants.FileStatus;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaData;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaDataDao;
import com.sorrisotech.batch.fffc.transaction.loader.api.IScanner;
import com.sorrisotech.batch.fffc.transaction.loader.exception.NoFileAvailableException;



/******************************************************************************
 * Implementation of the IScanner interface that scans a database table to get
 * the next available file for processing. This class scans a database table
 * specified by the {@link BaseFileMetaDataDao} querying for files with
 * a specified status (current file status).
 *
 * This class also implements the {@link Tasklet} interface, which enables it to
 * be configured as a {@link Step} of the job.
 *
 * <pre>
 * Example spring configuration for registering and using this scanner as a step
 *
 *    <!-- configure the bean for the scanner -->
 *    <bean id="fileScanner" class="com.sorrisotech.saas.batch.common.DbScanner">
 *        <property name="currentStatus" value="RECEIVED"/>
 *        <property name="newStatus" value="LOADING"/>
 *        <property name="fileMetaDataDao" ref="pmtFileMetaDataDao"/>
 *    </bean>
 *
 *    <!-- configure a TaskletStep to use the scanner tasklet -->
 *    <bean id="scannerStep" parent="taskletStep">
 *        <property name="tasklet" ref="fileScanner"/>
 *         <property name="stepExecutionListeners">
 *            <list>
 *                 <ref bean="fileScanner"/>
 *            </list>
 *        </property>
 *    </bean>
 *
 *    <!-- configure the TaskletStep as part of the job -->
 *    <bean id="someJob" parent="simpleJob">
 *        <property name="name" value="someJob" />
 *        <property name="steps">
 *            <list>
 *                <ref bean="scannerStep"/>
 *                <ref bean="processorStep"/>
 *            </list>
 *        </property>
 *    </bean>
 *
 * </pre>
 *
 * @author Sanket
 *
 */
public class DbScanner implements
    IScanner,
    InitializingBean,
    StepExecutionListener,
    Tasklet {

    /**************************************************************************
     * Development level logging.
     */
    private static final Logger mLog = LoggerFactory.getLogger(DbScanner.class);

    /**************************************************************************
     * Files with this status will be returned by the scanner.
     */
    protected FileStatus mCurrentStatus;

    /**************************************************************************
     * The current status of the file will be updated with the new status
     * when the scanner returns the file from getNextAvailableFile method.
     */
    protected FileStatus mNewStatus;

    /**************************************************************************
     * Boolean indicating if the most recent or the oldest files should be
     * returned.
     */
    protected boolean mMostRecent = true;

    /**************************************************************************
     * A boolean indicating if the failed files should be processed before
     * the unprocessed files.
     */
    protected boolean mProcessFailedFilesFirst = false;

    /**************************************************************************
     * Dao class for querying and updating the bill file meta data table.
     */
    protected IFileMetaDataDao mFileMetaDataDao;

    /**************************************************************************
     * Boolean indicating if the scanner should return failed files if no
     * un-processed files are available.
     */
    protected boolean mProcessFailedFiles = false;

    /**************************************************************************
     * The bill file id, name and path will be stored in the job execution path.
     */
    protected ExecutionContext mJobExecContext;


    /**************************************************************************
     * Bean class for passing any additional parameters to the
     * DbScanner.
     */
    protected DbScannerParamsBean mDbScannerParams;


    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IScanner#getNextAvailableFile
     * (com.sorrisotech.saas.batch.common.Constants.FileStatus, FileStatus, boolean)
     */
    public IFileMetaData getNextFile(
        final FileStatus            cCurrentStatus,
        final FileStatus            cNewStatus,
        final boolean               bMostRecent,
        final boolean               bProcessFailedFilesFirst,
        final boolean               bProcessFailedFiles,
        final DbScannerParamsBean   cParamsBean
    ) throws NoFileAvailableException {

        try {

            final IFileMetaData cBillFileMetaData;
            //----------------------------------------------------------------------
            // Get the next available file using the values passed in
            final FileStatus cStatus1, cStatus2;
            if (bProcessFailedFiles) {
                if (bProcessFailedFilesFirst) {
                    cStatus1 = Constants.FileStatus.FAILED;
                    cStatus2 = cCurrentStatus;
                } else {
                    cStatus1 = cCurrentStatus;
                    cStatus2 = Constants.FileStatus.FAILED;
                }
            } else {
                cStatus1 = cCurrentStatus;
                cStatus2 = null;
            }
            cBillFileMetaData = getNextFileInOrder(
                    cStatus1,
                    cStatus2,
                    cNewStatus,
                    bMostRecent,
                    cParamsBean);

            return cBillFileMetaData;
        } catch (NoFileAvailableException cException) {
            final StringBuffer cMessage = new StringBuffer(160);
            cMessage.append("\n\n No files found for processing with the following "
                + "search criteria \n\n currentStatus: ");
            cMessage.append(cCurrentStatus);
            cMessage.append("\nprocessFailedFiles: ");
            cMessage.append(bProcessFailedFiles);
            cMessage.append("\nprocessFailedFilesFirst: ");
            cMessage.append(bProcessFailedFilesFirst);
            cMessage.append("\nmostRecent: ");
            cMessage.append(bMostRecent);
            cMessage.append('\n');

            throw new NoFileAvailableException(cMessage.toString(), cException);
        }
    }

    /**************************************************************************
     * Helper method to get the next available file using status1. If no files
     * are found with status1 then get the next available file using status2.
     *
     * @param cStatus1      First search for files with status1
     * @param cStatus2      If no files wiht status1 then search for files with
     *                      status2
     * @param cNewStatus    Returned file will be set to this new status
     * @param bMostRecent   boolean indicating if the most recent or the oldest
     *                      file should be returned.
     *
     * @return  An instance of {@link BillFileMetaData}
     *
     * @throws NoFileAvailableException     Thrown when no files exist with
     *                                      status1 or with status2
     */
    private IFileMetaData getNextFileInOrder(
        final FileStatus            cStatus1,
        final FileStatus            cStatus2,
        final FileStatus            cNewStatus,
        final boolean               bMostRecent,
        final DbScannerParamsBean   cParamsBean
    ) throws NoFileAvailableException {

        IFileMetaData cBillFileMetaData;

        try {
            cBillFileMetaData = mFileMetaDataDao.getNextAvailableFile(
                cStatus1,
                cNewStatus,
                bMostRecent,
                cParamsBean);
        } catch (NoFileAvailableException cException) {

            //------------------------------------------------------------------
            // If no files are found, the query for files with an alternate
            // status
            if (cStatus2 != null) {
                try {
                    cBillFileMetaData = mFileMetaDataDao.getNextAvailableFile(
                        cStatus2,
                        cNewStatus,
                        bMostRecent,
                        cParamsBean);
                } catch (NoFileAvailableException cException1) {
                    throw cException1;
                }
            } else {
                throw cException;
            }
        }
        return cBillFileMetaData;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IScanner#getNextFile
     * (com.sorrisotech.saas.batch.common.Constants.FileStatus, FileStatus)
     */
    public IFileMetaData getNextFile(
        final FileStatus cCurrentStatus,
        final FileStatus cNewStatus,
        final DbScannerParamsBean     cParamsBean
    ) throws NoFileAvailableException {

        return getNextFile(
            cCurrentStatus,
            cNewStatus,
            mMostRecent,
            mProcessFailedFilesFirst,
            mProcessFailedFiles,
            cParamsBean);
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IScanner#getNextFile()
     */
    public IFileMetaData getNextFile() throws NoFileAvailableException {
        return getNextFile(
            mCurrentStatus,
            mNewStatus,
            mMostRecent,
            mProcessFailedFilesFirst,
            mProcessFailedFiles,
            mDbScannerParams);
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IScanner#getStatus()
     */
    public FileStatus getCurrentStatus() {
        return mCurrentStatus;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IScanner#setStatus
     * (com.sorrisotech.saas.batch.common.Constants.FileStatus)
     */
    public void setCurrentStatus(final FileStatus cStatus) {
        mCurrentStatus = cStatus;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IScanner#isMostRecent()
     */
    public boolean isMostRecent() {
        return mMostRecent;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IScanner#setMostRecent(boolean)
     */
    public void setMostRecent(final boolean cMostRecent) {
        mMostRecent = cMostRecent;
    }

    /**************************************************************************
     * @return the newStatus
     */
    public FileStatus getNewStatus() {
        return mNewStatus;
    }

    /**************************************************************************
     * @param cNewStatus the newStatus to set
     */
    public void setNewStatus(final FileStatus cNewStatus) {
        mNewStatus = cNewStatus;
    }

    /**************************************************************************
     * @return the FileMetaDataDao
     */
    public IFileMetaDataDao getFileMetaDataDao() {
        return mFileMetaDataDao;
    }

    /**************************************************************************
     * @param cFileMetaDataDao the FileMetaDataDao to set
     */
    public void setFileMetaDataDao(final IFileMetaDataDao cFileMetaDataDao) {
        mFileMetaDataDao = cFileMetaDataDao;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see org.springframework.beans.factory.InitializingBean#
     * afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mCurrentStatus, "currenStatus field used for searching "
            + "files is not initialized.");
        Assert.notNull(mNewStatus, "newStatus field used to update the status "
            + " of the file before returning the file id is not initialized.");
        Assert.notNull(mFileMetaDataDao, "BillFileMetaDataMapper instance used"
            + "to query the the database is not initialized.");

    }

    /**************************************************************************
     * Check if failed files need to be processed first.
     *
     * @return the processFailedFilesFirst
     */
    public boolean isProcessFailedFilesFirst() {
        return mProcessFailedFilesFirst;
    }

    /**************************************************************************
     * Set if the failed files need to be processed first.
     *
     * @param cProcessFailedFilesFirst the processFailedFilesFirst to set
     */
    public void setProcessFailedFilesFirst(final boolean cProcessFailedFilesFirst) {
        mProcessFailedFilesFirst = cProcessFailedFilesFirst;
    }

    /**************************************************************************
     * Check if failed files needs to be processed.
     *
     * @return the processFailedFiles
     */
    public boolean isProcessFailedFiles() {
        return mProcessFailedFiles;
    }

    /**************************************************************************
     * Set if failed files should be processed or not.
     *
     * @param cProcessFailedFiles the processFailedFiles to set
     */
    public void setProcessFailedFiles(final boolean cProcessFailedFiles) {
        mProcessFailedFiles = cProcessFailedFiles;
    }

    /**************************************************************************
     * (non-Javadoc)
     * @see org.springframework.batch.core.StepExecutionListener#afterStep(org.springframework.batch.core.StepExecution)
     */
    public ExitStatus afterStep(final StepExecution cStepExecution) {
        // TODO Auto-generated method stub
    	return null;
    }

    /**************************************************************************
     * (non-Javadoc)
     * @see org.springframework.batch.core.StepExecutionListener
     *      #beforeStep(org.springframework.batch.core.StepExecution)
     */
    public void beforeStep(final StepExecution cStepExecution) {
        mJobExecContext =
            cStepExecution.getJobExecution().getExecutionContext();
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see org.springframework.batch.core.step.tasklet.Tasklet
     *      #execute(org.springframework.batch.core.StepContribution,
     *               org.springframework.batch.core.scope.context.ChunkContext)
     */
    public RepeatStatus execute(
        final StepContribution  cContribution,
        final ChunkContext      cChunkContext
    ) throws Exception {

        if(mLog.isDebugEnabled()) {
            mLog.debug("entering execute: ");
        }

        try {
	        //----------------------------------------------------------------------
	        // Get the next available file for processing.
	        IFileMetaData cFileMetaData = this.getNextFile(
	                                            mCurrentStatus,
	                                            mNewStatus,
	                                            mMostRecent,
	                                            mProcessFailedFilesFirst,
	                                            mProcessFailedFiles,
	                                            mDbScannerParams);
	
	        //----------------------------------------------------------------------
	        // store the info for the next available file for processing in the step
	        // execution context. A promotionListener registered on the Step
	        // will promote the values to the Job execution context. Thus these
	        // parameters will be available for the next steps of the job.
	        cChunkContext.getStepContext().getStepExecution().getExecutionContext().put(
	            Constants.PARAM_FILE_ID,
	            cFileMetaData.getFileId());
	        cChunkContext.getStepContext().getStepExecution().getExecutionContext().put(
	            Constants.PARAM_FILE_NAME,
	            cFileMetaData.getFileName());
	        cChunkContext.getStepContext().getStepExecution().getExecutionContext().put(
	            Constants.PARAM_FILE_PATH,
	            cFileMetaData.getFilePath());
	
	        if (mLog.isDebugEnabled()) {
	            mLog.debug(Constants.PARAM_FILE_ID + ":" + cFileMetaData.getFileId());
	            mLog.debug(Constants.PARAM_FILE_NAME + ":" + cFileMetaData.getFileName());
	            mLog.debug(Constants.PARAM_FILE_PATH + ":" + cFileMetaData.getFilePath());
	        }
        } catch (NoFileAvailableException nfae) {
        	// Print no file message and set NOOP exit status
        	mLog.debug(nfae.getMessage());
        	cChunkContext.getStepContext().getStepExecution().setExitStatus(ExitStatus.NOOP);
        }

        if (mLog.isDebugEnabled()) {
            mLog.debug("returning from execute");
        }
        return RepeatStatus.FINISHED;
    }


    /**************************************************************************
     * Sets the bean which has additional parameters that need to be
     * passed to the Scanner. This is not a mandatory proeprty.
     *
     * @param cDbScannerParams
     */
    public void setDbScannerParams(
        final DbScannerParamsBean       cDbScannerParams)
    {
        mDbScannerParams = cDbScannerParams;
    }
}
