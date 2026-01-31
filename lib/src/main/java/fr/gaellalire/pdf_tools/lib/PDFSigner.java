package fr.gaellalire.pdf_tools.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import com.itextpdf.commons.bouncycastle.tsp.AbstractTSPException;
import com.itextpdf.forms.form.element.SignatureFieldAppearance;
import com.itextpdf.kernel.pdf.PdfDate;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.ISignatureMechanismParams;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.LtvVerification;
import com.itextpdf.signatures.PdfPadesSigner;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.SignerProperties;
import com.itextpdf.signatures.TSAClientBouncyCastle;
import com.itextpdf.signatures.PdfSigner.CryptoStandard;

import fr.gaellalire.pdf_tools.lib.utils.InvalidNameException;
import fr.gaellalire.pdf_tools.lib.utils.Rdn;
import fr.gaellalire.pdf_tools.lib.utils.Rfc2253Parser;

public class PDFSigner {
    
    private Certificate[] certificateChain;
    
    private IExternalSignature externalSignature;
    
    private String signatureName;
    
    private byte[] signature;

    private byte[] timestampToken;

    private String intermediateModifiedDocumentId;

    private String modifiedDocumentId;

    private Date intermediateModificationDate;

    private Date modificationDate;

    private Date claimedSignDate;
    
    public void setClaimedSignDate(Date claimedSignDate) {
        this.claimedSignDate = claimedSignDate;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
    
    public void setIntermediateModificationDate(Date intermediateModificationDate) {
        this.intermediateModificationDate = intermediateModificationDate;
    }
    
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }
    
    public void setIntermediateModifiedDocumentId(String intermediateModifiedDocumentId) {
        this.intermediateModifiedDocumentId = intermediateModifiedDocumentId;
    }

    public void setModifiedDocumentId(String modifiedDocumentId) {
        this.modifiedDocumentId = modifiedDocumentId;
    }
    
    public void setTimestampToken(byte[] timestampToken) {
        this.timestampToken = timestampToken;
    }
    
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


        Date claimedSignDate = this.claimedSignDate;
        if (claimedSignDate == null) {
            claimedSignDate = new Date();
        }
        
        String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z").format(claimedSignDate);
        date = date.substring(0, date.length() - 2) + "'" + date.substring(date.length() - 2) + "'";
        
        // Create the appearance instance and set the signature content to be shown and different appearance properties.
        SignatureFieldAppearance appearance = new SignatureFieldAppearance(signerProperties.getFieldName())
                .setContent(name, "Signature numérique de " + name +
                        "\nDate : " + date)
                ;

