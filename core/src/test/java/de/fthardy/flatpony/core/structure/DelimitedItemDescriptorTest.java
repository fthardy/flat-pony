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
package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DelimitedItemDescriptorTest {

    @Test
    void Cannot_create_with_no_item() {
        assertThrows(NullPointerException.class, () -> DelimitedItemDescriptor.newInstance(null));
    }

    @Test
    void Delimiter_is_missing_on_read() {
        Reader reader = new StringReader("Test123");

        FlatDataReadException exception = 
                assertThrows(FlatDataReadException.class, () -> DelimitedItemDescriptor.newInstance(
                        ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build())
                        .build()
                        .readItemEntityFrom(reader));
        assertThat(exception.getMessage()).isEqualTo(
                DelimitedItemDescriptor.MSG_No_delimiter_found("ID"));
    }

    @Test
    void Normal_read_with_delimiter() {
        Reader reader = new StringReader("Test\n123");

        DelimitedItemEntity item = DelimitedItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build())
                .build()
                .readItemEntityFrom(reader);
        assertNotNull(item);
    }

    @Test
    void Min_length_is_min_length_of_inner_item() {
        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build())
                .build();
        assertThat(descriptor.getMinLength()).isEqualTo(4);
    }

    @Test
    void Read_without_delimiter_but_end_of_stream() {
        Reader reader = new StringReader("Test");

        DelimitedItemEntity item = DelimitedItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build())
                .build()
                .readItemEntityFrom(reader);
        assertNotNull(item);
    }

    @Test
    void Read_with_IOException() throws IOException {
        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read()).thenThrow(ioException);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("Foo");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null); // Returning null doesn't matter here

        FlatDataReadException exception = 
                assertThrows(FlatDataReadException.class, () -> DelimitedItemDescriptor.newInstance(itemDescriptorMock)
                        .build()
                        .readItemEntityFrom(readerMock));
        assertThat(exception.getMessage()).isEqualTo(DelimitedItemDescriptor.MSG_Read_failed("Foo"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(readerMock).read();
        verifyNoMoreInteractions(readerMock);

        verify(itemDescriptorMock, times(2)).getName();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        verifyNoMoreInteractions(itemDescriptorMock);
    }

    @Test
    void Calls_correct_handler_method() {
        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build()).build();

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