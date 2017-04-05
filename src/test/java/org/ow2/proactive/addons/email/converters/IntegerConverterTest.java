/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.addons.email.converters;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.ow2.proactive.addons.email.exception.ConversionException;


/**
 * Unit tests associated to {@link IntegerConverter}.
 *
 * @author ActiveEon Team
 */
public class IntegerConverterTest extends ConverterTest<Integer> {

    public IntegerConverterTest() {
        super(IntegerConverter.class);
    }

    @Test(expected = ConversionException.class)
    public void testConvertInvalidInput() throws ConversionException {
        testConvert("param", "a");
    }

    @Test
    public void testConvertZero() throws ConversionException {
        assertThat(testConvert("param", "0")).isEqualTo(0);
    }

    @Test
    public void testConvertMaxIntegerValue() throws ConversionException {
        assertThat(testConvert("param", Integer.toString(Integer.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void testConvertMinIntegerValue() throws ConversionException {
        assertThat(testConvert("param", Integer.toString(Integer.MIN_VALUE))).isEqualTo(Integer.MIN_VALUE);
    }

    @Test(expected = ConversionException.class)
    public void testConvertDouble() throws ConversionException {
        assertThat(testConvert("param", "3.1415"));
    }

}
