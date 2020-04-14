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
package de.fthardy.flatpony.core.structure.sequence;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.PushReadItemEntityTreeWalker;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.structure.FlatDataStructure;
import de.fthardy.flatpony.core.structure.FlatDataStructureDescriptor;
import de.fthardy.flatpony.core.structure.sequence.SequenceItemDescriptor;
import de.fthardy.flatpony.core.structure.sequence.SequenceItemEntity;
import de.fthardy.flatpony.core.util.FieldReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SequenceItemTest {

    @Test
    void Cannot_set_element_item_descriptor_to_null() {
        assertThrows(NullPointerException.class, () ->
                SequenceItemDescriptor.newInstance("Sequence").withElementItemDescriptor(null));
    }

    @Test
    void Cannot_set_count_field_reference_to_null() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        assertThrows(NullPointerException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(null));
    }

    @Test
    void Cannot_set_multiplicity_of_0_to_0() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        assertThrows(IllegalArgumentException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withMultiplicity(0, 0));
    }

    @Test
    void Cannot_set_a_negative_bound1_in_multiplicity() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        assertThrows(IllegalArgumentException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withMultiplicity(-1, 1));
    }

    @Test
    void Cannot_set_a_negative_bound2_in_multiplicity() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        assertThrows(IllegalArgumentException.class, () -> SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withMultiplicity(1, -1));
    }
    
    @Test
    void Create_descriptor_without_count_field_reference_and_without_multiplicity() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();
        assertThat(sequenceItemDescriptor.getName()).isEqualTo("Sequence");
        assertThat(sequenceItemDescriptor.getMinLength()).isEqualTo(0);
        assertThat(sequenceItemDescriptor.getElementItemDescriptor()).isSameAs(itemDescriptorMock);
        
        verify(itemDescriptorMock).getMinLength();
        
        verifyNoMoreInteractions(itemDescriptorMock);
    }
    
    @Test
    void Create_descriptor_without_count_field_reference_but_with_multiplicity() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withMultiplicity(1, 10).build();
        assertThat(sequenceItemDescriptor.getName()).isEqualTo("Sequence");
        assertThat(sequenceItemDescriptor.getMinLength()).isEqualTo(42);
        assertThat(sequenceItemDescriptor.getElementItemDescriptor()).isSameAs(itemDescriptorMock);
        
        verify(itemDescriptorMock).getMinLength();
        
        verifyNoMoreInteractions(itemDescriptorMock);
    }
    
    @Test
    void Create_descriptor_with_count_field_reference_and_with_multiplicity() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);

        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock).withMultiplicity(2, 10).build();
        assertThat(sequenceItemDescriptor.getName()).isEqualTo("Sequence");
        assertThat(sequenceItemDescriptor.getMinLength()).isEqualTo(84);
        assertThat(sequenceItemDescriptor.getElementItemDescriptor()).isSameAs(itemDescriptorMock);
        
        verify(itemDescriptorMock).getMinLength();
        
        verifyNoMoreInteractions(itemDescriptorMock);
    }
    
    @Test
    void Create_entity_without_count_field_reference_and_without_multiplicity() {
        FlatDataItemEntity<?> itemEntityMock1 = mock(FlatDataItemEntity.class);
        when(itemEntityMock1.getLength()).thenReturn(42);
        FlatDataItemEntity<?> itemEntityMock2 = mock(FlatDataItemEntity.class);
        when(itemEntityMock2.getLength()).thenReturn(42);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock1).thenAnswer(i -> itemEntityMock2);
        when(itemEntityMock1.getDescriptor()).thenAnswer(i -> itemDescriptorMock);
        when(itemEntityMock2.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.createItemEntity();
        assertThat(sequenceItemEntity.getLength()).isEqualTo(0);
        assertThat(sequenceItemEntity.getElementItemEntities()).isEmpty();

        assertThat(sequenceItemEntity.createAndAddNewElementItemEntity()).isSameAs(itemEntityMock1);
        assertThat(sequenceItemEntity.createAndAddNewElementItemEntity()).isSameAs(itemEntityMock2);
        assertThat(sequenceItemEntity.getLength()).isEqualTo(84);
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock1, itemEntityMock2);
        
        sequenceItemEntity.discardElementItemEntity(itemEntityMock2);
        assertThat(sequenceItemEntity.getLength()).isEqualTo(42);
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock1);
        
        sequenceItemEntity.discardAllElementItemEntities();
        assertThat(sequenceItemEntity.getLength()).isEqualTo(0);
        assertThat(sequenceItemEntity.getElementItemEntities()).isEmpty();
        
        verify(itemDescriptorMock, times(2)).createItemEntity();
        
        verify(itemEntityMock1).getDescriptor();
        verify(itemEntityMock1, times(2)).getLength();
        
        verify(itemEntityMock2).getDescriptor();
        verify(itemEntityMock2).getLength();
        
        verifyNoMoreInteractions(itemEntityMock1, itemEntityMock2, itemDescriptorMock);
    }

    @Test
    void Create_entity_without_count_field_reference_but_with_multiplicity() {
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        when(itemEntityMock.getLength()).thenReturn(42);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withMultiplicity(1, 10).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.createItemEntity();
        assertThat(sequenceItemEntity.getLength()).isEqualTo(42);
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock);
        
        assertThrows(NullPointerException.class, () -> sequenceItemEntity.addElementItemEntity(null));
        
        assertThrows(IllegalArgumentException.class, () -> sequenceItemEntity.addElementItemEntity(itemEntityMock));

        assertThrows(IllegalArgumentException.class, () -> sequenceItemEntity.discardElementItemEntity(null));

        verify(itemEntityMock).getLength();
        
        verify(itemDescriptorMock).createItemEntity();

        verifyNoMoreInteractions(itemEntityMock, itemDescriptorMock);
    }

    @Test
    void Create_entity_with_count_field_reference_and_with_multiplicity__No_count_field_available() {
        FlatDataItemEntity<?> itemEntityMock1 = mock(FlatDataItemEntity.class);
        FlatDataItemEntity<?> itemEntityMock2 = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock1).thenAnswer(i -> itemEntityMock2);

        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock)
                .withMultiplicity(2, 10).build();
        
        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.createItemEntity();
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock1, itemEntityMock2);
        
        verify(itemDescriptorMock, times(2)).createItemEntity();
        
        verify(fieldReferenceMock).getReferencedField();

        verifyNoMoreInteractions(itemDescriptorMock, fieldReferenceMock);
        
        verifyZeroInteractions(itemEntityMock1, itemEntityMock2);
    }

    @Test
    void Create_entity_with_count_field_reference_and_with_multiplicity__Count_field_available__Invalid_count() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);

        FieldReference.ReferencedField<Integer> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        when(referencedFieldMock.getValue()).thenReturn(42);
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(referencedFieldMock);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock)
                .withMultiplicity(2, 10).build();

        FlatDataReadException exception = 
                assertThrows(FlatDataReadException.class, sequenceItemDescriptor::createItemEntity);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(SequenceItemDescriptor.MSG_Multiplicity_constraint_violated(
                "Sequence", sequenceItemDescriptor.getMultiplicity()));
        
        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock).getValue();

        verifyNoMoreInteractions(fieldReferenceMock, referencedFieldMock);
        
        verifyZeroInteractions(itemDescriptorMock);
    }

    @Test
    void Create_entity_with_count_field_reference_and_with_multiplicity__Count_field_available__Valid_count() {
        FlatDataItemEntity<?> itemEntityMock1 = mock(FlatDataItemEntity.class);
        FlatDataItemEntity<?> itemEntityMock2 = mock(FlatDataItemEntity.class);
        FlatDataItemEntity<?> itemEntityMock3 = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.createItemEntity())
                .thenAnswer(i -> itemEntityMock1)
                .thenAnswer(i -> itemEntityMock2)
                .thenAnswer(i -> itemEntityMock3);
        
        when(itemEntityMock3.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        FieldReference.ReferencedField<Integer> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        when(referencedFieldMock.getValue()).thenReturn(2);
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(referencedFieldMock);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock)
                .withMultiplicity(2, 10).build();
        
        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.createItemEntity();
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock1, itemEntityMock2);

        sequenceItemEntity.createAndAddNewElementItemEntity();
        
        verify(itemDescriptorMock, times(3)).createItemEntity();
        
        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock, times(2)).getValue();
        verify(referencedFieldMock).setValue(3);
        
        verify(itemEntityMock3).getDescriptor();

        verifyNoMoreInteractions(itemDescriptorMock, fieldReferenceMock, referencedFieldMock, itemEntityMock3);
        
        verifyZeroInteractions(itemEntityMock1, itemEntityMock2);
    }

    @Test
    void Apply_handler_to_descriptor() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        
        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataStructureDescriptor.Handler descriptorHandlerMock = mock(FlatDataStructureDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        
        verify(descriptorHandlerMock).handleSequenceItemDescriptor(descriptor);
        
        verifyNoMoreInteractions(handlerMock, descriptorHandlerMock);
        
        verifyZeroInteractions(itemDescriptorMock);
    }

    @Test
    void Apply_handler_to_entity() {
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);

        SequenceItemDescriptor descriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        SequenceItemEntity item = descriptor.createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        item.applyHandler(handlerMock);
        item.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(item);
        
        verify(structureHandlerMock).handleSequenceItemEntity(item);
        
        verifyNoMoreInteractions(handlerMock, structureHandlerMock);
        
        verifyZeroInteractions(itemDescriptorMock);
    }
    
    @Test
    void Read_entity_without_count_field_reference__No_elements_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.readItemEntityFrom(readerMock);
        assertThat(sequenceItemEntity.getLength()).isEqualTo(0);
        assertThat(sequenceItemEntity.getElementItemEntities()).isEmpty();
        
        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        
        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }

    @Test
    void Read_entity_without_count_field_reference__Two_elements_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemEntity<?> itemEntityMock1 = mock(FlatDataItemEntity.class);
        FlatDataItemEntity<?> itemEntityMock2 = mock(FlatDataItemEntity.class);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock))
                .thenAnswer(i -> itemEntityMock1).thenAnswer(i -> itemEntityMock2).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.readItemEntityFrom(readerMock);
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock1, itemEntityMock2);

        verify(readerMock, times(3)).markSupported();
        verify(readerMock, times(3)).mark(42);

        verify(itemDescriptorMock, times(3)).getMinLength();
        verify(itemDescriptorMock, times(3)).readItemEntityFrom(readerMock);

        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }

    @Test
    void Read_entity_without_count_field_reference__Mark_not_supported() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(false);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                sequenceItemDescriptor.readItemEntityFrom(readerMock));
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(SequenceItemDescriptor.MSG_Mark_not_supported("Sequence"));

        verify(readerMock).markSupported();
        
        verifyNoMoreInteractions(readerMock);
        
        verifyZeroInteractions(itemDescriptorMock);
    }

    @Test
    void Read_entity_without_count_field_reference__Mark_stream_fails() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        IOException ioException = new IOException("TEST");
        doThrow(ioException).when(readerMock).mark(anyInt());

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                sequenceItemDescriptor.readItemEntityFrom(readerMock));
        assertThat(exception.getCause()).isSameAs(ioException);
        assertThat(exception.getMessage()).isEqualTo(
                SequenceItemDescriptor.MSG_Failed_to_mark_stream("Sequence"));

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        
        verify(itemDescriptorMock).getMinLength();
        
        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }

    @Test
    void Read_entity_without_count_field_reference__Read_element_fails() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        FlatDataReadException ex = new FlatDataReadException("TEST");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenThrow(ex);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();
        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.readItemEntityFrom(readerMock);
        assertThat(sequenceItemEntity.getLength()).isEqualTo(0);
        assertThat(sequenceItemEntity.getElementItemEntities()).isEmpty();

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        verify(readerMock).reset();
        
        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        
        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }

    @Test
    void Read_entity_without_count_field_reference__Read_element_fails__Reset_fails() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        IOException ioException = new IOException("TEST");
        doThrow(ioException).when(readerMock).reset();

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        FlatDataReadException ex = new FlatDataReadException("TEST");
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenThrow(ex);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();
        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                sequenceItemDescriptor.readItemEntityFrom(readerMock));
        assertThat(exception.getCause()).isSameAs(ioException);
        assertThat(exception.getMessage()).isEqualTo(
                SequenceItemDescriptor.MSG_Failed_to_reset_stream("Sequence"));

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);
        verify(readerMock).reset();

        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verifyNoMoreInteractions(readerMock, itemDescriptorMock);
    }

    @Test
    void Read_entity_with_count_field_reference__No_count_field_available__One_element_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock).thenReturn(null);
        
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.readItemEntityFrom(readerMock);
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock);

        verify(readerMock, times(2)).markSupported();
        verify(readerMock, times(2)).mark(42);

        verify(itemDescriptorMock, times(2)).getMinLength();
        verify(itemDescriptorMock, times(2)).readItemEntityFrom(readerMock);
        
        verify(fieldReferenceMock).getReferencedField();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, fieldReferenceMock);
        
        verifyZeroInteractions(itemEntityMock);
    }

    @Test
    void Read_entity_with_count_field_reference__Count_field_with_one() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock);
        
        FieldReference.ReferencedField<Integer> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        when(referencedFieldMock.getValue()).thenReturn(1);
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenReturn(referencedFieldMock);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.readItemEntityFrom(readerMock);
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock);

        verify(itemDescriptorMock).readItemEntityFrom(readerMock);
        
        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock, times(2)).getValue();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, fieldReferenceMock, referencedFieldMock);
        
        verifyZeroInteractions(itemEntityMock);
    }

    @Test
    void Push_read_without_count_field_reference__One_element_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        sequenceItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        verify(readerMock, times(2)).markSupported();
        verify(readerMock, times(2)).mark(42);

        verify(itemDescriptorMock, times(2)).getMinLength();
        verify(itemDescriptorMock, times(2)).readItemEntityFrom(readerMock);
        
        verify(streamReadHandlerMock).onStructureItemStart(sequenceItemDescriptor);
        verify(streamReadHandlerMock).onStructureItemEnd(sequenceItemDescriptor);
        
        verify(itemEntityMock).applyHandler(any(PushReadItemEntityTreeWalker.class));

        verifyNoMoreInteractions(readerMock, itemEntityMock, itemDescriptorMock, streamReadHandlerMock);
    }

    @Test
    void Push_read_with_count_field_reference__No_count_field_value_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);
        
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(i -> itemEntityMock).thenReturn(null);
        
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        sequenceItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        verify(readerMock, times(2)).markSupported();
        verify(readerMock, times(2)).mark(42);

        verify(itemDescriptorMock, times(2)).getMinLength();
        verify(itemDescriptorMock, times(2)).readItemEntityFrom(readerMock);
        
        verify(streamReadHandlerMock).onStructureItemStart(sequenceItemDescriptor);
        verify(streamReadHandlerMock).onStructureItemEnd(sequenceItemDescriptor);
        
        verify(itemEntityMock).applyHandler(any(PushReadItemEntityTreeWalker.class));
        
        verify(fieldReferenceMock).getFieldValue();

        verifyNoMoreInteractions(
                readerMock, itemEntityMock, itemDescriptorMock, streamReadHandlerMock, fieldReferenceMock);
    }

    @Test
    void Push_read_with_count_field_reference__Count_field_value_is_1() throws IOException {
        Reader readerMock = mock(Reader.class);
        
        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(1);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        sequenceItemDescriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        verify(itemDescriptorMock).pushReadFrom(readerMock, streamReadHandlerMock);
        
        verify(streamReadHandlerMock).onStructureItemStart(sequenceItemDescriptor);
        verify(streamReadHandlerMock).onStructureItemEnd(sequenceItemDescriptor);
        
        verify(fieldReferenceMock).getFieldValue();

        verifyNoMoreInteractions(itemDescriptorMock, streamReadHandlerMock, fieldReferenceMock);
        
        verifyZeroInteractions(readerMock, itemEntityMock);
    }

    @Test
    void Pull_read_without_count_field_reference__No_element_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        PullReadIterator pullReadIterator = sequenceItemDescriptor.pullReadFrom(readerMock);
        
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);

        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verify(streamReadHandlerMock).onStructureItemStart(sequenceItemDescriptor);
        verify(streamReadHandlerMock).onStructureItemEnd(sequenceItemDescriptor);

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, streamReadHandlerMock);
        
    }

    @Test
    void Pull_read_with_count_field_reference__No_count_field_available() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);
        
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(null);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        PullReadIterator pullReadIterator = sequenceItemDescriptor.pullReadFrom(readerMock);
        
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        verify(readerMock).markSupported();
        verify(readerMock).mark(42);

        verify(itemDescriptorMock).getMinLength();
        verify(itemDescriptorMock).readItemEntityFrom(readerMock);

        verify(streamReadHandlerMock).onStructureItemStart(sequenceItemDescriptor);
        verify(streamReadHandlerMock).onStructureItemEnd(sequenceItemDescriptor);
        
        verify(fieldReferenceMock).getFieldValue();

        verifyNoMoreInteractions(readerMock, itemDescriptorMock, streamReadHandlerMock, fieldReferenceMock);
    }

    @Test
    void Pull_read_with_count_field_reference__Count_field_value_is_0() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.markSupported()).thenReturn(true);

        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.getMinLength()).thenReturn(42);
        when(itemDescriptorMock.readItemEntityFrom(readerMock)).thenReturn(null);
        
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getFieldValue()).thenReturn(0);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withCountFieldReference(fieldReferenceMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        PullReadIterator pullReadIterator = sequenceItemDescriptor.pullReadFrom(readerMock);
        
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());
        
        verify(streamReadHandlerMock).onStructureItemStart(sequenceItemDescriptor);
        verify(streamReadHandlerMock).onStructureItemEnd(sequenceItemDescriptor);
        
        verify(fieldReferenceMock).getFieldValue();

        verifyNoMoreInteractions(streamReadHandlerMock, fieldReferenceMock);
        
        verifyZeroInteractions(readerMock, itemDescriptorMock);
    }
    
    @Test
    void Write_entity_without_count_field_reference_and_without_multiplicity() {
        Writer writerMock = mock(Writer.class);
        
        FlatDataItemEntity<?> itemEntityMock1 = mock(FlatDataItemEntity.class);
        FlatDataItemEntity<?> itemEntityMock2 = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock1).thenAnswer(i -> itemEntityMock2);
        when(itemEntityMock1.getDescriptor()).thenAnswer(i -> itemDescriptorMock);
        when(itemEntityMock2.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.createItemEntity();
        assertThat(sequenceItemEntity.getLength()).isEqualTo(0);
        assertThat(sequenceItemEntity.getElementItemEntities()).isEmpty();

        assertThat(sequenceItemEntity.createAndAddNewElementItemEntity()).isSameAs(itemEntityMock1);
        assertThat(sequenceItemEntity.createAndAddNewElementItemEntity()).isSameAs(itemEntityMock2);
        
        sequenceItemEntity.writeTo(writerMock);

        verify(itemDescriptorMock, times(2)).createItemEntity();

        verify(itemEntityMock1).getDescriptor();
        verify(itemEntityMock1).writeTo(writerMock);

        verify(itemEntityMock2).getDescriptor();
        verify(itemEntityMock2).writeTo(writerMock);

        verifyNoMoreInteractions(itemEntityMock1, itemEntityMock2, itemDescriptorMock);
    }

    @Test
    void Write_entity_without_count_field_reference_and_with_multiplicity__Multiplicity_constraint_violated() {
        Writer writerMock = mock(Writer.class);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock).withMultiplicity(1, 3).build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.createItemEntity();
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock);

        sequenceItemEntity.discardAllElementItemEntities();

        FlatDataWriteException ex = assertThrows(FlatDataWriteException.class, () ->
                sequenceItemEntity.writeTo(writerMock));
        assertThat(ex.getCause()).isNull();
        assertThat(ex.getMessage()).isEqualTo(SequenceItemDescriptor.MSG_Multiplicity_constraint_violated(
                "Sequence", sequenceItemDescriptor.getMultiplicity()));

        verify(itemDescriptorMock).createItemEntity();

        verifyNoMoreInteractions(itemDescriptorMock);
        
        verifyZeroInteractions(itemEntityMock);
    }

    @Test
    void Write_entity_with_count_field_reference__Element_count_is_not_equal_to_count_field_value() {
        Writer writerMock = mock(Writer.class);

        FlatDataItemEntity<?> itemEntityMock = mock(FlatDataItemEntity.class);
        FlatDataItemDescriptor<?> itemDescriptorMock = mock(FlatDataItemDescriptor.class);
        when(itemDescriptorMock.createItemEntity()).thenAnswer(i -> itemEntityMock);
        when(itemEntityMock.getDescriptor()).thenAnswer(i -> itemDescriptorMock);
        
        FieldReference.ReferencedField<Integer> referencedFieldMock = mock(FieldReference.ReferencedField.class);
        FieldReference<Integer> fieldReferenceMock = mock(FieldReference.class);
        when(fieldReferenceMock.getReferencedField()).thenAnswer(i -> referencedFieldMock);
        when(referencedFieldMock.getValue()).thenReturn(1, 1, 2);

        SequenceItemDescriptor sequenceItemDescriptor = SequenceItemDescriptor.newInstance("Sequence")
                .withElementItemDescriptor(itemDescriptorMock)
                .withCountFieldReference(fieldReferenceMock)
                .build();

        SequenceItemEntity sequenceItemEntity = sequenceItemDescriptor.createItemEntity();
        assertThat(sequenceItemEntity.getElementItemEntities()).containsExactly(itemEntityMock);

        FlatDataWriteException ex = assertThrows(FlatDataWriteException.class, () ->
                sequenceItemEntity.writeTo(writerMock));
        assertThat(ex.getCause()).isNull();
        assertThat(ex.getMessage()).isEqualTo(SequenceItemEntity.MSG_Element_count_is_not_equal_to_count_field_value(
                "Sequence", 2));

        verify(itemDescriptorMock).createItemEntity();
        
        verify(fieldReferenceMock).getReferencedField();
        
        verify(referencedFieldMock, times(3)).getValue();

        verifyNoMoreInteractions(itemDescriptorMock, fieldReferenceMock, referencedFieldMock);
        
        verifyZeroInteractions(itemEntityMock);
    }
}
