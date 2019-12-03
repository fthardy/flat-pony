package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.util.FieldReferenceConfig;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SequenceItemDescriptorTest {

    @Test
    void Cannot_create_with_null_element_item_descriptor() {
        assertThrows(NullPointerException.class, () -> new SequenceItemDescriptor("Test", null));
    }

    @Test
    void Create_item_with_no_field_reference_and_no_multiplicity() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("Foo", "BAR");
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor("Test", constantFieldDescriptor);

        assertThat(descriptor.getMultiplicity()).isNull();
        assertThat(descriptor.getMinLength()).isEqualTo(0);
        assertThat(descriptor.getChildDescriptors()).containsExactly(constantFieldDescriptor);
        assertThat(descriptor.getElementItemDescriptor()).isSameAs(constantFieldDescriptor);

        SequenceItemEntity sequenceItemEntity = descriptor.createItem();
        assertThat(sequenceItemEntity.getChildItems()).isEmpty();
    }

    @Test
    void Create_item_with_field_reference_and_no_multiplicity() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("Foo", "BAR");

        FixedSizeFieldDescriptor countFieldDescriptor = new FixedSizeFieldDescriptor("Count", 1);
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

        FieldReferenceConfig<Integer> fieldReferenceConfig =
                new FieldReferenceConfig<>(countFieldDescriptor, converter);

        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test", constantFieldDescriptor, fieldReferenceConfig, null);

        assertThat(descriptor.getMultiplicity()).isNull();
        assertThat(descriptor.getMinLength()).isEqualTo(0);
        assertThat(descriptor.getChildDescriptors()).containsExactly(constantFieldDescriptor);
        assertThat(descriptor.getElementItemDescriptor()).isSameAs(constantFieldDescriptor);

        FlatDataMutableField<?> countField = fieldReferenceConfig.getObservableFieldDescriptor().createItem();
        countField.setValue("3");

        SequenceItemEntity sequenceItemEntity = descriptor.createItem();
        assertThat(sequenceItemEntity.getChildItems()).hasSize(3);
    }

    @Test
    void Create_item_with_field_reference_and_no_multiplicity_but_count_field_is_missing() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("Foo", "BAR");

        FixedSizeFieldDescriptor countFieldDescriptor = new FixedSizeFieldDescriptor("Count", 1);
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

        FieldReferenceConfig<Integer> fieldReferenceConfig =
                new FieldReferenceConfig<>(countFieldDescriptor, converter);

        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test", constantFieldDescriptor, fieldReferenceConfig, null);

        assertThat(descriptor.getMultiplicity()).isNull();
        assertThat(descriptor.getMinLength()).isEqualTo(0);
        assertThat(descriptor.getChildDescriptors()).containsExactly(constantFieldDescriptor);
        assertThat(descriptor.getElementItemDescriptor()).isSameAs(constantFieldDescriptor);

        assertThrows(IllegalStateException.class, descriptor::createItem);
    }

    @Test
    void Read_item_with_no_field_reference_and_no_multiplicity() {
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        StringReader reader = new StringReader("BARBARBARBAL");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);
        assertThat(sequenceItemEntity.getChildItems()).hasSize(3);
    }

    @Test
    void Read_item_with_no_field_reference_and_with_multiplicity() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("Foo", "BAR");
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test",
                constantFieldDescriptor,
                null,
                new SequenceItemDescriptor.Multiplicity(4, 2));

        assertThat(descriptor.getMultiplicity()).isNotNull();
        assertThat(descriptor.getMinLength()).isEqualTo(6);

        StringReader reader = new StringReader("BARBARBARBAL");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);
        assertThat(sequenceItemEntity.getChildItems()).hasSize(3);
    }

    @Test
    void Read_item_with_field_reference_and_no_multiplicity() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("Foo", "BAR");

        FixedSizeFieldDescriptor countFieldDescriptor = new FixedSizeFieldDescriptor("Count", 1);
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

        FieldReferenceConfig<Integer> fieldReferenceConfig =
                new FieldReferenceConfig<>(countFieldDescriptor, converter);

        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test", constantFieldDescriptor, fieldReferenceConfig, null);

        assertThat(descriptor.getMultiplicity()).isNull();
        assertThat(descriptor.getMinLength()).isEqualTo(0);
        assertThat(descriptor.getChildDescriptors()).containsExactly(constantFieldDescriptor);
        assertThat(descriptor.getElementItemDescriptor()).isSameAs(constantFieldDescriptor);

        StringReader reader = new StringReader("3BARBARBARBAL");

        fieldReferenceConfig.getObservableFieldDescriptor().readItemFrom(reader);

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);
        assertThat(sequenceItemEntity.getChildItems()).hasSize(3);
    }

    @Test
    void Read_item_with_no_field_reference_and_with_multiplicity_but_out_of_bounds() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("Foo", "BAR");
        SequenceItemDescriptor.Multiplicity multiplicity = new SequenceItemDescriptor.Multiplicity(1, 2);
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test",
                constantFieldDescriptor,
                null,
                multiplicity);

        assertThat(descriptor.getMultiplicity()).isNotNull();

        StringReader reader = new StringReader("BARBARBARBAL");

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemFrom(reader));
        assertThat(exception.getMessage()).isEqualTo(SequenceItemDescriptor.MSG_Multiplicity_constraint_violated(
                "Test", multiplicity));
    }

    @Test
    void Calls_correct_handler_method() {
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Record", new ConstantFieldDescriptor("ID", "Foo"));

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleSequenceItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }
}