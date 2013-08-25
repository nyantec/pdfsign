import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.PrivateKey;

public class PdfSign {
	public static void main(String[] arg) throws Exception {
		/* Read password from standard input */
		Console console = System.console();
		char[] password = console.readPassword("Private key password: ");

		/* Load PKCS #12 structure */
		KeyStore store = KeyStore.getInstance("pkcs12");
		store.load(new FileInputStream(arg[0]), password);

		/* Get private key and certificate chain */
		String alias = store.aliases().nextElement();
		PrivateKey key = (PrivateKey) store.getKey(alias, password);
		Certificate[] chain = store.getCertificateChain(alias);

		/* Open input file */
		File input = new File(arg[1]);
		PdfReader reader = new PdfReader(new FileInputStream(input));

		/* Create temporary output file */
		File output = File.createTempFile("temp", ".pdf", input.getParentFile());
		output.deleteOnExit();

		/* Create Signature */
		PdfStamper stamper = PdfStamper.createSignature(reader, null, '\0', output, true);
		PdfSignatureAppearance app = stamper.getSignatureAppearance();
		app.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
		app.setReason(arg[2]);
		app.setLocation(arg[3]);
		stamper.close();

		/* Replace input file */
		output.renameTo(input);
	}
}
