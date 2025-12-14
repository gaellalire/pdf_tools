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

package fr.gaellalire.pdf_tools.cli.match;

import fr.gaellalire.pdf_tools.lib.match.LicenceInformation;
import fr.gaellalire.pdf_tools.lib.match.LicenceInformationProvider;

public class NoDataLicenceInformationProvider implements LicenceInformationProvider {

    @Override
    public LicenceInformation getLicenceInformation(String playerIdendifier) {
        return new LicenceInformation() {
            
            @Override
            public String getUniformNumber() {
                return "";
            }
            
            @Override
            public String getName() {
                return "John Doe";
            }
        };
    }

}
