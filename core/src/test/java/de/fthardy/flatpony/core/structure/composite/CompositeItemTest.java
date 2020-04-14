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
package de.fthardy.flatpony.core.structure.composite;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.streamio.StructureItemPullReadIteratorBase;
import de.fthardy.flatpony.core.structure.FlatDataStructure;
import de.fthardy.flatpony.core.structure.FlatDataStructureDescriptor;
import de.fthardy.flatpony.core.structure.composite.CompositeItemDescriptor;
import de.fthardy.flatpony.core.structure.composite.CompositeItemEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.Reader;
import java.io.Writer;
import java.util.NoSuchElementException;

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
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        
        assertThrows(IllegalArgumentException.class, () -> CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptors(fieldDescriptorMock, fieldDescriptorMock));
    }

    @Test
    void Component_instance_names_must_be_unique() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock1 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock1.getName()).thenReturn("Field");
        FlatDataFieldDescriptor<?> fieldDescriptorMock2 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock2.getName()).thenReturn("Field");

        assertThrows(IllegalArgumentException.class, () -> CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptors(fieldDescriptorMock1, fieldDescriptorMock2));
    }
    
    @Test
    void Create_descriptor() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock1 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock1.getName()).thenReturn("Field1");
        when(fieldDescriptorMock1.getMinLength()).thenReturn(12);
        FlatDataFieldDescriptor<?> fieldDescriptorMock2 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock2.getName()).thenReturn("Field2");
        when(fieldDescriptorMock2.getMinLength()).thenReturn(30);
        
        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(fieldDescriptorMock1, fieldDescriptorMock2)
                .build();
        
        assertThat(descriptor.getMinLength()).isEqualTo(42);
        assertThat(descriptor.getComponentItemDescriptorByName("Field1")).isSameAs(fieldDescriptorMock1);
        assertThat(descriptor.getComponentItemDescriptorByName("Field2")).isSameAs(fieldDescriptorMock2);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> 
                descriptor.getComponentItemDescriptorByName("Unknown"));
        assertThat(ex.getMessage()).isEqualTo("Unknown");
    }

    @Test
    void Create_entity() {
        FlatDataField<?> fieldMock1 = mock(FlatDataField.class);
        FlatDataField<?> fieldMock2 = mock(FlatDataField.class);
        
        FlatDataFieldDescriptor<?> fieldDescriptorMock1 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock1.getName()).thenReturn("Field1");
        when(fieldDescriptorMock1.createItemEntity()).thenAnswer(i -> fieldMock1);
        FlatDataFieldDescriptor<?> fieldDescriptorMock2 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock2.getName()).thenReturn("Field2");
        when(fieldDescriptorMock2.createItemEntity()).thenAnswer(i -> fieldMock2);
        
        when(fieldMock1.getDescriptor()).thenAnswer(i -> fieldDescriptorMock1);
        when(fieldMock1.getLength()).thenReturn(30);
        when(fieldMock2.getDescriptor()).thenAnswer(i -> fieldDescriptorMock2);
        when(fieldMock2.getLength()).thenReturn(12);

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(fieldDescriptorMock1, fieldDescriptorMock2)
                .build();

        CompositeItemEntity entity = descriptor.createItemEntity();
        assertThat(entity.getLength()).isEqualTo(42);
        
        assertThat(entity.getComponentItemEntities()).containsExactly(fieldMock1, fieldMock2);
        
        assertThat(entity.getComponentItemEntityByName("Field1")).isSameAs(fieldMock1);
        assertThat(entity.getComponentItemEntityByName("Field2")).isSameAs(fieldMock2);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                entity.getComponentItemEntityByName("Unknown"));
        assertThat(ex.getMessage()).isEqualTo("Unknown");
    }

    @Test
    void Apply_handler_to_descriptor() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptor(fieldDescriptorMock)
                .build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(fieldDescriptorMock, times(2)).getName();
        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verify(descriptorHandlerMock).handleCompositeItemDescriptor(descriptor);
        
        verifyNoMoreInteractions(handlerMock, descriptorHandlerMock, fieldDescriptorMock);
    }

    @Test
    void Apply_handler_to_entity() {
        FlatDataField<?> fieldMock = mock(FlatDataField.class);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.createItemEntity()).thenAnswer(i -> fieldMock);

        when(fieldMock.getDescriptor()).thenAnswer(i -> fieldDescriptorMock);
        
        CompositeItemEntity entity = CompositeItemDescriptor.newInstance("Composite")
                .addComponentItemDescriptor(fieldDescriptorMock)
                .build()
                .createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        entity.applyHandler(handlerMock);
        entity.applyHandler(structureHandlerMock);

        verify(fieldDescriptorMock, times(3)).getName();
        verify(fieldDescriptorMock).createItemEntity();
        verify(fieldMock).getDescriptor();
        verify(handlerMock).handleFlatDataItemEntity(entity);
        verify(structureHandlerMock).handleCompositeItemEntity(entity);
        
        verifyNoMoreInteractions(handlerMock, structureHandlerMock, fieldDescriptorMock, fieldMock);
    }

    @Test
    void Read_entity_from_a_source_stream() {
        Reader readerMock = mock(Reader.class);
        
        FlatDataField<?> fieldMock1 = mock(FlatDataField.class);
        FlatDataField<?> fieldMock2 = mock(FlatDataField.class);

        FlatDataFieldDescriptor<?> fieldDescriptorMock1 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock1.getName()).thenReturn("Field1");
        when(fieldDescriptorMock1.readItemEntityFrom(readerMock)).thenAnswer(i -> fieldMock1);
        FlatDataFieldDescriptor<?> fieldDescriptorMock2 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock2.getName()).thenReturn("Field2");
        when(fieldDescriptorMock2.readItemEntityFrom(readerMock)).thenAnswer(i -> fieldMock2);

        when(fieldMock1.getDescriptor()).thenAnswer(i -> fieldDescriptorMock1);
        when(fieldMock2.getDescriptor()).thenAnswer(i -> fieldDescriptorMock2);

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(fieldDescriptorMock1, fieldDescriptorMock2)
                .build();
        
        CompositeItemEntity entity = descriptor.readItemEntityFrom(readerMock);
        assertThat(entity.getComponentItemEntities()).containsExactly(fieldMock1, fieldMock2);
        
        verify(fieldDescriptorMock1, times(3)).getName();
        verify(fieldDescriptorMock1).readItemEntityFrom(readerMock);
        verify(fieldDescriptorMock2, times(3)).getName();
        verify(fieldDescriptorMock2).readItemEntityFrom(readerMock);
        verify(fieldMock1).getDescriptor();
        verify(fieldMock2).getDescriptor();
        
        verifyNoMoreInteractions(readerMock, fieldMock1, fieldMock2, fieldDescriptorMock1, fieldDescriptorMock2);
    }
    
    @Test
    void Push_read() {
        Reader readerMock = mock(Reader.class);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        FlatDataField<?> fieldMock1 = mock(FlatDataField.class);
        FlatDataField<?> fieldMock2 = mock(FlatDataField.class);

        FlatDataFieldDescriptor<?> fieldDescriptorMock1 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock1.getName()).thenReturn("Field1");
        FlatDataFieldDescriptor<?> fieldDescriptorMock2 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock2.getName()).thenReturn("Field2");

        when(fieldMock1.getDescriptor()).thenAnswer(i -> fieldDescriptorMock1);
        when(fieldMock2.getDescriptor()).thenAnswer(i -> fieldDescriptorMock2);

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(fieldDescriptorMock1, fieldDescriptorMock2)
                .build();
        
        descriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(streamReadHandlerMock, fieldDescriptorMock1, fieldDescriptorMock2);
        inOrder.verify(fieldDescriptorMock1, times(2)).getName();
        inOrder.verify(fieldDescriptorMock2, times(2)).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(descriptor);
        inOrder.verify(fieldDescriptorMock1).pushReadFrom(readerMock, streamReadHandlerMock);
        inOrder.verify(fieldDescriptorMock2).pushReadFrom(readerMock, streamReadHandlerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(descriptor);
        
        verifyNoMoreInteractions(
                readerMock, streamReadHandlerMock, fieldDescriptorMock1, fieldDescriptorMock2, fieldMock1, fieldMock2);
    }
    
    @Test
    void Pull_read() {
        Reader readerMock = mock(Reader.class);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        
        PullReadIterator fieldIteratorMock = mock(PullReadIterator.class);
        when(fieldIteratorMock.hasNextEvent()).thenReturn(true, false);
        PullReadIterator structureIteratorMock = mock(PullReadIterator.class);
        when(structureIteratorMock.hasNextEvent()).thenReturn(true, true, false);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.pullReadFrom(readerMock)).thenReturn(fieldIteratorMock);
        FlatDataStructureDescriptor<?> structureDescriptorMock = mock(FlatDataStructureDescriptor.class);
        when(structureDescriptorMock.getName()).thenReturn("Structure");
        when(structureDescriptorMock.pullReadFrom(readerMock)).thenReturn(structureIteratorMock);

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(fieldDescriptorMock, structureDescriptorMock)
                .build();
        
        PullReadIterator pullReadIterator = descriptor.pullReadFrom(readerMock);
        
        // 1st iteration (start)
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        // 2nd iteration (field)
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        // 3rd iteration (structure start)
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        // 4th iteration (structure content)
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        // 5th iteration (structure end)
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        // 6th iteration (end)
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        
        // no elements left
        assertFalse(pullReadIterator.hasNextEvent());
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> 
                pullReadIterator.nextEvent(streamReadHandlerMock));
        assertThat(ex.getMessage()).isEqualTo(StructureItemPullReadIteratorBase.MSG_No_pull_read_event(
                descriptor.getName(), descriptor.getClass().getSimpleName()));
        
        verify(fieldDescriptorMock, times(2)).getName();
        verify(structureDescriptorMock, times(2)).getName();
        
        InOrder inOrder = inOrder(
                streamReadHandlerMock,
                fieldDescriptorMock, fieldIteratorMock,
                structureDescriptorMock, structureIteratorMock);
        
        // 1st iteration (start)
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(descriptor);
        inOrder.verify(fieldDescriptorMock).pullReadFrom(readerMock);
        
        // 2nd iteration (field)
        inOrder.verify(fieldIteratorMock).hasNextEvent();
        inOrder.verify(fieldIteratorMock).nextEvent(streamReadHandlerMock);

        // 3rd iteration (structure start)
        inOrder.verify(fieldIteratorMock).hasNextEvent();
        inOrder.verify(structureDescriptorMock).pullReadFrom(readerMock);
        inOrder.verify(structureIteratorMock).nextEvent(streamReadHandlerMock);
        
        // 4th iteration (structure content)
        inOrder.verify(structureIteratorMock).hasNextEvent();
        inOrder.verify(structureIteratorMock).nextEvent(streamReadHandlerMock);
        
        // 5th iteration (structure end)
        inOrder.verify(structureIteratorMock).hasNextEvent();
        inOrder.verify(structureIteratorMock).nextEvent(streamReadHandlerMock);
        
        // 6th iteration (end)
        inOrder.verify(structureIteratorMock).hasNextEvent();
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(descriptor);
        
        verifyNoMoreInteractions(streamReadHandlerMock,
                fieldDescriptorMock, fieldIteratorMock,
                structureDescriptorMock, structureIteratorMock);

        verifyZeroInteractions(readerMock);
        verifyZeroInteractions(streamReadHandlerMock);
    }

    @Test
    void Write_entity_to_target_stream() {
        Reader readerMock = mock(Reader.class);
        
        Writer writerMock = mock(Writer.class);

        FlatDataField<?> fieldMock1 = mock(FlatDataField.class);
        FlatDataField<?> fieldMock2 = mock(FlatDataField.class);

        FlatDataFieldDescriptor<?> fieldDescriptorMock1 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock1.getName()).thenReturn("Field1");
        when(fieldDescriptorMock1.readItemEntityFrom(readerMock)).thenAnswer(i -> fieldMock1);
        FlatDataFieldDescriptor<?> fieldDescriptorMock2 = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock2.getName()).thenReturn("Field2");
        when(fieldDescriptorMock2.readItemEntityFrom(readerMock)).thenAnswer(i -> fieldMock2);

        when(fieldMock1.getDescriptor()).thenAnswer(i -> fieldDescriptorMock1);
        when(fieldMock2.getDescriptor()).thenAnswer(i -> fieldDescriptorMock2);

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addComponentItemDescriptors(fieldDescriptorMock1, fieldDescriptorMock2)
                .build();

        descriptor.readItemEntityFrom(readerMock).writeTo(writerMock);
        
        verify(fieldDescriptorMock1, times(3)).getName();
        verify(fieldDescriptorMock1).readItemEntityFrom(readerMock);
        verify(fieldDescriptorMock2, times(3)).getName();
        verify(fieldDescriptorMock2).readItemEntityFrom(readerMock);
        
        verify(fieldMock1).getDescriptor();
        verify(fieldMock2).getDescriptor();
        
        verify(fieldMock1).writeTo(writerMock);
        verify(fieldMock2).writeTo(writerMock);
        
        verifyZeroInteractions(readerMock, writerMock);
    }
}