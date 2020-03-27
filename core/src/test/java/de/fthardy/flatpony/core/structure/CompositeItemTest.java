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
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.streamio.StructureItemPullReadIteratorBase;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompositeItemTest {
    
    @Test
    void Cannot_create_with_null_name() {
        assertThrows(NullPointerException.class, () -> CompositeItemDescriptor.newInstance(null));
    }

    @Test
    void Cannot_add_null() {
        assertThrows(NullPointerException.class, () -> CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptor(null));
        assertThrows(NullPointerException.class, () -> CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptors(null, null));
        assertThrows(NullPointerException.class, () -> CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptors((Iterable<FlatDataItemDescriptor<?>>) null));
    }

    @Test
    void Cannot_add_the_same_component_instance_twice() {
        ConstantFieldDescriptor constantFieldDescriptor = 
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build();
        
        assertThrows(IllegalArgumentException.class, () -> CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptors(constantFieldDescriptor, constantFieldDescriptor));
    }

    @Test
    void Component_instance_names_must_be_unique() {
        ConstantFieldDescriptor constantFieldDescriptor1 =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST1").build();
        ConstantFieldDescriptor constantFieldDescriptor2 =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST2").build();

        assertThrows(IllegalArgumentException.class, () -> CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptors(constantFieldDescriptor1, constantFieldDescriptor2));
    }
    
    @Test
    void Create_descriptor() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();
        
        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(constantFieldDescriptor, field1Descriptor, field2Descriptor)
                .build();
        
        assertThat(descriptor.getMinLength()).isEqualTo(17);
        assertThat(descriptor.getComponentItemDescriptorByName("ID")).isSameAs(constantFieldDescriptor);
        assertThat(descriptor.getComponentItemDescriptorByName("Field1")).isSameAs(field1Descriptor);
        assertThat(descriptor.getComponentItemDescriptorByName("Field2")).isSameAs(field2Descriptor);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> 
                descriptor.getComponentItemDescriptorByName("Unknown"));
        assertThat(ex.getMessage()).isEqualTo("Unknown");
    }

    @Test
    void Create_entity() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptor(constantFieldDescriptor)
                .addComponentItemDescriptor(field1Descriptor)
                .addComponentItemDescriptor(field2Descriptor)
                .build();

        CompositeItemEntity compositeItem = descriptor.createItemEntity();
        assertThat(compositeItem.getLength()).isEqualTo(17);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                compositeItem.getComponentItemEntityByName("Unknown"));
        assertThat(ex.getMessage()).isEqualTo("Unknown");
    }

    @Test
    void Apply_handler_to_descriptor() {
        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptor(ConstantFieldDescriptor.newInstance("ID").withConstant("Foo").build())
                .build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleCompositeItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }

    @Test
    void Apply_handler_to_entity() {
        CompositeItemEntity item = CompositeItemDescriptor.newInstance("Composite").addComponentItemDescriptor(
                (ConstantFieldDescriptor.newInstance("ID").withConstant("Foo").build())).build().createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        item.applyHandler(handlerMock);
        item.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(item);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleCompositeItemEntity(item);
        verifyNoMoreInteractions(structureHandlerMock);
    }

    @Test
    void Read_entity_from_a_source_stream() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(constantFieldDescriptor, field1Descriptor, field2Descriptor)
                .build();

        Reader reader = new StringReader("FOOTest1123456789");

        CompositeItemEntity compositeItem = descriptor.readItemEntityFrom(reader);
        assertThat(compositeItem.getLength()).isEqualTo(17);
        List<FlatDataItemEntity<?>> componentItemEntities = compositeItem.getComponentItemEntities();
        assertThat(componentItemEntities).hasSize(3);
        assertThat(componentItemEntities.stream().map(c -> c.getDescriptor().getName()).collect(Collectors.toList()))
                .containsExactly("ID", "Field1", "Field2");
        assertThat(compositeItem.getComponentItemEntityByName("ID").getDescriptor().getName()).isEqualTo("ID");
        assertThat(compositeItem.getComponentItemEntityByName("Field1").getDescriptor().getName()).isEqualTo("Field1");
        assertThat(compositeItem.getComponentItemEntityByName("Field2").getDescriptor().getName()).isEqualTo("Field2");
    }
    
    @Test
    void Push_read() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(constantFieldDescriptor, field1Descriptor, field2Descriptor)
                .build();

        Reader reader = new StringReader("FOOTest1123456789");

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        
        descriptor.pushReadFrom(reader, streamReadHandlerMock);

        InOrder inOrder = inOrder(streamReadHandlerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(descriptor);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constantFieldDescriptor, "FOO");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field1Descriptor, "Test1");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field2Descriptor, "123456789");
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(descriptor);
        
        verifyNoMoreInteractions(streamReadHandlerMock);
    }
    
    @Test
    void Pull_read() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor groupDescriptor = CompositeItemDescriptor.newInstance("Group")
                .addComponentItemDescriptors(field1Descriptor, field2Descriptor).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(constantFieldDescriptor, groupDescriptor)
                .build();

        Reader reader = new StringReader("FOOTest1123456789");

        PullReadIterator pullReadIterator = descriptor.pullReadFrom(reader);
        
        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());
        
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> 
                pullReadIterator.nextEvent(streamReadHandlerMock));
        assertThat(ex.getMessage()).isEqualTo(StructureItemPullReadIteratorBase.MSG_No_pull_read_event(descriptor));
        
        InOrder inOrder = inOrder(streamReadHandlerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(descriptor);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constantFieldDescriptor, "FOO");
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(groupDescriptor);
        inOrder.verify(streamReadHandlerMock).onFieldItem(field1Descriptor, "Test1");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field2Descriptor, "123456789");
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(groupDescriptor);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(descriptor);

        verifyNoMoreInteractions(streamReadHandlerMock);
    }

    @Test
    void What_is_read_is_what_is_written() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("ID")
                .withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor))
                .build();

        String data = "FOOTest1123456789";

        Reader reader = new StringReader(data);

        CompositeItemEntity compositeItem = descriptor.readItemEntityFrom(reader);

        StringWriter writer = new StringWriter();
        compositeItem.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo(data);
    }
}