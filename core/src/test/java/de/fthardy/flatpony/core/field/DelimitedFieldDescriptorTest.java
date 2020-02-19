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
        assertThrows(NullPointerException.class, () -> DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue(null));
    }

    @Test
    void The_default_value_is_allowed_to_be_empty() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue("")
                .build();
        assertThat(descriptor.getDefaultValue()).isEmpty();
    }

    @Test
    void A_new_field_has_the_default_value() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue("Bar")
                .build();

        DelimitedField field = descriptor.createItemEntity();
        assertThat(field.getValue()).isEqualTo("Bar");
    }

    @Test
    void The_delimiter_is_ignored_during_read() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Foo").build();

        StringReader reader = new StringReader("Test,");

        DelimitedField field = descriptor.readItemEntityFrom(reader);
        assertThat(field.getValue()).isEqualTo("Test");
    }

    @Test
    void Min_length_is_0() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Foo").build();

        assertThat(descriptor.getMinLength()).isEqualTo(0);
    }

    @Test
    void The_reader_throws_an_IOException() throws IOException {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Foo").build();

        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read()).thenThrow(ioException);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemEntityFrom(readerMock));

        assertEquals(DelimitedFieldDescriptor.MSG_Read_failed(descriptor.getName()), exception.getMessage());
        assertSame(ioException, exception.getCause());

        verify(readerMock).read();
        verifyNoMoreInteractions(readerMock);
    }

    @Test
    void Calls_correct_handler_method() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Foo").build();

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