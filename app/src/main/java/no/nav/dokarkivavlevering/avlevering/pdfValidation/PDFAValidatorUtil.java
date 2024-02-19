package no.nav.dokarkivavlevering.avlevering.pdfValidation;

import lombok.extern.slf4j.Slf4j;
import org.verapdf.core.ModelParsingException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.verapdf.pdfa.flavours.PDFAFlavour.NO_FLAVOUR;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_U;

@Slf4j
public class PDFAValidatorUtil {

	private static final List<PDFAFlavour> validPdfas = Arrays.asList(PDFA_1_A, PDFA_1_B, PDFA_2_A, PDFA_2_B, PDFA_2_U);
	public static final Set NOT_A_PDFA = singleton("Dokumentet er ikke en PDFA");
	public static final Set NON_VALID_PDFA_VERSION = singleton("Dokumentet er ikke på et av de lovlige formatene");

	//Static init to initialize the FoundryProvider
	static {
		VeraGreenfieldFoundryProvider.initialise();
	}

	/*
	 * Validering er mest for convenience. Ønsker ikke at den ødelegger for resten av logikken
	 */
	public static boolean isValidPdfa(byte[] fil) {
		try {
			return validatePDFA(fil).isValidPdf();
		} catch (Exception e) {
			log.warn("VeraPDF kunne ikke validere PDF'en! Feilmelding: " + e.getMessage(), e);
			return false;
		}
	}

	private static PDFAValidatorResponse validatePDFA(byte[] fil) throws Exception {
		if (fil == null || fil.length == 0) {
			throw new Exception("Filen er null!");
		}

		try (PDFAParser parser = Foundries.defaultInstance().createParser(new ByteArrayInputStream(fil))) {

			//Hvis PDF/A'en ikke er på et av de lovlige foratene hopp over valideringen
			if (!validPdfas.contains(parser.getFlavour())) {
				return returnIncorrectFlavourReponse(parser.getFlavour());
			}
			PDFAValidator validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
			ValidationResult result = validator.validate(parser);

			if (result.isCompliant()) {
				return returnCompliantValidatorResponse(true, result);
			}
			return returnNonCompliantValidatorResponse(false, result, result.getTestAssertions());

		} catch (ModelParsingException e) {
			return returnNotAPdfValidatorResponse();
		}
	}

	private static PDFAValidatorResponse returnCompliantValidatorResponse(boolean isValidPdf, ValidationResult result) {
		return new PDFAValidatorResponse(isValidPdf, true, result.getPDFAFlavour(), Collections.emptySet());
	}

	private static PDFAValidatorResponse returnNonCompliantValidatorResponse(boolean isValidPdf, ValidationResult result, List<TestAssertion> assertions) {
		Set<String> reasonsForFailing = assertions.stream().map(TestAssertion::getMessage).collect(Collectors.toSet());
		return new PDFAValidatorResponse(isValidPdf, false, result.getPDFAFlavour(), reasonsForFailing);
	}

	private static PDFAValidatorResponse returnNotAPdfValidatorResponse() {
		return new PDFAValidatorResponse(false, false, NO_FLAVOUR, NOT_A_PDFA);
	}

	private static PDFAValidatorResponse returnIncorrectFlavourReponse(PDFAFlavour flavour) {
		return new PDFAValidatorResponse(false, false, flavour, NON_VALID_PDFA_VERSION);
	}

}
