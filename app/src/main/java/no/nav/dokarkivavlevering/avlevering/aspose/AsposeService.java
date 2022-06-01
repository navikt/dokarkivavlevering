package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.PdfFormat;
import lombok.extern.slf4j.Slf4j;
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
		doc.convert("test", PdfFormat.PDF_A_2U, ConvertErrorAction.Delete);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		return stream;
	}

	public static byte[] convertToPDFA(byte[] pdf, String dokumentInfoId) {
		try {
			if (validatePDFA(pdf).isValidPdf()) {
				log.info("dokumentInfoId: {} er allerede en gyldig PDF/A!", dokumentInfoId);
				return pdf;
			}
		} catch (Exception e) {
			log.error("message: {} "+ e.getMessage(), e.getStackTrace());
			//gjør noe smart
		}
		Document doc = new Document(pdf);
		doc.convert("test", PdfFormat.PDF_A_2U, ConvertErrorAction.Delete);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		return stream.toByteArray();
	}
}
