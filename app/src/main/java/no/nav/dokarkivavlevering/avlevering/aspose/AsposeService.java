package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.PdfFormat;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorUtil.safeValidatePDFA;

@Slf4j
@Component
public class AsposeService {

	@Value("license")
	private static License lic;

	@Autowired
	public AsposeService (AvleveringProperties avleveringProperties) throws Exception {
		lic = new License();
		lic.setLicense(avleveringProperties.getAsposeLicense());
	}


	public byte[] convertToPDFA(byte[] pdf, long dokumentInfoId) {
		if(isValidPdf(pdf)){
			return pdf;
		}

		ByteArrayOutputStream logStream = new ByteArrayOutputStream();
		Document doc = new Document(pdf);
		boolean couldConvert = doc.convert(logStream, PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		pdf = stream.toByteArray();
		if(!couldConvert){
			validatePDF(pdf, ""+dokumentInfoId,  logStream);
		}

		return pdf;
	}

	private void validatePDF(byte[] pdf, String dokumentInfoId, ByteArrayOutputStream logstream){
		PDFAValidatorResponse response = safeValidatePDFA(pdf);
		if(!response.isValidPdf()){
			try {
				log.warn("dokumentInfo {} er ikke en gyldig PDF/A etter konvertering! Format: {} \n Aspose feilmeldinger: {} \n, Feilmeldinger: {}", dokumentInfoId, response.getPdfVersion(), logstream.toString("UTF-8"), response.getAssertionResults());
			} catch (UnsupportedEncodingException e) {
				log.error("Feil ved uthenting av charset UTF-8", e.getMessage());
			}
		}
	}

	private boolean isValidPdf(byte[] pdf) {
		return safeValidatePDFA(pdf).isValidPdf() ? true : false;
	}
}
