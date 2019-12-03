package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.util.FieldReferenceConfig;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SequenceItemEntityTest {

    @Test
    void Item_length_is_calculated_correctly() {
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        StringReader reader = new StringReader("BARBARBARBAL");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);
        assertThat(sequenceItemEntity.getChildItems()).hasSize(3);
        assertThat(sequenceItemEntity.getLength()).isEqualTo(9);
    }

    @Test
    void Write_with_no_multiplicity() {
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        StringReader reader = new StringReader("BARBARBARBAL");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);

        StringWriter writer = new StringWriter();

        sequenceItemEntity.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("BARBARBAR");
    }

    @Test
    void Write_with_multiplicity_and_count_in_bounds() {
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test",
                new ConstantFieldDescriptor("Foo", "BAR"),
                null,
                new SequenceItemDescriptor.Multiplicity(3, 5));

        StringReader reader = new StringReader("BARBARBARBAL");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);

        StringWriter writer = new StringWriter();

        sequenceItemEntity.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("BARBARBAR");
    }

    @Test
    void Write_with_multiplicity_and_count_out_of_bounds() {
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test",
                new ConstantFieldDescriptor("Foo", "BAR"),
                null,
                new SequenceItemDescriptor.Multiplicity(3, 5));

        StringReader reader = new StringReader("BARBARBARBAL");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);
        sequenceItemEntity.discardAllElementItems();

        StringWriter writer = new StringWriter();

        assertThrows(FlatDataWriteException.class, () -> sequenceItemEntity.writeTo(writer));
    }

    @Test
    void Discard_element() {
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
                "Test",
                constantFieldDescriptor,
                fieldReferenceConfig,
                new SequenceItemDescriptor.Multiplicity(3, 5));

        StringReader reader = new StringReader("3BARBARBARBAL");

        FlatDataMutableField<?> countField = fieldReferenceConfig.getObservableFieldDescriptor().readItemFrom(reader);
        assertThat(countField.getValue()).isEqualTo("3");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);

        FlatDataItemEntity<?> element = sequenceItemEntity.getChildItems().get(0);
        sequenceItemEntity.discardElement(element);
        assertThat(sequenceItemEntity.getChildItems()).hasSize(2);
        assertThat(countField.getValue()).isEqualTo("2");

        sequenceItemEntity.discardAllElementItems();
        assertThat(sequenceItemEntity.getChildItems()).hasSize(0);
        assertThat(countField.getValue()).isEqualTo("0");

        assertThrows(IllegalArgumentException.class, () -> sequenceItemEntity.discardElement(element));
    }

    @Test
    void Discard_and_add_element() {
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
                "Test",
                constantFieldDescriptor,
                fieldReferenceConfig,
                new SequenceItemDescriptor.Multiplicity(3, 5));

        StringReader reader = new StringReader("3BARBARBARBAL");

        FlatDataMutableField<?> countField = fieldReferenceConfig.getObservableFieldDescriptor().readItemFrom(reader);
        assertThat(countField.getValue()).isEqualTo("3");

        SequenceItemEntity sequenceItemEntity = descriptor.readItemFrom(reader);

        FlatDataItemEntity<?> element = sequenceItemEntity.getChildItems().get(0);
        sequenceItemEntity.discardElement(element);
        assertThat(sequenceItemEntity.getChildItems()).hasSize(2);
        assertThat(countField.getValue()).isEqualTo("2");

        sequenceItemEntity.addNewElementItem();
        assertThat(sequenceItemEntity.getChildItems()).hasSize(3);
        assertThat(countField.getValue()).isEqualTo("3");

        assertThrows(IllegalArgumentException.class, () ->
                sequenceItemEntity.addElementItem(new ConstantFieldDescriptor("Bar", "FOO").createItem()));
    }

    @Test
    void Calls_correct_handler_method() {
        SequenceItemDescriptor descriptor = new SequenceItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        SequenceItemEntity item = descriptor.createItem();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        item.applyHandler(handlerMock);
        item.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItem(item);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleSequenceItem(item);
        verifyNoMoreInteractions(structureHandlerMock);
    }
}