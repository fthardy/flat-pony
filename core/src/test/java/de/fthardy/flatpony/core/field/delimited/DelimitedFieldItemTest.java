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
package de.fthardy.flatpony.core.field.delimited;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.delimited.DelimitedField;
import de.fthardy.flatpony.core.field.delimited.DelimitedFieldDescriptor;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DelimitedFieldItemTest {

    @Test
    void Cannot_create_descriptor_with_default_value_null() {
        assertThrows(NullPointerException.class, () -> DelimitedFieldDescriptor.newInstance("Field")
                .withDefaultValue(null));
    }

    @Test
    void Create_descriptor_with_default_configuration() {
        DelimitedFieldDescriptor fieldDescriptor = DelimitedFieldDescriptor.newInstance("Field").build();
        assertThat(fieldDescriptor.toString()).startsWith(DelimitedFieldDescriptor.class.getSimpleName());
        
        assertThat(fieldDescriptor.getName()).isEqualTo("Field");
        assertThat(fieldDescriptor.getMinLength()).isEqualTo(0);
        assertThat(fieldDescriptor.getDefaultValue()).isEqualTo("");

        DelimitedField field = fieldDescriptor.createItemEntity();

        assertThat(field.getDescriptor()).isSameAs(fieldDescriptor);
        assertThat(field.getLength()).isEqualTo(0);
        assertThat(field.getValue()).isEqualTo("");

        StringWriter writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo(",");

        field.setValue("foo");
        writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("foo,");
    }

    @Test
    void Create_descriptor_with_other_default_value() {
        DelimitedFieldDescriptor fieldDescriptor =
                DelimitedFieldDescriptor.newInstance("Field").withDefaultValue("foo").build();
        
        assertThat(fieldDescriptor.getName()).isEqualTo("Field");
        assertThat(fieldDescriptor.getMinLength()).isEqualTo(0);
        assertThat(fieldDescriptor.getDefaultValue()).isEqualTo("foo");

        DelimitedField field = fieldDescriptor.createItemEntity();

        assertThat(field.getDescriptor()).isSameAs(fieldDescriptor);
        assertThat(field.getLength()).isEqualTo(3);
        assertThat(field.getValue()).isEqualTo("foo");

        StringWriter writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("foo,");
    }

    @Test
    void Create_descriptor_with_other_delimiter() {
        DelimitedFieldDescriptor fieldDescriptor =
                DelimitedFieldDescriptor.newInstance("Field").withDelimiter('-').build();
        
        assertThat(fieldDescriptor.getName()).isEqualTo("Field");
        assertThat(fieldDescriptor.getMinLength()).isEqualTo(0);
        assertThat(fieldDescriptor.getDefaultValue()).isEqualTo("");

        DelimitedField field = fieldDescriptor.createItemEntity();

        assertThat(field.getDescriptor()).isSameAs(fieldDescriptor);
        assertThat(field.getLength()).isEqualTo(0);
        assertThat(field.getValue()).isEqualTo("");

        StringWriter writer = new StringWriter();
        field.writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("-");

        field.setValue("foo");
        writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("foo-");
    }

    @Test
    void Apply_handler_to_descriptor() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Field").build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleDelimitedFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }

    @Test
    void Apply_handler_to_field() {
        DelimitedField field = DelimitedFieldDescriptor.newInstance("Field")
                .withDefaultValue("Bar")
                .build()
                .createItemEntity();
        assertThat(field.toString()).startsWith(DelimitedField.class.getSimpleName());

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleDelimitedField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }

    @Test
    void Read_with_delimiter() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Field").build();

        Reader reader = new StringReader("Test,Bla");

        DelimitedField field = descriptor.readItemEntityFrom(reader);
        
        assertThat(field.getValue()).isEqualTo("Test");
    }

    @Test
    void Read_where_content_is_at_the_end_of_the_stream() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Field").build();

        Reader reader = new StringReader("Test");

        DelimitedField field = descriptor.readItemEntityFrom(reader);
        
        assertThat(field.getValue()).isEqualTo("Test");
    }

    @Test
    void Reading_from_source_stream_fails() throws IOException {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Field").build();

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
    void Push_read() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Field").build();
        
        Reader reader = new StringReader("TestTest,Test");

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        descriptor.pushReadFrom(reader, streamReadHandlerMock);

        verify(streamReadHandlerMock).onFieldItem(descriptor, "TestTest");
        verifyNoMoreInteractions(streamReadHandlerMock);
    }

    @Test
    void Pull_read() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Field").build();

        Reader reader = new StringReader("TestTest,Test");

        PullReadIterator pullReadIterator = descriptor.pullReadFrom(reader);
        assertTrue(pullReadIterator.hasNextEvent());

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        pullReadIterator.nextEvent(streamReadHandlerMock);

        assertFalse(pullReadIterator.hasNextEvent());

        verify(streamReadHandlerMock).onFieldItem(descriptor, "TestTest");
        verifyNoMoreInteractions(streamReadHandlerMock);
    }

    @Test
    void Writing_to_target_stream_fails() throws IOException {
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
}
