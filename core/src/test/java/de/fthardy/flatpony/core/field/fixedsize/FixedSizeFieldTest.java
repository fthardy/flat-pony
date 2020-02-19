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
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Foo").withFieldSize(10).build().createItemEntity();
        assertThat(field.getLength()).isEqualTo(10);
    }

    @Test
    void Write_to_target_stream() {
        StringWriter writer = new StringWriter();
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Foo").withFieldSize(10).build().createItemEntity();

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

        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Foo").withFieldSize(10).build().createItemEntity();
        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () -> field.writeTo(writerMock));
        assertThat(exception.getMessage()).isEqualTo(FixedSizeField.MSG_Write_failed("Foo"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(writerMock).write(anyString());
        verifyNoMoreInteractions(writerMock);
    }

    @Test
    void Calls_correct_handler_method() {
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Foo").withFieldSize(10).build().createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleFixedSizeField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}