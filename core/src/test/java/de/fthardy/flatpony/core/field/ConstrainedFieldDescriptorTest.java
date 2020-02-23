/*
MIT License

Copyright (c) 2019 Frank Hardy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.field.constraint.AbstractValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConstrainedFieldDescriptorTest {

    @Test
    void cannot_create_with_null_descriptor() {
        assertThrows(NullPointerException.class, () -> ConstrainedFieldDescriptor.newInstance(null));
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

        assertThrows(IllegalArgumentException.class, () -> ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(constraint1).addConstraint(constraint2));
    }

    @Test
    void default_value_violates_constraint() {
        assertThrows(ValueConstraintViolationException.class, () -> ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return true;
                    }
                }).build());
    }

    @Test
    void create_without_constraint_violations() {
        ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return false;
                    }
                });
    }

    @Test
    void Min_length_is_min_length_of_field() {
        ConstrainedFieldDescriptor descriptor = ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return false;
                    }
                }).build();
        assertEquals(descriptor.getDecoratedFieldDescriptor().getMinLength(), descriptor.getMinLength());
    }

    @Test
    void Calls_correct_handler_method() {
        ConstrainedFieldDescriptor descriptor = ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return false;
                    }
                }).build();

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