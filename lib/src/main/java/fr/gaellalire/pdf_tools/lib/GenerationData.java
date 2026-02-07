package fr.gaellalire.pdf_tools.lib;

import java.util.Date;

public class GenerationData {
    
    private Date creationDate;

    private String initialDocumentId;

    private Date modificationDate;
    
    private String modifiedDocumentId;

    public GenerationData(Date creationDate, String initialDocumentId, Date modificationDate, String modifiedDocumentId) {
        this.creationDate = creationDate;
        this.initialDocumentId = initialDocumentId;
        this.modificationDate = modificationDate;
        this.modifiedDocumentId = modifiedDocumentId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getInitialDocumentId() {
        return initialDocumentId;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public String getModifiedDocumentId() {
        return modifiedDocumentId;
    }

}
