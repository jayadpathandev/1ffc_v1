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
package com.sorrisotech.client.model.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/******************************************************************************
 * The request POJO for add document endpoint.
 * 
 * @author Rohit Singh
 */
@JsonInclude(value = Include.NON_NULL)
public class AddDocumentRequest {
	
	/******************************************************************************
	 * Maximum of 20 characters. Can only contain A-Z, a-z, 0-9, and _.
	 */
	@JsonProperty("ShortName")
	public String m_szShortName;
	
	/******************************************************************************
	 * Maximum of 100 characters. Can only contain A-Z, a-z, 0-9, and _.
	 */
	@JsonProperty("LongName")
	public String m_szLongName;
	
	/******************************************************************************
	 * Maximum of 150 characters. This is the user friendly name of the document.
	 */
	@JsonProperty("Description")
	public String m_szDescription;
	
	/******************************************************************************
	 * [Optional] Indicate if the document is dynamic
	 */
	@JsonProperty("IsDynamic")
	public boolean m_bIsDynamic;
	
	/******************************************************************************
	 * [Optional] Set to true if the operator needs to interact with this document 
	 * when click on "Process" in the Session Details page in eSign
	 */
	@JsonProperty("DisplayInProcessing")
	public boolean m_bDisplayInProcessing;
	
	/******************************************************************************
	 * [Optional] Indicate if this document will be archived
	 */
	@JsonProperty("Archive")
	public boolean m_bArchive;
	
	/******************************************************************************
	 * [Optional] If the document has interactive fields, set this to true. 
	 * If the system already has a way to flatten the PDF, do not pass this field
	 */
	@JsonProperty("Flatten")
	public boolean m_bFlatten;
	
	/******************************************************************************
	 * [Optional] Indicate if the document should be eVaulted. If not using, 
	 * do not pass this field
	 */
	@JsonProperty("EVault")
	public boolean m_bEVault;
	
	/******************************************************************************
	 * [Optional] Indicate if a document definition/template needs to be used. 
	 * If not using, do not pass this field
	 */ 
	@JsonProperty("UseDocumentDefinition")
	public boolean m_bUseDocumentDefinition;
	
	/******************************************************************************
	 * [Optional] Indicate if the document needs to be split by document type. 
	 * If not using, do not pass this field
	 */
	@JsonProperty("SplitPDF")
	public boolean m_bSplitPDF;
	
	/******************************************************************************
	 * 64-base encoded string of the document
	 */
	@JsonProperty("PDFFileContents")
	public String m_szPDFFileContents;
	
	/******************************************************************************
	 * [Optional] Document indexes defined in eSign used for searching sessions in eSign
	 */
	@JsonProperty("DocumentIndexes")
	public List<DocumentIndex> m_cDocumentIndexes;
	
	/******************************************************************************
	 * [Optional]
	 */
	@JsonProperty("Fields")
	public List<Field> m_cFields;
	
	/******************************************************************************
	 * [Optional] This is where all input fields will be added. Additionally, 
	 * fields can also be added on the document itself using our naming convention. 
	 * See API documentation for more information.
	 */
	@JsonProperty("PartyMappings")
	public List<PartyMapping> m_cPartyMappings;

