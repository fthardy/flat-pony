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

import de.fthardy.flatpony.core.FlatDataItemEntity;
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
        DelimitedItemEntity item = DelimitedItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build())
                .build()
                .createItemEntity();

        StringWriter writer = new StringWriter();

        item.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("Test\n");
    }

    @Test
    void Write_causes_IOException() throws IOException {

        Writer writerMock = mock(Writer.class);

        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write(anyInt());

        FlatDataItemEntity<?> innerItemMock = mock(FlatDataItemEntity.class);

        DelimitedItemEntity item = new DelimitedItemEntity(DelimitedItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build())
                .build(), 
                innerItemMock);

        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () -> item.writeTo(writerMock));

        assertThat(exception.getMessage()).isEqualTo(DelimitedItemEntity.MSG_Write_failed("ID"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(innerItemMock).writeTo(writerMock);
        verifyNoMoreInteractions(innerItemMock);

        verify(writerMock).write(anyInt());
        verifyNoMoreInteractions(writerMock);
    }

    @Test
    void Calls_correct_handler_method() {
        DelimitedItemEntity item = DelimitedItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("ID").withConstant("Test").build())
                .build()
                .createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        item.applyHandler(handlerMock);
        item.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(item);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleDelimitedItemEntity(item);
        verifyNoMoreInteractions(structureHandlerMock);
    }
}