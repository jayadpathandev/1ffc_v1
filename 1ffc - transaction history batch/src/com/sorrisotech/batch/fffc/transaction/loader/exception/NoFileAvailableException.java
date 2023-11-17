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
package com.sorrisotech.batch.fffc.transaction.loader.exception;

/******************************************************************************
 * Exception class indicating that no file is available for processing. 
 * 
 * @author Sanket
 *
 */
public class NoFileAvailableException extends Exception {

    /**************************************************************************
     * 
     */
    private static final long serialVersionUID = 1L;

    /**************************************************************************
     * Default constructor.
     */
    public NoFileAvailableException() {
        super("No files are available for processing. ");
    }

    /**************************************************************************
     * Message string describing the exception.
     * 
     * @param sMessage      Exception message
     */
    public NoFileAvailableException(final String sMessage) {
        super(sMessage);
        // TODO Auto-generated constructor stub
    }

    /**************************************************************************
     * @param cThrowable    An instance of {@link Throwable}
     */
    public NoFileAvailableException(final Throwable cThrowable) {
        super(cThrowable);
        // TODO Auto-generated constructor stub
    }

    /**************************************************************************
     * 
     * @param sMessage          Exception message.
     * 
     * @param cThrowable        Instance of {@link Throwable}.
     */
    public NoFileAvailableException(final String sMessage, final Throwable cThrowable) {
        super(sMessage, cThrowable);
        // TODO Auto-generated constructor stub
    }
}
