<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:wf="xalan://com.sorrisotech.uc.application.web.xsltfront.xslt.WfExtension" 
                extension-element-prefixes="wf">
                
	<xsl:output method="html" encoding="UTF-8" doctype-system="about:legacy-compat"/>
    
    <xsl:include href="elements/data_message.xsl"/>
    <xsl:include href="elements/date.xsl"/>
    <xsl:include href="elements/displayText.xsl"/>
    <xsl:include href="elements/groupwrapper.xsl"/>
    <xsl:include href="elements/image.xsl"/>
    <xsl:include href="elements/input.xsl"/>
    <xsl:include href="elements/link.xsl"/>
    <xsl:include href="elements/menu.xsl"/>
    <xsl:include href="elements/screen_message.xsl"/>
    <xsl:include href="elements/select.xsl"/>
    <xsl:include href="elements/submit.xsl"/>
    <xsl:include href="elements/structure.xsl"/>
    <xsl:include href="elements/table.xsl"/>
    <xsl:include href="elements/tag.xsl"/>
    <xsl:include href="elements/textarea.xsl"/>
           
    <xsl:template match="attributes"/>
    <xsl:template match="messages"/>
    
    <xsl:variable name="settings" select="document('settings.xml')"/>
       
    <xsl:template match="screen">
        <html lang="en">

            <!-- ############################################################################## -->
            <head>
				<meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
            	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
            	<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
            	<meta charset="utf-8"/>
                <title>
					<wf:intntl key="{/screen/usecase}_page.title.{/screen/name}" default="{/screen/title}"/>
                </title>
                                
                <xsl:value-of select="$settings/config/header/common" disable-output-escaping="yes"/>      
                
                <xsl:for-each select="attributes/css">
                    <link rel="stylesheet" href="{.}"/>
                </xsl:for-each>         
                <xsl:for-each select="attributes/script">
                    <script src="{.}"></script>
                </xsl:for-each>         
            </head>

            <!-- ############################################################################## -->
            <body class="{$settings/config/class/body} {attributes/body_class}">
                           
               	<xsl:if test="$settings/config/props/default_app != ''">
               		<xsl:attribute name="ng-app"><xsl:value-of select="$settings/config/props/default_app"/></xsl:attribute>
               	</xsl:if>
                           
                <!-- ########################################################################## -->
                <xsl:if test="attributes/body_class_override">
                    <xsl:attribute name="class"><xsl:value-of select="attributes/body_class_override"/></xsl:attribute>
                </xsl:if>
            
                <!-- ########################################################################## -->
                <xsl:for-each select="attributes/*">
                    <xsl:choose>
                        <xsl:when test="starts-with(name(), 'body_attr_')">
                          <xsl:attribute name="{substring(name(), 11)}"><xsl:value-of select="."/></xsl:attribute>
                        </xsl:when>                     
                        <xsl:when test="starts-with(name(), 'body_ng')">
                          <xsl:attribute name="{substring(name(), 6)}"><xsl:value-of select="."/></xsl:attribute>
                        </xsl:when>                     
                    </xsl:choose>
                </xsl:for-each>

				<xsl:if test="/screen/group/groupwrapper[contains(@id, 'impersonationFrame')]/displayText[id='sImpersonationActive']/value='true'">
                    <xsl:attribute name="class">impersonation-active</xsl:attribute>
                </xsl:if>
                                                            
                <!-- ########################################################################## -->
				<xsl:value-of select="$settings/config/header/banner" disable-output-escaping="yes"/>
                
                <!-- ########################################################################## -->
                <xsl:if test="group/groupwrapper[@section='sti_menu']">
	                <div id="main-menu" class="container">
						<div class="row">
							<div class="col-6 col-sm-5 col-md-4 col-lg-3 col-xxl-2"/>
							<div class="col-6 col-sm-7 col-md-8 col-lg-9 col-xxl-10">
								<xsl:apply-templates select="group/groupwrapper[@section='sti_menu']"/>
							</div>
						</div>
					</div>
				</xsl:if>                
                <!-- ########################################################################## -->
				<main>                	
	                <xsl:if test="group/groupwrapper[@section='sti_menu']">
                		<xsl:attribute name="class">has-menu</xsl:attribute>
                	</xsl:if>
                	<xsl:if test="$settings/config/props/default_controller_init != ''">
                		<xsl:attribute name="ng-init"><xsl:value-of select="$settings/config/props/default_controller_init"/></xsl:attribute>
                	</xsl:if>
                	<xsl:if test="$settings/config/props/default_controller != ''">
                		<xsl:attribute name="ng-controller"><xsl:value-of select="$settings/config/props/default_controller"/></xsl:attribute>
                	</xsl:if>
                	
                    <!-- ###################################################################### -->
                    <xsl:if test="attributes/class_override">
                        <xsl:attribute name="class"><xsl:value-of select="attributes/class_override"/></xsl:attribute>
                    </xsl:if>
                
                    <!-- ###################################################################### -->
                    <xsl:for-each select="attributes/*">
                        <xsl:choose>
                            <xsl:when test="starts-with(name(), 'attr_')">
                              <xsl:attribute name="{substring(name(), 6)}"><xsl:value-of select="."/></xsl:attribute>
                            </xsl:when>                     
                            <xsl:when test="starts-with(name(), 'ng')">
                              <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                            </xsl:when>                     
                        </xsl:choose>
                    </xsl:for-each>
                
					<!-- ###################################################################### -->
					<xsl:apply-templates select="group/*[@section='header']"></xsl:apply-templates>
					
                	<div class="container messages-container">
						<!-- ###################################################################### -->
						<xsl:if test="not(//attributes[messages])">
							<xsl:apply-templates select="screen_messages/*[@association='screen']"/>
						</xsl:if>
                	</div>
					
	                <div class="container content-container">
						<!-- ###################################################################### -->
						<xsl:apply-templates select="group/*[not(@section='sti_menu' or @section='header')]"/>
                	</div>
                	
                	<div class="footer-spacer"></div>
				</main>
				
				<xsl:if test="$settings/config/props/expiring = 'message'">
	                <div ng-controller="sessionExpire" id="expires" sorriso="expires" at="{context/timeout}">
	                </div>
				</xsl:if>                 
                <xsl:if test="/screen/languages">
                    <div class="st-languages">
                        <xsl:for-each select="/screen/languages/language">
                            <span class="st-language">
                                <a id="{i18n}" href="{/*/urlname}?_internalMovement={value}&amp;_pagekey={/*/pagekey}" tabindex="-1">
                                    <xsl:if test="@country=''">
                                       <wf:intntl key="{i18n}" default="{@language}"/>
                                    </xsl:if>
                                    <xsl:if test="@country!=''">
                                       <wf:intntl key="{i18n}" default="{@language}-{@country}"/>
                                    </xsl:if>                                       
                                </a>
                            </span>
                        </xsl:for-each>
                    </div>
                </xsl:if>
                    
                <!-- ########################################################################## -->
				<xsl:value-of select="$settings/config/footer" disable-output-escaping="yes"/>
            </body>
        </html>
    </xsl:template>        
    
    <xsl:template match="fragment">
        <xsl:apply-templates select="group/*[not(@section='sti_menu')]"/>
    </xsl:template> 
    
    <xsl:template match="logicaction">

    </xsl:template> 

    <!-- ====================================================================================== -->
    <!-- = Finds the name of the form an element is in.                                       = -->
    <!-- ====================================================================================== -->
    <xsl:template name="get_form_name">
        <xsl:variable name="id" select="ancestor::*[@type='form'][1]/@id"/>
        <xsl:variable name="suffix" select="concat('_', ancestor::*[@type='form'][1]/@ucInstance)"/>
        <xsl:variable name="name" select="substring-before($id, $suffix)"/>
        
        <xsl:choose>
            <xsl:when test="$name != ''">
                <xsl:value-of select="$name"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$id"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- ====================================================================================== -->
    <!-- = This templates calculates the angularJS model for an element.                      = -->
    <!-- ====================================================================================== -->
    <xsl:template name="get_model">
        <xsl:choose>
            <xsl:when test="ancestor::*[@type='form'][1]/attributes/scope">
                <xsl:value-of select="ancestor::*[@type='form'][1]/attributes/scope"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>form</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="name()='item' and name(../..) = 'structure' and ../@display='radio'">\['<xsl:value-of select="../../@id"/>'\]</xsl:when>
            <xsl:when test="name()='item' and name(../..) = 'structure' and ../@display='checkbox'">\['<xsl:value-of select="../../@id"/>'\]\['<xsl:value-of select="id"/>'\]</xsl:when>
            <xsl:when test="name(..) = 'structure' and displayDataType='control'">\['<xsl:value-of select="../@id"/>'\]</xsl:when>
            <xsl:when test="name()='item' and ../@display='radio'">\['<xsl:value-of select="../id"/>'\]</xsl:when>
            <xsl:when test="name()='item' and ../@display='checkbox'">\['<xsl:value-of select="../id"/>'\]\['<xsl:value-of select="id"/>'\]</xsl:when>
            <xsl:when test="name(..) = 'structure'">\['<xsl:value-of select="id"/>'\]</xsl:when>
            <xsl:when test="name()='tableselect' and @display='radio'">\['<xsl:value-of select="@id"/>'\]</xsl:when>
            <xsl:when test="name()='tableselect' and @display='checkbox'">\['<xsl:value-of select="@id"/>'\]\['<xsl:value-of select="@optionvalue"/>'\]</xsl:when>
            <xsl:otherwise>\['<xsl:value-of select="id"/>'\]</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ====================================================================================== -->
    <!-- = This templates calculates the angularJS model for an element.                      = -->
    <!-- ====================================================================================== -->
    <xsl:template name="embedded_require">
        <xsl:param name="item"/>
        
	    <xsl:attribute name="ng-required">
	        <xsl:choose>
	            <xsl:when test="ancestor::*[@type='form'][1]/attributes/scope">
	                <xsl:value-of select="ancestor::*[@type='form'][1]/attributes/scope"/>
	            </xsl:when>
	            <xsl:otherwise>
	                <xsl:text>form</xsl:text>
	            </xsl:otherwise>
	        </xsl:choose>
	        <xsl:text>\['</xsl:text><xsl:value-of select="../@id"/><xsl:text>'\]</xsl:text>

	        <xsl:if test="../*[displayDataType='control']/@display = 'checkbox'">
	           <xsl:text>\['</xsl:text><xsl:value-of select="$item"/><xsl:text>'\]</xsl:text>
	        </xsl:if>
            <xsl:if test="../*[displayDataType='control']/@display = 'radio'">
                <xsl:text> == '</xsl:text><xsl:value-of select="$item"/><xsl:text>'</xsl:text>
            </xsl:if>
	        
	    </xsl:attribute>
    
    </xsl:template>
    
    <!-- ====================================================================================== -->
    <!-- = This template set the ng_model attribute for an form input field.                  = -->
    <!-- ====================================================================================== -->
    <xsl:template name="set_model">
        <xsl:attribute name="ng-model">
            <xsl:call-template name="get_model"/>
        </xsl:attribute>
    </xsl:template>

    <!-- ====================================================================================== -->
    <!-- = This template adds the I18N debug code to the parent element.                      = -->
    <!-- ====================================================================================== -->
    <xsl:template name="add_i18n">
        <xsl:param name="key" select="i18n"/>
        
		<xsl:if test="$settings/config/props/i18n_keys">
		    <xsl:attribute name="st-i18n"/>
		    <xsl:attribute name="data-bs-toggle">tooltip</xsl:attribute>
		    <xsl:attribute name="data-placement" >
		        <xsl:value-of select="$settings/config/props/i18n_keys"/>
		    </xsl:attribute> 
		    <xsl:attribute name="data-placement" >
		        <xsl:value-of select="$settings/config/props/i18n_keys"/>
		    </xsl:attribute> 
		    <xsl:attribute name="title">
		        <xsl:value-of select="$key"/>
		    </xsl:attribute> 
		</xsl:if>               
    </xsl:template>       
</xsl:stylesheet>
