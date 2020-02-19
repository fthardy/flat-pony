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
import de.fthardy.flatpony.core.field.ConstantField;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionalItemEntityTest {

    @Test
    void Calls_correct_handler_method() {
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        StringReader reader = new StringReader("TESTOMETER");

        OptionalItemEntity optionalItemEntity = descriptor.readItemEntityFrom(reader);

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        optionalItemEntity.applyHandler(handlerMock);
        optionalItemEntity.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(optionalItemEntity);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleOptionalItemEntity(optionalItemEntity);
        verifyNoMoreInteractions(structureHandlerMock);
    }

    @Test
    void Set_invalid_target_item() {
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        OptionalItemEntity optionalItemEntity = descriptor.createItemEntity();
        assertFalse(optionalItemEntity.isTargetItemPresent());
        assertThrows(IllegalArgumentException.class, () -> optionalItemEntity.setTargetItem(
                ConstantFieldDescriptor.newInstance("Bla").withConstant("blo").build().createItemEntity()));
    }

    @Test
    void Discard_and_set_same_target_item() {
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        StringReader reader = new StringReader("TESTOMETER");

        OptionalItemEntity optionalItemEntity = descriptor.readItemEntityFrom(reader);

        ConstantField itemEntity = (ConstantField) optionalItemEntity.getChildren().get(0);
        assertEquals("TEST", itemEntity.getValue());
        assertTrue(optionalItemEntity.getTargetItem().isPresent());
        assertSame(itemEntity, optionalItemEntity.getTargetItem().get());

        optionalItemEntity.setTargetItem(null);

        assertFalse(optionalItemEntity.isTargetItemPresent());

        optionalItemEntity.setTargetItem(itemEntity);

        assertTrue(optionalItemEntity.isTargetItemPresent());
        assertSame(itemEntity, optionalItemEntity.getTargetItem().get());
    }

    @Test
    void Discard_and_set_new_target_item() {
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        StringReader reader = new StringReader("TESTOMETER");

        OptionalItemEntity optionalItemEntity = descriptor.readItemEntityFrom(reader);

        optionalItemEntity.setTargetItem(null);
        assertFalse(optionalItemEntity.isTargetItemPresent());

        FlatDataItemEntity<?> newTargetItem = optionalItemEntity.newTargetItem();
        assertTrue(optionalItemEntity.getTargetItem().isPresent());
        assertSame(optionalItemEntity.getTargetItem().get(), newTargetItem);
    }
    
    @Test
    void Write_when_a_target_is_present() {
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        OptionalItemEntity optionalItemEntity = descriptor.createItemEntity();
        
        optionalItemEntity.newTargetItem();
        
        StringWriter writer = new StringWriter();
        
        optionalItemEntity.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("TEST");
    }
    
    @Test
    void Write_when_a_target_is_absent() {
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance("Optional")
                .withTargetItemDescriptor(
                        ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .build();

        OptionalItemEntity optionalItemEntity = descriptor.createItemEntity();
        
        StringWriter writer = new StringWriter();
        
        optionalItemEntity.writeTo(writer);
        assertThat(writer.getBuffer().toString()).isEqualTo("");
    }
}