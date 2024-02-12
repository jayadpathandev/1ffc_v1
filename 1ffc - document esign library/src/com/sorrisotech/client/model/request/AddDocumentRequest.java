package com.sorrisotech.client.model.request;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddDocumentRequest {
	
	@JsonProperty("ShortName")
	public String m_szShortName;
	
	@JsonProperty("LongName")
	public String m_szLongName;
	
	@JsonProperty("Description")
	public String m_szDescription;
	
	/**
	 * Optinal
	 */
	@JsonProperty("IsDynamic")
	public boolean m_bIsDynamic;
	
	/**
	 * Optinal
	 */
	@JsonProperty("DisplayInProcessing")
	public boolean m_bDisplayInProcessing;
	
	/**
	 * Optinal
	 */
	@JsonProperty("Archive")
	public boolean m_bArchive;
	
	/**
	 * Optinal
	 */
	@JsonProperty("Flatten")
	public boolean m_bFlatten;
	
	/**
	 * Optinal
	 */
	@JsonProperty("EVault")
	public boolean m_bEVault;
	
	/**
	 * Optinal
	 */
	@JsonProperty("UseDocumentDefinition")
	public boolean m_bUseDocumentDefinition;
	
	/**
	 * Optinal
	 */
	@JsonProperty("SplitPDF")
	public boolean m_bSplitPDF;
	
	@JsonProperty("PDFFileContents")
	public String m_szPDFFileContents;
	
	/**
	 * Optinal
	 */
	@JsonProperty("DocumentIndexes")
	public ArrayList<DocumentIndex> m_cDocumentIndexes;
	
	/**
	 * Optinal
	 */
	@JsonProperty("Fields")
	public ArrayList<Field> m_cFields;
	
	/**
	 * Optinal
	 */
	@JsonProperty("PartyMappings")
	public ArrayList<PartyMapping> m_cPartyMappings;

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

	public static class DocumentIndex {
		@JsonProperty("Name")
		public String m_szName;
		
		/**
		 * Optinal
		 */
		@JsonProperty("Value")
		public String m_szValue;

		public DocumentIndex(String name) {
			this.m_szName = name;
		}
		
	}

	public static class Field {
		
		@JsonProperty("Name")
		public String m_szName;
		
		@JsonProperty("PageNumber")
		public int m_iPageNumber;
		
		@JsonProperty("BBox")
		public BBox m_cBBox;
		
		@JsonProperty("PartyIndex")
		public String m_szPartyIndex;
		
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

		public static class BBox {
			
			/**
			 * Optinal
			 */
			@JsonProperty("X")
			public int m_iX;
			
			/**
			 * Optinal
			 */
			@JsonProperty("Y")
			public int m_iY;
			
			/**
			 * Optinal
			 */
			@JsonProperty("Width")
			public int m_iWidth;
			
			/**
			 * Optinal
			 */
			@JsonProperty("Height")
			public int m_iHeight;
			
			/**
			 * Optinal
			 */
			@JsonProperty("UpperLeftY")
			public int m_iUpperLeftY;
		}
	}

	public static class PartyMapping {
		
		@JsonProperty("FullName")
		public String m_szFullName;
		
		@JsonProperty("PartyId")
		public String m_szPartyId;
		
		/**
		 * Optinal
		 */
		@JsonProperty("Action")
		public String m_szAction;

		public PartyMapping(
				String fullName, 
				String partyId) {
			this.m_szFullName = fullName;
			this.m_szPartyId = partyId;
		}
	}
}
