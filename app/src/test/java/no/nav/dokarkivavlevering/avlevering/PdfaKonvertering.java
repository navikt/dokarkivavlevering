package no.nav.dokarkivavlevering.avlevering;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import no.nav.dokarkivavlevering.avlevering.utils.PdfaUtils;

import static no.nav.dokarkivavlevering.avlevering.aspose.AsposeService.convertPdf;


public class PdfaKonvertering {

	PdfaUtils utils = new PdfaUtils();


	@Test
	public void testPdfa() throws IOException {
		for(int i = 0; i < 1; i++) {
			byte[] pdfFile = classpathToInputStream("pdf/notPdfa.pdf");
			byte[] file = utils.convertPdfToPdfa(pdfFile);
			System.out.println(i);
		}
		System.out.println("Test");

	}

	@Test
	public void asposeTest() throws Exception {
		byte[] pdfFile = classpathToInputStream("pdf/notPdfa.pdf");
		byte[] result = convertPdf(pdfFile);

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

	private static byte[] classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream().readAllBytes();
	}
}
