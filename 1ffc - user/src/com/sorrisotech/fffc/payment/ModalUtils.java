package com.sorrisotech.fffc.payment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.app.utils.Freemarker;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class ModalUtils {

	/***************************************************************************
     * Logger for this class.
     */
    private static final Logger m_cLog = LoggerFactory.getLogger(ModalUtils.class);
    
    /**
	 * Get the requested modal Freemarker template and resolve it to a string.
	 * 
	 * @param cUserData User data.
	 * @param cServiceLocator Service locator.
	 * @param szTemplateName Name of template file.
	 * @param szPayGroup The default payGroup
	 * @param szModalId The id for modal
	 * @param szModalTitle The title of the modal
	 * @param szModalBody The content of the modal
	 * @param szLanguageCode Language code.
	 * @param szCountryCode Country code.
	 * @return Resolved template content.
	 * @throws MargaritaDataException 
	 */
	public String getModalTemplate(
			IUserData cUserData,
			IServiceLocator2 cServiceLocator,
			String szTemplateName,
			String szPayGroup,
			String szModalId,
			String szModalTitle,
			String szModalBody,
			String szLanguageCode,
			String szCountryCode) throws MargaritaDataException {
		
		String szResult = "";
		
		try {
			Map<String, Object> cRoot = new HashMap<>();
			
			cRoot.put("modalId", szModalId);
			cRoot.put("modalTitle", szModalTitle);
			cRoot.put("modalBody", szModalBody);
		
			szResult = Freemarker.resolveFreemarkerTemplate(
					cServiceLocator, 
					cUserData,
					szTemplateName, 
					cRoot,
					szPayGroup,
					szLanguageCode,
					szCountryCode);
		} catch (TemplateNotFoundException e) {
			m_cLog.error("getModalTemplate(): error see trace", e);
		} catch (MalformedTemplateNameException e) {
			m_cLog.error("getModalTemplate(): error see trace", e);
		} catch (ParseException e) {
			m_cLog.error("getModalTemplate(): error see trace", e);
		} catch (IOException e) {
			m_cLog.error("getModalTemplate(): error see trace", e);
		} catch (TemplateException e) {
			m_cLog.error("getModalTemplate(): error see trace", e);
		}
		
		return szResult;
	}
}
