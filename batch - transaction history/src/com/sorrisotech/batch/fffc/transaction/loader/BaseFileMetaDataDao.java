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

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sorrisotech.batch.fffc.transaction.loader.Constants.FileStatus;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaData;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaDataDao;
import com.sorrisotech.batch.fffc.transaction.loader.exception.NoFileAvailableException;


/******************************************************************************
 * Generic class to access and update the File_Meta_Data table.
 *
 * @author Sanket
 */
public class BaseFileMetaDataDao implements IFileMetaDataDao {

    /**************************************************************************
     *  Developer level logging.
     */
    private static final Logger mLog = LoggerFactory.getLogger(BaseFileMetaDataDao.class);

    /**************************************************************************
     * Jdbc template to query and update the database.
     */
    private NamedParameterJdbcTemplate mJdbcTemplate;

    /**************************************************************************
     * Map containing the queries used by the DbScanner class.
     */
    private Map<String, String> mQueryMap;

    /**************************************************************************
     * List of query names from the mQueryMap that will be executed in a
     * transaction.
     */
    private List<String> mExecuteInTxList;

    /**************************************************************************
     * Mapper to map the resultset rows to the IFileMetaData object.
     */
    private RowMapper<IFileMetaData> mRowMapper;

    /**************************************************************************
     * Query parameters for the executeInTx queries.
     */
    private MapSqlParameterSource mExecuteInTxQueryParams;

    /**************************************************************************
     * Get the next available file from the file meta data table with
     * current status matching the current status in the argument. The method
     * returns the most recent or the oldest file matching the criteria
     * based on the most recent parameter. Before returning the file id, the
     * current status of the file is updated to the new status. The method also
     * obtains a lock on a row on the lock table to avoid any concurrency issues
     * when multiple instances of this job are run in parallel.
     *
     * @param cCurrentStatus        Status of the file to search a file
     * @param cNewStatus            New status to set the file to
     * @param bMostRecent           Boolean indicating if most recent or the
     *                              oldest file matching the criteria should be
     *                              returned.
     * @return       Id of the file matching the given criteria.
     *
     * @throws NoFileAvailableException     Thrown when no files matching the
     *                                      specified criteria was found.
     */
    public IFileMetaData getNextAvailableFile(
        final FileStatus            cCurrentStatus,
        final FileStatus            cNewStatus,
        final boolean               bMostRecent,
        final DbScannerParamsBean   cParamsBean
        ) throws NoFileAvailableException {

        IFileMetaData cFileMetaData;

        //----------------------------------------------------------------------
        // Get a lock on a table row to prevent 2 instances of a
        // job from obtaining the same file for processing.
        mJdbcTemplate.update(
            mQueryMap.get("lockTableRow"),
            new HashMap<String, String>());

        final MapSqlParameterSource cParams = new MapSqlParameterSource();
        cParams.addValue("current_status", cCurrentStatus, Types.VARCHAR);
        String szFilenamePattern = null;
        if (cParamsBean != null) {
            szFilenamePattern = cParamsBean.getFilenamePattern();
        }
        if (szFilenamePattern != null) {
            cParams.addValue("filename_pattern", szFilenamePattern, Types.VARCHAR);
        }
        //----------------------------------------------------------------------
        //Get the file id for the next file to process
        if (bMostRecent) {
            if (mLog.isDebugEnabled()) {
                mLog.debug("querying for latestAvailableFile");
            }
            if (szFilenamePattern != null) {
                cFileMetaData = query(
                                mQueryMap.get("getLatestAvailableFileFiltered"),
                                cParams);            }
            else {
                cFileMetaData = query(
                mQueryMap.get("getLatestAvailableFile"),
                cParams);
            }
        } else {
            if (mLog.isDebugEnabled()) {
                mLog.debug("querying for OldestAvailableFile");
            }
            if (szFilenamePattern != null) {
                cFileMetaData = query(
                                mQueryMap.get("getOldestAvailableFileFiltered"),
                               cParams);
            }
            else {
                cFileMetaData = query(
                 mQueryMap.get("getOldestAvailableFile"),
                cParams);
            }
        }

        //----------------------------------------------------------------------
        //Update the status of the file to new status
        updateFileStatus(cFileMetaData.getFileId(), cNewStatus);

        //----------------------------------------------------------------------
        //the lock will be released when the enclosing tx is committed.
        if (mLog.isDebugEnabled()) {
            mLog.debug("Returning file: " + cFileMetaData);
        }
        return cFileMetaData;
    }

