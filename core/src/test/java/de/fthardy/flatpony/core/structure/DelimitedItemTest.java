package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItem;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DelimitedItemTest {

    @Test
    void Write_to_target_stream() {
        DelimitedItem item = new DelimitedItemDescriptor(
                "Foo", new ConstantFieldDescriptor("Id", "Test")).createItem();

        StringWriter writer = new StringWriter();

        item.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("Test\n");
    }

    @Test
    void Write_causes_IOException() throws IOException {

        Writer writerMock = mock(Writer.class);

        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write(anyInt());

        FlatDataItem<?> innerItemMock = mock(FlatDataItem.class);

        DelimitedItem item = new DelimitedItem(
                new DelimitedItemDescriptor("Foo", new ConstantFieldDescriptor("Id", "Test")),
                innerItemMock);

        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () -> item.writeTo(writerMock));

        assertThat(exception.getMessage()).isEqualTo(DelimitedItem.MSG_Write_failed("Foo"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(innerItemMock).writeTo(writerMock);
        verifyNoMoreInteractions(innerItemMock);

        verify(writerMock).write(anyInt());
        verifyNoMoreInteractions(writerMock);
    }

    @Test
    void Calls_correct_handler_method() {
        DelimitedItem item = new DelimitedItemDescriptor(
                "Foo", new ConstantFieldDescriptor("Id", "Test")).createItem();

        FlatDataItem.Handler handlerMock = mock(FlatDataItem.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        item.applyHandler(handlerMock);
        item.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItem(item);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleDelimitedItem(item);
        verifyNoMoreInteractions(structureHandlerMock);
    }
}