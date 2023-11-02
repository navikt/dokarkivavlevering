package no.nav.dokarkivavlevering.avlevering;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PdfaKonvertering {

	private static final String UGYLDIG_PDF_PATH = "pdf/notPdfa.pdf";


	//For manuell testing av validering.
	//Hent license fra vault for testing
/*	@Test
	public void convertPdfToPdfa() throws Exception {
		//sjekk at PDF'en vi tester ikke er gyldig før konvertering
		assertThat(safeValidatePDFA(getPdfStream(UGYLDIG_PDF_PATH).readAllBytes()).isValidPdf()).isEqualTo(false);

		byte[] result = asposeService.convertToPDFA(getPdfStream(UGYLDIG_PDF_PATH).readAllBytes(), 23423);

		PDFAValidatorResponse pdfaValidatorResponse = safeValidatePDFA(result);
		assertThat(pdfaValidatorResponse.isValidPdf()).isEqualTo(true);
		assertThat(pdfaValidatorResponse.getPdfVersion()).isEqualTo(PDFA_1_A);
	}*/

	private InputStream getPdfStream(String path) throws IOException {
		return classpathToInputStream(path);
	}

	/*
	 * For å lagre testfiler som filer man kan åpne lokalt.
	 * Gjør testing lettere
	 */
	private void writePdfToFileForInspection(byte[] result) {
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
