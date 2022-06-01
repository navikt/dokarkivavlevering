package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.PdfFormat;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorUtil.validatePDFA;

@Slf4j
public class AsposeService {
	private static License lic;

	static {
		try {
			lic = new License();
			lic.setLicense(new ClassPathResource("aspose/Aspose.Total.Product.Family.lic").getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ByteArrayOutputStream convertToPDFA(InputStream pdf) {
		Document doc = new Document(pdf);
		doc.convert("test", PdfFormat.PDF_A_1B, ConvertErrorAction.Delete);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		return stream;
	}

	public static byte[] convertToPDFA(byte[] pdf, String dokumentInfoId) {
		ByteArrayOutputStream logStream = new ByteArrayOutputStream();

		validatePDF(pdf, dokumentInfoId, "FØR", logStream);

		logStream = new ByteArrayOutputStream();

		Document doc = new Document(pdf);
		doc.convert("test", PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		pdf = stream.toByteArray();

		validatePDF(pdf, dokumentInfoId, "ETTER", logStream);

		return pdf;
	}

	private static void validatePDF(byte[] pdf, String dokumentInfoId, String tidspunkt, ByteArrayOutputStream logstream){
		try {
			PDFAValidatorResponse response = response = validatePDFA(pdf);
			if(response.isValidPdf()){
				log.info("dokumentInfo {} er en gyldig pdf/a {} konvertering! Format:{}", dokumentInfoId, tidspunkt, response.getPdfVersion());
			} else{
				log.warn("dokumentInfo {} er ikke en gyldig PDF/A {} konvertering! Format: {} \n Aspose feilmeldinger: {}, Feilmeldinger: {}", dokumentInfoId, tidspunkt, response.getPdfVersion(), logstream.toString("UTF-8"), response.getAssertionResults());
			}
		} catch (Exception e){
			//Bare for test
		}
	}

	private static boolean isValidPdf(byte[] pdf){
		try {
			if (validatePDFA(pdf).isValidPdf()) {
				return true;
			}
		} catch (Exception e) {
			log.error("message: {} "+ e.getMessage(), e.getStackTrace());
			//gjør noe smart
		}
		return false;
	}
}
