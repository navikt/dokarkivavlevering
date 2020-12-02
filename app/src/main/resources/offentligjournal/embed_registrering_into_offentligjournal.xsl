<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ofj="http://www.arkivverket.no/standarder/noark5/offentligJournal"
		version="2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="offentligjournal_xml"/>

	<xsl:variable name="journalregistrering" select="collection($offentligjournal_xml)/ofj:offentligJournal/ofj:journalregistrering"/>

	<xsl:template match="node()|@*" name="identity">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//ofj:offentligJournal[not(ofj:journalregistrering)]">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:for-each select="$journalregistrering">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//ofj:offentligJournal/ofj:journalhode/ofj:antallJournalposter/text()">
		<xsl:value-of select="count($journalregistrering)"/>
	</xsl:template>
</xsl:stylesheet>