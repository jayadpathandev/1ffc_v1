/*
 * (c) Copyright 2024 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
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
package com.sorrisotech.pdf.service;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**********************************************************************************************
 * The document class for performing actions on the PDF.
 * 
 * @author Rohit Singh
 */
public class PDFDocument implements Closeable {

	/**********************************************************************************************
	 * Logger for system logging.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PDFDocument.class);
	
	/**********************************************************************************************
	 * The instance of the template pdf.
	 */
	private final PDDocument doc;
	
	/**********************************************************************************************
	 * The instance of template pdf form.
	 */
	private final PDAcroForm form;

	public PDFDocument(String url) throws IOException {
		this(new URL(url).openStream());
		LOGGER.debug("The path of the template pdf : {}", url);
	}

	public PDFDocument(InputStream pdfStream) throws IOException {
		doc = Loader.loadPDF(pdfStream.readAllBytes());
		form = doc.getDocumentCatalog().getAcroForm();
	}

	/**
	 * Gets all the form fields of the PDF.
	 * 
	 * @return map of form fields
	 */
	public LinkedHashMap<String, Object> getFields() {
		var iter = doc.getDocumentCatalog().getAcroForm().getFieldTree().iterator();
		var result = new LinkedHashMap<String, Object>();
		while (iter.hasNext()) {
			var field = iter.next();
			if (!field.isReadOnly() && field.getFieldType() != null) {
				result.put(field.getFullyQualifiedName(), field.getValueAsString());
			}
		}
		return result;
	}

	/**
	 * Converts the PDF to the byte array.
	 * 
	 * @return byte array of teh PDF.
	 * @throws IOException
	 */
	public byte[] getBytes() throws IOException {
		try (var os = new ByteArrayOutputStream()) {
			form.flatten();
			doc.save(os);
			return os.toByteArray();
		}
	}

	/**
	 * Sets a field value to the PDF form.
	 * 
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	public void setFieldValue(String name, Object value) throws IOException {
		var field = form.getField(name);
		if (field != null) {
			field.setValue(value == null ? "" : value.toString());
		}
	}

	/**
	 * Closes the PDF form
	 */
	@Override
	public void close() throws IOException {
		doc.close();
	}
}
