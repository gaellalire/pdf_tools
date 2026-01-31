package fr.gaellalire.pdf_tools.lib;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Collection;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.IssuingCertificateRetriever;
import com.itextpdf.signatures.validation.SignatureValidationProperties;
import com.itextpdf.signatures.validation.SignatureValidationProperties.OnlineFetching;
import com.itextpdf.signatures.validation.SignatureValidator;
import com.itextpdf.signatures.validation.ValidatorChainBuilder;
import com.itextpdf.signatures.validation.context.CertificateSources;
import com.itextpdf.signatures.validation.context.TimeBasedContexts;
import com.itextpdf.signatures.validation.context.ValidatorContexts;
import com.itextpdf.signatures.validation.report.ValidationReport;

public class PDFVerifier {
    
    private Collection<Certificate> trustedCertificates;

    public void setTrustedCertificates(Collection<Certificate> trustedCertificates) {
        this.trustedCertificates = trustedCertificates;
    }

    public ValidationReport verify(InputStream signedPdf) throws Exception {

        ValidatorChainBuilder builder = new ValidatorChainBuilder();

        SignatureValidationProperties properties = builder.getProperties();
        
        properties.setRevocationOnlineFetching(ValidatorContexts.all(), CertificateSources.all(), TimeBasedContexts.all(), OnlineFetching.ALWAYS_FETCH);
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(signedPdf))) {
            builder.withTrustedCertificates(trustedCertificates);

            IssuingCertificateRetriever certificateRetriever = builder.getCertificateRetriever();

            SignatureValidator signatureValidator = builder.withIssuingCertificateRetrieverFactory(() -> certificateRetriever).buildSignatureValidator(pdfDoc);

            return signatureValidator.validateSignatures();
        }
    }

}
