/* (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, 
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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sorrisotech.svcs.external.IExternalReuse;

/******************************************************************************
 * This class Process the JSon request
 * 
 * @author Yvette
 *
 */
public class JsonResponse implements IExternalReuse {

	/**************************************************************************
     * The UID for this class.
     */	
	private static final long serialVersionUID = -4771130864226281020L;
	
	/***************************************************************************
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JsonResponse.class);
    
    private ObjectMapper mMapper = new ObjectMapper();  
    
    private ObjectNode mResponse;
    
    private static final Pattern offSetPattern = Pattern.compile("^([\\p{Alnum}\\-_]+)(\\[(\\+?)\\])?$");
    
    	
    /**************************************************************************
     * This method always returns IExternalReuse.REUSE_SESSION_SINGLETON.
     * 
     * @return  Return to Persona engine telling it to use this instance 
     *          as session-singleton.
     */
	public int getReuse() {    
		return IExternalReuse.REUSE_SESSION_SINGLETON;		
	}

	/*************************************************************************
	 * Reset
	 */
	public void reset() {
				
		mResponse = mMapper.createObjectNode();
	}

	/**************************************************************************
	 * Parent
	 * 
	 * @param path
	 * @return ObjectNode
	 * @throws Exception 
	 */
	private ObjectNode parent(String path) throws Exception {
		
		ObjectNode node = mResponse; 
		
		if (path != null && path.length() != 0 )  {
		    String[] arrOfStr = path.split("\\.");
		    
		    // check if string contains a []
		    for (int i=0; i < arrOfStr.length-1; i++) {
	    	   
	          	Matcher match = offSetPattern.matcher(arrOfStr[i]);

	          	if (match.find()) {
	          		if (match.group(3) == null) {
	          			if (node.has(match.group(1)) == false) {
	          				node.set(match.group(1), mMapper.createObjectNode());
	          			}
	          			node = (ObjectNode) node.get(match.group(1));
	          		} else if (match.group(3).length() == 0) {
          				// case of empty bracket. Then get the last element
	          			LOG.debug("JsonResponse:parent....path: " + path + ", processing : " + arrOfStr[i]);
          				ArrayNode arrNode = node.withArray(match.group(1));  
          				node = (ObjectNode) arrNode.get(arrNode.size()-1);
          			} else {
          				// case bracket with a + sign
          				LOG.debug("JsonResponse:parent....path: " + path + ", processing : " + arrOfStr[i]);
          				ArrayNode arrNode = node.withArray(match.group(1));  
          				node = arrNode.addObject();		          				
          			}
		       } else {
		    	   LOG.debug("JsonResponse:parent: invalid reference....path: " + path + ", processing : " + arrOfStr[i]);
		    	   throw new Exception ("Parent - Invalid reference");
		       }
		    }
		}
		
		return node;		
	} 
	
	/*************************************************************************
	 * SetString
	 * 
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public void setString(String key, String value) throws Exception {

		if (mResponse == null) 
			throw new NullPointerException("response is not set");
		
		ObjectNode node = parent(key);
		
		String last = key.substring(key.lastIndexOf('.')+1);
		
      	Matcher match = offSetPattern.matcher(last);

      	if (match.find()) {
      		if (match.group(3) == null) {
      			node.put(match.group(1), value);
      		} else if (match.group(3).length() == 0) {
  				// empty bracket
      			LOG.debug("JsonResponse:setString....key: " + key + ", last : " + last);
      			throw new Exception ("Array offset is alread set");  
  			} else {
  				LOG.debug("JsonResponse:setString....key: " + key + ", last : " + last);
  				ArrayNode arrNode = node.withArray(match.group(1));  
  				arrNode.add(value);		          				
  			}
       } else {
    	   LOG.debug("JsonResponse:setString: Invalid reference. Key: " + key + ", last : " + last);
    	   throw new Exception ("Invalid reference");   	
       }
	}
	
	/*************************************************************************
	 * setEmptyString
	 * 
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public void setEmptyString(String key) throws Exception {
		this.setString(key, "");
	}
	
	/*************************************************************************
	 * set null value to given path
	 * 
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public void setNull(String key) throws Exception {
		this.setString(key, null);
	}
	
	/*************************************************************************
	 * SetNumber
	 * 
	 * @param key
	 * @param value
	 * @throws Exception 
	 */
	public void setNumber(String key, String value) throws Exception {

		if (mResponse == null) 
			throw new NullPointerException("response is not set");
		
		ObjectNode node = parent(key);
		
		String last = key.substring(key.lastIndexOf('.')+1);
		
      	Matcher match = offSetPattern.matcher(last);

      	if (match.find()) {
      		if (match.group(3) == null) {
      			node.put(match.group(1), new BigDecimal(value));
      		} else if (match.group(3).length() == 0) {
  				// empty bracket
      			LOG.debug("JsonResponse:setNumber....key: " + key + ", last : " + last);
      			throw new Exception ("Array offset is alread set");  
  			} else {
  				LOG.debug("JsonResponse:setNumber....key: " + key + ", last : " + last);
  				ArrayNode arrNode = node.withArray(match.group(1));  
  				arrNode.add(new BigDecimal(value));		          				
  			}
       } else {
    	   LOG.debug("JsonResponse:setNumber: Invalid reference. Key: " + key + ", last : " + last);
    	   throw new Exception ("Invalid reference");    	
       }
	}

