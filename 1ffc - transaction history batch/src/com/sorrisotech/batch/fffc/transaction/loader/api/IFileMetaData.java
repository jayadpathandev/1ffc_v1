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

import java.util.Date;

/******************************************************************************
 * Interface representing the file meta data.
 * 
 * @author Sanket
 *
 */
public interface IFileMetaData {

    /**************************************************************************
     * Get the file id.
     * 
     * @return the fileId
     */
    long getFileId();

    /**************************************************************************
     * Set the  fild id.
     * 
     * @param cFileId the FileId to set
     */
    void setFileId(final long cFileId);

    /**************************************************************************
     * Get the created date.
     * 
     * @return the createdDate
     */
    Date getCreatedDate();

    /**************************************************************************
     * Set the date the file was created.
     * 
     * @param cCreatedDate the createdDate to set
     */
    void setCreatedDate(final Date cCreatedDate);

    /**************************************************************************
     * Get the  file name.
     * 
     * @return the FileName
     */
    String getFileName();

    /**************************************************************************
     * Set the  file name.
     * 
     * @param cFileName the FileName to set
     */
    void setFileName(final String cFileName);

    /**************************************************************************
     * Get the path of the  file.
     * 
     * @return the FilePath
     */
    String getFilePath();

    /**************************************************************************
     * Set the path of the  file.
     * 
     * @param cFilePath the FilePath to set
     */
    void setFilePath(final String cFilePath);

    /**************************************************************************
     * Get the current status of the file.
     * 
     * @return the status
     */
    String getStatus();

    /**************************************************************************
     * Set the current status of the file.
     * 
     * @param cStatus the status to set
     */
    void setStatus(final String cStatus);

    /**************************************************************************
     * Get the date when the status was last updated.
     * 
     * @return the statusUpdateDate
     */
    Date getStatusUpdateDate();

    /**************************************************************************
     * Set the date when the status was last updated.
     * 
     * @param cStatusUpdateDate the statusUpdateDate to set
     */
    void setStatusUpdateDate(final Date cStatusUpdateDate);

    /**************************************************************************
     * (non-Javadoc).
     * @see java.lang.Object#toString()
     */
    String toString();

}