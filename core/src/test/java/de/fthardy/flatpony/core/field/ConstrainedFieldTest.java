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

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.constraint.PredicateAdaptingValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConstrainedFieldTest {

    @Test
    void new_value_violates_constraint() {
        ConstrainedFieldDescriptor descriptor = ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(new PredicateAdaptingValueConstraint("constraint", v -> !v.equals("FOO"))).build();

        ConstrainedField field = descriptor.createItemEntity();

        assertThrows(ValueConstraintViolationException.class, () -> field.setValue("FOO"));
    }

    @Test
    void new_value_does_not_violate_constraint() {
        ConstrainedFieldDescriptor descriptor = ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(new PredicateAdaptingValueConstraint("constraint", v -> !v.equals("FOO"))).build();

        ConstrainedField field = descriptor.createItemEntity();

        field.setValue("BAR");
    }

    @Test
    void Calls_correct_handler_method() {
        ConstrainedFieldDescriptor descriptor = ConstrainedFieldDescriptor.newInstance(
                DelimitedFieldDescriptor.newInstance("Foo").build())
                .addConstraint(new PredicateAdaptingValueConstraint("constraint", v -> !v.equals("FOO"))).build();
        
        ConstrainedField field = descriptor.createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleConstrainedField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}