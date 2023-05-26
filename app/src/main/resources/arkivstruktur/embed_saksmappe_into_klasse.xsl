<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ark="http://www.arkivverket.no/standarder/noark5/arkivstruktur"
		version="2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="saksmappe_xml"/>

	<xsl:variable name="saksmapper" select="collection($saksmappe_xml)/ark:klasse/ark:mappe"/>

	<xsl:template match="node()|@*" name="identity">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//ark:klasse[not(ark:mappe)]">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:for-each select="$saksmapper">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>