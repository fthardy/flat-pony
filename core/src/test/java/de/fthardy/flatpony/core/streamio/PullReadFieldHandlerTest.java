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
package de.fthardy.flatpony.core.streamio;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.*;
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.converter.FieldValueConvertException;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeField;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PullReadFieldHandlerTest {

    private StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
    private PullReadFieldHandler handler = new PullReadFieldHandler(streamReadHandlerMock);

    @Test
    void handleConstantField() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build();
        ConstantField field = descriptor.createItemEntity();

        handler.handleConstantField(field);

        verify(streamReadHandlerMock).onFieldItem(descriptor, field.getValue());
    }

    @Test
    void handleDelimitedField() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Delimited").build();
        DelimitedField field = descriptor.createItemEntity();

        handler.handleDelimitedField(field);

        verify(streamReadHandlerMock).onFieldItem(descriptor, field.getValue());
    }

    @Test
    void handleFixedSizeField() {
        FixedSizeFieldDescriptor descriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        FixedSizeField field = descriptor.createItemEntity();

        handler.handleFixedSizeField(field);

        verify(streamReadHandlerMock).onFieldItem(descriptor, field.getValue());
    }

    @Test
    void handleConstrainedField() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptor).addConstraint(new ValueConstraint() {
                    @Override
                    public String getName() {
                        return "Test";
                    }

                    @Override
                    public boolean acceptValue(String value) {
                        return true;
                    }
                }).build();
        ConstrainedField field = descriptor.createItemEntity();

        handler.handleConstrainedField(field);

        verify(streamReadHandlerMock).onFieldItem(fieldDescriptor, field.getValue());
    }

    @Test
    void handleTypedField() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor.newInstance(fieldDescriptor)
                .withFieldValueConverter(new FieldValueConverter<Object>() {

                    @Override
                    public Class<Object> getTargetType() {
                        return Object.class;
                    }

                    @Override
                    public Object convertFromFieldValue(String fieldValue) throws FieldValueConvertException {
                        return null;
                    }

                    @Override
                    public String convertToFieldValue(Object value) {
                        return null;
                    }
                }).build();
        TypedField<Object> field = descriptor.createItemEntity();

        handler.handleTypedField(field);

        verify(streamReadHandlerMock).onFieldItem(fieldDescriptor, field.getValue());
    }

    @Test
    void handleFlatDataItemEntity() {
        FlatDataItemDescriptor<?> descriptorMock = mock(FlatDataItemDescriptor.class);
        FlatDataItemEntity<FlatDataItemDescriptor<?>> itemEntityMock = mock(FlatDataItemEntity.class);

        when(itemEntityMock.getDescriptor()).thenAnswer(
                (Answer<FlatDataItemDescriptor<?>>) invocationOnMock -> descriptorMock);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class, () -> handler.handleFlatDataItemEntity(itemEntityMock));
        assertThat(ex.getMessage()).isEqualTo(PullReadFieldHandler.MSG_Unsupported_item_entity(itemEntityMock));
    }
}