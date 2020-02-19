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
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.util.FieldReference;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SequenceItemEntityTest {

    @Test
    void Calls_correct_handler_method() {
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        SequenceItemEntity item = descriptor.createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        item.applyHandler(handlerMock);
        item.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(item);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleSequenceItemEntity(item);
        verifyNoMoreInteractions(structureHandlerMock);
    }

    @Test
    void Item_length_is_calculated_correctly() {
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();
        
        StringReader reader = new StringReader("TESTTESTTESTBLA");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemEntityFrom(reader);
        assertThat(sequenceItemEntity.getChildren()).hasSize(3);
        assertThat(sequenceItemEntity.getLength()).isEqualTo(12);
    }

    @Test
    void Write_with_no_multiplicity() {
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        StringReader reader = new StringReader("TESTTESTTESTBLA");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemEntityFrom(reader);

        StringWriter writer = new StringWriter();

        sequenceItemEntity.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("TESTTESTTEST");
    }

    @Test
    void Write_with_multiplicity_and_count_in_bounds() {
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withMultiplicity(3, 5)
                .build();

        StringReader reader = new StringReader("TESTTESTTESTBLA");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemEntityFrom(reader);

        StringWriter writer = new StringWriter();

        sequenceItemEntity.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("TESTTESTTEST");
    }

    @Test
    void Write_with_multiplicity_and_count_out_of_bounds() {
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withMultiplicity(3, 5)
                .build();

        StringReader reader = new StringReader("TESTTESTTESTBLA");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemEntityFrom(reader);
        sequenceItemEntity.discardAllElementItems();

        StringWriter writer = new StringWriter();

        assertThrows(FlatDataWriteException.class, () -> sequenceItemEntity.writeTo(writer));
    }

    @Test
    void Discard_element() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();

        FixedSizeFieldDescriptor countFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Count").build();
        FieldValueConverter<Integer> converter = new FieldValueConverter<Integer>() {
            @Override
            public Integer convertFromFieldValue(String fieldValue) {
                return Integer.valueOf(fieldValue);
            }

            @Override
            public String convertToFieldValue(Integer value) {
                return value.toString();
            }
        };

        FieldReference<Integer> fieldReference = 
                FieldReference.<Integer>newInstance(countFieldDescriptor).usingValueConverter(converter).build();

        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .withCountFieldReference(fieldReference)
                .withMultiplicity(3, 5)
                .build();

        StringReader reader = new StringReader("3TESTTESTTESTBAL");

        FlatDataMutableField<?> countField = fieldReference.getFieldDescriptorDecorator().readItemEntityFrom(reader);
        assertThat(countField.getValue()).isEqualTo("3");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemEntityFrom(reader);

        FlatDataItemEntity<?> element = sequenceItemEntity.getChildren().get(0);
        sequenceItemEntity.discardElement(element);
        assertThat(sequenceItemEntity.getChildren()).hasSize(2);
        assertThat(countField.getValue()).isEqualTo("2");

        sequenceItemEntity.discardAllElementItems();
        assertThat(sequenceItemEntity.getChildren()).hasSize(0);
        assertThat(countField.getValue()).isEqualTo("0");

        assertThrows(IllegalArgumentException.class, () -> sequenceItemEntity.discardElement(element));
    }

    @Test
    void Discard_and_add_element() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();

        FixedSizeFieldDescriptor countFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Count").build();
        FieldValueConverter<Integer> converter = new FieldValueConverter<Integer>() {
            @Override
            public Integer convertFromFieldValue(String fieldValue) {
                return Integer.valueOf(fieldValue);
            }

            @Override
            public String convertToFieldValue(Integer value) {
                return value.toString();
            }
        };

        FieldReference<Integer> fieldReference =
                FieldReference.<Integer>newInstance(countFieldDescriptor).usingValueConverter(converter).build();

        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .withCountFieldReference(fieldReference)
                .withMultiplicity(3, 5)
                .build();

        StringReader reader = new StringReader("3TESTTESTTESTBAL");

        FlatDataMutableField<?> countField = fieldReference.getFieldDescriptorDecorator().readItemEntityFrom(reader);
        assertThat(countField.getValue()).isEqualTo("3");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemEntityFrom(reader);

        FlatDataItemEntity<?> element = sequenceItemEntity.getChildren().get(0);
        sequenceItemEntity.discardElement(element);
        assertThat(sequenceItemEntity.getChildren()).hasSize(2);
        assertThat(countField.getValue()).isEqualTo("2");

        sequenceItemEntity.addNewElementItem();
        assertThat(sequenceItemEntity.getChildren()).hasSize(3);
        assertThat(countField.getValue()).isEqualTo("3");

        assertThrows(IllegalArgumentException.class, () ->
                sequenceItemEntity.addElementItem(ConstantFieldDescriptor.newInstance("Bar")
                        .withConstant("FOO").build()
                        .createItemEntity()));
    }
}