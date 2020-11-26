<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ark="http://www.arkivverket.no/standarder/noark5/arkivstruktur"
		version="2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="journalregistrering_xml"/>

	<xsl:template match="node()|@*" name="identity">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//ark:loependeJournal">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:apply-templates select="collection($journalregistrering_xml)"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>