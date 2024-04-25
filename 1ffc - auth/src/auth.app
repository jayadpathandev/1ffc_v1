application [
			 
	defaultLanguage en
	languages [ en ]

	initialUseCase loginEssentialsCheck
	
	modules [
	    LANDING => landing
		REGISTRATION => fffcReg05BillingInfo
		COMPLETE_REGISTRATION => regCompleteEnrollment
		LOGIN => ^login
		ERROR_PASSWORD_LOCKOUT => errorPwdFailureLockout
		ERROR_AUTH_CODE_LOCKOUT => errorAuthCodeFailureLockout
		
	]

	include [
	    auth_admin
	    auth_b2b
	    auth_b2c
	    error
	    landing
	    
		loginLib
		registrationLib
        utilsLib
        
		// 1FFC 
		fffcReg01EmailTnC
		fffcReg02SmsTnC        
		fffcReg03ElectronicTnC
		fffcReg04WebTnC
		fffcReg05BillingInfo
		fffcReg06LoginInfo
		fffcReg07SecretQuestion
		fffcReg08PersonalImage
 		fffcReg99B2C
 	] 	

    features [
        feature noop [
            noop
        ]
        feature b2c [
            b2c
        ]
        feature b2b [
            b2b
        ]
    ]
 	
    roles [
        role Role_Admin_SystemAdmin [ noop ]
        role Role_Admin_OrganizationAdmin [ noop ]
        role Role_Admin_Agent [ noop ]
        
        role Role_Consumer_EndUser [ noop ]
        role Role_Biz_OrganizationAdmin [ noop ]
        role Role_Biz_Admin [ noop ]
        role Role_Biz_EndUser [ noop ]
    ]
 	
]