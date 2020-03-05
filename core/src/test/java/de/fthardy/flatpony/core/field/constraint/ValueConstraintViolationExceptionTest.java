package de.fthardy.flatpony.core.field.constraint;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

class ValueConstraintViolationExceptionTest {
    
    @Test
    void Create_instance() {
        ValueConstraintViolationException exception = new ValueConstraintViolationException(
                "Field", "Test", new HashSet<>(Arrays.asList("foo", "bar")));
        
        assertThat(exception.getFieldName()).isEqualTo("Field");
        assertThat(exception.getValue()).isEqualTo("Test");
        assertThat(exception.getConstraintNames()).containsOnly("foo", "bar");
    }
}