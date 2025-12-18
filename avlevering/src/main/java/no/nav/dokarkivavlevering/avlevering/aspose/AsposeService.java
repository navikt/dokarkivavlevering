package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.License;
import com.aspose.pdf.PdfFormat;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkivavlevering.avlevering.AvleveringProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
@Profile("genererAvlevering")
public class AsposeService {

	private static final License lic = new License();

	@Autowired
	public AsposeService(AvleveringProperties avleveringProperties){
		lic.setLicense(new ByteArrayInputStream(avleveringProperties.getAsposeLicense().getBytes(StandardCharsets.UTF_8)));
	}

	public byte[] convertToPDFA(byte[] inputPdf, long dokumentInfoId) {

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<byte[]> task = executorService.submit(() -> doConvertToPDFA(inputPdf, dokumentInfoId));
		try {
			return task.get(15, SECONDS);
		} catch (Exception e) {
			log.error("Timet ut under konvertering av dokumentInfoId={}. Feilmelding:{}. Returnerer input-pdf.", dokumentInfoId, e.getMessage());
			return inputPdf;
		} finally {
			executorService.shutdown();
		}
	}

	public byte[] doConvertToPDFA(byte[] inputPdf, long dokumentInfoId) {
		try (
				InputStream inputStream = new ByteArrayInputStream(inputPdf);
				ByteArrayOutputStream logStream = new ByteArrayOutputStream();
				ByteArrayOutputStream stream = new ByteArrayOutputStream()
		) {
			Document doc = new Document(inputStream);
			doc.convert(logStream, PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
			doc.save(stream);
			return stream.toByteArray();
		} catch (Exception e) {
			log.warn("Klarte ikke konvertere dokumentInfoId={}. Feilmelding:{}. Returnerer input-pdf", dokumentInfoId, e.getMessage());
			return inputPdf;
		}
	}

}
