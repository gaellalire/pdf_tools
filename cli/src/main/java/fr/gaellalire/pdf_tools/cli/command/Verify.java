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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.signatures.validation.report.ReportItem;
import com.itextpdf.signatures.validation.report.ValidationReport;
import com.itextpdf.signatures.validation.report.ValidationReport.ValidationResult;

import fr.gaellalire.pdf_tools.lib.PDFVerifier;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "verify", mixinStandardHelpOptions = true)
public class Verify implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Verify.class);

    @Option(names = "--signature-name")
    private String signatureName;

    @Option(names = "--trusted-p12-file")
    private File p12File;

    @Option(names = {"-p", "--password"}, description = "P12 password (read from STDIN)", interactive = true)
    public String password;

    @Option(names = "--password:file", description = "P12 password (read from first line of file)")
    public File passwordFile;

    @Option(names = "--password:env", description = "P12 password (read from environment variable)")
    public String passwordEnvironmentVariable;

    @Parameters(index = "0", description = "The file to verify.")
    private File srcFile;

    @Spec
    CommandSpec spec;
    
    public Certificate[] getCertificateChain(String certificatePath, char[] password) throws Exception {
        Certificate[] certChain = null;

        KeyStore p12 = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
        p12.load(new FileInputStream(certificatePath), password);

        Enumeration<String> aliases = p12.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (p12.isKeyEntry(alias)) {
                certChain = p12.getCertificateChain(alias);
                break;
            }
        }
        return certChain;
    }

    @Override
    public Integer call() throws Exception {
        
        PDFVerifier verifier = new PDFVerifier();

        if (password == null) {
            if (passwordEnvironmentVariable != null) {
                password = System.getenv(passwordEnvironmentVariable);
                if (password == null) {
                    throw new ParameterException(spec.commandLine(), "Password environment variable (" + passwordEnvironmentVariable + ") is not defined");
                }
            } else if (passwordFile != null) {
                if (!passwordFile.isFile()) {
                    throw new ParameterException(spec.commandLine(), "Password file (" + passwordFile + ") does not exist");
                }
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(passwordFile), "UTF-8"))) {
                    password = bufferedReader.readLine();
                }
            }
        }
        if (password == null) {
            throw new ParameterException(spec.commandLine(), "Password required");
        }

        KeyStore p12 = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
        p12.load(new FileInputStream(p12File), password.toCharArray());
        
        List<Certificate> trustedCertificates = new ArrayList<Certificate>();

        Enumeration<String> aliases = p12.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (p12.isCertificateEntry(alias)) {
                trustedCertificates.add(p12.getCertificate(alias));
            }
        }
        
        verifier.setTrustedCertificates(trustedCertificates);
        
        ValidationReport validateSignatures = verifier.verify(new FileInputStream(srcFile));
        List<ReportItem> logs = validateSignatures.getLogs();
        for (ReportItem log : logs) {
            System.out.println(log);
        }
        System.out.println("---");
        System.out.println(validateSignatures.getFailures());
        System.out.println("---");
        System.out.println(validateSignatures.getValidationResult());

        if (validateSignatures.getValidationResult() == ValidationResult.INVALID) {
            return 2;
        }
        if (validateSignatures.getValidationResult() != ValidationResult.VALID) {
            return 1;
        }
        return 0;
    }

}
