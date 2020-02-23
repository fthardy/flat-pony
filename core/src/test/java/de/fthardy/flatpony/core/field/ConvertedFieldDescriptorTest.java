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
import de.fthardy.flatpony.core.field.converter.IntegerFieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConvertedFieldDescriptorTest {
    
    @Test
    void Cannot_create_with_null_descriptor() {
        assertThrows(NullPointerException.class, () -> ConvertedFieldDescriptor.newInstance(null));
    }
    
    @Test
    void Cannot_create_with_null_converter() {
        assertThrows(NullPointerException.class, () -> ConvertedFieldDescriptor.newInstance(
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(1).build()).withFieldValueConverter(null));
    }
    
    @Test
    void Create_instance() {
        FixedSizeFieldDescriptor fieldDescriptor = 
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(1).build();
        ConvertedFieldDescriptor<Integer> convertedFieldDescriptor = 
                ConvertedFieldDescriptor.<Integer>newInstance(fieldDescriptor).withFieldValueConverter(
                        new IntegerFieldValueConverter()).build();
        
        assertThat(convertedFieldDescriptor.getDecoratedFieldDescriptor()).isSameAs(fieldDescriptor);
        assertThat(convertedFieldDescriptor.getName()).isEqualTo(fieldDescriptor.getName());
        assertThat(convertedFieldDescriptor.getDefaultValue()).isEqualTo(fieldDescriptor.getDefaultValue());
        assertThat(convertedFieldDescriptor.getMinLength()).isEqualTo(fieldDescriptor.getMinLength());
    }
    
    @Test
    void Calls_correct_handler_method() {
        ConvertedFieldDescriptor<Integer> descriptor = ConvertedFieldDescriptor.<Integer>newInstance(
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(1).build()).withFieldValueConverter(
                        new IntegerFieldValueConverter()).build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldDescriptorHandlerMock).handleConvertedFieldDescriptor(descriptor);
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }
}
