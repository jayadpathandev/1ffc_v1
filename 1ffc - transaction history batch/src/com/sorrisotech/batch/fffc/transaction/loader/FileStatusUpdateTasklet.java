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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import com.sorrisotech.batch.fffc.transaction.loader.Constants.FileStatus;
import com.sorrisotech.batch.fffc.transaction.loader.api.IFileMetaDataDao;


/******************************************************************************
 * Base implementation of the Tasklet interface used to implement simple
 * job steps such as file operations- moving a file to a new directory
 * and updating a database with the status of the processing of the file.
 *
 * @author Mariana Jbantova
 */

public class FileStatusUpdateTasklet implements
    InitializingBean, Tasklet
{
    /**************************************************************************
     * Development level logging.
     */
    private static final Logger mLog = LoggerFactory.getLogger(FileStatusUpdateTasklet.class);

    /**************************************************************************
     * Used to update the status of the file after the job has finished
     * processing it.
     */
    private IFileMetaDataDao mFileMetaDataDao;

    /**************************************************************************
     * Directory location for the archiving the files.
     */
    private String mDestinationDirPath;

    /**************************************************************************
     * Source directory location.
     */
    private String mSourceDirPath;

    /**************************************************************************
     * Input file name.
     */
    private String mSourceFileName;
    
    /***************************************************************************
     * Zip UBF file name.
     */
    private String mZipUbfName = null;

    /***************************************************************************
     * ID of the Bill File
     */
    private long mBillFileId;

    /**************************************************************************
     * A boolean flag indicating if new files will be moved to a provided
     * destination directory. If set to FALSE then the new files will be left
     * in the input directory.
     */
    private boolean mMove = true;


    /**************************************************************************
     * Status the bill file will be set to in the db,
     * when the job finishes processing it.
     */
    private FileStatus mStatus = null;

    /***************************************
     * ***********************************
     * Date format to create the prefix for the file name if file moved to
     * a new archive directory.
     */
    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyyMMddhhmmss");


    /**************************************************************************
     * Getter for destinationDirPath.
     *
     * @return the destinationDirPath
     */
    public String getDestinationDirPath() {
        return mDestinationDirPath;
    }

    /**************************************************************************
     * Setter for destinationDirPath.
     *
     * @param cDestinationDirPath the destinationDirPath to set
     */
    public void setDestinationDirPath(final String cDestinationDirPath) {
        mDestinationDirPath = cDestinationDirPath;
    }

    /**************************************************************************
     * Should the file be moved?
     *
     * @return mMove            true if the file should be moved to a new dir
     */
    public boolean isMove() 
    {
        return mMove;
    }

    /**************************************************************************
     * @param bMove            mMove will be set to it
     */
    public void setMove(final boolean bMove) {
        mMove = bMove;
    }




    /**************************************************************************
     * (non-Javadoc).
     * @see org.springframework.beans.factory.InitializingBean#
     * afterPropertiesSet()
     *
     * @throws Exception        if illegal property values used
     */
    public void afterPropertiesSet()
        throws Exception
    {
        Assert.notNull(mFileMetaDataDao,
            " The fileMetaDao property has to be set.");

//        if (mMove) {
//
//            final File cDestDir = new File(mDestinationDirPath);
//            Assert.isTrue(cDestDir.exists(),
//                "Invalid destination archive directory. Directory: "
//                + mDestinationDirPath + " does not exist"
//                );
//            Assert.isTrue(!cDestDir.isFile(),
//                "The provided source dir path: "
//                + mDestinationDirPath + " points to a file not a directory"
//                );
//        }

    }


    /**************************************************************************
     * 1. Updates the status of the file to <code>status</code>.
     * 2. Moves the file to an archive directory if <code>move</code> is set
     *    to true.
     *
     * @param   cStepcontribution
     * @param   cChunkcontext
     *
     * @return RepeatStatus     status of step execution
     *
     * @throws Exception
     */
    public RepeatStatus execute(
        final   StepContribution    cStepcontribution,
        final   ChunkContext        cChunkcontext
    )
    throws  Exception
    {

        final ExecutionContext cExecContext =
            cChunkcontext.getStepContext().getStepExecution().
                getJobExecution().getExecutionContext();


        if (mLog.isInfoEnabled()) {
            mLog.info("execute: file status to be set to: " + getStatus());
        }

        /*
         * Update the status of the file identified by lFileId
         */
        long lFileId = 0;
        if (mBillFileId > 0) {
            lFileId = mBillFileId;
         } else if (cExecContext.get(Constants.PARAM_FILE_ID) != null) {
            lFileId =
                cExecContext.getLong(Constants.PARAM_FILE_ID);
         }
            mFileMetaDataDao.updateFileStatus(
                    lFileId,
                    mStatus);


        /*
         * Move the processed file to a new directory if move = true
         */
        if(isMove()) 
        {
        	if(mZipUbfName != null && !(mZipUbfName.equals("")) )
            {
				String ubfTempLocation = mZipUbfName;
        		
				
        		File ubfTmp = new File(ubfTempLocation);
        		ubfTmp.delete();
            	
            	final String cFilePath = mSourceDirPath;
                final String cFileName = mSourceFileName;
                final File cSourceFile = new File(cFilePath, cFileName);
                archiveFile(
                        cChunkcontext.getStepContext().getStepExecution().
                        getJobExecution(), 
                        cSourceFile
                    );
            }
        	else
        	{
					final String cFilePath = mSourceDirPath;
					final String cFileName = mSourceFileName;
					final File cSourceFile = new File(cFilePath, cFileName);
					File cDestFile = archiveFile(
							cChunkcontext.getStepContext().getStepExecution().
					        getJobExecution(), 
					        cSourceFile
						  );
					if(cDestFile != null)
					{
						compressFile(cDestFile);
					}

        	}
        }
        return RepeatStatus.FINISHED;
    }

    /**************************************************************************
     * Move the file from source directory to destination directory.
     *
     * @param cSourceFile   File to move.
     * @param cDestFile     Destination file.
     */
    private void moveFile(
        final   File    cSourceFile,
        final   File    cDestFile)
    {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Starting to move the file: "
                + "\n SourceFileName: " + cSourceFile.getName() + "\n SourceDir: "
                + cSourceFile.getParent()
                + "\n DestFileName: " + cDestFile.getName()
                + "\n DestDir: " + cDestFile.getParent());
        }

        // If dest dir does not exist, create it.
        if(!cDestFile.getParentFile().exists()) {
            createDirectory(cDestFile.getParentFile());
        }

        // Try to rename (move) the file first
        if (!cSourceFile.renameTo(cDestFile)) {
            if (mLog.isDebugEnabled()) {
                mLog.debug("moveFile(cSourceFile, cDestFile) : could not move file."
                    + " File will be copied to new directory and then "
                    + " deleted from input directory.");
            }
            try {

                FileCopyUtils.copy(cSourceFile, cDestFile);
                cSourceFile.delete();
                if (mLog.isDebugEnabled()) {
                    mLog.debug("Moving file complete: " + cSourceFile.getName());
                }
            } catch (IOException cException) {
                mLog.error(
                    "Error occured while moving the file to "
                    + " directory. \n SourceFileName: " + cSourceFile.getName()
                    + "\n SourceDir: " + cSourceFile.getParent()
                    + "\n DestFileName: " + cDestFile.getName()
                    + "\n DestDir: " + cDestFile.getParent() + "\n",
                    cException);
            }
        } else {
            if (mLog.isDebugEnabled()) {
                mLog.debug("Moving file complete: " + cSourceFile.getName());
            }
        }

        // Compress file
        //compressFile(cDestFile);
    }
    
    /**
     * Compress a file and delete the source.
     * 
     * @param cfile File to compress
     */
    public void compressFile(File cFile) {
        if (cFile.exists()) {
        	final int BUFFER = 2048;
        	 
        	try {
	            BufferedInputStream origin = null;
	            FileOutputStream dest = new FileOutputStream(cFile.getPath() + ".zip");
	            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	            //out.setMethod(ZipOutputStream.DEFLATED);
	            byte data[] = new byte[BUFFER];
	            
				FileInputStream fi = new FileInputStream(cFile);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(cFile.getName());
				out.putNextEntry(entry);
				int count;
				while((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
	            out.close();	            
	            cFile.delete();
        	} catch(FileNotFoundException fnfe) {
                mLog.error("File not found for compression, " + fnfe);
        	} catch(IOException ioe) {
                mLog.error("Error writing compressed file, " + ioe);
            }
        }
    }

    /**************************************************************************
     * Move the current file as denoted by the {@link Constants.PARAM_FILE_NAME}
     * stored in the job's execution context to an archive directory.
     *
     * @param cJobExecution  Job's execution context.
     * @param cSourceFile the initial source file
     * @return cDestFile 
     */
    public File archiveFile(final JobExecution cJobExecution, File cSourceFile)
    {
        final ExecutionContext cExecContext =
            cJobExecution.getExecutionContext();
        File cDestFile = null;
        
        if (cExecContext.get(Constants.PARAM_FILE_ID) != null || mBillFileId > 0) 
        {
        	String szBillFileId = "";
        	if (mBillFileId > 0) {
        		szBillFileId = Long.toString(mBillFileId);
        	} 
        	else 
        	{
        		szBillFileId = Long.toString((Long)(cExecContext.get(Constants.PARAM_FILE_ID)));
        	}
        	
            final String cFileName = mSourceFileName;

            String cDestName = prefixTimeStamp(cFileName);
            cDestName = szBillFileId + "_" + cDestName;      
            
            final String cDestDir = mDestinationDirPath;
            cDestFile = new File(cDestDir, cDestName);
            moveFile(cSourceFile, cDestFile);

        }
		return cDestFile;
    }

    /**************************************************************************
     * @return mStatus          the status of a file
     */
    public FileStatus getStatus() {
        return mStatus;
    }

    /**************************************************************************
     * @param cSuccessStatus            mStatus will be set to it
     */

    public void setStatus(final FileStatus cSuccessStatus) {
        mStatus = cSuccessStatus;
    }

    /**************************************************************************
     * Prefix the datestamp to the given string.
     *
     * @param cString   String that should be prefixed.
     *
     * @return  String prefixed with timestamp.
     */
    private String prefixTimeStamp(final String cString) {
        final String cTimeStamp = DATE_FORMAT.format(new Date());
        return cTimeStamp + "_" + cString;
    }

    /**************************************************************************
     * Getter for FileMetaDataDao.
     *
     * @return the FileMetaDataDao
     */
    public IFileMetaDataDao getFileMetaDataDao() {
        return mFileMetaDataDao;
    }

    /**************************************************************************
     * Setter for FileMetaDataDao.
     *
     * @param cFileMetaDataDao the FileMetaDataDao to set
     */
    public void setFileMetaDataDao(final IFileMetaDataDao cFileMetaDataDao) {
        mFileMetaDataDao = cFileMetaDataDao;
    }


    /***************************************************************************
     * The method creates a directory on disk using <code>cDir</code>.
     *
     * @param cDir      directory to be created
     *
     * @return true     if directory successfully created, false otherwise
     */
    private boolean createDirectory(final File cDir) {

        if (mLog.isDebugEnabled()) {
            mLog.debug("createDirectory(cDir) : destination directory "
                + cDir + " will be created.");
        }

        final boolean bIsSuccess = cDir.mkdirs();

        if (!bIsSuccess) {
            mLog.error("createDirectory(cDir) : could not create  "
                + "destination directory: " + cDir + " File will not be moved. ");

        } else {
            mLog.error("createDirectory(cDir) : destination directory: "
                + cDir + " was successfully created.");
        }

        return bIsSuccess;
    }

    /***********************************************************************************************
     * @return the sourceDirPath
     */
    public String getSourceDirPath()
    {
        return mSourceDirPath;
    }

    /***********************************************************************************************
     * @param szSourceDirPath
     * @param sourceDirPath the sourceDirPath to set
     */
    public void setSourceDirPath(
        final String szSourceDirPath)
    {
        mSourceDirPath = szSourceDirPath;
    }

    /***********************************************************************************************
     * @return the sourceFileName
     */
    public String getSourceFileName()
    {
        return mSourceFileName;
    }

    /***********************************************************************************************
     * @param szSourceFileName
     * @param sourceFileName the sourceFileName to set
     */
    public void setSourceFileName(
        final String szSourceFileName)
    {
        mSourceFileName = szSourceFileName;
    }

    /***********************************************************************************************
     * @param lBillFileId the billFileId to set
     */
    public void setBillFileId(
        final long lBillFileId)
    {
        mBillFileId = lBillFileId;
    }

    /***********************************************************************************************
     * Getter for the bill file ID
     * 
     * @return the billFileId
     */
    public long getBillFileId()
    {
        return mBillFileId;
    }
    
    /**
     * Setter for mZipUbfName 
     * 
     * @param mZipUbfName
     */
    public void setZipUbfName(String mZipUbfName )
    {
    	this.mZipUbfName = mZipUbfName;
    }
    
    /**
     * Getter for mZipUbfName 
     * 
     * @return zipFileName
     */
    public String getZipUbfName()
    {
    	return mZipUbfName;
    }
}
