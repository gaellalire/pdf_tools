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
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.gaellalire.pdf_tools.lib.PDFRevisionExtractor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "extract", mixinStandardHelpOptions = true)
public class Extract implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Extract.class);

    @Parameters(index = "0", description = "The file to extract.")
    private File srcFile;

    @Parameters(index = "1", description = "The result file.")
    private File destFile;

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        if (srcFile.equals(destFile)) {
            System.err.println("Cannot extract to the same file");
            return 1;
        }
        
        PDFRevisionExtractor pdfRevisionExtractor = new PDFRevisionExtractor(); 

        try (FileInputStream fileInputStream = new FileInputStream(srcFile)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
                pdfRevisionExtractor.extract(fileInputStream, fileOutputStream);
            }
        }
        
        System.out.print("File extracted");

        return 0;
    }

}