    public List<IFileMetaData> getAvailableFileList(
        final FileStatus cCurrentStatus,
        final DbScannerParamsBean cParamsBean) throws NoFileAvailableException
    {

        List<IFileMetaData> cFileMetaDataList;

        // ----------------------------------------------------------------------
        // Get a lock on a table row to prevent 2 instances of a
        // job from obtaining the same file for processing.
        mJdbcTemplate.update(mQueryMap.get("lockTableRow"), new HashMap<String, String>());

        final MapSqlParameterSource cParams = new MapSqlParameterSource();
        cParams.addValue("current_status", cCurrentStatus, Types.VARCHAR);
        String szFilenamePattern = null;
        if (cParamsBean != null) {
            szFilenamePattern = cParamsBean.getFilenamePattern();
        }
        if (szFilenamePattern != null) {
            cParams.addValue("filename_pattern", szFilenamePattern, Types.VARCHAR);
        }
        // ----------------------------------------------------------------------
        // Get the file id for the next file to process
        if (mLog.isDebugEnabled()) {
            mLog.debug("querying for AvailableFileList");
        }
        if (szFilenamePattern != null) {
            mLog.debug("sql: \n" + mQueryMap.get("getAvailableFileListFiltered"));
            mLog.debug(cParams.getValues().toString());
            cFileMetaDataList =
                this.getJdbcTemplate().query(mQueryMap.get("getAvailableFileListFiltered"), cParams, mRowMapper);
        } else {
            cFileMetaDataList =
                this.getJdbcTemplate().query(mQueryMap.get("getAvailableFileList"),cParams, mRowMapper);
        }

        // ----------------------------------------------------------------------
        // the lock will be released when the enclosing tx is committed.
        if (mLog.isDebugEnabled()) {
            mLog.debug("Returning " + cFileMetaDataList.size() + " files.");
        }
        if (cFileMetaDataList.size() == 0) {
            throw new NoFileAvailableException("No files available");
        }
        return cFileMetaDataList;
    }


    /**************************************************************************
     * Update the status of the file with given fileId to new status.
     *
     * @param lFileId       File id
     * @param cNewStatus    New status to set to.
     */
    public void updateFileStatus(
        final long          lFileId,
        final FileStatus    cNewStatus) {

        if (mLog.isDebugEnabled()) {
            mLog.debug("Updating status of file_id: " + lFileId
                + " to: " + cNewStatus);
        }
        final MapSqlParameterSource cUpdateParams = new MapSqlParameterSource();
        cUpdateParams.addValue(
            "new_status",
            cNewStatus,
            Types.VARCHAR);
        cUpdateParams.addValue(
            "file_id",
            Long.valueOf(lFileId),
            Types.BIGINT);

        final String szSql = mQueryMap.get("updateFileStatus");

        mJdbcTemplate.update(
            szSql,
            cUpdateParams);
                
        if (mLog.isDebugEnabled()) {
            mLog.debug("Updated status of file file_id: " + lFileId
                + " to: " + cNewStatus);
        }
    }
    
    /**************************************************************************
     * Update the bill_stream of the file with given fileId to new bill_stream.
     *
     * @param lFileId       File id
     * @param szBillStream  The type of bills in this file (b2b, b2c, electric).
     * @param szPayGroup    What payment driver to use to pay this bills.
     */
    public void updateFileBillStream(
        final long      lFileId,
        final String    szBillStream,
        final String    szPayGroup) {

        if (mLog.isDebugEnabled()) {
            mLog.debug("updateFileBillStream(): Updating bill_stream of the file_id: " + lFileId
                + " to: " + szBillStream);
        }
        final MapSqlParameterSource cUpdateParams = new MapSqlParameterSource();
        cUpdateParams.addValue(
            "bill_stream",
            szBillStream,
            Types.VARCHAR);
        cUpdateParams.addValue(
                "pay_group",
                szPayGroup,
                Types.VARCHAR);
        cUpdateParams.addValue(
            "file_id",
            Long.valueOf(lFileId),
            Types.BIGINT);

        int iCount = 
        	mJdbcTemplate.update(
            mQueryMap.get("updateFileBillStream"),
            cUpdateParams);

        if (mLog.isDebugEnabled()) {
            mLog.debug("updateFileBillStream(): Updated bill_stream of the file file_id: " + lFileId
                + " to: " + szBillStream + ", update count: " + iCount);
        }
    }

    /*************************************************************************
     * Auto updating the bill files status.
     *
     * @param arg0 List<? extends IFileMetaData>
     */