	public AddDocumentRequest(
			String shortName, 
			String longName, 
			String description, 
			String pDFFileContents) {
		this.m_szShortName = shortName;
		this.m_szLongName = longName;
		this.m_szDescription = description;
		this.m_szPDFFileContents = pDFFileContents;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AddDocumentRequest [m_szShortName=");
		builder.append(m_szShortName);
		builder.append(", m_szLongName=");
		builder.append(m_szLongName);
		builder.append(", m_szDescription=");
		builder.append(m_szDescription);
		builder.append(", m_bIsDynamic=");
		builder.append(m_bIsDynamic);
		builder.append(", m_bDisplayInProcessing=");
		builder.append(m_bDisplayInProcessing);
		builder.append(", m_bArchive=");
		builder.append(m_bArchive);
		builder.append(", m_bFlatten=");
		builder.append(m_bFlatten);
		builder.append(", m_bEVault=");
		builder.append(m_bEVault);
		builder.append(", m_bUseDocumentDefinition=");
		builder.append(m_bUseDocumentDefinition);
		builder.append(", m_bSplitPDF=");
		builder.append(m_bSplitPDF);
		builder.append(", m_szPDFFileContents=");
		builder.append(m_szPDFFileContents);
		builder.append(", m_cDocumentIndexes=");
		builder.append(m_cDocumentIndexes);
		builder.append(", m_cFields=");
		builder.append(m_cFields);
		builder.append(", m_cPartyMappings=");
		builder.append(m_cPartyMappings);
		builder.append("]");
		return builder.toString();
	}

	public static class DocumentIndex {
		
		/******************************************************************************
		 * Index Field Name
		 */
		@JsonProperty("Name")
		public String m_szName;
		
		/******************************************************************************
		 * Index Field Value
		 */
		@JsonProperty("Value")
		public String m_szValue;

		public DocumentIndex(String name, String value) {
			this.m_szName = name;
			this.m_szValue = value;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("DocumentIndex [m_szName=");
			builder.append(m_szName);
			builder.append(", m_szValue=");
			builder.append(m_szValue);
			builder.append("]");
			return builder.toString();
		}
		
	}

	public static class Field {
		
		/******************************************************************************
		 * [Optional] naming convention for the signature field
		 */
		@JsonProperty("Name")
		public String m_szName;
		
		/******************************************************************************
		 * Page number where the signature will be added
		 */
		@JsonProperty("PageNumber")
		public int m_iPageNumber;
		
		/******************************************************************************
		 * This is where the coordinates of the signature fields will go
		 */
		@JsonProperty("BBox")
		public BBox m_cBBox;
		
		/******************************************************************************
		 * Which signer this field will be assigned to
		 */
		@JsonProperty("PartyIndex")
		public String m_szPartyIndex;
		
		/******************************************************************************
		 * Type of field
		 */
		@JsonProperty("FieldType")
		public String m_szFieldType;

		public Field(
				String name, 
				int pageNumber, 
				BBox bBox, 
				String partyIndex, 
				String fieldType) {
			this.m_szName = name;
			this.m_iPageNumber = pageNumber;
			this.m_cBBox = bBox;
			this.m_szPartyIndex = partyIndex;
			this.m_szFieldType = fieldType;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Field [m_szName=");
			builder.append(m_szName);
			builder.append(", m_iPageNumber=");
			builder.append(m_iPageNumber);
			builder.append(", m_cBBox=");
			builder.append(m_cBBox);
			builder.append(", m_szPartyIndex=");
			builder.append(m_szPartyIndex);
			builder.append(", m_szFieldType=");
			builder.append(m_szFieldType);
			builder.append("]");
			return builder.toString();
		}

		public static class BBox {
			
			/******************************************************************************
			 * [Optional] Horizontal position of the field, in pixels, starting from the left of the page
			 */
			@JsonProperty("X")
			public int m_iX;
			
			/******************************************************************************
			 * [Optional] Vertical position of the field, in pixels, starting from the bottom of the page
			 */
			@JsonProperty("Y")
			public int m_iY;
			
			/******************************************************************************
			 * [Optional] Width of the field in pixels
			 */
			@JsonProperty("Width")
			public int m_iWidth;
			
			/******************************************************************************
			 * [Optional] Height of the field in pixels
			 */
			@JsonProperty("Height")
			public int m_iHeight;
			
			/******************************************************************************
			 * [Optional] Vertical position of the field, in pixels, starting from the top of the page. 
			 * If UpperLeftY and Y parameter are passed, then Y will take precedence.
			 */
			@JsonProperty("UpperLeftY")
			public int m_iUpperLeftY;

			public BBox(
					int m_iX, 
					int m_iY, 
					int m_iWidth, 
					int m_iHeight) {
				this.m_iX = m_iX;
				this.m_iY = m_iY;
				this.m_iWidth = m_iWidth;
				this.m_iHeight = m_iHeight;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("BBox [m_iX=");
				builder.append(m_iX);
				builder.append(", m_iY=");
				builder.append(m_iY);
				builder.append(", m_iWidth=");
				builder.append(m_iWidth);
				builder.append(", m_iHeight=");
				builder.append(m_iHeight);
				builder.append(", m_iUpperLeftY=");
				builder.append(m_iUpperLeftY);
				builder.append("]");
				return builder.toString();
			}
		}
	}

	public static class PartyMapping {
		
		/******************************************************************************
		 * The party full name
		 */
		@JsonProperty("FullName")
		public String m_szFullName;
		
		/******************************************************************************
		 * The party id
		 */
		@JsonProperty("PartyId")
		public String m_szPartyId;
		
		/******************************************************************************
		 * [Optional] The action of the party
		 */
		@JsonProperty("Action")
		public String m_szAction = "sign";

		public PartyMapping(
				String fullName, 
				String partyId) {
			this.m_szFullName = fullName;
			this.m_szPartyId = partyId;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PartyMapping [m_szFullName=");
			builder.append(m_szFullName);
			builder.append(", m_szPartyId=");
			builder.append(m_szPartyId);
			builder.append(", m_szAction=");
			builder.append(m_szAction);
			builder.append("]");
			return builder.toString();
		}
	}
}
