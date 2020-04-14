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
package de.fthardy.flatpony.core.field.constant;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.constant.ConstantField;
import de.fthardy.flatpony.core.field.constant.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConstantFieldItemTest {

    @Test
    void Cannot_create_descriptor_with_null_name() {
        assertThrows(NullPointerException.class, () -> ConstantFieldDescriptor.newInstance(null));
    }

    @Test
    void Cannot_create_descriptor_with_empty_name() {
        assertThrows(IllegalArgumentException.class, () -> ConstantFieldDescriptor.newInstance(""));
    }

    @Test
    void Cannot_create_descriptor_with_null_constant() {
        assertThrows(NullPointerException.class, () -> ConstantFieldDescriptor.newInstance("Test")
                .withConstant(null));
    }

    @Test
    void Cannot_create_descriptor_with_empty_constant() {
        assertThrows(IllegalArgumentException.class, () -> ConstantFieldDescriptor.newInstance("Test")
                .withConstant(""));
    }

    @Test
    void Create_descriptor_with_constant_containing_only_whitespace() {
        assertEquals("\t \n", ConstantFieldDescriptor.newInstance("Test")
                .withConstant("\t \n")
                .build()
                .getDefaultValue());
    }
    
    @Test
    void Create_descriptor() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build();
        
        assertThat(descriptor.getMinLength()).isEqualTo(descriptor.getDefaultValue().length());
        assertThat(descriptor.getDefaultValue()).isEqualTo("Foo");

        ConstantField constantField = descriptor.createItemEntity();
        
        assertThat(constantField.getLength()).isEqualTo(descriptor.getMinLength());
        assertThat(constantField.getValue()).isEqualTo(descriptor.getDefaultValue());
    }
    
    @Test
    void Create_descriptor_for_a_reserved_space_with_default_fill_char() {
        ConstantFieldDescriptor descriptor = ConstantFieldDescriptor.reservedSpace(3);
        
        assertThat(descriptor.getName()).startsWith("reserve-");
        
        StringWriter writer = new StringWriter();
        descriptor.createItemEntity().writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("   ");
    }
    
    @Test
    void Create_descriptor_for_a_reserved_space_with_other_fill_char() {
        ConstantFieldDescriptor descriptor = ConstantFieldDescriptor.reservedSpace(3, '!');
        
        assertThat(descriptor.getName()).startsWith("reserve-");
        
        StringWriter writer = new StringWriter();
        descriptor.createItemEntity().writeTo(writer);
        
        assertThat(writer.getBuffer().toString()).isEqualTo("!!!");
    }

    @Test
    void Field_is_immutable() {
        assertThrows(UnsupportedOperationException.class, () -> ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("Foo").build()
                .createItemEntity()
                .asMutableField());
    }

    @Test
    void Apply_handler_to_descriptor() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleConstantFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }

    @Test
    void Apply_handler_to_field() {
        ConstantField field = 
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build().createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleConstantField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }

    @Test
    void Read_from_stream_which_is_too_short() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build();

        StringReader reader = new StringReader("Fu");

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemEntityFrom(reader));

        assertEquals(ConstantFieldDescriptor.MSG_Invalid_length(descriptor.getName()), exception.getMessage());
    }

    @Test
    void Read_from_stream_which_contains_wrong_value() {
        ConstantFieldDescriptor descriptor = 
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build();

        String value = "Fuu";
        StringReader reader = new StringReader(value);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemEntityFrom(reader));

        assertEquals(ConstantFieldDescriptor.MSG_Invalid_value(
                descriptor.getName(), value, descriptor.getDefaultValue()), exception.getMessage());
    }

    @Test
    void Reading_from_source_stream_fails() throws IOException {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build();

        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read(any(char[].class))).thenThrow(ioException);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemEntityFrom(readerMock));

        assertEquals(ConstantFieldDescriptor.MSG_Read_failed(descriptor.getName()), exception.getMessage());
        assertSame(ioException, exception.getCause());

        verify(readerMock).read(any(char[].class));
        verifyNoMoreInteractions(readerMock);
    }

    @Test
    void Push_read() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build();

        Reader reader = new StringReader("FooFoo");

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        descriptor.pushReadFrom(reader, streamReadHandlerMock);

        verify(streamReadHandlerMock).onFieldItem(descriptor, "Foo");
        verifyNoMoreInteractions(streamReadHandlerMock);
    }

    @Test
    void Pull_read() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("Foo").build();

        Reader reader = new StringReader("FooFoo");

        PullReadIterator pullReadIterator = descriptor.pullReadFrom(reader);
        assertTrue(pullReadIterator.hasNextEvent());

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        pullReadIterator.nextEvent(streamReadHandlerMock);
        
        assertFalse(pullReadIterator.hasNextEvent());

        verify(streamReadHandlerMock).onFieldItem(descriptor, "Foo");
        verifyNoMoreInteractions(streamReadHandlerMock);
    }

    @Test
    void Writing_to_target_stream_fails() throws IOException {
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
}