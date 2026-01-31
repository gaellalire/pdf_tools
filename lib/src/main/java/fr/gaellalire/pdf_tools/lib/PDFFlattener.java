package fr.gaellalire.pdf_tools.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormCreator;
import com.itextpdf.kernel.pdf.PdfDate;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;

public class PDFFlattener {
    
    private List<String> fieldsToRemove;
    
    public void setFieldsToRemove(List<String> fieldsToRemove) {
        this.fieldsToRemove = fieldsToRemove;
    }

    private String initialDocumentId;

    private String modifiedDocumentId;
    
    public void setInitialDocumentId(String initialDocumentId) {
        this.initialDocumentId = initialDocumentId;
    }
    
    public void setModifiedDocumentId(String modifiedDocumentId) {
        this.modifiedDocumentId = modifiedDocumentId;
    }

    private Date creationDate;

    private Date modificationDate;

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void flatten(InputStream inputStream, OutputStream outputStream) throws GeneralSecurityException, IOException {
        String[] modDateHolder = new String[1];
        PdfDocument[] pdfHolder = new PdfDocument[1];
        WriterProperties[] writerPropertiesHolder = new WriterProperties[1];
        try (PdfReader reader = new PdfReader(inputStream) {
            protected void readPdf() throws IOException {
                super.readPdf();
                if (initialDocumentId != null) {
                    writerPropertiesHolder[0].setInitialDocumentId(new PdfString(initialDocumentId));
                }
                if (modifiedDocumentId != null) {
                    writerPropertiesHolder[0].setModifiedDocumentId(new PdfString(modifiedDocumentId));
                } else {
                    writerPropertiesHolder[0].setModifiedDocumentId(new PdfString(getModifiedFileId()));
                }
            };
        }) {
            try (PdfWriter writer = new PdfWriter(outputStream)) {
                writerPropertiesHolder[0] = writer.getProperties();
                
                try (PdfDocument pdf = new PdfDocument(reader, writer) {
                    protected void open(com.itextpdf.kernel.pdf.PdfVersion newPdfVersion) {
                        pdfHolder[0] = this;
                        super.open(newPdfVersion);
                    }
                    
                    public PdfDocumentInfo getDocumentInfo() {
                        PdfDocumentInfo documentInfo = super.getDocumentInfo();
                        if (modDateHolder[0] == null) {
                            modDateHolder[0] = documentInfo.getMoreInfo(PdfName.ModDate.getValue());
                        }
                        return documentInfo;
                    }
                    
                }) {
                    
                    if (creationDate != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(creationDate);
                        pdf.getDocumentInfo().setMoreInfo(PdfName.CreationDate.getValue(), new PdfDate(calendar).getPdfObject().getValue());
                    }
                    if (modificationDate != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(modificationDate);
                        pdf.getDocumentInfo().setMoreInfo(PdfName.ModDate.getValue(), new PdfDate(calendar).getPdfObject().getValue());
                    } else {
                        pdf.getDocumentInfo().setMoreInfo(PdfName.ModDate.getValue(), modDateHolder[0]);
                    }

                    PdfAcroForm form = PdfFormCreator.getAcroForm(pdf, true);
                    
                    if (fieldsToRemove != null) {
                        for (String field : fieldsToRemove) {
                            form.removeField(field);
                        }
                    }

                    // If no fields have been explicitly included, then all
                    // fields
                    // are
                    // flattened.
                    // Otherwise only the included fields are flattened.
                    form.flattenFields();
                }
            }
        }
    }

}
