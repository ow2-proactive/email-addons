package org.ow2.proactive.addons.email.converters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ow2.proactive.addons.email.exception.ConversionException;
import com.google.common.truth.Truth;
import org.junit.Test;


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