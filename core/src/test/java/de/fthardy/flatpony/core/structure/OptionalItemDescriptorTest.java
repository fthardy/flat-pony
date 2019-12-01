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
import de.fthardy.flatpony.core.util.FieldReferenceConfig;
import de.fthardy.flatpony.core.util.ObservableFieldDescriptorDecorator;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionalItemDescriptorTest {

    @Test
    void Cannot_create_without_target_item_descriptor() {
        assertThrows(NullPointerException.class, () -> new OptionalItemDescriptor("Test", null));
    }

    @Test
    void Read_with_trial_and_error_No_success() {
        ConstantFieldDescriptor itemDescriptor = new ConstantFieldDescriptor("Foo", "BAR");
        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", itemDescriptor);

        List<FlatDataItemDescriptor<?>> childDescriptors = descriptor.getChildDescriptors();
        assertEquals(1, childDescriptors.size());
        assertSame(itemDescriptor, childDescriptors.get(0));
        assertSame(itemDescriptor, descriptor.getTargetItemDescriptor());

        StringReader reader = new StringReader("BAZ");

        OptionalItemEntity optionalItemEntity = descriptor.readItemFrom(reader);
        assertFalse(optionalItemEntity.isTargetItemPresent());
    }

    @Test
    void Read_with_trial_and_error_Success() {
        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        StringReader reader = new StringReader("BAROMETER");

        OptionalItemEntity optionalItemEntity = descriptor.readItemFrom(reader);
        assertTrue(optionalItemEntity.isTargetItemPresent());
    }

    @Test
    void Read_with_reference_flag_field_No_success() {
        FixedSizeFieldDescriptor flagFieldDescriptor = new FixedSizeFieldDescriptor("Test", 1);

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

        FieldReferenceConfig<Boolean> fieldReferenceConfig =
                new FieldReferenceConfig<>(flagFieldDescriptor, flagConverter);

        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"), fieldReferenceConfig);

        StringReader reader = new StringReader(" BAR");

        FlatDataMutableField<?> flagField = fieldReferenceConfig.getObservableFieldDescriptor().readItemFrom(reader);
        assertEquals("", flagField.getValue());
        OptionalItemEntity optionalItemEntity = descriptor.readItemFrom(reader);
        assertFalse(optionalItemEntity.isTargetItemPresent());
        assertFalse(optionalItemEntity.getTargetItem().isPresent());
        assertEquals(0, optionalItemEntity.getLength());
        assertEquals(0, optionalItemEntity.getChildItems().size());

        // Test if flag field is updated
        optionalItemEntity.newTargetItem();
        assertEquals("X", flagField.getValue());
    }

    @Test
    void Read_with_reference_flag_field_Success() {
        FixedSizeFieldDescriptor flagFieldDescriptor = new FixedSizeFieldDescriptor("Test", 1);

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

        FieldReferenceConfig<Boolean> fieldReferenceConfig =
                new FieldReferenceConfig<>(flagFieldDescriptor, flagConverter);

        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"), fieldReferenceConfig);

        StringReader reader = new StringReader("XBAR");

        fieldReferenceConfig.getObservableFieldDescriptor().readItemFrom(reader);
        OptionalItemEntity optionalItemEntity = descriptor.readItemFrom(reader);
        assertTrue(optionalItemEntity.isTargetItemPresent());
        assertTrue(optionalItemEntity.getTargetItem().isPresent());
        assertEquals(3, optionalItemEntity.getLength());
        assertEquals(1, optionalItemEntity.getChildItems().size());
    }

    @Test
    void Calls_correct_handler_method() {
        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Record", new ConstantFieldDescriptor("ID", "Foo"));

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleOptionalItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }
}