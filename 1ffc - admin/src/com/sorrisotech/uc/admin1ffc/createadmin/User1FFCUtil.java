package com.sorrisotech.uc.admin1ffc.createadmin;

import com.sorrisotech.uc.admin1ffc.createadmin.Role1FFCConstants;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.i18n.IInternationalization;
import com.sorrisotech.app.common.utils.AuthUtil;

/******************************************************************************
 * This class deals with common user related tasks
 * 
 */
public class User1FFCUtil {
	
	/**
	 * Returns the role to be displayed on UI
	 * 
	 * @param cData
	 * @param cI18n
	 * @param szRole db role value
	 * @return
	 */
	public static String getDisplayRole(final IServiceLocator2 cLocator, final IUserData cData, String szRole) {
		
		String zsReturn = "";
		
		final IInternationalization cI18n = cLocator.findService(IInternationalization.class);
		
		if (Role1FFCConstants.NAMESPACE_ADMIN_APP.equals(AuthUtil.getAppNameSpace())) {
			
			switch (szRole) {
			
			case Role1FFCConstants.ROLE_ADMIN_SYSTEM_ADMIN:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Admin_SystemAdmin", "{System Administrator}");
				break;
			
			case Role1FFCConstants.ROLE_ADMIN_COMPANY_ADMIN:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Admin_Company", "{Company Administrator}");
				break;
				
			case Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Admin_OrganizationAdmin", "{Agent Administrator}");
				break;
				
			case Role1FFCConstants.ROLE_ADMIN_AGENT:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Admin_Agent", "{Agent}");
				break;
			
			case Role1FFCConstants.ROLE_ADMIN_DOCUMENT_CONTROLLER:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Admin_DocumentController", "{Agent Document Controller}");
				break;
			}
		
		} else if (Role1FFCConstants.NAMESPACE_USER_APP.equals(AuthUtil.getAppNameSpace())) {
			
			switch (szRole) {
			
			case Role1FFCConstants.ROLE_BIZ_ORGANIZATION_ADMIN:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Biz_OrganizationAdmin", "{Organization Administrator}");
				break;
			
			case Role1FFCConstants.ROLE_BIZ_ADMIN:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Biz_Admin", "{Manager}");
				break;
				
			case Role1FFCConstants.ROLE_BIZ_END_USER:
				zsReturn = cI18n.internationalize(cData, "adminListUsers_Role_Biz_EndUser", "{End User}");
				break;
			}
		}
		
		return zsReturn;
	}

}