	/*************************************************************************
	 * SetBoolean
	 * 
	 * @param key
	 * @param value
	 * @throws Exception 
	 */
	public void setBoolean(String key, String value) throws Exception {

		if (mResponse == null) 
			throw new NullPointerException("response is not set");
		
		ObjectNode node = parent(key);
		
		String last = key.substring(key.lastIndexOf('.')+1);
		
      	Matcher match = offSetPattern.matcher(last);

      	if (match.find()) {
      		if (match.group(3) == null) {
      			node.put(match.group(1),  Boolean.parseBoolean(value));
      		} else if (match.group(3).length() == 0) {
  				// empty bracket
      			LOG.debug("JsonResponse:setBoolean....key: " + key + ", last : " + last);
      			throw new Exception ("Array offset is alread set");  
  			} else {
  				LOG.debug("JsonResponse:setBoolean....key: " + key + ", last : " + last);
  				ArrayNode arrNode = node.withArray(match.group(1));  
  				arrNode.add( Boolean.parseBoolean(value));		          				
  			}
       } else {
    	   LOG.debug("JsonResponse:setBoolean: Invalid reference. Key: " + key + ", last : " + last);
    	   throw new Exception ("Invalid reference");  		
       }
	}

	/************************************************************************* 
	 * Send
	 * 
	 * @param path
	 * @return
	 * @throws IOException 
	 */
	public void send(HttpServletResponse response) throws IOException {

		if (mResponse == null) 
			throw new NullPointerException("response is not set");
		
		String val = mMapper.writeValueAsString(mResponse);
		byte[] bValue = val.getBytes("utf-8");

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");

		final OutputStream cOs = response.getOutputStream();
		
		cOs.write(bValue);
		
		cOs.close();
	}
	
	/************************************************************************* 
	 * Send
	 * 
	 * @param path
	 * @return
	 * @throws IOException 
	 */
	public static void send(HttpServletResponse response, String jsonString) throws IOException {

		if (jsonString == null) 
			throw new NullPointerException("response is not set");
		
		byte[] bValue = jsonString.getBytes("utf-8");

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");

		final OutputStream cOs = response.getOutputStream();
		
		cOs.write(bValue);
		
		cOs.close();
	}

	/*************************************************************************
	 * error
	 * 
	 * @param response
	 * @param code
	 */
	public static void error(HttpServletResponse response, int code) {
	
		response.setStatus(code);
	}
	
	/*************************************************************************
	 * send status
	 * 
	 * @param response
	 * @param code
	 */
	public static void sendStatus(HttpServletResponse response, int code) {
	
		response.setStatus(code);
	}
	
	/*************************************************************************
	 * error with response
	 * 
	 * @param response
	 * @param code
	 */
	public void errorWithResponse(HttpServletResponse response, int code) throws IOException {
		
		if (mResponse == null) 
			throw new NullPointerException("response is not set");
		
		String val = mMapper.writeValueAsString(mResponse);
		byte[] bValue = val.getBytes("utf-8");

		response.setStatus(code);
		response.setContentType("application/json");

		final OutputStream cOs = response.getOutputStream();
		
		cOs.write(bValue);
		
		cOs.close();
	
	}
}