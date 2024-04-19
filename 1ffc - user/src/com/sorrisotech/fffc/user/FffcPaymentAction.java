/* (c) Copyright 2016-2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
 * Patents Pending.
 * 
 * This product is distributed under license from Sorriso Technologies, Inc.
 * Use without a proper license is strictly prohibited.  To license this
 * software, you may contact Sorriso Technologies at:
 * 
 * Sorriso Technologies, Inc.
 * 400 West Cummings Park,
 * Suite 1725-184,
 * Woburn, MA 01801, USA
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
package com.sorrisotech.fffc.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.common.utils.I18n;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IListData;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.payment.model.PaymentWalletFields;
import com.sorrisotech.uc.payment.UcPaymentAction;

/******************************************************************************
 * This class deals with the payment transactions.
 * 
 * @author Maybelle Johnsy Kanjirapallil.
 *
 */
public class FffcPaymentAction extends UcPaymentAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2053358112986566477L;

	/***************************************************************************
	 * Logger for this class.
	 */
	private static final Logger m_cLog = LoggerFactory.getLogger(FffcPaymentAction.class);

	/**************************************************************************
	 * This method returns a 2D array of payment due values.
	 * 
	 * @param cData             The user data.
	 * @param cLocator          The service locator.
	 * @param szCurrent         The current balance.
	 * @param szStatement       The statement balance.
	 * @param szMinimum         The minimum due.
	 * @param bIsAccountCurrent -- if "true" we don't dispaly current balance text
	 * 
	 * @return A 2D array of payment due.
	 */
	public String[][] getPayAmountDropdown(IUserData cData, IServiceLocator2 cLocator, String szCurrentDisplay,
			String szStatementDisplay, String szMinimumDisplay, final String bIsAccountCurrent) {

		String szCurrentBalanceText = szCurrentDisplay + " - "
				+ I18n.translate(cLocator, cData, "paymentOneTime_sCurrentBalText");
		String szOther = I18n.translate(cLocator, cData, "paymentOneTime_sOther");

		if (!bIsAccountCurrent.equalsIgnoreCase("true")) {
			return new String[][] { { "current", szCurrentBalanceText }, { "other", szOther } };
		} else {
			return new String[][] { { "other", szOther } };
		}
	}

	@Override
	public String getWalletItems(IUserData cData, IServiceLocator2 cLocator, String szUserId, String szSourceId,
			IListData cWalletList) {
		String szResult = "error";
		m_cLog.debug("UcPaymentAction.....getWallets()...User id: " + szUserId);

		try {
			String[][] cItems = null;
			String szDefault = I18n.translate(cLocator, cData, "paymentOneTime_sDefault");
			String szExpired = I18n.translate(cLocator, cData, "paymentOneTime_sExpired");
			PaymentWalletFields[] cWallets = m_cBean.getPaymentWallet(szUserId);

			if (!cData.getActors().contains("bank_payment_enabled")) {
				List<PaymentWalletFields> cPaymentWalletList = new ArrayList<>(Arrays.asList(cWallets));
				cPaymentWalletList.removeIf(val -> "bank".equals(val.getSourceType()));
				cWallets = cPaymentWalletList.toArray(new PaymentWalletFields[cPaymentWalletList.size()]);
			}

			if (cWallets != null && cWallets.length > 0) {

				int i = 0;

				// if we have default and it is not expired, then it goes first, otherwise we
				// have a null "choose method" option first
				if (cWallets[0].getSourceDefault().equalsIgnoreCase("true")
						&& !isExpired(cWallets[0].getSourceExpiry())) {
					cItems = new String[cWallets.length][2];
				} else {
					cItems = new String[cWallets.length][2];
				}

				for (PaymentWalletFields cWallet : cWallets) {
					cItems[i][0] = cWallet.getSourceId();

					boolean bExpired = false;
					bExpired = isExpired(cWallet.getSourceExpiry());

					if (cWallet.getSourceDefault().equalsIgnoreCase("true")) {
						if (bExpired) {
							cItems[i][0] = "expired";
							cItems[i][1] = cWallet.getSourceName() + " " + cWallet.getSourceNum() + szDefault
									+ szExpired;
						} else {
							cItems[i][1] = cWallet.getSourceName() + " " + cWallet.getSourceNum() + szDefault;
						}
					} else if (bExpired) {
						cItems[i][0] = "expired";
						cItems[i][1] = cWallet.getSourceName() + " " + cWallet.getSourceNum() + szExpired;
					} else {
						cItems[i][1] = cWallet.getSourceName() + " " + cWallet.getSourceNum();
					}

					i++;
				}
			} else {
				cItems = new String[0][2];
			}

			cWalletList.setItems(cItems);

			if (!szSourceId.isEmpty()) {
				String[] szSelectedId = new String[1];
				szSelectedId[0] = szSourceId;
				cWalletList.setSelectedItem(szSelectedId);
			}

			szResult = "success";
		} catch (Exception e) {
			m_cLog.error("UcPaymentAction.....getWallets()...An exception was thrown", e);
		}

		return szResult;
	}
}
