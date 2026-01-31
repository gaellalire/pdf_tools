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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.Callable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.crypto.DigestAlgorithms;
import com.itextpdf.signatures.PrivateKeySignature;

import fr.gaellalire.pdf_tools.lib.PDFSigner;
import fr.gaellalire.pdf_tools.lib.SignatureData;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "sign", mixinStandardHelpOptions = true)
public class Sign implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sign.class);

    @Option(names = "--signature-name", required = true)
    private String signatureName;

    @Option(names = "--signature")
    private String signature;

    @Option(names = "--timestamp-token")
    private String timestampToken;

    @Option(names = "--intermediate-modified-document-id")
    private String intermediateModifiedDocumentId;

    @Option(names = "--modified-document-id")
    private String modifiedDocumentId;

    @Option(names = "--intermediate-modification-date")
    private Long intermediateModificationDate;

    @Option(names = "--modification-date")
    private Long modificationDate;

    @Option(names = "--claimed-sign-date")
    private Long claimedSignDate;

    @Option(names = "--timestamp-service-url", required = true)
    private String timestampServiceURL;

    @Option(names = "--p12-file")
    private File p12File;

    @Option(names = {"-p", "--password"}, description = "P12 password (read from STDIN)", interactive = true)
    public String password;

    @Option(names = "--password:file", description = "P12 password (read from first line of file)")
    public File passwordFile;

    @Option(names = "--password:env", description = "P12 password (read from environment variable)")
    public String passwordEnvironmentVariable;

    @Parameters(index = "0", description = "The file to sign.")
    private File srcFile;

    @Parameters(index = "1", description = "The result file.")
    private File destFile;

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
        if (srcFile.equals(destFile)) {
            System.err.println("Cannot sign to the same file");
            return 1;
        }

        PDFSigner sign = new PDFSigner();

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
        if (p12File == null) {
            throw new ParameterException(spec.commandLine(), "P12 file required");
        }

        KeyStore p12 = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
        p12.load(new FileInputStream(p12File), password.toCharArray());

        Enumeration<String> aliases = p12.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (p12.isKeyEntry(alias)) {
                sign.setExternalSignature(
                        new PrivateKeySignature((PrivateKey) p12.getKey(alias, password.toCharArray()), DigestAlgorithms.SHA512, BouncyCastleProvider.PROVIDER_NAME));
                sign.setCertificateChain(p12.getCertificateChain(alias));
                break;
            }
        }
        
        if (signature != null) {
            sign.setSignature(Base64.getDecoder().decode(signature));
        }
        
        if (intermediateModificationDate != null) {
            sign.setIntermediateModificationDate(new Date(intermediateModificationDate));
        }

        if (modificationDate != null) {
            sign.setModificationDate(new Date(modificationDate));
        }
        if (claimedSignDate != null) {
            sign.setClaimedSignDate(new Date(claimedSignDate));
        }

        sign.setSignatureName(signatureName);
        
        sign.setTimestampURL(timestampServiceURL);
        
        if (timestampToken != null) {
            sign.setTimestampToken(Base64.getDecoder().decode(timestampToken));
        }
        
        if (modifiedDocumentId != null) {
            sign.setModifiedDocumentId(new String(Base64.getDecoder().decode(modifiedDocumentId), "UTF-8"));
        }
        
        if (intermediateModifiedDocumentId != null) {
            sign.setIntermediateModifiedDocumentId(new String(Base64.getDecoder().decode(intermediateModifiedDocumentId), "UTF-8"));
        }

        try (FileInputStream fileInputStream = new FileInputStream(srcFile)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
                SignatureData signatureData = sign.sign(fileInputStream, fileOutputStream);
                System.out.println("--signature=" + Base64.getEncoder().encodeToString(signatureData.getSignature()));
                System.out.println("--claimed-sign-date=" + signatureData.getClaimedSignDate().getTime());
                System.out.println("--timestamp-token=" + Base64.getEncoder().encodeToString(signatureData.getTimestampToken()));
                System.out.println("--intermediate-modification-date=" + signatureData.getIntermediateModificationDate().getTime());
                System.out.println("--intermediate-modified-document-id=" + Base64.getEncoder().encodeToString(signatureData.getIntermediateModifiedDocumentId().getBytes("UTF-8")));
                System.out.println("--modification-date=" + signatureData.getModificationDate().getTime());
                System.out.println("--modified-document-id=" + Base64.getEncoder().encodeToString(signatureData.getModifiedDocumentId().getBytes("UTF-8")));
            }
        }
        
        System.out.print("File signed");

        return 0;
    }

}
