<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ark="http://www.arkivverket.no/standarder/noark5/endringslogg"
		version="2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="endring_xml"/>

	<xsl:template match="node()|@*" name="identity">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//ark:endringslogg">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:apply-templates select="collection($endring_xml)/ark:endringslogg/ark:endring"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>