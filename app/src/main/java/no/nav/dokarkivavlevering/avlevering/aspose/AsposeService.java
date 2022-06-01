package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.PdfFormat;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorUtil.safeValidatePDFA;
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

	public static byte[] convertToPDFA(byte[] pdf, String dokumentInfoId) {
		if(isValidPdf(pdf)){
			return pdf;
		}

		ByteArrayOutputStream logStream = new ByteArrayOutputStream();
		Document doc = new Document(pdf);
		doc.convert(logStream, PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		pdf = stream.toByteArray();

		validatePDF(pdf, dokumentInfoId,  logStream);

		return pdf;
	}

	private static void validatePDF(byte[] pdf, String dokumentInfoId, ByteArrayOutputStream logstream){
		PDFAValidatorResponse response = safeValidatePDFA(pdf);
		if(response.isValidPdf()){
		} else{
			try {
				log.warn("dokumentInfo {} er ikke en gyldig PDF/A etter konvertering! Format: {} \n Aspose feilmeldinger: {}, Feilmeldinger: {}", dokumentInfoId, response.getPdfVersion(), logstream.toString(Charset. "UTF-8"), response.getAssertionResults());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean isValidPdf(byte[] pdf){
		return safeValidatePDFA(pdf).isValidPdf() ? true : false;
}
