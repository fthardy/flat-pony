package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.field.constraint.AbstractValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConstrainedFieldDescriptorTest {

    @Test
    void cannot_create_with_null_descriptor() {
        assertThrows(NullPointerException.class, () -> new ConstrainedFieldDescriptor(null, null));
    }

    @Test
    void cannot_create_with_null_constraints() {
        assertThrows(NullPointerException.class, () -> new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"), null));
    }

    @Test
    void cannot_create_with_empty_constraints() {
        assertThrows(IllegalArgumentException.class, () -> new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"), Collections.emptySet()));
    }

    @Test
    void cannot_create_with_ambiguous_constraint_names() {
        ValueConstraint constraint1 = new ValueConstraint() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public boolean test(String s) {
                return false;
            }
        };

        ValueConstraint constraint2 = new ValueConstraint() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public boolean test(String s) {
                return false;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"), new HashSet<>(Arrays.asList(constraint1, constraint2))));
    }

    @Test
    void default_value_violates_constraint() {
        assertThrows(ValueConstraintViolationException.class, () -> new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"), new HashSet<>(Collections.singletonList(
                        new AbstractValueConstraint() {
            @Override
            public boolean test(String s) {
                return true;
            }
        }))));
    }

    @Test
    void create_without_constraint_violations() {
        new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"),
                new HashSet<>(Collections.singletonList(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return false;
                    }
                })));
    }

    @Test
    void Min_length_is_min_length_of_field() {
        ConstrainedFieldDescriptor descriptor = new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"),
                new HashSet<>(Collections.singletonList(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return false;
                    }
                })));
        assertEquals(descriptor.getFieldDescriptor().getMinLength(), descriptor.getMinLength());
    }

    @Test
    void Calls_correct_handler_method() {
        ConstrainedFieldDescriptor descriptor = new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"),
                new HashSet<>(Collections.singleton(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return false;
                    }
                })));

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleConstrainedFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }
}