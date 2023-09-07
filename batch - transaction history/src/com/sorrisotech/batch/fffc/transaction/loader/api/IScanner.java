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

import com.sorrisotech.batch.fffc.transaction.loader.Constants.FileStatus;
import com.sorrisotech.batch.fffc.transaction.loader.DbScannerParamsBean;
import com.sorrisotech.batch.fffc.transaction.loader.exception.NoFileAvailableException;

/******************************************************************************
 * An interface for obtaining the next available file for processing.
 *
 * @author Sanket
 *
 */
public interface IScanner {

    /**************************************************************************
     * Return the file_id for the file whose current status is identified by
     * the first parameter. Either the oldest or the most recent file will be
     * returned depending on the mostRecent argument.
     *
     * @param cCurrentStatus            This will be matched with the current
     *                                  status value of the file.
     * @param cNewStatus                Status of the file will be updated to
     *                                  the new status before returning the file
     *                                  id. This is to avoid concurrency issues.
     * @param bMostRecent               A boolean indicating if the most recent
     *                                  file matching the status will be
     *                                  returned by this method.
     *                                  True - Most recent file (based on
     *                                  created_date field) will be returned
     *                                  False - Oldest file (based on created_date
     *                                  field) will be returned.
     * @param bProcessFailedFilesFirst  Boolean indicating if the failed files
     *                                  should be processed over the unprocessed
     *                                  files.
     * @param bProcessFailedFiles       Boolean indicating if the failed files
     *                                  should be processed or not. By default
     *                                  failed files will not be processed.
     *
     * @return                  A long representing the file_id.
     *
     * @throws NoFileAvailableException    The exception is thrown when no
     *                                      files are available for processing.
     */
    IFileMetaData getNextFile(
        final FileStatus        cCurrentStatus,
        final FileStatus        cNewStatus,
        boolean                 bMostRecent,
        boolean                 bProcessFailedFilesFirst,
        boolean                 bProcessFailedFiles,
        DbScannerParamsBean     cParamsBean
    ) throws NoFileAvailableException;

    /**************************************************************************
     * Return the file_id for the oldest file whose status is identified by
     * the given parameter. Failed files will be considered only if there are
     * no unprocessed files available.
     *
     * @param cCurrentStatus    File status to search by.
     * @param cNewStatus        File status to update the status of the returned
     *                          file to avoid concurrency issues
     *
     * @return                  A long representing the file_id.
     *
     * @throws NoFileAvailableException     The Eception is thrown when no
     *                                      files are available for processing.
     */
    IFileMetaData getNextFile(
        FileStatus cCurrentStatus,
        FileStatus cNewStatus,
        DbScannerParamsBean   cParamsBean
    ) throws NoFileAvailableException;

    /**************************************************************************
     * Get the next available file for processing using default values. Failed
     * files will be considered only if there are no unprocessed files available
     * for processing.
     *
     * @return  BillFileId for the file
     *
     * @throws NoFileAvailableException     Thrown when no files can be found
     *                                      for processing
     */
    IFileMetaData getNextFile() throws NoFileAvailableException;

}
