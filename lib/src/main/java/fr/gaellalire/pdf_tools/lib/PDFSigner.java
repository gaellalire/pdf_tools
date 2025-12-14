package fr.gaellalire.pdf_tools.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.itextpdf.forms.form.element.SignatureFieldAppearance;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfPadesSigner;
import com.itextpdf.signatures.SignerProperties;
import com.itextpdf.signatures.TSAClientBouncyCastle;

import fr.gaellalire.pdf_tools.lib.utils.InvalidNameException;
import fr.gaellalire.pdf_tools.lib.utils.Rdn;
import fr.gaellalire.pdf_tools.lib.utils.Rfc2253Parser;

public class PDFSigner {
    
    private Certificate[] certificateChain;
    
    private IExternalSignature externalSignature;
    
    private String signatureName;
    
    public void setCertificateChain(Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }
    
    public void setExternalSignature(IExternalSignature externalSignature) {
        this.externalSignature = externalSignature;
    }
    
    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }
    
    protected SignerProperties createSignerProperties(Certificate[] certificateChain, String signatureName) {
        SignerProperties signerProperties = new SignerProperties().setFieldName(signatureName);

        X509Certificate certificate = (X509Certificate) certificateChain[0];
        
        String name = "?";
        

        try {
            for (Rdn rdn : new Rfc2253Parser(certificate.getSubjectX500Principal().getName()).parseDn()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    name = rdn.getValue().toString();
                    break;
                }
            }
        } catch (InvalidNameException e) {
            // ignore
        }


        
        String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z").format(new Date());
        date = date.substring(0, date.length() - 2) + "'" + date.substring(date.length() - 2) + "'";
        
        // Create the appearance instance and set the signature content to be shown and different appearance properties.
        SignatureFieldAppearance appearance = new SignatureFieldAppearance(signerProperties.getFieldName())
                .setContent(name, "Signature numérique de " + name +
                        "\nDate : " + date)
                ;

        // Set created signature appearance and other signer properties.
        signerProperties
        // .setReason(null).setLocation(null)
                .setSignatureAppearance(appearance);
        return signerProperties;
    }
    
    
    private String timestampURL;
    
    public void setTimestampURL(String timestampURL) {
        this.timestampURL = timestampURL;
    }


    public void sign(InputStream inputStream, OutputStream outputStream) throws GeneralSecurityException, IOException {
        PdfPadesSigner padesSigner = new PdfPadesSigner(new PdfReader(inputStream),
                outputStream);
        // We can pass the appearance through the signer properties.
        SignerProperties signerProperties = createSignerProperties(certificateChain, signatureName);

        padesSigner.signWithBaselineLTProfile(signerProperties, certificateChain, externalSignature, new TSAClientBouncyCastle(timestampURL));

    }
}
