package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemDescriptorHandler;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.constraint.DefaultValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DelimitedFieldDescriptorTest {

    @Test
    void The_default_value_cannot_be_null() {
        assertThrows(NullPointerException.class, () -> new DelimitedFieldDescriptor("Foo", null));
    }

    @Test
    void The_default_value_is_allowed_to_be_empty() {
        DelimitedFieldDescriptor descriptor = new DelimitedFieldDescriptor("Foo", "");
        assertThat(descriptor.getDefaultValue()).isEmpty();
    }

    @Test
    void The_default_value_violates_the_given_constraints() {
        DefaultValueConstraint notEmpty1 = new DefaultValueConstraint("Foo", String::isEmpty);
        DefaultValueConstraint notEmpty2 = new DefaultValueConstraint("Bar", String::isEmpty);

        Set<ValueConstraint> constraints = new LinkedHashSet<>(Arrays.asList(notEmpty1, notEmpty2));

        ValueConstraintViolationException exception = assertThrows(
                ValueConstraintViolationException.class, () ->
                        new DelimitedFieldDescriptor("Field", ',', "", constraints));
        assertThat(exception.getFieldName()).isEqualTo("Field");
        assertThat(exception.getValue()).isEqualTo("");
        assertThat(exception.getConstraintNames()).containsExactly("Foo", "Bar");
    }

    @Test
    void A_new_field_has_the_default_value() {
        DelimitedFieldDescriptor descriptor = new DelimitedFieldDescriptor("Foo", "Bar");

        DelimitedField field = descriptor.createItem();
        assertThat(field.getValue()).isEqualTo("Bar");
    }

    @Test
    void The_delimiter_is_ignored_during_read() {
        DelimitedFieldDescriptor descriptor = new DelimitedFieldDescriptor("Foo");

        StringReader reader = new StringReader("Test,");

        DelimitedField field = descriptor.readItemFrom(reader);
        assertThat(field.getValue()).isEqualTo("Test");
    }

    @Test
    void The_reader_throws_an_IOException() throws IOException {
        DelimitedFieldDescriptor descriptor = new DelimitedFieldDescriptor("Foo");

        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read()).thenThrow(ioException);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemFrom(readerMock));

        assertEquals(DelimitedFieldDescriptor.MSG_Read_failed(descriptor.getName()), exception.getMessage());
        assertSame(ioException, exception.getCause());

        verify(readerMock).read();
        verifyNoMoreInteractions(readerMock);
    }

    @Test
    void The_read_value_violates_the_given_constraints() {
        DefaultValueConstraint dontBeAFoo = new DefaultValueConstraint("You Foo!", v -> v.equals("Foo"));

        Set<ValueConstraint> constraints =
                new LinkedHashSet<>(Collections.singletonList(dontBeAFoo));

        DelimitedFieldDescriptor descriptor =
                new DelimitedFieldDescriptor("Field", ',', "", constraints);

        StringReader reader = new StringReader("Foo");

        FlatDataReadException readException =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemFrom(reader));
        assertThat(readException.getMessage()).isEqualTo(DelimitedFieldDescriptor.MSG_Read_failed("Field"));

        ValueConstraintViolationException exception =
                (ValueConstraintViolationException) readException.getCause();
        assertThat(exception.getFieldName()).isEqualTo("Field");
        assertThat(exception.getValue()).isEqualTo("Foo");
        assertThat(exception.getConstraintNames()).containsExactly("You Foo!");
    }

    @Test
    void Calls_correct_handler_method() {
        DelimitedFieldDescriptor descriptor = new DelimitedFieldDescriptor("Foo");

        FlatDataItemDescriptorHandler handlerMock = mock(FlatDataItemDescriptorHandler.class);
        FlatDataFieldDescriptorHandler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptorHandler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleDelimitedFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }
}