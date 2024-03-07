package com.sorrisotech.uc.admin1ffc.createadmin;

import com.sorrisotech.app.common.utils.AuthUtil;
import com.sorrisotech.app.uc.CreateUser;
import com.sorrisotech.app.utils.Session;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;

public class Get1FFCAdminUser extends CreateUser {

    /**************************************************************************
     * Populates the roles available for the newly created user.
     * 
     * @param szRole  The role of the current user.
     * 
     * @return  The roles available to user.
     */
	public static String [] [] populateRole(
				final IServiceLocator2 cLocator, 
				final IUserData cData, 
				final String szRole
			)  throws ClassNotFoundException
	{
		
		if (Role1FFCConstants.NAMESPACE_ADMIN_APP.equals(AuthUtil.getAppNameSpace())) {
			return populateRoleAdminApp(cLocator, cData, szRole);
		} else {
			return null;
		}
		
	}
	
	private static String[][] populateRoleAdminApp(
			final IServiceLocator2 cLocator, 
			final IUserData        cData, 
			final String           szRole
		) throws ClassNotFoundException {
	final Session session = cData.getJavaObj(Session.class);
			
	if (szRole.equals(Role1FFCConstants.ROLE_ADMIN_SYSTEM_ADMIN))
	{
		if (session.isAccountLinkingEnabled().equalsIgnoreCase("true")) {
			return new String [][] {
				{ Role1FFCConstants.ROLE_ADMIN_SYSTEM_ADMIN, User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_SYSTEM_ADMIN) },
				{ Role1FFCConstants.ROLE_ADMIN_COMPANY_ADMIN, User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_COMPANY_ADMIN) },
				{ Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN,User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN) }
					
			};
		} else {
			return new String [][] {
				{ Role1FFCConstants.ROLE_ADMIN_SYSTEM_ADMIN, User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_SYSTEM_ADMIN) },
				{ Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN,User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN) }
					
			};				
		}
	}
	if (szRole.equals(Role1FFCConstants.ROLE_ADMIN_COMPANY_ADMIN))
	{
		return new String [][] {
			{ Role1FFCConstants.ROLE_ADMIN_COMPANY_ADMIN, User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_COMPANY_ADMIN) },
			{ Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN, User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN) }
		};
	}
	else {
		return new String [][] {
			{ Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN, User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_ORGANIZATION_ADMIN) },
			{ Role1FFCConstants.ROLE_ADMIN_AGENT, User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_AGENT) },
			{ Role1FFCConstants.ROLE_ADMIN_DOCUMENT_CONTROLLER,  User1FFCUtil.getDisplayRole(cLocator, cData, Role1FFCConstants.ROLE_ADMIN_DOCUMENT_CONTROLLER) }
			
		};
    }
}
	
}