    public void updateBillFiles(final List<? extends IFileMetaData> arg0) {

        if (mLog.isDebugEnabled()) {
            mLog.debug("Updating bill files : " + arg0);
        }
        for (IFileMetaData elem : arg0) {
            final MapSqlParameterSource cUpdateParams = new MapSqlParameterSource();
            cUpdateParams.addValue(
            "file_id",
            elem.getFileId(),
            Types.BIGINT);
            cUpdateParams.addValue(
            "new_status,",
            elem.getStatus(),
            Types.VARCHAR);

            mJdbcTemplate.update(getQueryMap().get("updateFileStatus"), cUpdateParams);
            if (mLog.isDebugEnabled()) {
            mLog.debug("Updated status of file with file_id: " + elem.getFileId());
        }
        }
    }
    /**************************************************************************
     * Execute the query with the given parameters.
     *
     * @param cSql          Query to execute
     * @param cParamValues  Parameters to substitute in the query
     *
     * @return      An instance of {@link IFileMetaData}
     *
     * @throws NoFileAvailableException     Thrown when no files found for the
     *                                      given status.
     */
    public IFileMetaData query(
        final String                    cSql,
        final MapSqlParameterSource     cParamValues
    ) throws NoFileAvailableException {

        mLog.debug("sql: \n" + cSql);
        mLog.debug(cParamValues.getValues().toString());
        final List <IFileMetaData> cFileList =
            this.getJdbcTemplate().query(
                cSql,
                cParamValues,
                mRowMapper);

        if (cFileList.size() > 0) {
            return cFileList.remove(0);
        } else {
            throw new NoFileAvailableException("No files available");
        }
    }

    /**************************************************************************
     * Get the JdbcTemplate.
     *
     * @return the cJdbcTemplate    The JdbcTemplate.
     */
    public NamedParameterJdbcTemplate getJdbcTemplate() {
        return mJdbcTemplate;
    }

    /**************************************************************************
     * Injection setter for the JdbcTemplate.
     *
     * @param cJdbcTemplate the cJdbcTemplate to set
     */
    public void setJdbcTemplate(final NamedParameterJdbcTemplate cJdbcTemplate) {
        mJdbcTemplate = cJdbcTemplate;
    }

    /**************************************************************************
     * Get a map of sql queries used by this object.
     *
     * @return the queryMap     A map of sql queries key'ed by query name.
     */
    public Map<String, String> getQueryMap() {
        return mQueryMap;
    }

    /**************************************************************************
     * Set map of sql queries used by this class.
     *
     * @param cMap      A map of sql queries key'ed by query name.
     */
    public void setQueryMap(final Map<String, String> cMap) {
        mQueryMap = cMap;
    }


