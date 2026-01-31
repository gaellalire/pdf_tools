package fr.gaellalire.pdf_tools.lib;

import java.util.Date;

public class SignatureData {
    
    private byte[] signature;

    private Date claimedSignDate;

    private byte[] timestampToken;
    
    private Date intermediateModificationDate;
    
    private String intermediateModifiedDocumentId;

    private Date modificationDate;
    
    private String modifiedDocumentId;

    public SignatureData(byte[] signature, Date claimedSignDate, byte[] timestampToken, Date intermediateModificationDate, String intermediateModifiedDocumentId, Date modificationDate, String modifiedDocumentId) {
        this.signature = signature;
        this.claimedSignDate = claimedSignDate;
        this.timestampToken = timestampToken;
        this.intermediateModificationDate = intermediateModificationDate;
        this.intermediateModifiedDocumentId = intermediateModifiedDocumentId;
        this.modificationDate = modificationDate;
        this.modifiedDocumentId = modifiedDocumentId;
    }
    
    public byte[] getSignature() {
        return signature;
    }

    public byte[] getTimestampToken() {
        return timestampToken;
    }
    
    public Date getIntermediateModificationDate() {
        return intermediateModificationDate;
    }
    
    public String getIntermediateModifiedDocumentId() {
        return intermediateModifiedDocumentId;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public String getModifiedDocumentId() {
        return modifiedDocumentId;
    }

    public Date getClaimedSignDate() {
        return claimedSignDate;
    }    
    
}
