<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:loj="http://www.arkivverket.no/standarder/noark5/loependeJournal"
		version="2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="loependejournal_xml"/>

	<xsl:variable name="journalregistrering" select="collection($loependejournal_xml)/loj:loependeJournal/loj:journalregistrering"/>

	<xsl:template match="node()|@*" name="identity">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//loj:loependeJournal[not(loj:journalregistrering)]">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:for-each select="$journalregistrering">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//loj:loependeJournal/loj:journalhode/loj:antallJournalposter/text()">
		<xsl:value-of select="count($journalregistrering)"/>
	</xsl:template>
</xsl:stylesheet>