    /**************************************************************************
     * Inserts a new file entry in the file_meta_data table.
     *
     * @param cFileId           file id to uniquely id the file
     * @param szFileName        File name
     * @param szFilePath        Path to file
     * @param cNewStatus        Status to set to.
     */
    public void insertNewFile(
        final Long          cFileId,
        final String        szFileName,
        final String        szFilePath,
        final FileStatus    cNewStatus
        ) 
    {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Inserting new entry " + cFileId + " for a file name: " + szFileName
                + " located in: " + szFilePath + " with a status: "
                + cNewStatus);
        }
        final MapSqlParameterSource cUpdateParams = new MapSqlParameterSource();
        cUpdateParams.addValue(
            "new_file_id",
            cFileId,
            Types.BIGINT);
        cUpdateParams.addValue(
            "new_status",
            cNewStatus,
            Types.VARCHAR);
        cUpdateParams.addValue(
            "new_file_name",
            szFileName,
            Types.VARCHAR);
        cUpdateParams.addValue(
            "new_file_path",
            szFilePath,
            Types.VARCHAR);

        getJdbcTemplate().update(
            getQueryMap().get("insertNewFile"),
            cUpdateParams);

        if (mLog.isDebugEnabled()) {
            mLog.debug("Inserted new entry for a file name: " + szFileName
                + " located in: " + szFilePath + " with a status: "
                + cNewStatus);
        }
    }

    /*************************************************************************
     * Get the BillFile for Auto Updatation.
     *
     * @param  iDaysAutoPublish number of days for auto publish
     * @param  iDaysAutoAccept number of days for auto accept
     * @return IFileMetaData
     */

    public IFileMetaData getBillFileToUpdate(
        final int        iDaysAutoPublish,
        final int        iDaysAutoAccept) {

        if (mLog.isDebugEnabled()) {
            mLog.debug("getting load the bill files to be Auto Update");
        }
        IFileMetaData cRetValue = null;
        try {
        final MapSqlParameterSource cUpdateParams = new MapSqlParameterSource();
        cUpdateParams.addValue(
            "days_auto_accept",
            iDaysAutoAccept,
            Types.INTEGER);
        cUpdateParams.addValue(
            "days_auto_publish",
            iDaysAutoPublish,
            Types.INTEGER);

          cRetValue = query(getQueryMap().get("getBillFileToUpdate"), cUpdateParams);
        } catch (NoFileAvailableException cEx) {
          mLog.warn("noFile Available to update" + cEx, cEx);
        }
        return cRetValue;
    }

    /**************************************************************************
     * Looks for a duplicate entries for a file based on the file name.
     *
     * @param szFileName    search for duplicate entries for this filename
     *
     * @return List         List of duplicate files
     */
    public List< ? extends IFileMetaData> findDuplicateFilesByFileName(
        final String        szFileName
    ) {

        if (mLog.isDebugEnabled()) {
            mLog.debug("Looking for duplicate file entries for file name "
                + szFileName);
        }
        final MapSqlParameterSource cQueryParams = new MapSqlParameterSource();
        cQueryParams.addValue(
            "new_file_name",
            szFileName,
            Types.VARCHAR);

        final List <IFileMetaData> cFileList =  this.getJdbcTemplate().query(
            getQueryMap().get("findDuplicateFiles"),
            cQueryParams,
            mRowMapper);

        if (mLog.isDebugEnabled()) {
            mLog.debug("Looking for duplicate file entries for file name "
                + szFileName);
        }

        return cFileList;
    }

    /**************************************************************************
     * Getter for rowMapper.
     *
     * @return the rowMapper
     */
    public RowMapper<IFileMetaData> getRowMapper() {
        return mRowMapper;
    }

    /**************************************************************************
     * Setter for rowMapper.
     *
     * @param cRowMapper the rowMapper to set
     */
    public void setRowMapper(
        final RowMapper<IFileMetaData> cRowMapper
    ) {
        mRowMapper = cRowMapper;
    }

    /**************************************************************************
     * Execute the list of sqls in a transaction. Before executing this method,
     * set the parameters by calling the {@link setQueryParamsForExecuteInTx}
     * method.
     */
    public void executeInTx() {
        for (String cSqlName : mExecuteInTxList) {
            final String cSql = getQueryMap().get(cSqlName);
            if (mLog.isDebugEnabled()) {
                mLog.debug("Executing query: " + cSqlName);
            }
            if (cSql != null) {
                this.getJdbcTemplate().update(
                    cSql,
                    mExecuteInTxQueryParams);
            } else {
                mLog.error(cSqlName + " Query not found in the queryMap. "
                    + "Please check the name of the query and/or add the "
                    + "missing query to the queryMap.");
            }
        }
    }

    
    /**************************************************************************
     * Returns the next ID to use.
     * 
     * @return  The next ID to use.
     */
    @Override
    public long nextId() 
    {
        final Random                  cRand   = new Random();
        final String                  szSql   = mQueryMap.get("uniqueId");
        final HashMap<String, Object> cParams = new HashMap<String, Object>();

        long lId = 0;
        
        while(lId == 0)
        {
            lId = cRand.nextLong();
            
            if (lId < 0) lId = -1 * lId;
            
            cParams.put("id", lId);
            
            final int iCount = mJdbcTemplate.queryForObject(szSql, cParams, Integer.class);
            
            if (iCount > 0)
            {
                lId = 0;
            }
        }
        
        return(lId);
    }

    /**************************************************************************
     * Add params for all the sql that will be executed in a tx.
     *
     * @param cQueryParams  Map of sql query parameters, key'ed by the parameter
     *                      name in the sql.
     */
    public void setQueryParamsForExecuteInTx(
        final MapSqlParameterSource cQueryParams
    ) {
       mExecuteInTxQueryParams = cQueryParams;
    }

    /**************************************************************************
     * Getter for executeInTxList.
     *
     * @return the executeInTxList
     */
    public List<String> getExecuteInTxList() {
        return mExecuteInTxList;
    }

    /**************************************************************************
     * Setter for executeInTxList.
     *
     * @param cExecuteInTxList the executeInTxList to set
     */
    public void setExecuteInTxList(final List<String> cExecuteInTxList) {
        mExecuteInTxList = cExecuteInTxList;
    }
}
