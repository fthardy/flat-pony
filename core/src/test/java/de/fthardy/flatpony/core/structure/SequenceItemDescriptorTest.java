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

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import de.fthardy.flatpony.core.field.converter.IntegerFieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.util.FieldReference;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SequenceItemDescriptorTest {

    @Test
    void Cannot_set_element_item_descriptor_to_null() {
        assertThrows(NullPointerException.class, () -> 
                SequenceItemDescriptor.newInstance("Sequence").withElementItemDescriptor(null));
    }

    @Test
    void Cannot_set_count_field_reference_to_null() {
        assertThrows(NullPointerException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withCountFieldReference(null));
    }

    @Test
    void Cannot_set_multiplicity_of_0_to_0() {
        assertThrows(IllegalArgumentException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withMultiplicity(0, 0));
    }

    @Test
    void Cannot_set_a_negative_bound1_in_multiplicity() {
        assertThrows(IllegalArgumentException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withMultiplicity(-1, 1));
    }

    @Test
    void Cannot_set_a_negative_bound2_in_multiplicity() {
        assertThrows(IllegalArgumentException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withMultiplicity(1, -1));
    }

    @Test
    void Calls_correct_handler_method() {
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleSequenceItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }

    @Test
    void Create_with_no_field_reference_and_no_multiplicity() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .build();

        assertThat(descriptor.getMultiplicity().getMinOccurrences()).isEqualTo(0);
        assertThat(descriptor.getMultiplicity().getMaxOccurrences()).isEqualTo(Integer.MAX_VALUE);
        assertThat(descriptor.getMinLength()).isEqualTo(0);
        assertThat(descriptor.getElementItemDescriptor()).isSameAs(constantFieldDescriptor);

        SequenceItemEntity sequenceItemEntity = descriptor.createItemEntity();
        assertThat(sequenceItemEntity.getElements()).isEmpty();
    }

    @Test
    void Create_with_field_reference_and_no_multiplicity() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();

        FixedSizeFieldDescriptor countFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Count")
                .withFieldSize(1).build();
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
                .build();

        assertThat(descriptor.getMultiplicity().getMinOccurrences()).isEqualTo(0);
        assertThat(descriptor.getMultiplicity().getMaxOccurrences()).isEqualTo(Integer.MAX_VALUE);
        assertThat(descriptor.getMinLength()).isEqualTo(0);
        assertThat(descriptor.getElementItemDescriptor()).isSameAs(constantFieldDescriptor);

        FlatDataMutableField<?> countField = fieldReference.getFieldDescriptorDecorator().createItemEntity();
        countField.setValue("3");

        SequenceItemEntity sequenceItemEntity = descriptor.createItemEntity();
        assertThat(sequenceItemEntity.getElements()).hasSize(3);
    }
    
    @Test
    void Create_item() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .build();

        SequenceItemEntity sequenceItemEntity = descriptor.createItemEntity();
        assertThat(sequenceItemEntity.getElements()).isEmpty();
    }

    @Test
    void Read_by_trial_and_error() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .build();

        StringReader reader1 = new StringReader("TESTTESTTESTbla");
        StringReader reader2 = new StringReader("TESTTEST");

        SequenceItemEntity sequenceItemEntity1 = descriptor.readItemEntityFrom(reader1);
        assertThat(sequenceItemEntity1.getElements()).hasSize(3);
        SequenceItemEntity sequenceItemEntity2 = descriptor.readItemEntityFrom(reader2);
        assertThat(sequenceItemEntity2.getElements()).hasSize(2);
    }

    @Test
    void Read_only_with_multiplicity() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .withMultiplicity(4, 2)
                .build();

        assertThat(descriptor.getMultiplicity()).isNotNull();
        assertThat(descriptor.getMinLength()).isEqualTo(8);

        StringReader reader1 = new StringReader("TESTTESTTESTbla");
        StringReader reader2 = new StringReader("TESTTEST");

        SequenceItemEntity sequenceItemEntity1 = descriptor.readItemEntityFrom(reader1);
        assertThat(sequenceItemEntity1.getElements()).hasSize(3);
        SequenceItemEntity sequenceItemEntity2 = descriptor.readItemEntityFrom(reader2);
        assertThat(sequenceItemEntity2.getElements()).hasSize(2);
    }

    @Test
    void Read_only_with_multiplicity_but_count_is_out_of_bounds() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .withMultiplicity(1, 2)
                .build();

        StringReader reader = new StringReader("TESTTESTTESTbla");

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemEntityFrom(reader));
        assertThat(exception.getMessage()).isEqualTo(SequenceItemDescriptor.MSG_Multiplicity_constraint_violated(
                descriptor.getName(), descriptor.getMultiplicity()));
    }

    @Test
    void Read_only_with_count_field_reference() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();

        FixedSizeFieldDescriptor countFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Count")
                .withFieldSize(1).build();
        FieldValueConverter<Integer> converter = new IntegerFieldValueConverter();

        FieldReference<Integer> fieldReference = 
                FieldReference.<Integer>newInstance(countFieldDescriptor).usingValueConverter(converter).build();

        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(constantFieldDescriptor)
                .withCountFieldReference(fieldReference)
                .build();

        assertThat(descriptor.getMultiplicity().getMinOccurrences()).isEqualTo(0);
        assertThat(descriptor.getMultiplicity().getMaxOccurrences()).isEqualTo(Integer.MAX_VALUE);
        assertThat(descriptor.getMinLength()).isEqualTo(0);
        assertThat(descriptor.getElementItemDescriptor()).isSameAs(constantFieldDescriptor);

        StringReader reader1 = new StringReader("3TESTTESTTESTbla");
        StringReader reader2 = new StringReader("2TESTTEST");

        fieldReference.getFieldDescriptorDecorator().readItemEntityFrom(reader1);
        SequenceItemEntity sequenceItemEntity1 = descriptor.readItemEntityFrom(reader1);
        assertThat(sequenceItemEntity1.getElements()).hasSize(3);

        fieldReference.getFieldDescriptorDecorator().readItemEntityFrom(reader2);
        SequenceItemEntity sequenceItemEntity2 = descriptor.readItemEntityFrom(reader2);
        assertThat(sequenceItemEntity2.getElements()).hasSize(2);
    }
}