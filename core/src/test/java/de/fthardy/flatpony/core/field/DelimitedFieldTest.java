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
        DelimitedField field = DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue("Bar")
                .build()
                .createItemEntity();
        assertThat(field.getValue()).isEqualTo("Bar");
    }

    @Test
    void Field_length_is_length_of_value() {
        DelimitedField field = DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue("Bar")
                .build()
                .createItemEntity();
        assertThat(field.getLength()).isEqualTo(3);
    }

    @Test
    void Value_with_delimiter_is_written_to_target_stream() {
        DelimitedField field = DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue("Bar")
                .build()
                .createItemEntity();

        StringWriter writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("Bar,");
    }

    @Test
    void Writer_throws_an_IOException() throws IOException {
        DelimitedField field = DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue("Bar")
                .build()
                .createItemEntity();

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
        DelimitedField field = DelimitedFieldDescriptor.newInstance("Foo")
                .withDefaultValue("Bar")
                .build()
                .createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleDelimitedField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}