package org.ow2.proactive.addons.email.converters;

import org.ow2.proactive.addons.email.exception.ConversionException;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


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