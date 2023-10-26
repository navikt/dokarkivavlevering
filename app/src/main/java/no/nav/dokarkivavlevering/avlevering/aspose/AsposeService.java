package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.PdfFormat;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorUtil.safeValidatePDFA;

@Slf4j
@Component
public class AsposeService {

	private static final License lic = new License();

	@Autowired
	public AsposeService(AvleveringProperties avleveringProperties) throws Exception {
		lic.setLicense(new ByteArrayInputStream(avleveringProperties.getAsposeLicense().getBytes(StandardCharsets.UTF_8)));
	}

	public byte[] convertToPDFA(byte[] inputPdf, long dokumentInfoId) {
		if (isValidPdf(inputPdf)) {
			return inputPdf;
		}

		boolean couldConvert;
		ByteArrayOutputStream logStream = new ByteArrayOutputStream();
		Document doc = new Document(inputPdf);
		try {
			couldConvert = doc.convert(logStream, PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
		} catch (Exception e) {
			log.warn(String.format("Klarte ikke konvertere dokumentInfoId=%s. Feilmelding:%s", dokumentInfoId, e.getMessage()));
			return inputPdf;
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		byte[] outputPdf = stream.toByteArray();
		
		if (!couldConvert) {
			validatePDF(outputPdf, String.valueOf(dokumentInfoId), logStream);
		}

		return outputPdf;
	}

	private void validatePDF(byte[] pdf, String dokumentInfoId, ByteArrayOutputStream logstream) {
		PDFAValidatorResponse response = safeValidatePDFA(pdf);
		if (!response.isValidPdf()) {
			try {
				log.warn("dokumentInfo {} er ikke en gyldig PDF/A etter konvertering! Format: {} \n Aspose feilmeldinger: {} \n, Feilmeldinger: {}", dokumentInfoId, response.getPdfVersion(), logstream.toString("UTF-8"), response.getAssertionResults());
			} catch (UnsupportedEncodingException e) {
				log.error(String.format("Feil ved uthenting av charset UTF-8. Feilmelding:%s", e.getMessage()));
			}
		}
	}

	private boolean isValidPdf(byte[] pdf) {
		return safeValidatePDFA(pdf).isValidPdf() ? true : false;
	}
}
