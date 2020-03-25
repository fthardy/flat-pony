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

import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class FieldPullReadIteratorTest {

    @Test
    void Cannot_create_with_null_descriptor() {
        assertThrows(NullPointerException.class, () -> new FieldPullReadIterator<>(null, null));
    }
    
    @Test
    void Cannot_create_with_null_source() {
        assertThrows(NullPointerException.class, () -> new FieldPullReadIterator<>(
                ConstantFieldDescriptor.reservedSpace(42), null));
    }
    
    @Test
    void Iteration_process() {
        ConstantFieldDescriptor descriptor =
                ConstantFieldDescriptor.newInstance("Constant").withConstant("TEST").build();
        
        Reader reader = new StringReader("TESTbla");
        
        FieldPullReadIterator<ConstantFieldDescriptor> iterator = new FieldPullReadIterator<>(descriptor, reader);
        
        assertTrue(iterator.hasNextEvent());
        
        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        
        iterator.nextEvent(streamReadHandlerMock);
        
        assertFalse(iterator.hasNextEvent());
        
        NoSuchElementException ex = 
                assertThrows(NoSuchElementException.class, () -> iterator.nextEvent(streamReadHandlerMock));
        assertThat(ex.getMessage()).isEqualTo(FieldPullReadIterator.MSG_No_pull_read_event(descriptor));
        
        verify(streamReadHandlerMock).onFieldItem(descriptor, "TEST");
        verifyNoMoreInteractions(streamReadHandlerMock);
    }
}