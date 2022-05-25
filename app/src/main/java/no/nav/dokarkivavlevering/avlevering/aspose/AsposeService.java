package no.nav.dokarkivavlevering.avlevering.aspose;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.PdfFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AsposeService {

	public static byte[] convertPdf(byte[] pdf){
		Document doc = new Document(pdf);
		doc.convert("test", PdfFormat.PDF_A_2U, ConvertErrorAction.Delete);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		doc.save(stream);
		return stream.toByteArray();
	}
}
