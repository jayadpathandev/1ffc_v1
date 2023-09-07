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

import org.springframework.batch.core.ExitStatus;

/******************************************************************************
 * Class containing definitions for constants used across all batch jobs.
 *
 * @author Sanket
 *
 */
public final class Constants {

    /**************************************************************************
     * Prevents any instantiation of this class.
     */
    private Constants() 
    { }

    /**************************************************************************
     * Name of the parameter to store the file id. This parameter is
     * typically stored in the JobParameters object.
     */
    public static final String PARAM_FILE_ID = "FILE_ID";

    /**************************************************************************
     * Name of the parameter to store the file path.
     */
    public static final String PARAM_FILE_PATH = "FILE_PATH";

    /**************************************************************************
     * Name of the parameter to store the file name.
     */
    public static final String PARAM_FILE_NAME = "FILE_NAME";

    /**************************************************************************
     * Name of the parameter to store the file id. This parameter is
     * typically stored in the JobParameters object.
     */
    public static final String PARAM_SUM_FILE_ID = "SUM_FILE_ID";

    /**************************************************************************
     * Name of the parameter to store the file name.
     */
    public static final String PARAM_SUM_FILE_NAME = "SUM_FILE_NAME";

    /**************************************************************************
     * Name of the parameter to store the file id. This parameter is
     * typically stored in the JobParameters object.
     */
    public static final String PARAM_ASSET_DTL_FILE_ID = "ASSET_DTL_FILE_ID";

    /**************************************************************************
     * Name of the parameter to store the file name.
     */
    public static final String PARAM_ASSET_DTL_FILE_NAME = "ASSET_DTL_FILE_NAME";

    /**************************************************************************
     * Status indicating that no files are available for processing by the batch
     * job. An int exit code is mapped to this exit status via the spring
     * config file.
     */
    public static final ExitStatus NO_FILE_AVAILABLE =
        new ExitStatus("NO_FILE_AVAILABLE");

    /**************************************************************************
     * Name of the parameter storing the contents of the bill.
     */
    public static final String PARAM_BILL = "PARAM_BILL";
    
    /**************************************************************************
     * Name of the parameter storing the contents of the zip file.
     */
    public static final String PARAM_ZIP_UBF = "ZIP_UBF";

    /**************************************************************************
     * Name of the parameter storing the bill id.
     */
    public static final String PARAM_BILL_ID = "PARAM_BILL_ID";

    /**************************************************************************
     * Name of the parameter storing the bill asset id.
     */
    public static final String PARAM_BILL_ASSET_ID = "PARAM_ASSET_BILL_ID";

    /**************************************************************************
     * Name of the parameter to store the number of rows inserted.
     */
    public static final String ROWS_INSERTED = "ROWS_INSERTED";

    /**************************************************************************
     * Name of the parameter to store the number of rows skipped.
     */
    public static final String ROWS_SKIPPED = "ROWS_SKIPPED";

    /**************************************************************************
     * Parameter storing the list of error rows.
     */
    public static final String ERROR_ROWS_LIST = "ERROR_ROWS_LIST";

    /**************************************************************************
     * Name of the parameter to store boolean indicating whether the writer
     * should write output.
     */
    public static final String PARAM_SKIP_WRITING = "SKIP_WRITING";

    /******************************************************************************
     * Constants representing the various statuses of a file.
     */
    public enum FileStatus {
        /**************************************************************************
         * Indicates that the file has been received by the saas system.
         */
        RECEIVED,
        /***********************************************************************
         * Indicates that the preprocessor job is processing the file.
         */
        PREPROCESSING,
        /**************************************************************************
         * Indicates that the file has been preprocessed.
         */
        PREPROCESSED,
        /***********************************************************************
         * Indicates that the file is currently being loaded.
         */
        LOADING,
        /***********************************************************************
         * Indicates that the file has been loaded.
         */
        LOADED,
        /***********************************************************************
         * Indicates that the asset details file has been successfully loaded in
         * two-file loader mode. Its bill file id hasn't been used in the data
         * loaded, but the corresponding summary file id instead.
         */
        MERGED,
        /***********************************************************************
         * Indicates that the loading of the file has failed.
         */
        FAILED,
        /***********************************************************************
         * Indicates that the bill file has been accepted. Bills from this file
         * are not available to the customers yet. They are only available
         * to the users of the Bill Verify application.
         */
        ACCEPTED,
        /***********************************************************************
         * Indicates that the bill file has been rejected. Bills from this
         * data file will not be available to the customers.
         */
        REJECTED,
        /***********************************************************************
         * Indicates that the bill file has been published. All bills from
         * this file will now be available for viewing to the customers.
         */
        PUBLISHED,
        /***********************************************************************
         * Indicates that the bill file has been scheduled for purge and will
         * be purged the next time purge job runs.
         */
        SCHEDULED_FOR_PURGE,
        /***********************************************************************
         * Indicates that the file has been loaded partially. Not all records
         * from this file were loaded successfully. Usually in this case, the
         * job creates a .bad file containing the records that didnt get loaded
         * in the database.
         */
        PARTIALLY_LOADED,
        /***********************************************************************
         * Indicates that a newly received file has been successfully moved to
         * the input directory and it is ready for further processing.
         */
        READY_FOR_PROCESSING,
        /***********************************************************************
         * Indicates that a newly received file could NOT be successfully
         * moved to the output directory so it is not ready for further processing.
         */
        NOT_READY_FOR_PROCESSING,
        /***********************************************************************
         * Indicates that a job is in processing.
         */
        PROCESSING,
        /***********************************************************************
         * Indicates successful completion.
         */
        SUCCESS,
        /***********************************************************************
         * Indicates partial success.
         */
        PARTIALLY_SUCCESSFUL,
        /***********************************************************************
         * Indicates an error status for a file. A duplicate file will be set
         * to this state if such a file is submitted for processing
         * and duplicate files are not allowed.
         */
        ERROR
        
    };

}
