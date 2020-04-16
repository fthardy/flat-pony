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
package de.fthardy.flatpony.core.structure.optional;

import de.fthardy.flatpony.core.*;
import de.fthardy.flatpony.core.field.constant.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.streamio.*;
import de.fthardy.flatpony.core.structure.FlatDataStructureDescriptorHandler;
import de.fthardy.flatpony.core.structure.FlatDataStructureHandler;
import de.fthardy.flatpony.core.util.FieldReference;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OptionalItemTest {

    @Test
    void Cannot_set_target_item_descriptor_to_null() {
        assertThrows(NullPointerException.class, () ->
                OptionalItemDescriptor.newInstance(null));
    }

    @Test
    void Cannot_set_FlagFieldReference_to_null() {
        assertThrows(NullPointerException.class, () -> OptionalItemDescriptor.newInstance(
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build())
                .withFlagFieldReference(null));
    }

    @Test
    void Create_descriptor_without_flag_field_reference() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();
        assertThat(optionalItemDescriptor.toString()).startsWith(OptionalItemDescriptor.class.getSimpleName());
        assertThat(optionalItemDescriptor.getName()).isEqualTo("mock");
        assertThat(optionalItemDescriptor.getMinLength()).isEqualTo(0); // is always 0
        assertThat(optionalItemDescriptor.getTargetItemDescriptor()).isSameAs(itemDescriptorMock);
        assertThat(optionalItemDescriptor.getFlagFieldReference()).isNull();
        
        verify(itemDescriptorMock, times(2)).getName();
        
        verifyNoMoreInteractions(itemDescriptorMock);
    }

    @Test
    void Create_descriptor_with_flag_field_reference() {
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();
        assertThat(optionalItemDescriptor.getName()).isEqualTo("mock");
        assertThat(optionalItemDescriptor.getMinLength()).isEqualTo(0); // is always 0
        assertThat(optionalItemDescriptor.getTargetItemDescriptor()).isSameAs(itemDescriptorMock);
        assertThat(optionalItemDescriptor.getFlagFieldReference()).isSameAs(fieldReferenceMock);
        
        verify(itemDescriptorMock, times(2)).getName();
        
        verifyNoMoreInteractions(itemDescriptorMock);
        
        verifyZeroInteractions(fieldReferenceMock);
    }
    
    @Test
    void Create_entity_without_flag_field_reference() {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        
        when(itemEntityMock.getLength()).thenReturn(42);
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();
        
        OptionalItemEntity itemEntity = optionalItemDescriptor.createItemEntity();
        assertThat(itemEntity.toString()).startsWith(OptionalItemEntity.class.getSimpleName());
        assertThat(itemEntity.getLength()).isEqualTo(0);
        assertFalse(itemEntity.getTargetItem().isPresent());

        FlatDataItemEntity<?> newTargetItem = itemEntity.newTargetItem();
        assertTrue(itemEntity.getTargetItem().isPresent());
        assertThat(newTargetItem).isSameAs(itemEntityMock);
        assertThat(newTargetItem.getLength()).isEqualTo(42);

        FlatDataItemEntity<?> otherItemEntityMock = mock(FlatDataItemEntity.class);
        assertThrows(IllegalArgumentException.class, () -> itemEntity.setTargetItem(otherItemEntityMock));
        
        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).createItemEntity();
        
        verify(itemEntityMock).getLength();
        verify(itemEntityMock).getDescriptor();
        
        verifyNoMoreInteractions(itemEntityMock, itemDescriptorMock);
    }
    
    @Test
    void Create_entity_with_flag_field_reference__No_field_reference_available() {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        
        when(fieldReferenceMock.getReferencedField()).thenReturn(null);
        
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        
        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();
        
        OptionalItemEntity itemEntity = optionalItemDescriptor.createItemEntity();
        assertThat(itemEntity.getLength()).isEqualTo(0);
        assertFalse(itemEntity.getTargetItem().isPresent());
        
        verify(itemDescriptorMock).getName();
        
        verify(fieldReferenceMock).getReferencedField();
        
        verifyNoMoreInteractions(itemDescriptorMock, fieldReferenceMock);
        
        verifyZeroInteractions(itemEntityMock);
    }
    
    @Test
    void Create_entity_with_flag_field_reference__Flag_is_false() {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        
        FieldReference.ReferencedField<Boolean> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        
        when(fieldReferenceMock.getReferencedField()).thenReturn(referencedFieldMock);
        when(referencedFieldMock.getValue()).thenReturn(false);
        
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        
        when(itemEntityMock.getLength()).thenReturn(42);
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();
        
        OptionalItemEntity itemEntity = optionalItemDescriptor.createItemEntity();
        assertThat(itemEntity.getLength()).isEqualTo(0);
        assertFalse(itemEntity.getTargetItem().isPresent());

        FlatDataItemEntity<?> newTargetItem = itemEntity.newTargetItem();
        assertTrue(itemEntity.getTargetItem().isPresent());
        assertThat(newTargetItem).isSameAs(itemEntityMock);
        assertThat(newTargetItem.getLength()).isEqualTo(42);
        
        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).createItemEntity();
        
        verify(itemEntityMock).getLength();
        verify(itemEntityMock).getDescriptor();

        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock).getValue();
        verify(referencedFieldMock).setValue(true);
        
        verifyNoMoreInteractions(itemEntityMock, itemDescriptorMock, fieldReferenceMock, referencedFieldMock);
    }
    
    @Test
    void Create_entity_with_flag_field_reference__Flag_is_true() {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        
        FieldReference.ReferencedField<Boolean> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        
        when(fieldReferenceMock.getReferencedField()).thenReturn(referencedFieldMock);
        
        when(referencedFieldMock.getValue()).thenReturn(true);
        
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        
        when(itemEntityMock.getLength()).thenReturn(42);
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();
        
        OptionalItemEntity itemEntity = optionalItemDescriptor.createItemEntity();
        assertThat(itemEntity.getLength()).isEqualTo(42);
        assertTrue(itemEntity.getTargetItem().isPresent());
        assertThat(itemEntity.getTargetItem().get()).isSameAs(itemEntityMock);
        
        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).createItemEntity();
        
        verify(itemEntityMock).getLength();

        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock).getValue();
        
        verifyNoMoreInteractions(itemEntityMock, itemDescriptorMock, fieldReferenceMock, referencedFieldMock);
    }

    @Test
    void Apply_handler_to_descriptor() {
        FlatDataItemDescriptorHandler handlerMock = mock(FlatDataItemDescriptorHandler.class);
        FlatDataStructureDescriptorHandler descriptorHandlerMock = mock(FlatDataStructureDescriptorHandler.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        
        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(itemDescriptorMock).getName();
        
        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        
        verify(descriptorHandlerMock).handleOptionalItemDescriptor(descriptor);
        
        verifyNoMoreInteractions(handlerMock, descriptorHandlerMock);
    }

    @Test
    void Apply_handler_to_entity() {
        FlatDataItemEntityHandler handlerMock = mock(FlatDataItemEntityHandler.class);
        FlatDataStructureHandler structureHandlerMock = mock(FlatDataStructureHandler.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");

        OptionalItemDescriptor descriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        OptionalItemEntity optionalItemEntity = descriptor.createItemEntity();

        optionalItemEntity.applyHandler(handlerMock);
        optionalItemEntity.applyHandler(structureHandlerMock);

        verify(itemDescriptorMock).getName();
        
        verify(handlerMock).handleFlatDataItemEntity(optionalItemEntity);
        
        verify(structureHandlerMock).handleOptionalItemEntity(optionalItemEntity);
        
        verifyNoMoreInteractions(handlerMock, structureHandlerMock);
    }
    
    @Test
    void Read_entity_without_flag_field_reference__No_target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);
        
        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        OptionalItemEntity itemEntity = optionalItemDescriptor.readItemEntityFrom(readerMock);
        assertFalse(itemEntity.getTargetItem().isPresent());

        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        
        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        
        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }
    
    @Test
    void Read_entity_without_flag_field_reference__No_target_item_available__Mark_not_supported() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(false);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        
        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                optionalItemDescriptor.readItemEntityFrom(readerMock));
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(OptionalItemDescriptor.MSG_Mark_not_supported("mock"));

        verify(itemDescriptorMock, times(2)).getName();
        
        verify(readerMock).markSupported();
        
        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }
    
    @Test
    void Read_entity_without_flag_field_reference__No_target_item_available__Mark_fails() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        IOException ioException = new IOException("TEST");
        doThrow(ioException).when(readerMock).mark(42);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        
        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                optionalItemDescriptor.readItemEntityFrom(readerMock));
        assertThat(exception.getCause()).isSameAs(ioException);
        assertThat(exception.getMessage()).isEqualTo(OptionalItemDescriptor.MSG_Failed_to_mark_stream("mock"));

        verify(itemDescriptorMock, times(2)).getName();
        verify(itemDescriptorMock).getMinLength();
        
        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        
        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }

    @Test
    void Read_entity_without_flag_field_reference__Target_item_read_fails() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        FlatDataReadException exception = new FlatDataReadException("TEST");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenThrow(exception);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        OptionalItemEntity itemEntity = optionalItemDescriptor.readItemEntityFrom(readerMock);
        assertFalse(itemEntity.getTargetItem().isPresent());

        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        verify(readerMock).reset();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }

    @Test
    void Read_entity_without_flag_field_reference__Target_item_read_fails__Reset_reader_fails() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        IOException ioException = new IOException("TEST");
        doThrow(ioException).when(readerMock).reset();

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        FlatDataReadException exception = new FlatDataReadException("TEST");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenThrow(exception);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        FlatDataReadException ex = assertThrows(FlatDataReadException.class, () ->
                optionalItemDescriptor.readItemEntityFrom(readerMock));
        assertThat(ex.getCause()).isSameAs(ioException);
        assertThat(ex.getMessage()).isEqualTo(OptionalItemDescriptor.MSG_Failed_to_reset_stream("mock"));

        verify(itemDescriptorMock, times(2)).getName();
        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        verify(readerMock).reset();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }
    
    @Test
    void Read_entity_with_flag_field_reference__No_target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(null);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        OptionalItemEntity itemEntity = optionalItemDescriptor.readItemEntityFrom(readerMock);
        assertFalse(itemEntity.getTargetItem().isPresent());

        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        
        verify(fieldReferenceMock).getReferencedField();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, fieldReferenceMock);
    }
    
    @Test
    void Read_entity_with_flag_field_reference__Flag_is_false() {
        Reader readerMock = mock(Reader.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        
        FieldReference.ReferencedField<Boolean> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        when(referencedFieldMock.getValue()).thenReturn(false);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(referencedFieldMock);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        OptionalItemEntity itemEntity = optionalItemDescriptor.readItemEntityFrom(readerMock);
        assertFalse(itemEntity.getTargetItem().isPresent());

        verify(itemDescriptorMock).getName();

        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock).getValue();

        verifyNoMoreInteractions(itemDescriptorMock, fieldReferenceMock, referencedFieldMock);
        
        verifyZeroInteractions(readerMock);
    }
    
    @Test
    void Read_entity_with_flag_field_reference__Flag_is_true() {
        Reader readerMock = mock(Reader.class);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);
        
        FieldReference.ReferencedField<Boolean> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        when(referencedFieldMock.getValue()).thenReturn(true);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(referencedFieldMock);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        OptionalItemEntity itemEntity = optionalItemDescriptor.readItemEntityFrom(readerMock);
        assertTrue(itemEntity.getTargetItem().isPresent());

        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock).getValue();

        verifyNoMoreInteractions(itemDescriptorMock, fieldReferenceMock, referencedFieldMock);
        
        verifyZeroInteractions(readerMock, itemEntityMock);
    }
    
    @Test
    void Push_read_without_flag_field_reference__No_target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        optionalItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(itemDescriptorMock, readerMock, streamReadHandlerMock);
        
        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(readerMock).markSupported();
        inOrder.verify(itemDescriptorMock).getMinLength();
        inOrder.verify(readerMock).mark(42);
        inOrder.verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, streamReadHandlerMock);
    }
    
    @Test
    void Push_read_without_flag_field_reference__Target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);
        
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        optionalItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(readerMock, itemEntityMock, itemDescriptorMock, streamReadHandlerMock);
        
        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(readerMock).markSupported();
        inOrder.verify(itemDescriptorMock).getMinLength();
        inOrder.verify(readerMock).mark(42);
        inOrder.verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(itemEntityMock).applyHandler(any(PushReadItemEntityTreeWalker.class));
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(readerMock, itemEntityMock, itemDescriptorMock, streamReadHandlerMock);
    }

    @Test
    void Push_read_with_flag_field_reference__No_flag_value__No_target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(null);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        optionalItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(readerMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(readerMock).markSupported();
        inOrder.verify(itemDescriptorMock).getMinLength();
        inOrder.verify(readerMock).mark(42);
        inOrder.verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);
    }

    @Test
    void Push_read_with_flag_field_reference__No_flag_value__Target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(null);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        optionalItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(
                readerMock, itemEntityMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(readerMock).markSupported();
        inOrder.verify(itemDescriptorMock).getMinLength();
        inOrder.verify(readerMock).mark(42);
        inOrder.verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(itemEntityMock).applyHandler(any(PushReadItemEntityTreeWalker.class));
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(
                readerMock, itemEntityMock,  itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);
    }

    @Test
    void Push_read_with_flag_field_reference__Flag_is_false() throws IOException {
        Reader readerMock = mock(Reader.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(false);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        optionalItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(
                readerMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(
                readerMock,  itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);
    }

    @Test
    void Push_read_with_flag_field_reference__Flag_is_true() throws IOException {
        Reader readerMock = mock(Reader.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        
        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(true);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        optionalItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        InOrder inOrder = inOrder(
                readerMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(itemDescriptorMock).pushReadFrom(readerMock, streamReadHandlerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(
                readerMock,  itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);
    }
    
    @Test
    void Pull_read_without_flag_field_reference__No_target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        PullReadIterator pullReadIterator = optionalItemDescriptor.pullReadFrom(readerMock);
        
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> 
                pullReadIterator.nextEvent(streamReadHandlerMock));
        assertThat(ex.getCause()).isNull();
        assertThat(ex.getMessage()).isEqualTo(StructureItemPullReadIteratorBase.MSG_No_pull_read_event(
                "mock", OptionalItemDescriptor.class.getSimpleName()));

        InOrder inOrder = inOrder(itemDescriptorMock, readerMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(readerMock).markSupported();
        inOrder.verify(itemDescriptorMock).getMinLength();
        inOrder.verify(readerMock).mark(42);
        inOrder.verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);
        inOrder.verify(itemDescriptorMock).getName();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, streamReadHandlerMock);
    }

    @Test
    void Pull_read_without_flag_field_reference__Target_item_available_but_empty() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        PullReadIterator pullReadIterator = optionalItemDescriptor.pullReadFrom(readerMock);

        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        InOrder inOrder = inOrder(readerMock, itemEntityMock, itemDescriptorMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(readerMock).markSupported();
        inOrder.verify(itemDescriptorMock).getMinLength();
        inOrder.verify(readerMock).mark(42);
        inOrder.verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(itemEntityMock).applyHandler(any(ItemEntityStructureFlattener.class));
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(readerMock, itemEntityMock, itemDescriptorMock, streamReadHandlerMock);
    }

    @Test
    void Pull_read_with_flag_field_reference__No_flag_value__No_target_item_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);

        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(null);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        PullReadIterator pullReadIterator = optionalItemDescriptor.pullReadFrom(readerMock);
        
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());
        
        InOrder inOrder = inOrder(readerMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(readerMock).markSupported();
        inOrder.verify(itemDescriptorMock).getMinLength();
        inOrder.verify(readerMock).mark(42);
        inOrder.verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);
    }

    @Test
    void Pull_read_with_flag_field_reference__Flag_is_false() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");

        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(false);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        PullReadIterator pullReadIterator = optionalItemDescriptor.pullReadFrom(readerMock);

        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        InOrder inOrder = inOrder(
                readerMock, itemEntityMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(
                readerMock, itemEntityMock, itemDescriptorMock, fieldReferenceMock, streamReadHandlerMock);
    }

    @Test
    void Pull_read_with_flag_field_reference__Flag_is_true__No_pull_read_events() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        PullReadIterator pullReadIteratorMock = mock(PullReadIterator.class);
        when(pullReadIteratorMock.hasNextEvent()).thenReturn(false);
        
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.pullReadFrom(readerMock)).thenAnswer(i -> pullReadIteratorMock);

        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(true);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        PullReadIterator pullReadIterator = optionalItemDescriptor.pullReadFrom(readerMock);

        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        InOrder inOrder = inOrder(
                readerMock,
                itemEntityMock,
                itemDescriptorMock,
                pullReadIteratorMock,
                fieldReferenceMock,
                streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(itemDescriptorMock).pullReadFrom(readerMock);
        inOrder.verify(pullReadIteratorMock).hasNextEvent();
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(
                readerMock, 
                itemEntityMock, 
                itemDescriptorMock,
                pullReadIteratorMock,
                fieldReferenceMock,
                streamReadHandlerMock);
    }

    @Test
    void Pull_read_with_flag_field_reference__Flag_is_true__One_field() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        PullReadIterator pullReadIteratorMock = mock(PullReadIterator.class);
        when(pullReadIteratorMock.hasNextEvent()).thenReturn(true, false);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.pullReadFrom(readerMock)).thenAnswer(i -> pullReadIteratorMock);

        FieldReference<Boolean> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(true);

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock)
                .withFlagFieldReference(fieldReferenceMock).build();

        PullReadIterator pullReadIterator = optionalItemDescriptor.pullReadFrom(readerMock);

        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        InOrder inOrder = inOrder(
                readerMock,
                itemEntityMock,
                itemDescriptorMock,
                pullReadIteratorMock,
                fieldReferenceMock,
                streamReadHandlerMock);

        inOrder.verify(itemDescriptorMock).getName();
        inOrder.verify(fieldReferenceMock).getFieldValue();
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(optionalItemDescriptor);
        inOrder.verify(itemDescriptorMock).pullReadFrom(readerMock);
        inOrder.verify(pullReadIteratorMock).hasNextEvent();
        inOrder.verify(pullReadIteratorMock).nextEvent(streamReadHandlerMock);
        inOrder.verify(pullReadIteratorMock).hasNextEvent();
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(optionalItemDescriptor);

        verifyNoMoreInteractions(
                readerMock,
                itemEntityMock,
                itemDescriptorMock,
                pullReadIteratorMock,
                fieldReferenceMock,
                streamReadHandlerMock);
    }

    @Test
    void Write_entity_without_target_item() {
        Writer writerMock = mock(Writer.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        
        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        OptionalItemEntity itemEntity = optionalItemDescriptor.createItemEntity();
        itemEntity.writeTo(writerMock);

        verify(itemDescriptorMock).getName();

        verifyNoMoreInteractions(itemDescriptorMock);
        
        verifyZeroInteractions(writerMock);
    }

    @Test
    void Write_entity_with_target_item() {
        Writer writerMock = mock(Writer.class);
        
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);
        when(itemDescriptorMock.getName()).thenReturn("mock");
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        
        OptionalItemDescriptor optionalItemDescriptor = OptionalItemDescriptor.newInstance(itemDescriptorMock).build();

        OptionalItemEntity itemEntity = optionalItemDescriptor.createItemEntity();
        itemEntity.newTargetItem();
        itemEntity.writeTo(writerMock);

        verify(itemDescriptorMock).getName();
        verify(itemDescriptorMock).createItemEntity();

        verify(itemEntityMock).getDescriptor();
        verify(itemEntityMock).writeTo(writerMock);

        verifyNoMoreInteractions(itemEntityMock, itemDescriptorMock);
        
        verifyZeroInteractions(writerMock);
    }
}
