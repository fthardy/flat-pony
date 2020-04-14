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
import de.fthardy.flatpony.core.field.constant.ConstantField;
import de.fthardy.flatpony.core.field.constant.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.constrained.ConstrainedField;
import de.fthardy.flatpony.core.field.constrained.ConstrainedFieldDescriptor;
import de.fthardy.flatpony.core.field.constrained.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.typed.converter.FieldValueConvertException;
import de.fthardy.flatpony.core.field.typed.converter.FieldValueConverter;
import de.fthardy.flatpony.core.field.delimited.DelimitedField;
import de.fthardy.flatpony.core.field.delimited.DelimitedFieldDescriptor;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeField;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.field.typed.TypedField;
import de.fthardy.flatpony.core.field.typed.TypedFieldDescriptor;
import de.fthardy.flatpony.core.structure.composite.CompositeItemDescriptor;
import de.fthardy.flatpony.core.structure.composite.CompositeItemEntity;
import de.fthardy.flatpony.core.structure.delimited.DelimitedItemDescriptor;
import de.fthardy.flatpony.core.structure.delimited.DelimitedItemEntity;
import de.fthardy.flatpony.core.structure.optional.OptionalItemDescriptor;
import de.fthardy.flatpony.core.structure.optional.OptionalItemEntity;
import de.fthardy.flatpony.core.structure.sequence.SequenceItemDescriptor;
import de.fthardy.flatpony.core.structure.sequence.SequenceItemEntity;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ItemEntityStructureFlattenerTest {

    private final ItemEntityStructureFlattener flattener = new ItemEntityStructureFlattener();

    @Test
    void handleConstantField() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build();
        ConstantField field = descriptor.createItemEntity();

        flattener.handleConstantField(field);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(field);
    }

    @Test
    void handleDelimitedField() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Delimited").build();
        DelimitedField field = descriptor.createItemEntity();

        flattener.handleDelimitedField(field);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(field);
    }

    @Test
    void handleFixedSizeField() {
        FixedSizeFieldDescriptor descriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        FixedSizeField field = descriptor.createItemEntity();

        flattener.handleFixedSizeField(field);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(field);
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

        flattener.handleConstrainedField(field);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(field.getDecoratedField());
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

        flattener.handleTypedField(field);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(field.getDecoratedField());
    }

    @Test
    void handleCompositeItemEntity() {
        FixedSizeFieldDescriptor fixedSizeFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        DelimitedFieldDescriptor delimitedFieldDescriptor = 
                DelimitedFieldDescriptor.newInstance("Delimited").build();

        CompositeItemDescriptor compositeDescriptor = CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptors(fixedSizeFieldDescriptor, delimitedFieldDescriptor).build();
        CompositeItemEntity compositeEntity = compositeDescriptor.createItemEntity();
        
        flattener.handleCompositeItemEntity(compositeEntity);
        
        assertThat(flattener.getFlattenedItemEntities()).containsExactly(
                compositeEntity,
                compositeEntity.getComponentItemEntityByName("Fixed"),
                compositeEntity.getComponentItemEntityByName("Delimited"),
                compositeEntity);
    }

    @Test
    void handleDelimitedItemEntity() {
        FixedSizeFieldDescriptor fixedSizeFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();

        DelimitedItemDescriptor delimitedDescriptor =
                DelimitedItemDescriptor.newInstance(fixedSizeFieldDescriptor).build();
        DelimitedItemEntity delimitedEntity = delimitedDescriptor.createItemEntity();

        flattener.handleDelimitedItemEntity(delimitedEntity);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(
                delimitedEntity, delimitedEntity.getTargetItem(), delimitedEntity);
    }

    @Test
    void handleOptionalItemEntity_Target_absent() {
        FixedSizeFieldDescriptor fixedSizeFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();

        OptionalItemDescriptor optionalDescriptor =
                OptionalItemDescriptor.newInstance(fixedSizeFieldDescriptor).build();
        OptionalItemEntity optionalEntity = optionalDescriptor.createItemEntity();

        flattener.handleOptionalItemEntity(optionalEntity);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(optionalEntity, optionalEntity);
    }

    @Test
    void handleOptionalItemEntity_Target_present() {
        FixedSizeFieldDescriptor fixedSizeFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();

        OptionalItemDescriptor  optionalDescriptor =
                OptionalItemDescriptor.newInstance(fixedSizeFieldDescriptor).build();
        OptionalItemEntity optionalEntity = optionalDescriptor.createItemEntity();
        FlatDataItemEntity<?> targetItem = optionalEntity.newTargetItem();

        flattener.handleOptionalItemEntity(optionalEntity);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(optionalEntity, targetItem, optionalEntity);
    }

    @Test
    void handleSequenceItemEntity_No_elements() {
        FixedSizeFieldDescriptor fixedSizeFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();

        SequenceItemDescriptor sequenceDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(fixedSizeFieldDescriptor).build();
        SequenceItemEntity sequenceEntity = sequenceDescriptor.createItemEntity();

        flattener.handleSequenceItemEntity(sequenceEntity);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(sequenceEntity, sequenceEntity);
    }

    @Test
    void handleSequenceItemEntity_Two_elements() {
        FixedSizeFieldDescriptor fixedSizeFieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();

        SequenceItemDescriptor sequenceDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(fixedSizeFieldDescriptor).build();
        SequenceItemEntity sequenceEntity = sequenceDescriptor.createItemEntity();
        FlatDataItemEntity<?> elementEntityOne = sequenceEntity.createAndAddNewElementItemEntity();
        FlatDataItemEntity<?> elementEntityTwo = sequenceEntity.createAndAddNewElementItemEntity();

        flattener.handleSequenceItemEntity(sequenceEntity);

        assertThat(flattener.getFlattenedItemEntities()).containsExactly(
                sequenceEntity, elementEntityOne, elementEntityTwo, sequenceEntity);
    }

    @Test
    void handleFlatDataItemEntity() {
        FlatDataItemDescriptor<?> descriptorMock = mock(FlatDataItemDescriptor.class);
        FlatDataItemEntity<FlatDataItemDescriptor<?>> itemEntityMock = mock(FlatDataItemEntity.class);

        when(itemEntityMock.getDescriptor()).thenAnswer(
                (Answer<FlatDataItemDescriptor<?>>) invocationOnMock -> descriptorMock);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class, () -> flattener.handleFlatDataItemEntity(itemEntityMock));
        assertThat(ex.getMessage()).isEqualTo(ItemEntityStructureFlattener.MSG_Unsupported_item_entity(itemEntityMock));
    }
}