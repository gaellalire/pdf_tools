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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JSONUtils {

    public static final ObjectMapper OBJECT_MAPPER;

    static {
        SimpleModule simpleModule = new SimpleModule();
        OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(simpleModule);
    }

    public static <E> List<E> readListPojo(String stringValue, Class<E[]> pojoClass) {
        if (stringValue != null) {
            try {
                return Arrays.asList(OBJECT_MAPPER.readerFor(pojoClass).readValue(stringValue));
            } catch (JsonProcessingException e) {
            }
        }
        return null;
    }

    public static <E> E readPojo(InputStream inputStream, Class<E> pojoClass) throws IOException {
        try {
            return OBJECT_MAPPER.readerFor(pojoClass).readValue(inputStream);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <E> E readPojo(String stringValue, Class<E> pojoClass) {
        if (stringValue != null) {
            try {
                return OBJECT_MAPPER.readerFor(pojoClass).readValue(stringValue);
            } catch (JsonProcessingException e) {
            }
        }
        return null;
    }

    public static String writePojo(Object pojo) throws JsonProcessingException {
        if (pojo != null) {
            return OBJECT_MAPPER.writeValueAsString(pojo);
        }
        return null;
    }

}
