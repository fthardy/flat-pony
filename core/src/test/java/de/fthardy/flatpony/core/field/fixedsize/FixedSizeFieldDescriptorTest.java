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
package de.fthardy.flatpony.core.field.fixedsize;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FixedSizeFieldDescriptorTest {

    @Test
    void Cannot_create_with_a_field_size_smaller_than_1() {
        assertThrows(IllegalArgumentException.class, () -> FixedSizeFieldDescriptor.newInstance("Foo")
                .withFieldSize(0));
    }

    @Test
    void Cannot_create_with_null_ContentValueTransformer() {
        assertThrows(NullPointerException.class, () ->
                FixedSizeFieldDescriptor.newInstance("Foo")
                        .withDefaultValue("J")
                        .useContentValueTransformer(null));
    }

    @Test
    void New_field_has_default_value() {
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Foo").withFieldSize(10).build().createItemEntity();
        assertThat(field.getValue()).isEmpty();
    }

    @Test
    void Min_Lenght_is_size() {
        FixedSizeFieldDescriptor descriptor = FixedSizeFieldDescriptor.newInstance("Foo")
                .withFieldSize(10)
                .build();
        assertThat(descriptor.getMinLength()).isEqualTo(descriptor.getFieldSize());
    }

    @Test
    void Field_is_read_from_source_stream() {
        Reader reader = new StringReader("FooBar    ");
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Foo")
                .withFieldSize(10)
                .build()
                .readItemEntityFrom(reader);
        assertThat(field.getValue()).isEqualTo("FooBar");
    }

    @Test
    void IOException_during_read() throws IOException {
        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read(any(char[].class))).thenThrow(ioException);

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                FixedSizeFieldDescriptor.newInstance("Foo").withFieldSize(10).build().readItemEntityFrom(readerMock));
        assertThat(exception.getMessage()).isEqualTo(FixedSizeFieldDescriptor.MSG_Read_failed("Foo"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(readerMock).read(any(char[].class));
    }

    @Test
    void Calls_correct_handler_method() {
        FixedSizeFieldDescriptor descriptor = FixedSizeFieldDescriptor.newInstance("Foo")
                .withFieldSize(10).build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleFixedSizeFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }
}