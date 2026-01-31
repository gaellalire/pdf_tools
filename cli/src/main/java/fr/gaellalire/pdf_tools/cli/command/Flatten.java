/*
 * This file is part of PDF Tools.
 *
 * PDF Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDF Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDF Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.gaellalire.pdf_tools.cli.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.gaellalire.pdf_tools.lib.PDFFlattener;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "flatten", mixinStandardHelpOptions = true)
public class Flatten implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Flatten.class);

    @Option(names = "--fields-to-remove")
    private String fieldsToRemove;

    @Option(names = "--initial-document-id")
    private String initialDocumentId;

    @Option(names = "--modified-document-id")
    private String modifiedDocumentId;

    @Option(names = "--creation-date")
    private Date creationDate;

    @Option(names = "--modification-date")
    private Date modificationDate;

    @Parameters(index = "0", description = "The file to flatten.")
    private File srcFile;

    @Parameters(index = "1", description = "The result file.")
    private File destFile;

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        if (srcFile.equals(destFile)) {
            System.err.println("Cannot flatten to the same file");
            return 1;
        }
        
        PDFFlattener pdfFlattener = new PDFFlattener(); 
        
        if (fieldsToRemove != null) {
            pdfFlattener.setFieldsToRemove(Arrays.asList(fieldsToRemove.split(",")));
        }
        pdfFlattener.setCreationDate(creationDate);
        pdfFlattener.setModificationDate(modificationDate);
        pdfFlattener.setInitialDocumentId(initialDocumentId);
        pdfFlattener.setModifiedDocumentId(modifiedDocumentId);

        try (FileInputStream fileInputStream = new FileInputStream(srcFile)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
                pdfFlattener.flatten(fileInputStream, fileOutputStream);
            }
        }
        
        System.out.print("File flattened");

        return 0;
    }

}
