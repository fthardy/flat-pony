package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DelimitedItemDescriptorTest {

    @Test
    void Cannot_create_with_no_item() {
        assertThrows(NullPointerException.class, () -> new DelimitedItemDescriptor("Foo", null));
    }

    @Test
    void Delimiter_is_missing_on_read() {
        Reader reader = new StringReader("Test123");

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () -> new DelimitedItemDescriptor(
                "Foo", new ConstantFieldDescriptor("ID", "Test")).readItemFrom(reader));
        assertThat(exception.getMessage()).isEqualTo(
                DelimitedItemDescriptor.MSG_No_delimiter_found("Foo", "ID"));
    }

    @Test
    void Normal_read_with_delimiter() {
        Reader reader = new StringReader("Test\n123");

        DelimitedItem item = new DelimitedItemDescriptor(
                "Foo", new ConstantFieldDescriptor("ID", "Test")).readItemFrom(reader);
        assertNotNull(item);
    }

    @Test
    void Read_without_delimiter_but_end_of_stream() {
        Reader reader = new StringReader("Test");

        DelimitedItem item = new DelimitedItemDescriptor(
                "Foo", new ConstantFieldDescriptor("ID", "Test")).readItemFrom(reader);
        assertNotNull(item);
    }

    @Test
    void Read_with_IOException() throws IOException {
        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read()).thenThrow(ioException);

        FlatDataItemDescriptor itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.readItemFrom(readerMock)).thenReturn(null); // Returning null doesn't matter here

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () -> new DelimitedItemDescriptor(
                "Foo", itemDescriptorMock).readItemFrom(readerMock));
        assertThat(exception.getMessage()).isEqualTo(DelimitedItemDescriptor.MSG_Read_failed("Foo"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(readerMock).read();
        verifyNoMoreInteractions(readerMock);

        verify(itemDescriptorMock).readItemFrom(readerMock);
        verifyNoMoreInteractions(itemDescriptorMock);
    }

    @Test
    void Calls_correct_handler_method() {
        DelimitedItemDescriptor descriptor = new DelimitedItemDescriptor(
                "Record", new ConstantFieldDescriptor("ID", "Foo"));

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleDelimitedItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }
}