package de.fthardy.flatpony.core.field.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegerFieldValueConverterTest {

    @Test
    void getTargetType() {
        assertEquals(Integer.class, new IntegerFieldValueConverter().getTargetType());
    }

    @Test
    void convertFromFieldValue() {
        assertEquals(42, new IntegerFieldValueConverter().convertFromFieldValue("42"));
    }

    @Test
    void convertToFieldValue() {
        assertEquals("42", new IntegerFieldValueConverter().convertToFieldValue(42));
    }
}