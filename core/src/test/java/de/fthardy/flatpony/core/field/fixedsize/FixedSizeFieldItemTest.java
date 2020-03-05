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
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FixedSizeFieldItemTest {

    @Test
    void Cannot_create_descriptor_with_field_size_0() {
        assertThrows(IllegalArgumentException.class, () -> FixedSizeFieldDescriptor.newInstance("Field")
                .withFieldSize(0));
    }
    
    @Test
    void Cannot_create_descriptor_with_negative_field_size() {
        assertThrows(IllegalArgumentException.class, () -> FixedSizeFieldDescriptor.newInstance("Field")
                .withFieldSize(-1));
    }

    @Test
    void Cannot_create_descriptor_with_default_value_null() {
        assertThrows(NullPointerException.class, () -> FixedSizeFieldDescriptor.newInstance("Field")
                .withDefaultValue(null));
    }

    @Test
    void Cannot_create_descriptor_with_null_ContentValueTransformer() {
        assertThrows(NullPointerException.class, () -> FixedSizeFieldDescriptor.newInstance("Field")
                .useContentValueTransformer(null));
    }

    @Test
    void Create_descriptor_with_default_configuration() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Field").build();
        
        assertThat(fieldDescriptor.getName()).isEqualTo("Field");
        assertThat(fieldDescriptor.getMinLength()).isEqualTo(1);
        assertThat(fieldDescriptor.getDefaultValue()).isEqualTo("");

        FixedSizeField field = fieldDescriptor.createItemEntity();
        
        assertThat(field.getDescriptor()).isSameAs(fieldDescriptor);
        assertThat(field.getLength()).isEqualTo(1);
        assertThat(field.getValue()).isEqualTo("");

        StringWriter writer = new StringWriter();
        field.writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo(" ");
        
        field.setValue("foo");
        writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("f");
    }
    
    @Test
    void Create_descriptor_with_other_size() {
        FixedSizeFieldDescriptor fieldDescriptor = 
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(10).build();
        
        assertThat(fieldDescriptor.getName()).isEqualTo("Field");
        assertThat(fieldDescriptor.getMinLength()).isEqualTo(10);
        assertThat(fieldDescriptor.getDefaultValue()).isEqualTo("");

        FixedSizeField field = fieldDescriptor.createItemEntity();

        assertThat(field.getDescriptor()).isSameAs(fieldDescriptor);
        assertThat(field.getLength()).isEqualTo(10);
        assertThat(field.getValue()).isEqualTo("");

        StringWriter writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("          ");

        field.setValue("foo");
        writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("foo       ");
    }
    
    @Test
    void Create_descriptor_with_other_default_value() {
        FixedSizeFieldDescriptor fieldDescriptor = 
                FixedSizeFieldDescriptor.newInstance("Field").withDefaultValue("foo").build();
        
        assertThat(fieldDescriptor.getName()).isEqualTo("Field");
        assertThat(fieldDescriptor.getMinLength()).isEqualTo(1);
        assertThat(fieldDescriptor.getDefaultValue()).isEqualTo("foo");

        FixedSizeField field = fieldDescriptor.createItemEntity();

        assertThat(field.getDescriptor()).isSameAs(fieldDescriptor);
        assertThat(field.getLength()).isEqualTo(1);
        assertThat(field.getValue()).isEqualTo("foo");

        StringWriter writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("f");
    }
    
    @Test
    void Create_descriptor_with_other_content_value_transformer() {
        FixedSizeFieldDescriptor fieldDescriptor = 
                FixedSizeFieldDescriptor.newInstance("Field")
                        .useContentValueTransformer(new DefaultFieldContentValueTransformer('0', false))
                        .build();
        
        assertThat(fieldDescriptor.getName()).isEqualTo("Field");
        assertThat(fieldDescriptor.getMinLength()).isEqualTo(1);
        assertThat(fieldDescriptor.getDefaultValue()).isEqualTo("");

        FixedSizeField field = fieldDescriptor.createItemEntity();

        assertThat(field.getDescriptor()).isSameAs(fieldDescriptor);
        assertThat(field.getLength()).isEqualTo(1);
        assertThat(field.getValue()).isEqualTo("");

        StringWriter writer = new StringWriter();
        field.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo("0");
        
        field.setValue("foo");
        writer = new StringWriter();
        field.writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("o");
    }

    @Test
    void Apply_handler_to_descriptor() {
        FixedSizeFieldDescriptor descriptor = FixedSizeFieldDescriptor.newInstance("Field").build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleFixedSizeFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }

    @Test
    void Apply_handler_to_field() {
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Field").build().createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleFixedSizeField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }

    @Test
    void Read_correctly_padded_field_from_source_stream() {
        Reader reader = new StringReader("Foo  ");
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Field")
                .withFieldSize(5)
                .build()
                .readItemEntityFrom(reader);
        
        assertThat(field.getValue()).isEqualTo("Foo");
    }

    @Test
    void Read_incorrectly_padded_field_from_source_stream() {
        Reader reader = new StringReader("  FooBar");
        FixedSizeField field = FixedSizeFieldDescriptor.newInstance("Field")
                .withFieldSize(5)
                .build()
                .readItemEntityFrom(reader);
        
        assertThat(field.getValue()).isEqualTo("  Foo");
    }

    @Test
    void Reading_from_source_stream_fails() throws IOException {
        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read(any(char[].class))).thenThrow(ioException);

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                FixedSizeFieldDescriptor.newInstance("Field").build().readItemEntityFrom(readerMock));
        assertThat(exception.getMessage()).isEqualTo(FixedSizeFieldDescriptor.MSG_Read_failed("Field"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(readerMock).read(any(char[].class));
    }
    
    @Test
    void Source_stream_is_not_long_enough() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Field")
                .withFieldSize(5).build();
        
        Reader reader = new StringReader("Foo");

        FlatDataReadException exception = 
                assertThrows(FlatDataReadException.class, () -> fieldDescriptor.readItemEntityFrom(reader));
        assertThat(exception.getMessage()).isEqualTo(FixedSizeFieldDescriptor.MSG_Input_stream_too_short(
                fieldDescriptor.getName(), fieldDescriptor.getMinLength(), 3));
    }

    @Test
    void Write_to_target_stream() {
        FixedSizeField field =
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(10).build().createItemEntity();

        StringWriter writer = new StringWriter();
        field.writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("          ");

        field.setValue("1234567890");
        writer = new StringWriter();
        field.writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("1234567890");

        field.setValue("12345");
        writer = new StringWriter();
        field.writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("12345     ");

        field.setValue("1234567890abc");
        writer = new StringWriter();
        field.writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("1234567890");
    }

    @Test
    void Writing_to_target_stream_fails() throws IOException {
        Writer writerMock = mock(Writer.class);
        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write(anyString());

        FixedSizeField field = 
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(10).build().createItemEntity();
        
        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () -> field.writeTo(writerMock));
        assertThat(exception.getMessage()).isEqualTo(FixedSizeField.MSG_Write_failed("Field"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(writerMock).write(anyString());
        verifyNoMoreInteractions(writerMock);
    }
}