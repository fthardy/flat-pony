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
import de.fthardy.flatpony.core.structure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushReadItemEntityTreeWalkerTest {

    private StreamReadHandler handlerMock = mock(StreamReadHandler.class);
    private PushReadItemEntityTreeWalker treeWalker = new PushReadItemEntityTreeWalker(handlerMock);
    
    @AfterEach
    void checkMock() {
        verifyNoMoreInteractions(handlerMock);
    }
    
    @Test
    void handleConstantField() {
        ConstantFieldDescriptor descriptor = 
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build();
        ConstantField field = descriptor.createItemEntity();
        
        treeWalker.handleConstantField(field);
        
        verify(handlerMock).onFieldItem(descriptor, field.getValue());
    }

    @Test
    void handleDelimitedField() {
        DelimitedFieldDescriptor descriptor = DelimitedFieldDescriptor.newInstance("Delimited").build();
        DelimitedField field = descriptor.createItemEntity();
        
        treeWalker.handleDelimitedField(field);
        
        verify(handlerMock).onFieldItem(descriptor, field.getValue());
    }

    @Test
    void handleFixedSizeField() {
        FixedSizeFieldDescriptor descriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        FixedSizeField field = descriptor.createItemEntity();
        
        treeWalker.handleFixedSizeField(field);
        
        verify(handlerMock).onFieldItem(descriptor, field.getValue());
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
        
        treeWalker.handleConstrainedField(field);
        
        verify(handlerMock).onFieldItem(fieldDescriptor, field.getValue());
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
        
        treeWalker.handleTypedField(field);
        
        verify(handlerMock).onFieldItem(fieldDescriptor, field.getValue());
    }
    
    @Test
    void handleCompositeItemEntity() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        CompositeItemDescriptor descriptor = 
                CompositeItemDescriptor.newInstance("Composite").addElementItemDescriptor(fieldDescriptor).build();
        CompositeItemEntity composite = descriptor.createItemEntity();
        
        treeWalker.handleCompositeItemEntity(composite);

        InOrder inOrder = inOrder(handlerMock);
        inOrder.verify(handlerMock).onStructureItemStart(descriptor);
        inOrder.verify(handlerMock).onFieldItem(fieldDescriptor, fieldDescriptor.getDefaultValue());
        inOrder.verify(handlerMock).onStructureItemEnd(descriptor);
    }

    @Test
    void handleDelimitedItemEntity() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(fieldDescriptor).build();
        DelimitedItemEntity delimited = descriptor.createItemEntity();

        treeWalker.handleDelimitedItemEntity(delimited);

        InOrder inOrder = inOrder(handlerMock);
        inOrder.verify(handlerMock).onStructureItemStart(descriptor);
        inOrder.verify(handlerMock).onFieldItem(fieldDescriptor, fieldDescriptor.getDefaultValue());
        inOrder.verify(handlerMock).onStructureItemEnd(descriptor);
    }

    @Test
    void handleOptionalItemEntity_Target_absent() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance(fieldDescriptor).build();
        OptionalItemEntity optional = descriptor.createItemEntity();

        treeWalker.handleOptionalItemEntity(optional);

        InOrder inOrder = inOrder(handlerMock);
        inOrder.verify(handlerMock).onStructureItemStart(descriptor);
        inOrder.verify(handlerMock).onStructureItemEnd(descriptor);
    }

    @Test
    void handleOptionalItemEntity_Target_present() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance(fieldDescriptor).build();
        OptionalItemEntity optional = descriptor.createItemEntity();
        optional.newTargetItem();

        treeWalker.handleOptionalItemEntity(optional);

        InOrder inOrder = inOrder(handlerMock);
        inOrder.verify(handlerMock).onStructureItemStart(descriptor);
        inOrder.verify(handlerMock).onFieldItem(fieldDescriptor, fieldDescriptor.getDefaultValue());
        inOrder.verify(handlerMock).onStructureItemEnd(descriptor);
    }

    @Test
    void handleSequenceItemEntity_No_elements() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(fieldDescriptor).build();
        SequenceItemEntity sequence = descriptor.createItemEntity();
        
        treeWalker.handleSequenceItemEntity(sequence);
        
        InOrder inOrder = inOrder(handlerMock);
        inOrder.verify(handlerMock).onStructureItemStart(descriptor);
        inOrder.verify(handlerMock).onStructureItemEnd(descriptor);
    }

    @Test
    void handleSequenceItemEntity_Two_elements() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("Fixed").build();
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(fieldDescriptor).build();
        SequenceItemEntity sequence = descriptor.createItemEntity();
        FlatDataField<?> fieldOne = (FlatDataField<?>) sequence.createAndAddNewElementItemEntity();
        fieldOne.asMutableField().setValue("Field-One");
        FlatDataField<?> fieldTwo = (FlatDataField<?>) sequence.createAndAddNewElementItemEntity();
        fieldTwo.asMutableField().setValue("Field-Two");

        treeWalker.handleSequenceItemEntity(sequence);
        
        InOrder inOrder = inOrder(handlerMock);
        inOrder.verify(handlerMock).onStructureItemStart(descriptor);
        inOrder.verify(handlerMock).onFieldItem(fieldDescriptor, fieldOne.getValue());
        inOrder.verify(handlerMock).onFieldItem(fieldDescriptor, fieldTwo.getValue());
        inOrder.verify(handlerMock).onStructureItemEnd(descriptor);
    }

    @Test
    void handleFlatDataItemEntity() {
        FlatDataItemDescriptor<?> descriptorMock = mock(FlatDataItemDescriptor.class);
        FlatDataItemEntity<FlatDataItemDescriptor<?>> itemEntityMock = mock(FlatDataItemEntity.class);
        
        when(itemEntityMock.getDescriptor()).thenAnswer(
                (Answer<FlatDataItemDescriptor<?>>) invocationOnMock -> descriptorMock);
        
        IllegalStateException ex = assertThrows(
                IllegalStateException.class, () -> treeWalker.handleFlatDataItemEntity(itemEntityMock));
        assertThat(ex.getMessage()).isEqualTo(PushReadItemEntityTreeWalker.MSG_Unsupported_item_entity(itemEntityMock));
    }
}