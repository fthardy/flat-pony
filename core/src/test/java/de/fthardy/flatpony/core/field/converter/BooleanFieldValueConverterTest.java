package de.fthardy.flatpony.core.field.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanFieldValueConverterTest {
    
    @Test
    void Cannot_create_with_null_for_true_value() {
        assertThrows(NullPointerException.class, () -> new BooleanFieldValueConverter(null, ""));
    }
    
    @Test
    void Cannot_create_with_null_for_false_value() {
        assertThrows(NullPointerException.class, () -> new BooleanFieldValueConverter("", null));
    }
    
    @Test
    void Cannot_create_with_the_same_string_for_true_and_false() {
        assertThrows(IllegalArgumentException.class, () -> new BooleanFieldValueConverter("", ""));
    }

    @Test
    void getTargetType() {
        assertEquals(Boolean.class, new BooleanFieldValueConverter("true", "false").getTargetType());
    }

    @Test
    void convertFromFieldValue() {
        assertTrue(new BooleanFieldValueConverter("A", "B").convertFromFieldValue("A"));
        assertFalse(new BooleanFieldValueConverter("A", "B").convertFromFieldValue("B"));
        assertThrows(FieldValueConvertException.class, () -> new BooleanFieldValueConverter("A", "B").convertFromFieldValue("C"));
    }

    @Test
    void convertToFieldValue() {
        assertEquals("A", new BooleanFieldValueConverter("A", "B").convertToFieldValue(true));
        assertEquals("B", new BooleanFieldValueConverter("A", "B").convertToFieldValue(false));
    }
}