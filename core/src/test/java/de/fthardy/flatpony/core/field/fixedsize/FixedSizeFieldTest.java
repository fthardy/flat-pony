package de.fthardy.flatpony.core.field.fixedsize;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.FlatDataField;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FixedSizeFieldTest {

    @Test
    void Length_of_the_field_is_field_size_from_descriptor() {
        FixedSizeField field = new FixedSizeFieldDescriptor("Foo", 10).createItem();
        assertThat(field.getLength()).isEqualTo(10);
    }

    @Test
    void Write_to_target_stream() {
        StringWriter writer = new StringWriter();
        FixedSizeField field = new FixedSizeFieldDescriptor("Foo", 10).createItem();

        field.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("          ");

        field.setValue("1234567890");
        field.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("          1234567890");
    }

    @Test
    void IOException_from_writer() throws IOException {
        Writer writerMock = mock(Writer.class);
        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write(anyString());

        FixedSizeField field = new FixedSizeFieldDescriptor("Foo", 10).createItem();
        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () -> field.writeTo(writerMock));
        assertThat(exception.getMessage()).isEqualTo(FixedSizeField.MSG_Write_failed("Foo"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(writerMock).write(anyString());
        verifyNoMoreInteractions(writerMock);
    }

    @Test
    void Calls_correct_handler_method() {
        FixedSizeField field = new FixedSizeFieldDescriptor("Foo", 10).createItem();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItem(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleFixedSizeField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}