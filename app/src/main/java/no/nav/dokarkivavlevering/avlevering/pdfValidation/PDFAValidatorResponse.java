package no.nav.dokarkivavlevering.avlevering.pdfValidation;

import lombok.Getter;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.util.Set;

@Getter
public class PDFAValidatorResponse {

	private boolean isValidPdf;
	private boolean isCompliant;
	private PDFAFlavour pdfVersion;
	private Set<String> assertionResults;

	public PDFAValidatorResponse(boolean isValidPdf, boolean isCompliant, PDFAFlavour pdfVersion, Set<String> assertionResults) {
		this.isValidPdf = isValidPdf;
		this.isCompliant = isCompliant;
		this.pdfVersion = pdfVersion;
		this.assertionResults = assertionResults;

	}


}
