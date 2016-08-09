package org.ow2.proactive.addons.email.converters;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.ow2.proactive.addons.email.exception.ConversionException;


/**
 * Unit tests associated to {@link BooleanConverter}.
 *
 * @author ActiveEon Team
 */
public class BooleanConverterTest extends ConverterTest<Boolean> {

    public BooleanConverterTest() {
        super(BooleanConverter.class);
    }

    @Test
    public void testFalseCamelCase() throws ConversionException {
        assertThat(testConvert("param", "False")).isFalse();
    }

    @Test
    public void testTrueCamelCase() throws ConversionException {
        assertThat(testConvert("param", "True")).isTrue();
    }

    @Test
    public void testFalseLowerCase() throws ConversionException {
        assertThat(testConvert("param", "false")).isFalse();
    }

    @Test
    public void testTrueLowerCase() throws ConversionException {
        assertThat(testConvert("param", "true")).isTrue();
    }

    @Test(expected = ConversionException.class)
    public void testEmpty() throws ConversionException {
        testConvert("param", "");
    }

    @Test(expected = ConversionException.class)
    public void testTrueTrailingSpace() throws ConversionException {
        testConvert("param", "True ");
    }

}