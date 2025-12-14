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

import java.util.Collection;
import java.util.Comparator;

import picocli.CommandLine;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

public class UnsortedSynopsisHelpFactory implements CommandLine.IHelpFactory {

    @Override
    public CommandLine.Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
        return new CommandLine.Help(commandSpec, colorScheme) {
            @Override
            protected Ansi.Text createDetailedSynopsisOptionsText(
                    Collection<ArgSpec> done,
                    Comparator<OptionSpec> optionSort,
                    boolean clusterBooleanOptions) {

                return super.createDetailedSynopsisOptionsText(
                        done,
                        null,  // do not sort options in synopsis
                        clusterBooleanOptions);
            }
        };
    }

}
