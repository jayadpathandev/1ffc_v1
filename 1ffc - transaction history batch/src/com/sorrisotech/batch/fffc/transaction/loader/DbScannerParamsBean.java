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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/******************************************************************************
 * Bean which will hold any extra parameters passed to the
 * {@link DbScanner} class.
 *
 * @author Mariana Jbantova
 *
 */
public class DbScannerParamsBean implements InitializingBean
{


    /**************************************************************************
     * Stores time delay for an action.
     */
    private Long mTimeDelay;

    /**************************************************************************
     * Stores file name pattern.
     */
    private String mFilenamePattern;


    /**************************************************************************
     * Stores comma separated matching file name patterns.
     */
    private String mFilenamePatternPair;

    /**************************************************************************
     * Returns time delay.
     *
     * @return mTimeDelay
     */
    public Long getTimeDelay() {
        return mTimeDelay;
    }

    /**************************************************************************
     * Sets the time delay.
     *
     * @param cTimeDelay
     */
    public void setTimeDelay(final Long cTimeDelay) {
        mTimeDelay = cTimeDelay;
    }


    /**************************************************************************
     * (non-Javadoc).
     * @see org.springframework.batch.core.JobExecutionListener
     *  #afterPropertiesSet(org.springframework.beans.factory.InitializingBean)
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mTimeDelay, "Time.delay property cannot be null.");
        Assert.isTrue(
            mTimeDelay >= 0,
            "Time.delay property has to be greater than or equal to zero."
                  );

    }

    /***********************************************************************************************
     * @return the mFilenamePattern
     */
    public String getFilenamePattern()
    {
        return mFilenamePattern;
    }

    /***********************************************************************************************
     * @param szFilenamePattern the filenamePattern to set
     */
    public void setFilenamePattern(final String szFilenamePattern)
    {
        mFilenamePattern = szFilenamePattern;
    }

    /***********************************************************************************************
     * @param filenamePatternPair the filenamePatternPair to set
     */
    public void setFilenamePatternPair(
        final String filenamePatternPair)
    {
        mFilenamePatternPair = filenamePatternPair;
    }

    /***********************************************************************************************
     * @return the filenamePatternPair
     */
    public String getFilenamePatternPair()
    {
        return mFilenamePatternPair;
    }

}
