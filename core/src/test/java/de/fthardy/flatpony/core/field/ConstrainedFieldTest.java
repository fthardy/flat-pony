package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.constraint.AbstractValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConstrainedFieldTest {

    @Test
    void new_value_violates_constraint() {
        ConstrainedFieldDescriptor descriptor = new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"),
                new HashSet<>(Collections.singletonList(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return s.equals("FOO");
                    }
                })));

        ConstrainedField field = descriptor.createItem();

        assertThrows(ValueConstraintViolationException.class, () -> field.setValue("FOO"));
    }

    @Test
    void new_value_does_not_violate_constraint() {
        ConstrainedFieldDescriptor descriptor = new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"),
                new HashSet<>(Collections.singletonList(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return s.equals("FOO");
                    }
                })));

        ConstrainedField field = descriptor.createItem();

        field.setValue("BAR");
    }

    @Test
    void Calls_correct_handler_method() {
        ConstrainedFieldDescriptor descriptor = new ConstrainedFieldDescriptor(
                new DelimitedFieldDescriptor("Foo"),
                new HashSet<>(Collections.singletonList(new AbstractValueConstraint() {
                    @Override
                    public boolean test(String s) {
                        return s.equals("FOO");
                    }
                })));
        ConstrainedField field = descriptor.createItem();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItem(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleConstrainedField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}