        // Set created signature appearance and other signer properties.
        Calendar claimedSignCalendar = Calendar.getInstance();
        claimedSignCalendar.setTime(claimedSignDate);
        signerProperties
        // .setReason(null).setLocation(null)
        .setClaimedSignDate(claimedSignCalendar)
                .setSignatureAppearance(appearance);
        return signerProperties;
    }
    
    
    private String timestampURL;
    
    public void setTimestampURL(String timestampURL) {
        this.timestampURL = timestampURL;
    }


    public SignatureData sign(InputStream inputStream, OutputStream outputStream) throws GeneralSecurityException, IOException {
        byte[][] timestampTokenHolder = new byte[1][];
        byte[][] signatureHolder = new byte[1][];
        Date[] modificationDateHolder = new Date[2];
        String[] modifiedDocumentIdHolder = new String[2];
        PdfPadesSigner padesSigner = new PdfPadesSigner(new PdfReader(inputStream), outputStream) {
            
            @Override
            public PdfSigner createPdfSigner(SignerProperties signerProperties, boolean isFinal) throws IOException {
                String tempFilePath = null;
                if (temporaryDirectoryPath != null) {
                    tempFilePath = getNextTempFile().getAbsolutePath();
                }
                return new PdfSigner(reader,
                        isFinal ? outputStream : createOutputStream(), tempFilePath, stampingProperties, signerProperties) {
                    @Override
                    protected PdfDocument initDocument(PdfReader reader, PdfWriter writer, StampingProperties properties) {
                        WriterProperties writerProperties = writer.getProperties();
                        if (intermediateModifiedDocumentId != null) {
                            writerProperties.setModifiedDocumentId(new PdfString(intermediateModifiedDocumentId));
                        }
                        PdfDocument initDocument = super.initDocument(reader, writer, properties);
                        if (intermediateModificationDate != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(intermediateModificationDate);
                            initDocument.getDocumentInfo().setMoreInfo(PdfName.ModDate.getValue(), new PdfDate(calendar).getPdfObject().getValue());
                        }
                        modificationDateHolder[0] = PdfDate.decode(initDocument.getDocumentInfo().getMoreInfo(PdfName.ModDate.getValue())).getTime();
                        modifiedDocumentIdHolder[0] = initDocument.getModifiedDocumentId().getValue();

                        return initDocument;
                    }
                };
            }
            
            public void signWithBaselineLTProfile(SignerProperties signerProperties, Certificate[] chain,
                    IExternalSignature externalSignature, ITSAClient tsaClient) throws GeneralSecurityException, IOException {
                createRevocationClients(chain[0], true);
                try {
                    performSignDetached(signerProperties, false, new IExternalSignature() {
                        
                        @Override
                        public byte[] sign(byte[] message) throws GeneralSecurityException {
                            if (signature == null) {
                                signatureHolder[0] = externalSignature.sign(message);
                            } else {
                                signatureHolder[0] = signature;
                            }
                            return signatureHolder[0];
                        }
                        
                        @Override
                        public ISignatureMechanismParams getSignatureMechanismParameters() {
                            return externalSignature.getSignatureMechanismParameters();
                        }
                        
                        @Override
                        public String getSignatureAlgorithmName() {
                            return externalSignature.getSignatureAlgorithmName();
                        }
                        
                        @Override
                        public String getDigestAlgorithmName() {
                            return externalSignature.getDigestAlgorithmName();
                        }
                    }, chain, tsaClient);
                    PdfWriter writer = new PdfWriter(outputStream);
                    WriterProperties properties = writer.getProperties();
                    if (modifiedDocumentId != null) {
                        properties.setModifiedDocumentId(new PdfString(modifiedDocumentId));
                    }

                    try (InputStream inputStream = createInputStream();
                            PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputStream),
                                    writer, stampingPropertiesWithMetaInfo)) {
                        if (modificationDate != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(modificationDate);
                            pdfDocument.getDocumentInfo().setMoreInfo(PdfName.ModDate.getValue(), new PdfDate(calendar).getPdfObject().getValue());
                        }
                        modificationDateHolder[1] = PdfDate.decode(pdfDocument.getDocumentInfo().getMoreInfo(PdfName.ModDate.getValue())).getTime();

                        modifiedDocumentIdHolder[1] = pdfDocument.getModifiedDocumentId().getValue();

                        performLtvVerification(pdfDocument, Collections.singletonList(signerProperties.getFieldName()),
                                LtvVerification.RevocationDataNecessity.REQUIRED_FOR_SIGNING_CERTIFICATE);
                    }
                } finally {
                    deleteTempFiles();
                }
            }

        };
        // We can pass the appearance through the signer properties.
        SignerProperties signerProperties = createSignerProperties(certificateChain, signatureName);

        padesSigner.signWithBaselineLTProfile(signerProperties, certificateChain, externalSignature, new TSAClientBouncyCastle(timestampURL) {
            @Override
            public byte[] getTimeStampToken(byte[] imprint) throws IOException, AbstractTSPException {
                if (timestampToken != null) {
                    timestampTokenHolder[0] = timestampToken;
                } else {
                    timestampTokenHolder[0] = super.getTimeStampToken(imprint);
                }
                return timestampTokenHolder[0];
            }
        });
        return new SignatureData(signatureHolder[0], signerProperties.getClaimedSignDate().getTime(), timestampTokenHolder[0], modificationDateHolder[0], modifiedDocumentIdHolder[0], modificationDateHolder[1], modifiedDocumentIdHolder[1]);

    }
}
