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

package fr.gaellalire.pdf_tools.cli;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.gaellalire.pdf_tools.cli.command.Extract;
import fr.gaellalire.pdf_tools.cli.command.FSGTGenerate;
import fr.gaellalire.pdf_tools.cli.command.Flatten;
import fr.gaellalire.pdf_tools.cli.command.Sign;
import fr.gaellalire.pdf_tools.cli.command.Verify;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(sortOptions = false, mixinStandardHelpOptions = true, versionProvider = PicocliVersionProvider.class, name = "pdftools", subcommands = {FSGTGenerate.class, Sign.class, Verify.class, Flatten.class, Extract.class})
public class PDFTools {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(PDFTools.class);

    @Spec
    CommandSpec spec;

    public static void main(final String[] args) {
        int exitCode = new CommandLine(new PDFTools()).setHelpFactory(new UnsortedSynopsisHelpFactory()).execute(args);
        System.exit(exitCode);
    }

}
