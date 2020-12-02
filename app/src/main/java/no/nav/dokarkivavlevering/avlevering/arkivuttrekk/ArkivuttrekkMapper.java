package no.nav.dokarkivavlevering.avlevering.arkivuttrekk;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.AvleveringRoute;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.apache.commons.io.FileUtils.readFileToByteArray;

@Slf4j
@Component
public class ArkivuttrekkMapper {

	private static final String AVLEVERING_SLUTTDATO = "$AVLEVERING_SLUTTDATO$";
	private static final String AVLEVERING_ANTALLDOKUMENTER = "$AVLEVERING_ANTALLDOKUMENTER$";
	private static final String METADATAKATALOG_XSD_SJEKKSUM = "$METADATAKATALOG_XSD_SJEKKSUM$";

	private static final String ARKIVSTRUKTUR_XML_SJEKKSUM = "$ARKIVSTRUKTUR_XML_SJEKKSUM$";
	private static final String ARKIVSTRUKTUR_XSD_SJEKKSUM = "$ARKIVSTRUKTUR_XSD_SJEKKSUM$";
	private static final String ARKIVSTRUKTUR_ANTALL_MAPPE = "$ARKIVSTRUKTUR_ANTALL_MAPPE$";
	private static final String ARKIVSTRUKTUR_ANTALL_REGISTRERING = "$ARKIVSTRUKTUR_ANTALL_REGISTRERING$";

	private static final String ENDRINGSLOGG_XML_SJEKKSUM = "$ENDRINGSLOGG_XML_SJEKKSUM$";
	private static final String ENDRINGSLOGG_XSD_SJEKKSUM = "$ENDRINGSLOGG_XSD_SJEKKSUM$";
	private static final String ENDRINGSLOGG_ANTALL_ENDRING = "$ENDRINGSLOGG_ANTALL_ENDRING$";

	private static final String LOEPENDEJOURNAL_XML_SJEKKSUM = "$LOEPENDEJOURNAL_XML_SJEKKSUM$";
	private static final String LOEPENDEJOURNAL_XSD_SJEKKSUM = "$LOEPENDEJOURNAL_XSD_SJEKKSUM$";
	private static final String LOEPENDEJOURNAL_ANTALL_JOURNALREGISTRERING = "$LOEPENDEJOURNAL_ANTALL_JOURNALREGISTRERING$";

	private static final String OFFENTLIGJOURNAL_XML_SJEKKSUM = "$OFFENTLIGJOURNAL_XML_SJEKKSUM$";
	private static final String OFFENTLIGJOURNAL_XSD_SJEKKSUM = "$OFFENTLIGJOURNAL_XSD_SJEKKSUM$";
	private static final String OFFENTLIGJOURNAL_ANTALL_JOURNALREGISTRERING = "$OFFENTLIGJOURNAL_ANTALL_JOURNALREGISTRERING$";


	@Value("${avlevering.filomraade.work}")
	private String filomraade;
	private String avleveringId;

	private final AvleveringProperties avleveringProperties;

	@Autowired
	public ArkivuttrekkMapper(AvleveringProperties avleveringProperties) {
		this.avleveringProperties = avleveringProperties;
	}

	@Handler
	public String insertValues(@ExchangeProperty(AvleveringRoute.PROPERTY_AVLEVERING_ID) final String avleveringId) throws Exception {
		this.avleveringId = avleveringId;
		Path path = Paths.get(ClassLoader.getSystemResource("arkivuttrekk/arkivuttrekk_template.xml").toURI());

		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
				.replace(AVLEVERING_SLUTTDATO, avleveringProperties.getPeriode().getSluttdato().toString())
				.replace(AVLEVERING_ANTALLDOKUMENTER, countElements("arkivstruktur.xml", "dokumentobjekt")) // FIXME: Er dette en grei nok måte å sjekke dette på ?
				.replace(METADATAKATALOG_XSD_SJEKKSUM, generateSHA256("metadatakatalog.xsd"))
				.replace(ARKIVSTRUKTUR_XML_SJEKKSUM, generateSHA256("arkivstruktur.xml"))
				.replace(ARKIVSTRUKTUR_XSD_SJEKKSUM, generateSHA256("arkivstruktur.xsd"))
				.replace(ARKIVSTRUKTUR_ANTALL_MAPPE, countElements("arkivstruktur.xml", "mappe"))
				.replace(ARKIVSTRUKTUR_ANTALL_REGISTRERING, countElements("arkivstruktur.xml", "registrering"))
				.replace(ENDRINGSLOGG_XML_SJEKKSUM, generateSHA256("endringslogg.xml"))
				.replace(ENDRINGSLOGG_XSD_SJEKKSUM, generateSHA256("endringslogg.xsd"))
				.replace(ENDRINGSLOGG_ANTALL_ENDRING, countElements("endringslogg.xml", "endring"))
				.replace(LOEPENDEJOURNAL_XML_SJEKKSUM, generateSHA256("loependejournal.xml"))
				.replace(LOEPENDEJOURNAL_XSD_SJEKKSUM, generateSHA256("loependejournal.xsd"))
				.replace(LOEPENDEJOURNAL_ANTALL_JOURNALREGISTRERING, countElements("loependejournal.xml", "journalregistrering"))
				.replace(OFFENTLIGJOURNAL_XML_SJEKKSUM, generateSHA256("offentligjournal.xml"))
				.replace(OFFENTLIGJOURNAL_XSD_SJEKKSUM, generateSHA256("offentligjournal.xsd"))
				.replace(OFFENTLIGJOURNAL_ANTALL_JOURNALREGISTRERING, countElements("offentligjournal.xml", "journalregistrering"));
	}

	private String generateSHA256(String fileName) throws IOException {
		File file;
		if (fileName.endsWith(".xml")) {
			file = Paths.get(filomraade + "/" + avleveringId + "/" + fileName).toFile();
		} else {
			file = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)).getFile());
		}
		return DigestUtils.sha256Hex(readFileToByteArray(file));
	}

	private String countElements(String fileName, String element) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(filomraade + "/" + avleveringId + "/" + fileName);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return String.valueOf(Objects.requireNonNull(doc).getElementsByTagName(element).getLength());
	}
}
