package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.PdfFormat;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.config.AvleveringProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorUtil.isValidPdfa;

@Slf4j
@Component
public class AsposeService {

	private static final License lic = new License();

	@Autowired
	public AsposeService(AvleveringProperties avleveringProperties) throws Exception {
		lic.setLicense(new ByteArrayInputStream(avleveringProperties.getAsposeLicense().getBytes(StandardCharsets.UTF_8)));
	}

	public byte[] convertToPDFA(byte[] inputPdf, long dokumentInfoId) {
		if (isValidPdfa(inputPdf)) {
			return inputPdf;
		}
		try (Document doc = new Document(inputPdf)){
			ByteArrayOutputStream logStream = new ByteArrayOutputStream();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			doc.convert(logStream, PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
			doc.save(stream);

			return stream.toByteArray();
		} catch (Exception e) {
			log.warn(String.format("Klarte ikke konvertere dokumentInfoId=%s. Feilmelding:%s. Returnerer input-pdf", dokumentInfoId, e.getMessage()));
			return inputPdf;
		}
	}

}
