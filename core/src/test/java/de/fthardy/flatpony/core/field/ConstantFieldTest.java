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
import static org.mockito.Mockito.*;

class ConstantFieldTest {

    @Test
    void Field_length_equals_length_of_constant() {
        assertEquals(3, ConstantFieldDescriptor.newInstance("Test")
                .withConstant("Foo").build()
                .createItemEntity()
                .getLength());
    }

    @Test
    void Field_value_equals_constant() {
        assertEquals("Foo", ConstantFieldDescriptor.newInstance("Test")
                .withConstant("Foo").build()
                .createItemEntity()
                .getValue());
    }

    @Test
    void Field_is_immutable() {
        assertThrows(UnsupportedOperationException.class, () -> ConstantFieldDescriptor.newInstance("Test")
                .withConstant("Foo").build()
                .createItemEntity()
                .asMutableField());
    }

    @Test
    void Writing_causes_an_IOException() throws IOException {
        Writer writerMock = mock(Writer.class);

        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write("Foo");

        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () ->
                ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build()
                        .createItemEntity().writeTo(writerMock));

        assertEquals(ConstantField.MSG_Write_failed("Test"), exception.getMessage());
        assertSame(ioException, exception.getCause());

        verify(writerMock).write("Foo");
        verifyNoMoreInteractions(writerMock);
    }

    @Test
    void Write_constant_to_target_stream() {
        StringWriter writer = new StringWriter();

        ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build().createItemEntity().writeTo(writer);

        assertEquals("Foo", writer.getBuffer().toString());
    }

    @Test
    void Calls_correct_handler_method() {
        ConstantField field = ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build().createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleConstantField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}