package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataWriteException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DelimitedFieldTest {

    @Test
    void Field_has_initially_default_value_from_descriptor() {
        DelimitedField field = new DelimitedFieldDescriptor("Foo", "Bar").createItem();
        assertThat(field.getValue()).isEqualTo("Bar");
    }

    @Test
    void Field_length_is_length_of_value() {
        DelimitedField field = new DelimitedFieldDescriptor("Foo", "Bar").createItem();
        assertThat(field.getLength()).isEqualTo(3);
    }

    @Test
    void Value_with_delimiter_is_written_to_target_stream() {
        DelimitedField field = new DelimitedFieldDescriptor("Foo", "Bar").createItem();

        StringWriter writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("Bar,");
    }

    @Test
    void Writer_throws_an_IOException() throws IOException {
        DelimitedField field = new DelimitedFieldDescriptor("Foo", "Bar").createItem();

        Writer writerMock = mock(Writer.class);

        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write(anyString());

        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () -> field.writeTo(writerMock));

        assertEquals(DelimitedField.MSG_Write_failed(field.getDescriptor().getName()), exception.getMessage());
        assertSame(ioException, exception.getCause());

        verify(writerMock).write(anyString());
        verifyNoMoreInteractions(writerMock);
    }

    @Test
    void Calls_correct_handler_method() {
        DelimitedField field = new DelimitedFieldDescriptor("Foo", "Bar").createItem();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItem(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleDelimitedField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}