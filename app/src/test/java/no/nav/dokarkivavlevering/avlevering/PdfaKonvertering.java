package no.nav.dokarkivavlevering.avlevering;

import no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorResponse;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static no.nav.dokarkivavlevering.avlevering.aspose.AsposeService.convertPdf;
import static no.nav.dokarkivavlevering.avlevering.pdfValidation.PDFAValidatorUtil.validatePDFA;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_U;


public class PdfaKonvertering {

	private static final String UGYLDIG_PDF_PATH = "pdf/JasperReports-Ultimate-Guide-3.pdf";

	@Test
	public void convertPdfToPdfa() throws Exception {
		//sjekk at PDF'en vi tester ikke er gyldig før konvertering
		assertThat(validatePDFA(getPdfStream(UGYLDIG_PDF_PATH)).isValidPdf()).isEqualTo(false);

		ByteArrayOutputStream result = convertPdf(getPdfStream(UGYLDIG_PDF_PATH));
		InputStream stream = new ByteArrayInputStream(result.toByteArray());

		PDFAValidatorResponse pdfaValidatorResponse = validatePDFA(stream);
		assertThat(pdfaValidatorResponse.isValidPdf()).isEqualTo(true);
		assertThat(pdfaValidatorResponse.getPdfVersion()).isEqualTo(PDFA_2_U);
	}

	private InputStream getPdfStream(String path) throws IOException {
		return classpathToInputStream(path);
	}

	private void writePdfToFileForInspection(byte[] result){
		File tempOutput = null;
		try {
			tempOutput = Files.createTempFile(null, ".pdf").getFileName().toFile();
			FileUtils.writeByteArrayToFile(tempOutput, result);

			System.out.println(tempOutput.getAbsoluteFile());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tempOutput.delete();
		}
	}

	private static InputStream classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream();
	}
}
