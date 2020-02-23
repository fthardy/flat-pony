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
import de.fthardy.flatpony.core.field.converter.IntegerFieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConvertedFieldTest {
    
    @Test
    void Read_use_and_write() {
        FixedSizeFieldDescriptor fieldDescriptor =
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(1).build();
        ConvertedFieldDescriptor<Integer> convertedFieldDescriptor =
                ConvertedFieldDescriptor.<Integer>newInstance(fieldDescriptor).withFieldValueConverter(
                        new IntegerFieldValueConverter()).build();
        
        StringReader reader = new StringReader("9");
        
        ConvertedField<Integer> convertedField = convertedFieldDescriptor.readItemEntityFrom(reader);
        assertThat(convertedField.asMutableField()).isSameAs(convertedField);
        FlatDataField<?> decoratedField = convertedField.getDecoratedField();

        assertThat(convertedField.getLength()).isEqualTo(decoratedField.getLength());
        assertThat(convertedField.getValue()).isEqualTo(decoratedField.getValue());

        assertThat(convertedField.getValue()).isEqualTo("9");
        assertThat(convertedField.getConvertedValue()).isEqualTo(9);
        
        convertedField.setConvertedValue(1);
        assertThat(decoratedField.getValue()).isEqualTo("1");
        
        convertedField.setValue("6");
        assertThat(convertedField.getConvertedValue()).isEqualTo(6);
        
        StringWriter writer = new StringWriter();
        convertedField.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("6");
    }

    @Test
    void Calls_correct_handler_method() {
        ConvertedFieldDescriptor<Integer> descriptor = ConvertedFieldDescriptor.<Integer>newInstance(
                FixedSizeFieldDescriptor.newInstance("Field").withFieldSize(1).build()).withFieldValueConverter(
                new IntegerFieldValueConverter()).build();

        ConvertedField<Integer> field = descriptor.createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verifyNoMoreInteractions(handlerMock);
        verify(fieldHandlerMock).handleConvertedField(field);
        verifyNoMoreInteractions(fieldHandlerMock);
    }
}
