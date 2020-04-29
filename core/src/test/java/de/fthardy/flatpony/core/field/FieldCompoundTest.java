package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.constrained.ConstrainedField;
import de.fthardy.flatpony.core.field.constrained.ConstrainedFieldDescriptor;
import de.fthardy.flatpony.core.field.constrained.constraint.ChoiceValueConstraint;
import de.fthardy.flatpony.core.field.constrained.constraint.RegExValueConstraint;
import de.fthardy.flatpony.core.field.constrained.constraint.ValueConstraintViolationException;
import de.fthardy.flatpony.core.field.delimited.DelimitedFieldDescriptor;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldCompoundTest {
    
    @Test
    void ConstrainedFixedSizeField()  {
        // Field is allowed to be empty
        ConstrainedFieldDescriptor dateFieldDescriptor = ConstrainedFieldDescriptor.newInstance(
                FixedSizeFieldDescriptor.newInstance("date").withFieldSize(10).build())
                .addConstraint(new RegExValueConstraint(
                        "german-date-format", "(\\d{2}\\.\\d{2}\\.\\d{4})*"))
                .build();
        
        ConstrainedField dateField = dateFieldDescriptor.readItemEntityFrom(new StringReader("12.12.2020"));
        assertThat(dateField.getValue()).isEqualTo("12.12.2020");
        
        dateField = dateFieldDescriptor.readItemEntityFrom(new StringReader("12.12.9999abc"));
        assertThat(dateField.getValue()).isEqualTo("12.12.9999");

        assertThrows(FlatDataReadException.class, () ->
                dateFieldDescriptor.readItemEntityFrom(new StringReader("12.12.000")));
        
        dateField = dateFieldDescriptor.readItemEntityFrom(new StringReader("          "));
        assertThat(dateField.getValue()).isEmpty();

        assertThrows(FlatDataReadException.class, () ->
                dateFieldDescriptor.readItemEntityFrom(new StringReader("         ")));
    }
    
    @Test
    void ConstrainedDelimitedField() {
        ConstrainedFieldDescriptor numberFieldDescriptor = ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("number").withDefaultValue("42").withDelimiter(':').build())
                .addConstraint(new ChoiceValueConstraint(
                        "allowed-numbers", new HashSet<>(Arrays.asList("", "42", "0815", "4711"))))
                .build();

        ConstrainedField newNumberField = numberFieldDescriptor.createItemEntity();
        assertThat(newNumberField.getValue()).isEqualTo("42");
        
        StringReader reader = new StringReader("0815:4711::666");

        ConstrainedField number1 = numberFieldDescriptor.readItemEntityFrom(reader);
        assertThat(number1.getValue()).isEqualTo("0815");
        ConstrainedField number2 = numberFieldDescriptor.readItemEntityFrom(reader);
        assertThat(number2.getValue()).isEqualTo("4711");
        ConstrainedField number3 = numberFieldDescriptor.readItemEntityFrom(reader);
        assertThat(number3.getValue()).isEmpty();
        
        assertThrows(ValueConstraintViolationException.class, () -> numberFieldDescriptor.readItemEntityFrom(reader));
    }
}
