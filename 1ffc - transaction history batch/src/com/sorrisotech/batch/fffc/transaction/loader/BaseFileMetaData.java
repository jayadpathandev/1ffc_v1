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

import java.util.Date;

import com.sorrisotech.batch.fffc.transaction.loader.Constants.FileStatus;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaData;

/******************************************************************************
 * Bean to hold data associated for a file. 
 */
public class BaseFileMetaData implements IFileMetaData {
    
    /**************************************************************************
     * Unique identifier representing the file.
     */
    private long mFileId = -1;
    
    /**************************************************************************
     * Date on which the row representing the file was created in the 
     * database.
     */
    private Date mCreatedDate;
    
    /**************************************************************************
     * Name of the file.
     */
    private String mFileName;
    
    /**************************************************************************
     * Path on the filesystem where the data file resides.
     */
    private String mFilePath;
    
    /*************************************************************************
     * 
     */
    private String mFileType;
    
    /**************************************************************************
     * Current status of the file. Possible values are {@link FileStatus}.
     */
    private String mStatus;
    
    /**************************************************************************
     * Date of last status update.
     */
    private Date mStatusUpdateDate;

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#getFileId()
     */
    public long getFileId() {
        return mFileId;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#setFileId(long)
     */
    public void setFileId(final long cFileId) {
        mFileId = cFileId;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#getCreatedDate()
     */
    public Date getCreatedDate() {
        return mCreatedDate;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#setCreatedDate(java.util.Date)
     */
    public void setCreatedDate(final Date cCreatedDate) {
        mCreatedDate = cCreatedDate;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#getFileName()
     */
    public String getFileName() {
        return mFileName;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#setFileName(java.lang.String)
     */
    public void setFileName(final String cFileName) {
        mFileName = cFileName;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#getFilePath()
     */
    public String getFilePath() {
        return mFilePath;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#setFilePath(java.lang.String)
     */
    public void setFilePath(final String cFilePath) {
        mFilePath = cFilePath;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#getStatus()
     */
    public String getStatus() {
        return mStatus;
    }

    /**************************************************************************
     * Sets the file type
     */
    public void setFileType(final String cFileType) 
    {
    	mFileType = cFileType;
    }
    
    /**************************************************************************
     * Returns the file type 
     */
    public String getFileType() 
    {
        return mFileType;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#setStatus(java.lang.String)
     */
    public void setStatus(final String cStatus) 
    {
        mStatus = cStatus;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#getStatusUpdateDate()
     */
    public Date getStatusUpdateDate() {
        return mStatusUpdateDate;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#setStatusUpdateDate(java.util.Date)
     */
    public void setStatusUpdateDate(final Date cStatusUpdateDate) {
        mStatusUpdateDate = cStatusUpdateDate;
    }

    /**************************************************************************
     * (non-Javadoc).
     * @see com.sorrisotech.saas.batch.common.api.IFileMetaData#toString()
     */
    public String toString() {
        final StringBuffer cSb = new StringBuffer(120);
        cSb.append("\n_file_id: ");
        cSb.append(this.mFileId);
        cSb.append("\n_file_name: ");
        cSb.append(this.mFileName);
        cSb.append("\n_file_path: ");
        cSb.append(this.mFilePath);
        cSb.append("\nStatus: ");
        cSb.append(this.mStatus);
        cSb.append("\nCreated_date: ");
        cSb.append(this.mCreatedDate);
        cSb.append("\nLast_update_date: ");
        cSb.append(this.mStatusUpdateDate);
        cSb.append('\n');
        return cSb.toString();
    }
}
