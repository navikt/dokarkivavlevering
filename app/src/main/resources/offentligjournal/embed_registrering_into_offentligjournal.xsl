<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ark="http://www.arkivverket.no/standarder/noark5/offentligJournal"
		version="2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="offentligjournal_xml"/>

	<xsl:variable name="journalregistrering" select="collection($offentligjournal_xml)/ark:offentligJournal/ark:journalregistrering"/>

	<xsl:template match="node()|@*" name="identity">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//ark:offentligJournal[not(ark:journalregistrering)]">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:for-each select="$journalregistrering">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>