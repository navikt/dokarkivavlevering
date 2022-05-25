package no.nav.dokarkivavlevering.avlevering.utils;

import no.nav.dokarkivavlevering.avlevering.dokument.Dokument;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class PdfaUtils {

	public byte[] convertPdfToPdfa(byte[] fil)  {
		Path temp = null;
		File tempInput = null;
		File tempOutput = null;
		try {
			tempOutput = Files.createTempFile(null, ".pdf").getFileName().toFile();
			tempInput = Files.createTempFile(null, ".pdf").getFileName().toFile();
			System.out.println(tempOutput.getAbsoluteFile());
			FileUtils.writeByteArrayToFile(tempInput, fil);
			String command = "C:/Program Files/gs/gs9.56.1/bin/gswin64c -dPDFA -dBATCH -dNOPAUSE -dUseCIEColor -sProcessColorModel=DeviceCMYK -sDEVICE=pdfwrite -sPDFACompatibilityPolicy=1 -sOutputFile=Test.pdf pdf.pdf";
			//String command = "C:/Program Files/gs/gs9.56.1/bin/gswin64c.exe -dPDFA -dBATCH -dNOPAUSE -dUseCIEColor -sProcessColorModel=DeviceCMYK -sDEVICE=pdfwrite -sPDFACompatibilityPolicy=1 input_file.pdf -sOutputFile=" + tempOutput + " " + tempInput;
			Process pr = Runtime.getRuntime().exec(command);
			while(pr.waitFor() != 0){

			}

			return Files.readAllBytes(tempOutput.getAbsoluteFile().toPath());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tempInput.delete();
			tempOutput.delete();
			temp = null;
		}
		return null;
	}
}
