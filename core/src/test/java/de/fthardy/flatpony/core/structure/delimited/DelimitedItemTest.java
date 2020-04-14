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
package de.fthardy.flatpony.core.structure.delimited;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.streamio.StructureItemPullReadIteratorBase;
import de.fthardy.flatpony.core.structure.FlatDataStructure;
import de.fthardy.flatpony.core.structure.FlatDataStructureDescriptor;
import de.fthardy.flatpony.core.structure.delimited.DelimitedItemDescriptor;
import de.fthardy.flatpony.core.structure.delimited.DelimitedItemEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DelimitedItemTest {

    @Test
    void Cannot_create_with_no_item() {
        assertThrows(NullPointerException.class, () -> DelimitedItemDescriptor.newInstance(null));
    }
    
    @Test
    void Create_descriptor() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);

        DelimitedItemDescriptor descriptor = 
                DelimitedItemDescriptor.newInstance(itemDescriptorMock).withDelimiter('\t').build();
        
        assertThat(descriptor.getName()).isEqualTo("mock");
        assertThat(descriptor.getMinLength()).isEqualTo(42);
        assertThat(descriptor.getDelimiter()).isEqualTo('\t');
        assertThat(descriptor.getTargetItemDescriptor()).isSameAs(itemDescriptorMock);
        
        verify(itemDescriptorMock, times(2)).getName();
        verify(itemDescriptorMock).getMinLength();
        
        verifyNoMoreInteractions(itemDescriptorMock);
    }
    
    @Test
    void Create_entity() {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        when(itemEntityMock.getLength()).thenReturn(42);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);

        DelimitedItemDescriptor descriptor =
                DelimitedItemDescriptor.newInstance(itemDescriptorMock).withDelimiter('\t').build();

        DelimitedItemEntity entity = descriptor.createItemEntity();

        assertThat(entity.getLength()).isEqualTo(42);
        assertThat(entity.getTargetItem()).isSameAs(itemEntityMock);
        
        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).createItemEntity();
        
        verifyNoMoreInteractions(itemDescriptorMock);
        
        verify(itemEntityMock).getLength();
        
        verifyNoMoreInteractions(itemEntityMock);
    }

    @Test
    void Apply_handler_to_descriptor() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");

        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(itemDescriptorMock).build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleDelimitedItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }

    @Test
    void Apply_handler_to_entity() {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);

        DelimitedItemDescriptor descriptor =
                DelimitedItemDescriptor.newInstance(itemDescriptorMock).withDelimiter('\t').build();

        DelimitedItemEntity entity = descriptor.createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        entity.applyHandler(handlerMock);
        entity.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(entity);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleDelimitedItemEntity(entity);
        verifyNoMoreInteractions(structureHandlerMock);
    }

    @Test
    void Delimiter_is_missing_on_read() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.read()).thenReturn((int)'x'); // anything else but no delimiter char

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> 
                        DelimitedItemDescriptor.newInstance(itemDescriptorMock).build().readItemEntityFrom(readerMock));
        assertNull(exception.getCause());
        assertThat(exception.getMessage()).isEqualTo(
                DelimitedItemDescriptor.MSG_No_delimiter_found("mock"));
    }

    @Test
    void Normal_read_with_delimiter() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.read()).thenReturn((int)'\n');

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);

        DelimitedItemEntity entity =
                DelimitedItemDescriptor.newInstance(itemDescriptorMock).build().readItemEntityFrom(readerMock);
        assertThat(entity.getTargetItem()).isSameAs(itemEntityMock);
    }

    @Test
    void Normal_read_without_delimiter_but_EOF() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.read()).thenReturn(-1);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);

        DelimitedItemEntity entity = 
                DelimitedItemDescriptor.newInstance(itemDescriptorMock).build().readItemEntityFrom(readerMock);
        assertThat(entity.getTargetItem()).isSameAs(itemEntityMock);
    }
    
    @Test
    void IOException_during_read() throws IOException {
        Reader readerMock = mock(Reader.class);
        IOException ioException = new IOException("TEST");
        when(readerMock.read()).thenThrow(ioException);
        
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);

        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(itemDescriptorMock).build();

        FlatDataReadException ex = assertThrows(FlatDataReadException.class, () ->
                descriptor.readItemEntityFrom(readerMock));
        assertThat(ex.getMessage()).isEqualTo(DelimitedItemDescriptor.MSG_Read_failed(descriptor.getName()));
        assertThat(ex.getCause()).isSameAs(ioException);

        //noinspection ResultOfMethodCallIgnored
        verify(readerMock).read();
        
        verifyNoMoreInteractions(readerMock);
    }
    
    @Test
    void Push_read() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.read()).thenReturn(-1);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);

        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(itemDescriptorMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        descriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(streamReadHandlerMock, itemDescriptorMock, readerMock);
        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(descriptor);
        inOrder.verify(itemDescriptorMock).pushReadFrom(readerMock, streamReadHandlerMock);
        //noinspection ResultOfMethodCallIgnored
        inOrder.verify(readerMock).read();
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(descriptor);

        verifyNoMoreInteractions(streamReadHandlerMock);
        verifyNoMoreInteractions(itemDescriptorMock);
        verifyNoMoreInteractions(readerMock);
    }
    
    @Test
    void Pull_read() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.read()).thenReturn(-1);

        PullReadIterator targetItemIteratorMock = mock(PullReadIterator.class);
        when(targetItemIteratorMock.hasNextEvent()).thenReturn(true, false);

        FlatDataFieldDescriptor<?> itemDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.pullReadFrom(readerMock)).thenAnswer(i -> targetItemIteratorMock);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(itemDescriptorMock).build();

        PullReadIterator pullReadIterator = descriptor.pullReadFrom(readerMock);

        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                pullReadIterator.nextEvent(streamReadHandlerMock));
        assertNull(exception.getCause());
        assertThat(exception.getMessage()).isEqualTo(StructureItemPullReadIteratorBase.MSG_No_pull_read_event(
                "mock", DelimitedItemDescriptor.class.getSimpleName()));

        InOrder inOrder = inOrder(readerMock, targetItemIteratorMock, itemDescriptorMock, streamReadHandlerMock);
        
        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(descriptor);
        inOrder.verify(itemDescriptorMock).pullReadFrom(readerMock);
        inOrder.verify(targetItemIteratorMock).hasNextEvent();
        inOrder.verify(targetItemIteratorMock).nextEvent(streamReadHandlerMock);
        inOrder.verify(targetItemIteratorMock).hasNextEvent();
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(descriptor);
        inOrder.verify(itemDescriptorMock).getName();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, targetItemIteratorMock, streamReadHandlerMock);
    }

    @Test
    void Write_to_target_stream() throws IOException {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);

        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(itemDescriptorMock).build();
        
        DelimitedItemEntity entity = descriptor.createItemEntity();

        Writer writerMock = mock(Writer.class);

        entity.writeTo(writerMock);
        
        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).createItemEntity();
        verify(itemEntityMock).writeTo(writerMock);
        verify(writerMock).write(descriptor.getDelimiter());

        verifyNoMoreInteractions(itemDescriptorMock, itemEntityMock, writerMock);
    }

    @Test
    void Write_causes_IOException() throws IOException {
        Writer writerMock = mock(Writer.class);

        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write(anyInt());

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);

        DelimitedItemDescriptor descriptor = DelimitedItemDescriptor.newInstance(itemDescriptorMock).build();

        DelimitedItemEntity entity = descriptor.createItemEntity();

        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () -> entity.writeTo(writerMock));
        assertThat(exception.getMessage()).isEqualTo(DelimitedItemEntity.MSG_Write_failed("mock"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(itemDescriptorMock, times(2)).getName();
        verify(itemDescriptorMock).createItemEntity();
        verify(itemEntityMock).writeTo(writerMock);
        verify(writerMock).write(descriptor.getDelimiter());
        
        verifyNoMoreInteractions(itemDescriptorMock, itemEntityMock, writerMock);
    }
}