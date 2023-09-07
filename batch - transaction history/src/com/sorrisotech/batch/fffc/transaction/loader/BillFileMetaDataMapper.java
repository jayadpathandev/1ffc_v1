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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaData;


/******************************************************************************
 * Map a resultset row to a BillFileMetaData bean.
 */
public class BillFileMetaDataMapper implements RowMapper<IFileMetaData> {


    /**************************************************************************
     * (non-Javadoc).
     * @see org.springframework.jdbc.core.simple.ParameterizedRowMapper
     *      #mapRow(java.sql.ResultSet, int)
     */
    public IFileMetaData mapRow(
        final ResultSet cResultSet, 
        final int cRow
    ) throws SQLException {
        
        final BaseFileMetaData cFileMetaData = 
            new BaseFileMetaData();
        cFileMetaData.setFileId(
            cResultSet.getLong("BILL_FILE_ID"));
        cFileMetaData.setFileName(
            cResultSet.getString("BILL_FILE_NAME"));
        cFileMetaData.setFilePath(
            cResultSet.getString("BILL_FILE_PATH"));
        cFileMetaData.setCreatedDate(
            cResultSet.getTimestamp("CREATED_DATE"));
        cFileMetaData.setStatus(
            cResultSet.getString("STATUS"));
        cFileMetaData.setStatusUpdateDate(
            cResultSet.getTimestamp("STATUS_UPDATE_DATE"));
        return cFileMetaData;    
    }

}
