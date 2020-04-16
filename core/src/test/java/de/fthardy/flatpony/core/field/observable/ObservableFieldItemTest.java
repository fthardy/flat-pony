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
package de.fthardy.flatpony.core.field.observable;

import de.fthardy.flatpony.core.FlatDataItemDescriptorHandler;
import de.fthardy.flatpony.core.FlatDataItemEntityHandler;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptorHandler;
import de.fthardy.flatpony.core.field.FlatDataFieldHandler;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ObservableFieldItemTest {
    
    @Test
    void Cannot_create_with_null_field_descriptor() {
        assertThrows(NullPointerException.class, () -> ObservableFieldDescriptor.newInstance(null));
    }
    
    @Test
    void Cannot_create_with_null_observer() {
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptorMock = 
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        
        assertThrows(NullPointerException.class, () ->
                ObservableFieldDescriptor.newInstance(fieldDescriptorMock).addObserver(null));
        ObservableFieldDescriptor.Observer[] observerArray = null;
        assertThrows(NullPointerException.class, () ->
                ObservableFieldDescriptor.newInstance(fieldDescriptorMock).addObservers(observerArray));
        Iterable<ObservableFieldDescriptor.Observer> observerIterable = null;
        assertThrows(NullPointerException.class, () ->
                ObservableFieldDescriptor.newInstance(fieldDescriptorMock).addObservers(observerIterable));
    }
    
    @Test
    void Cannot_add_same_observer_twice() {
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>>fieldDescriptorMock =
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        
        ObservableFieldDescriptor.Observer observerMock = mock(ObservableFieldDescriptor.Observer.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ObservableFieldDescriptor.newInstance(fieldDescriptorMock)
                        .addObserver(observerMock).addObserver(observerMock));
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(ObservableFieldDescriptor.MSG_OBSERVER_ALREADY_ADDED);
    }
    
    @Test
    void Create_descriptor() {
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptorMock =
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("default");
        when(fieldDescriptorMock.getMinLength()).thenReturn(42);

        ObservableFieldDescriptor.Observer observerMock1 = mock(ObservableFieldDescriptor.Observer.class);
        ObservableFieldDescriptor.Observer observerMock2 = mock(ObservableFieldDescriptor.Observer.class);
        ObservableFieldDescriptor.Observer observerMock3 = mock(ObservableFieldDescriptor.Observer.class);
        ObservableFieldDescriptor.Observer observerMock4 = mock(ObservableFieldDescriptor.Observer.class);
        ObservableFieldDescriptor.Observer observerMock5 = mock(ObservableFieldDescriptor.Observer.class);
        ObservableFieldDescriptor.Observer observerMock6 = mock(ObservableFieldDescriptor.Observer.class);

        ObservableFieldDescriptor descriptor = ObservableFieldDescriptor.newInstance(fieldDescriptorMock)
                .addObserver(observerMock1)
                .addObservers(observerMock2, observerMock3)
                .addObservers(Arrays.asList(observerMock4, observerMock5)).build();
        assertThat(descriptor.toString()).startsWith(ObservableFieldDescriptor.class.getSimpleName());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                descriptor.addObserver(observerMock1));
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(ObservableFieldDescriptor.MSG_OBSERVER_ALREADY_ADDED);

        descriptor.addObserver(observerMock6);
        
        assertThat(descriptor.getName()).isEqualTo("mock");
        assertThat(descriptor.getDefaultValue()).isEqualTo("default");
        assertThat(descriptor.getMinLength()).isEqualTo(42);
        assertThat(descriptor.getObservedFieldDescriptor()).isSameAs(fieldDescriptorMock);
        assertThat(descriptor.getObservers()).containsExactly(
                observerMock1, observerMock2, observerMock3, observerMock4, observerMock5, observerMock6);
        
        descriptor.removeObserver(observerMock4);
        assertThat(descriptor.getObservers()).containsExactly(
                observerMock1, observerMock2, observerMock3, observerMock5, observerMock6);
        
        descriptor.removeObserver(observerMock6);
        assertThat(descriptor.getObservers()).containsExactly(
                observerMock1, observerMock2, observerMock3, observerMock5);
        
        descriptor.removeObserver(observerMock1);
        assertThat(descriptor.getObservers()).containsExactly(observerMock2, observerMock3, observerMock5);
        
        descriptor.removeObserver(observerMock3);
        assertThat(descriptor.getObservers()).containsExactly(observerMock2, observerMock5);
        
        descriptor.removeObserver(observerMock5);
        assertThat(descriptor.getObservers()).containsExactly(observerMock2);
        
        descriptor.removeObserver(observerMock2);
        assertThat(descriptor.getObservers()).isEmpty();
        
        verify(fieldDescriptorMock, times(2)).getName();
        verify(fieldDescriptorMock).getMinLength();
        verify(fieldDescriptorMock).getDefaultValue();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verifyZeroInteractions(observerMock1, observerMock2, observerMock3, observerMock4, observerMock5, observerMock6);
    }
    
    @Test
    void Create_field() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("value");
        
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptorMock =
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        when(fieldDescriptorMock.createItemEntity()).thenAnswer(i -> fieldMock);
        
        ObservableFieldDescriptor.Observer observerMock1 = mock(ObservableFieldDescriptor.Observer.class);
        ObservableFieldDescriptor.Observer observerMock2 = mock(ObservableFieldDescriptor.Observer.class);

        ObservableFieldDescriptor descriptor = ObservableFieldDescriptor.newInstance(fieldDescriptorMock)
                .addObservers(observerMock1, observerMock2).build();
        
        ObservableField field = descriptor.createItemEntity();
        assertThat(field.toString()).startsWith(ObservableField.class.getSimpleName());
        assertThat(field.getDescriptor()).isSameAs(descriptor);
        assertThat(field.getLength()).isEqualTo(42);
        assertThat(field.getObservedField()).isSameAs(fieldMock);
        assertThat(field.getValue()).isEqualTo("value");
        assertThat(field.asMutableField()).isSameAs(field);
        
        Writer writer = new StringWriter();
        field.writeTo(writer);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).createItemEntity();

        InOrder inOrder = inOrder(observerMock1, observerMock2);
        inOrder.verify(observerMock1).onFieldEntityCreated(field);
        inOrder.verify(observerMock2).onFieldEntityCreated(field);
        
        verify(fieldMock).getLength();
        verify(fieldMock).getValue();
        verify(fieldMock).writeTo(writer);
        
        verifyNoMoreInteractions(fieldMock, fieldDescriptorMock, observerMock1, observerMock2);
    }
    
    @Test
    void Observe_field_and_change_value() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getValue()).thenReturn("value");

        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptorMock =
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        when(fieldDescriptorMock.createItemEntity()).thenAnswer(i -> fieldMock);

        ObservableFieldDescriptor descriptor = ObservableFieldDescriptor.newInstance(fieldDescriptorMock).build();

        ObservableField.Observer observerMock1 = mock(ObservableField.Observer.class);
        ObservableField.Observer observerMock2 = mock(ObservableField.Observer.class);

        ObservableField field = descriptor.createItemEntity();
        field.addObserver(observerMock1);
        field.addObserver(observerMock2);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                field.addObserver(observerMock1));
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).isEqualTo(ObservableFieldDescriptor.MSG_OBSERVER_ALREADY_ADDED);
        
        field.setValue("new value");

        assertThat(field.getObservers()).containsExactly(observerMock1, observerMock2);
        field.removeObserver(observerMock1);

        assertThat(field.getObservers()).containsExactly(observerMock2);
        field.removeObserver(observerMock2);
        assertThat(field.getObservers()).isEmpty();
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).createItemEntity();
        
        InOrder inOrder = inOrder(observerMock1, observerMock2, fieldMock);
        inOrder.verify(fieldMock).getValue();
        inOrder.verify(fieldMock).setValue("new value");
        inOrder.verify(observerMock1).onValueChange(field, "value", "new value");
        inOrder.verify(observerMock2).onValueChange(field, "value", "new value");
        
        verifyNoMoreInteractions(fieldMock, fieldDescriptorMock, observerMock1, observerMock2);
    }

    @Test
    void Apply_handler_to_descriptor() {
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>>fieldDescriptorMock =
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        
        ObservableFieldDescriptor descriptor = ObservableFieldDescriptor.newInstance(fieldDescriptorMock).build();

        FlatDataItemDescriptorHandler handlerMock = mock(FlatDataItemDescriptorHandler.class);
        FlatDataFieldDescriptorHandler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptorHandler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(fieldDescriptorMock).getName();
        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verify(fieldDescriptorHandlerMock).handleObservableFieldDescriptor(descriptor);
        
        verifyNoMoreInteractions(fieldDescriptorMock, handlerMock, fieldDescriptorHandlerMock);
    }

    @Test
    void Apply_handler_to_field() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>>fieldDescriptorMock =
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        when(fieldDescriptorMock.createItemEntity()).thenAnswer(i -> fieldMock);
        
        ObservableField field = ObservableFieldDescriptor.newInstance(fieldDescriptorMock).build().createItemEntity();

        FlatDataItemEntityHandler handlerMock = mock(FlatDataItemEntityHandler.class);
        FlatDataFieldHandler fieldHandlerMock = mock(FlatDataFieldHandler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        verify(fieldHandlerMock).handleObservableField(field);
        verifyNoMoreInteractions(handlerMock, fieldHandlerMock);
    }

    @Test
    void Read_field() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("value");

        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptorMock =
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        Reader reader = new StringReader("dummy");
        when(fieldDescriptorMock.readItemEntityFrom(reader)).thenAnswer(i -> fieldMock);

        ObservableFieldDescriptor.Observer observerMock1 = mock(ObservableFieldDescriptor.Observer.class);
        ObservableFieldDescriptor.Observer observerMock2 = mock(ObservableFieldDescriptor.Observer.class);

        ObservableFieldDescriptor descriptor = ObservableFieldDescriptor.newInstance(fieldDescriptorMock)
                .addObservers(observerMock1, observerMock2).build();
        
        ObservableField field = descriptor.readItemEntityFrom(reader);
        assertThat(field.getDescriptor()).isSameAs(descriptor);

        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).readItemEntityFrom(reader);

        InOrder inOrder = inOrder(observerMock1, observerMock2);
        inOrder.verify(observerMock1).onFieldEntityRead(field);
        inOrder.verify(observerMock2).onFieldEntityRead(field);
        
        verifyNoMoreInteractions(fieldMock, fieldDescriptorMock, observerMock1, observerMock2);
    }

    @Test
    void Push_read() {
        Reader readerMock = mock(Reader.class);
        
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptorMock = 
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("mock");
        when(fieldDescriptorMock.readValue(readerMock)).thenReturn("value");

        ObservableFieldDescriptor.Observer observerMock = mock(ObservableFieldDescriptor.Observer.class);
        
        ObservableFieldDescriptor descriptor = ObservableFieldDescriptor.newInstance(fieldDescriptorMock)
                .addObserver(observerMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        descriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).readValue(readerMock);
        verify(streamReadHandlerMock).onFieldItem(descriptor, "value");
        verify(observerMock).onFieldValueRead(descriptor, "value");
        
        verifyNoMoreInteractions(fieldDescriptorMock, observerMock);

        verifyZeroInteractions(readerMock, streamReadHandlerMock);
    }

    @Test
    void Pull_read() {
        Reader readerMock = mock(Reader.class);
        
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptorMock = 
                mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.readValue(readerMock)).thenReturn("value");
        
        ObservableFieldDescriptor.Observer observerMock = mock(ObservableFieldDescriptor.Observer.class);

        ObservableFieldDescriptor descriptor = ObservableFieldDescriptor.newInstance(fieldDescriptorMock)
                .addObserver(observerMock).build();

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        PullReadIterator pullReadIterator = descriptor.pullReadFrom(readerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);

        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).readValue(readerMock);
        verify(streamReadHandlerMock).onFieldItem(descriptor, "value");
        verify(observerMock).onFieldValueRead(descriptor, "value");
        
        verifyNoMoreInteractions(fieldDescriptorMock, streamReadHandlerMock, observerMock);
        
        verifyZeroInteractions(readerMock);
    }
}
