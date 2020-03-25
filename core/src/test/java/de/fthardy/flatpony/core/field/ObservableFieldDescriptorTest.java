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
package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.Reader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObservableFieldDescriptorTest {

    @Test
    void Cannot_create_with_null_descriptor() {
        assertThrows(NullPointerException.class, () -> new ObservableFieldDescriptor(null));
    }
    
    @Test
    void Most_stuff_is_delegated() {
        FlatDataFieldDescriptor<? extends FlatDataField<?>> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        
        ObservableFieldDescriptor descriptor = new ObservableFieldDescriptor(fieldDescriptorMock);
        
        descriptor.getName();
        descriptor.getDefaultValue();
        descriptor.getMinLength();
        
        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        descriptor.applyHandler(handlerMock);

        Reader readerMock = mock(Reader.class);
        
        descriptor.readValue(readerMock);
        
        descriptor.pullReadFrom(readerMock);
        
        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        descriptor.pushReadFrom(readerMock, streamReadHandlerMock);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).getMinLength();
        
        verify(fieldDescriptorMock).applyHandler(handlerMock);
        
        verify(fieldDescriptorMock).readValue(readerMock);
        
        verify(fieldDescriptorMock).pullReadFrom(readerMock);
        
        verify(fieldDescriptorMock).pushReadFrom(readerMock, streamReadHandlerMock);
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verifyZeroInteractions(handlerMock);
        verifyZeroInteractions(readerMock);
        verifyZeroInteractions(streamReadHandlerMock);
    }

    @Test
    void Observer_is_notified_on_create() {
        FlatDataFieldDescriptor<? extends FlatDataField<?>> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);

        ObservableFieldDescriptor descriptor = new ObservableFieldDescriptor(fieldDescriptorMock);
        
        ObservableFieldDescriptor.Observer observerMock = mock(ObservableFieldDescriptor.Observer.class);
        descriptor.addObserver(observerMock);
        
        FlatDataField<?> fieldMock = mock(FlatDataField.class);
        when(fieldDescriptorMock.createItemEntity()).thenAnswer(invocation -> fieldMock);
        
        descriptor.createItemEntity();

        InOrder inOrder = inOrder(fieldDescriptorMock, observerMock);
        
        inOrder.verify(fieldDescriptorMock).createItemEntity();
        inOrder.verify(observerMock).onFieldEntityCreated(fieldMock);
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        verifyNoMoreInteractions(observerMock);
        
        verifyZeroInteractions(fieldMock);
    }

    @Test
    void Observer_is_notified_on_read() {
        FlatDataFieldDescriptor<? extends FlatDataField<?>> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);

        ObservableFieldDescriptor descriptor = new ObservableFieldDescriptor(fieldDescriptorMock);
        
        ObservableFieldDescriptor.Observer observerMock = mock(ObservableFieldDescriptor.Observer.class);
        descriptor.addObserver(observerMock);
        
        Reader readerMock = mock(Reader.class);
        FlatDataField<?> fieldMock = mock(FlatDataField.class);
        when(fieldDescriptorMock.readItemEntityFrom(readerMock)).thenAnswer(invocation -> fieldMock);
        
        descriptor.readItemEntityFrom(readerMock);

        InOrder inOrder = inOrder(fieldDescriptorMock, observerMock);
        
        inOrder.verify(fieldDescriptorMock).readItemEntityFrom(readerMock);
        inOrder.verify(observerMock).onFieldEntityRead(fieldMock);
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        verifyNoMoreInteractions(observerMock);
        
        verifyZeroInteractions(fieldMock);
        verifyZeroInteractions(readerMock);
    }
}