<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml">

<xsl:template match="/">
    <myXmlFormat>
		<xsl:apply-templates select="//xhtml:tr"/>
	</myXmlFormat>
</xsl:template>

<!-- attributes -->
<xsl:template match="xhtml:thead/xhtml:tr">
	<attributes>
	<xsl:apply-templates mode="attributes" />
	</attributes>
</xsl:template>
<xsl:template match="xhtml:th" mode="attributes">
	<attribute>
		<xsl:value-of select="."/>
	</attribute>
</xsl:template>

<!-- values -->
<xsl:template match="xhtml:tbody/xhtml:tr">
    <records>
	<xsl:apply-templates mode="values" />
	</records>
</xsl:template>
<xsl:template match="xhtml:td" mode="values">
	<record>
		<xsl:value-of select="."/>
	</record>
</xsl:template>

<!-- 
(Based on "T_AGG_EMPLOYEE - Basis: Top Employee per Office" DEMO Variant
but you can use this in other dynamic list variants as well)
(Version: 1.0.0)
(Last tested with: ReportServer 4.0.0) 
-->

</xsl:stylesheet>