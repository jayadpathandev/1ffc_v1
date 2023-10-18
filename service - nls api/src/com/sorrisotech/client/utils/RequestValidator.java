/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 *
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.client.utils;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.client.exception.InvalidRequestException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/******************************************************************************
 * Used to validate request.
 * 
 * @author Asrar Saloda
 */
public class RequestValidator {
	
	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RequestValidator.class);
	
	/**********************************************************************************************
	 * hibernate validator used to validate the request before sending request
	 */
	private static Validator m_cValidator = null;
	
	/**********************************************************************************************
	 * Initialize member variable.
	 */
	static {
		m_cValidator = Validation.buildDefaultValidatorFactory().getValidator();
	}
	
	/**
	 * Validates request's fields.
	 * 
	 * @param cRequest any request having Hibernate validations into it.
	 * 
	 * @throws InvalidRequestException When request validation failed.
	 */
	public static <RequestType> void ValidateRequest(RequestType cRequest)
	        throws InvalidRequestException {
		var szClassName = cRequest.getClass().getName();
		LOG.debug("Validating class : {}", szClassName);
		
		Set<ConstraintViolation<RequestType>> cViolations = m_cValidator.validate(cRequest);
		
		if (!cViolations.isEmpty()) {
			var cErrorMessage = new StringBuffer("Request Validation failed:");
			for (var violation : cViolations) {
				cErrorMessage.append(" ").append(violation.getMessage()).append(";");
			}
			LOG.error(cErrorMessage.toString());
			throw new InvalidRequestException(cErrorMessage.toString());
		}
	}
	
}
