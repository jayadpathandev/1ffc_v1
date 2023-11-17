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
package com.sorrisotech.batch.fffc.transaction.loader.api;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.sorrisotech.batch.fffc.transaction.loader.DbScannerParamsBean;
import com.sorrisotech.batch.fffc.transaction.loader.Constants.FileStatus;
import com.sorrisotech.batch.fffc.transaction.loader.exception.NoFileAvailableException;



/******************************************************************************
 * Interface to manage inserts and updates to File_Meta_Data table.
 *
 * @author Sanket
 */
public interface IFileMetaDataDao {

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
    IFileMetaData getNextAvailableFile(
        final FileStatus            cCurrentStatus,
        final FileStatus            cNewStatus,
        final boolean               bMostRecent,
        final DbScannerParamsBean   cParamsBean) throws NoFileAvailableException;

    /**************************************************************************
     * Get a list of available files from the file meta data table with
     * current status matching the current status in the argument.
     *
     * @param cCurrentStatus        Status of the file to search a file
     * @param bMostRecent           Boolean indicating if most recent or the
     *                              oldest file matching the criteria should be
     *                              returned.
     * @param cParamsBean
     *
     * @return       Id of the file matching the given criteria.
     *
     * @throws NoFileAvailableException     Thrown when no files matching the
     *                                      specified criteria was found.
     */
     List<IFileMetaData> getAvailableFileList(
        final FileStatus            cCurrentStatus,
        final DbScannerParamsBean   cParamsBean) throws NoFileAvailableException;

    /**************************************************************************
     * Update the status of the file with given fileId to new status.
     *
     * @param lFileId       File id
     * @param cNewStatus    New status to set to.
     */
    void updateFileStatus(
        final long lFileId,
        final FileStatus cNewStatus);

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
    IFileMetaData query(
        final String cSql,
        final MapSqlParameterSource cParamValues)
        throws NoFileAvailableException;

    /**************************************************************************
     * Get a map of sql queries used by this object.
     *
     * @return the queryMap     A map of sql queries key'ed by query name.
     */
    Map<String, String> getQueryMap();

    /**************************************************************************
     * Set map of sql queries used by this class.
     *
     * @param cMap      A map of sql queries key'ed by query name.
     */
    void setQueryMap(final Map<String, String> cMap);

    /**************************************************************************
     * Inserts a new file entry in the file_meta_data table.
     *
     * @param cFileId           file id to uniquely id the file
     * @param szFileName        File name
     * @param szFilePath        Path to file
     * @param cNewStatus        Status to set to.
     */
    void insertNewFile(
        final Long cFileId,
        final String szFileName,
        final String szFilePath,
        final FileStatus cNewStatus);

    /**************************************************************************
     * Looks for a duplicate entries for a file based on the file name.
     *
     * @param szFileName    search for duplicate entries for this filename
     *
     * @return List         List of duplicate files
     */
    List< ? extends IFileMetaData> findDuplicateFilesByFileName(
        final String szFileName);

    /**************************************************************************
     * Getter for rowMapper.
     *
     * @return the rowMapper
     */
    RowMapper<IFileMetaData> getRowMapper();

    /**************************************************************************
     * Setter for rowMapper.
     *
     * @param cRowMapper the rowMapper to set
     */
    void setRowMapper(
        final RowMapper<IFileMetaData> cRowMapper);

    /**************************************************************************
     * Execute the list of sqls in a transaction. Before executing this method,
     * set the parameters by calling the {@link setQueryParamsForExecuteInTx}
     * method.
     */
    void executeInTx();

    /**************************************************************************
     * Add params for all the sql that will be executed in a tx.
     *
     * @param cQueryParams  Map of sql query parameters, key'ed by the parameter
     *                      name in the sql.
     */
    void setQueryParamsForExecuteInTx(
        final MapSqlParameterSource cQueryParams);

    /**************************************************************************
     * Getter for executeInTxList.
     *
     * @return the executeInTxList
     */
    List<String> getExecuteInTxList();

    /**************************************************************************
     * Setter for executeInTxList.
     *
     * @param cExecuteInTxList the executeInTxList to set
     */
    void setExecuteInTxList(final List<String> cExecuteInTxList);

    /**
     * Get the BillFile for Auto Updatation.
     *
     * @param  iDaysAutoPublish number of days for auto publish
     * @param  iDaysAutoAccept number of days for auto accept
     * @return IFileMetaData
     */
    IFileMetaData getBillFileToUpdate(final int iDaysAutoPublish, final int iDaysAutoAccept);

    /**
     * Auto updating the bill files status.
     *
     * @param arg0 List<? extends IFileMetaData>
     */
    void updateBillFiles(List<? extends IFileMetaData> arg0);
    
    /**************************************************************************
     * Update the bill stream of the file with given fileId.
     *
     * @param lFileId       File id
     * @param cBillStream   The "type" of bills contained in this file.
     * @param cPayGroup     The payment driver to use when paying bills in the
     *                      specified file.
     */
	void updateFileBillStream(
			final long lFileId, 
			final String cBillStream,
			final String cPayGroup);

	/**************************************************************************
	 * Returns the next ID to use.
	 * 
	 * @return  The next ID to use.
	 */
	long nextId();
	
}