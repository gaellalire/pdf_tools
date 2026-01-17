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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import fr.gaellalire.match_pojo.MatchConfiguration;
import fr.gaellalire.match_pojo.MatchState;
import fr.gaellalire.pdf_tools.cli.JSONUtils;
import fr.gaellalire.pdf_tools.cli.match.NoDataLicenceInformationProvider;
import fr.gaellalire.pdf_tools.lib.FSGTPDFGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "fsgt-generate", mixinStandardHelpOptions = true)
public class FSGTGenerate implements Callable<Integer> {

    @Option(names = "--match-configuration-file")
    private File matchConfigurationFile;

    @Parameters(index = "0", description = "The result file.")
    private File destFile;

    @Parameters(index = "1",  description = "The match state file.")
    private File matchStateFile;

    public static String readFull(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder json = new StringBuilder();
        String readLine = bufferedReader.readLine();
        while (readLine != null) {
            json.append(readLine);
            json.append('\n');
            readLine = bufferedReader.readLine();
        }
        return json.toString();
    }

    @Override
    public Integer call() throws Exception {
        FSGTPDFGenerator generator = new FSGTPDFGenerator();
        generator.setHomeLicenceInformationProvider(new NoDataLicenceInformationProvider());
        generator.setGuestLicenceInformationProvider(new NoDataLicenceInformationProvider());
        MatchConfiguration matchConfiguration = null;
        if (matchConfigurationFile != null) {
            matchConfiguration = JSONUtils.readPojo(new FileInputStream(matchConfigurationFile), MatchConfiguration.class);
        }
        MatchState matchState = JSONUtils.readPojo(new FileInputStream(matchStateFile), MatchState.class);
        try (FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
            generator.generate(fileOutputStream, matchConfiguration, matchState);
        }
        System.out.print("File generated");
        return 0;
    }
}
