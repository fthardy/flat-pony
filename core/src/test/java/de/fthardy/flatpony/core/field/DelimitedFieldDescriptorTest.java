package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    void Calls_correct_handler_method() {
        DelimitedFieldDescriptor descriptor = new DelimitedFieldDescriptor("Foo");

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleDelimitedFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }
}