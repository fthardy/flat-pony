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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConstantFieldDescriptorTest {

    @Test
    void Cannot_create_with_null_name() {
        assertThrows(NullPointerException.class, () -> ConstantFieldDescriptor.newInstance(null));
    }

    @Test
    void Cannot_create_with_empty_name() {
        assertThrows(IllegalArgumentException.class, () -> ConstantFieldDescriptor.newInstance(""));
    }

    @Test
    void Cannot_create_with_null_constant() {
        assertThrows(NullPointerException.class, () -> ConstantFieldDescriptor.newInstance("Test")
                .withConstant(null));
    }

    @Test
    void Cannot_create_with_empty_constant() {
        assertThrows(IllegalArgumentException.class, () -> ConstantFieldDescriptor.newInstance("Test")
                .withConstant(""));
    }

    @Test
    void Can_create_with_constant_containing_only_whitespace() {
        assertEquals("\t \n", ConstantFieldDescriptor.newInstance("Test")
                .withConstant("\t \n")
                .build()
                .getDefaultValue());
    }

    @Test
    void Default_value_is_constant_value() {
        assertEquals("Foo", 
                ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build().getDefaultValue());
    }

    @Test
    void Min_length_is_length_of_constant() {
        ConstantFieldDescriptor descriptor = 
                ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build();
        assertEquals(descriptor.getDefaultValue().length(), descriptor.getMinLength());
    }

    @Test
    void Read_from_stream_which_is_too_short() {
        ConstantFieldDescriptor descriptor = ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build();

        StringReader reader = new StringReader("Fu");

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemEntityFrom(reader));

        assertEquals(ConstantFieldDescriptor.MSG_Invalid_length(descriptor.getName()), exception.getMessage());
    }

    @Test
    void Read_from_stream_which_contains_wrong_value() {
        ConstantFieldDescriptor descriptor = 
                ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build();

        String value = "Fuu";
        StringReader reader = new StringReader(value);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemEntityFrom(reader));

        assertEquals(ConstantFieldDescriptor.MSG_Invalid_value(
                descriptor.getName(), value, descriptor.getDefaultValue()), exception.getMessage());
    }

    @Test
    void Read_from_stream_which_throws_an_IOException() throws IOException {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build();

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
    void Returns_always_the_same_field_instance() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build();
        assertSame(descriptor.createItemEntity(), descriptor.createItemEntity());
        StringReader reader = new StringReader("Foo");
        assertSame(descriptor.createItemEntity(), descriptor.readItemEntityFrom(reader));
    }

    @Test
    void Calls_correct_handler_method() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Test").withConstant("Foo").build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleConstantFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }
}