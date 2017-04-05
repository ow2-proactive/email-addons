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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.ow2.proactive.addons.email.exception.ConversionException;

import com.google.common.truth.Truth;


/**
 * Base class to write test against a {@link Converter} implementation.
 *
 * @author ActiveEon Team
 */
public abstract class ConverterTest<T> {

    private final Class<? extends Converter<T>> converterClass;

    public ConverterTest(Class<? extends Converter<T>> converterClass) {
        this.converterClass = converterClass;
    }

    public T testConvert(String parameterName, String parameterValue) throws ConversionException {
        try {
            return getConverter().convert(parameterName, parameterValue);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Converter<T> getConverter() throws InstantiationException, IllegalAccessException {
        return converterClass.newInstance();
    }

    @Test
    public void testGetInstance() throws InvocationTargetException, IllegalAccessException {
        try {
            Method method = converterClass.getMethod("getInstance");
            Truth.assertThat(method.invoke(null)).isSameAs(method.invoke(null));
        } catch (NoSuchMethodException e) {
            // ignore test if method does not exist
        }
    }

}
