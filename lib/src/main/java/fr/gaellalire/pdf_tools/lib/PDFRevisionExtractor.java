package fr.gaellalire.pdf_tools.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.SignatureUtil;

public class PDFRevisionExtractor {

    public void extract(InputStream signedPdf, OutputStream outputStream) throws IOException, GeneralSecurityException {
        byte[] buffer = new byte[1024*1024];
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(signedPdf))) {
            SignatureUtil signUtil = new SignatureUtil(pdfDoc);
            List<String> names = signUtil.getSignatureNames();
            for (String name : names) {
                System.out.println("===== " + name + " =====");
                InputStream extractRevision = signUtil.extractRevision(name);
                int read = extractRevision.read(buffer);
                while (read != -1) {
                    outputStream.write(buffer, 0, read);
                    read = extractRevision.read(buffer);
                }
                // only first signature
                break;
//                System.out.println("Signature covers whole document: " + signUtil.signatureCoversWholeDocument(name));
//                System.out.println("Document revision: " + signUtil.getRevision(name) + " of " + signUtil.getTotalRevisions());
//                PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
//                System.out.println("Subject: " + CertificateInfo.getSubjectFields(pkcs7.getSigningCertificate()));
//                System.out.println("Integrity check OK? " + pkcs7.verifySignatureIntegrityAndAuthenticity());
            }
        }

    }

}
