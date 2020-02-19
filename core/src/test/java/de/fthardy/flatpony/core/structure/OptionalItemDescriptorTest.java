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
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.util.FieldReference;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionalItemDescriptorTest {

    @Test
    void Cannot_set_target_item_descriptor_to_null() {
        assertThrows(NullPointerException.class, () -> 
                OptionalItemDescriptor.newInstance("Optional").withTargetItemDescriptor(null));
    }

    @Test
    void Cannot_set_FlagFieldReference_to_null() {
        assertThrows(NullPointerException.class, () -> OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withFlagFieldReference(null));
    }

    @Test
    void Calls_correct_handler_method() {
        ConstantFieldDescriptor itemDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        OptionalItemDescriptor descriptor =
                OptionalItemDescriptor.newInstance("Optional").withTargetItemDescriptor(itemDescriptor).build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleOptionalItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }

    @Test
    void Read_with_trial_and_error_No_success() {
        ConstantFieldDescriptor itemDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        OptionalItemDescriptor descriptor = 
                OptionalItemDescriptor.newInstance("Optional").withTargetItemDescriptor(itemDescriptor).build();

        List<FlatDataItemDescriptor<?>> childDescriptors = descriptor.getChildren();
        assertEquals(1, childDescriptors.size());
        assertSame(itemDescriptor, childDescriptors.get(0));
        assertSame(itemDescriptor, descriptor.getTargetItemDescriptor());

        StringReader reader = new StringReader("BAZ");

        OptionalItemEntity optionalItemEntity = descriptor.readItemEntityFrom(reader);
        assertFalse(optionalItemEntity.isTargetItemPresent());
    }

    @Test
    void Read_with_trial_and_error_Success() {
        ConstantFieldDescriptor itemDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        OptionalItemDescriptor descriptor =
                OptionalItemDescriptor.newInstance("Optional").withTargetItemDescriptor(itemDescriptor).build();

        StringReader reader = new StringReader("TESTOMETER");

        OptionalItemEntity optionalItemEntity = descriptor.readItemEntityFrom(reader);
        assertTrue(optionalItemEntity.isTargetItemPresent());
    }

    @Test
    void Read_with_reference_flag_field_No_success() {
        FixedSizeFieldDescriptor flagFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Flag Field").build();

        FieldValueConverter<Boolean> flagConverter = new FieldValueConverter<Boolean>() {
            @Override
            public Boolean convertFromFieldValue(String fieldValue) {
                return fieldValue.equals("X");
            }

            @Override
            public String convertToFieldValue(Boolean value) {
                return value ? "X" : " ";
            }
        };

        FieldReference<Boolean> flagFieldReference = 
                FieldReference.<Boolean>newInstance(flagFieldDescriptor).usingValueConverter(flagConverter).build();

        ConstantFieldDescriptor itemDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(itemDescriptor)
                .withFlagFieldReference(flagFieldReference)
                .build();

        StringReader reader = new StringReader(" TEST");

        FlatDataMutableField<?> flagField = flagFieldReference.getFieldDescriptorDecorator().readItemEntityFrom(reader);
        assertEquals("", flagField.getValue());
        
        OptionalItemEntity optionalItemEntity = descriptor.readItemEntityFrom(reader);
        assertFalse(optionalItemEntity.isTargetItemPresent());
        assertFalse(optionalItemEntity.getTargetItem().isPresent());
        assertEquals(0, optionalItemEntity.getLength());
        assertEquals(0, optionalItemEntity.getChildren().size());

        // Test if flag field is updated
        optionalItemEntity.newTargetItem();
        assertEquals("X", flagField.getValue());
    }

    @Test
    void Read_with_reference_flag_field_Success() {
        FixedSizeFieldDescriptor flagFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Flag Field").build();

        FieldValueConverter<Boolean> flagConverter = new FieldValueConverter<Boolean>() {
            @Override
            public Boolean convertFromFieldValue(String fieldValue) {
                return fieldValue.equals("J");
            }

            @Override
            public String convertToFieldValue(Boolean value) {
                return value ? "J" : "N";
            }
        };

        FieldReference<Boolean> flagFieldReference =
                FieldReference.<Boolean>newInstance(flagFieldDescriptor).usingValueConverter(flagConverter).build();

        ConstantFieldDescriptor itemDescriptor = ConstantFieldDescriptor.newInstance("Constant")
                .withConstant("TEST").build();
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(itemDescriptor)
                .withFlagFieldReference(flagFieldReference)
                .build();

        StringReader reader = new StringReader("JTEST");

        FlatDataMutableField<?> flagField = flagFieldReference.getFieldDescriptorDecorator().readItemEntityFrom(reader);
        assertEquals("J", flagField.getValue());
        
        OptionalItemEntity optionalItemEntity = descriptor.readItemEntityFrom(reader);
        assertTrue(optionalItemEntity.isTargetItemPresent());
        assertTrue(optionalItemEntity.getTargetItem().isPresent());
        assertEquals(4, optionalItemEntity.getLength());
        assertEquals(1, optionalItemEntity.getChildren().size());
    }
}