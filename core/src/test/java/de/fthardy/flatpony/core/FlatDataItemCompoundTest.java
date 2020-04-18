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
package de.fthardy.flatpony.core;

import de.fthardy.flatpony.core.field.constant.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeField;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import de.fthardy.flatpony.core.field.observable.ObservableFieldDescriptor;
import de.fthardy.flatpony.core.field.typed.converter.IntegerFieldValueConverter;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.structure.composite.CompositeItemDescriptor;
import de.fthardy.flatpony.core.structure.optional.OptionalItemDescriptor;
import de.fthardy.flatpony.core.structure.sequence.SequenceItemDescriptor;
import de.fthardy.flatpony.core.util.FieldReference;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class FlatDataItemCompoundTest {
    
    @Test
    void Pull_read_an_optional_without_flag_field() {
        FixedSizeFieldDescriptor fieldDescriptor = 
                FixedSizeFieldDescriptor.newInstance("field").withFieldSize(5).build();
        
        CompositeItemDescriptor compositeDescriptor = CompositeItemDescriptor.newInstance("record")
                .addComponentItemDescriptor(fieldDescriptor).build();

        OptionalItemDescriptor optionalDescriptor = OptionalItemDescriptor.newInstance(compositeDescriptor).build();
        
        Reader reader = new StringReader("1234567890");
        
        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        PullReadIterator pullReadIterator = optionalDescriptor.pullReadFrom(reader);
        
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
        
        verify(streamReadHandlerMock).onStructureItemStart(optionalDescriptor);
        verify(streamReadHandlerMock).onStructureItemStart(compositeDescriptor);
        verify(streamReadHandlerMock).onFieldItem(fieldDescriptor, "12345");
        verify(streamReadHandlerMock).onStructureItemEnd(compositeDescriptor);
        verify(streamReadHandlerMock).onStructureItemEnd(optionalDescriptor);
        
        verifyNoMoreInteractions(streamReadHandlerMock);
    }
    
    @Test
    void Pull_read_sequence_with_field_elements() {
        FixedSizeFieldDescriptor fieldDescriptor = FixedSizeFieldDescriptor.newInstance("field")
                .withFieldSize(10).build();
        
        SequenceItemDescriptor sequenceDescriptor = SequenceItemDescriptor.newInstance("sequence")
                .withElementItemDescriptor(fieldDescriptor).build();
        
        Reader reader = new StringReader("f1        f2        f3        f4");

        PullReadIterator pullReadIterator = sequenceDescriptor.pullReadFrom(reader);
        
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
        assertFalse(pullReadIterator.hasNextEvent());
        
        verify(streamReadHandlerMock).onStructureItemStart(sequenceDescriptor);
        verify(streamReadHandlerMock).onFieldItem(fieldDescriptor, "f1");
        verify(streamReadHandlerMock).onFieldItem(fieldDescriptor, "f2");
        verify(streamReadHandlerMock).onFieldItem(fieldDescriptor, "f3");
        verify(streamReadHandlerMock).onStructureItemEnd(sequenceDescriptor);
        
        verifyNoMoreInteractions(streamReadHandlerMock);
    }

    @Test
    void Pull_read_sequence_without_count_field() {

        ConstantFieldDescriptor constant2 = ConstantFieldDescriptor.newInstance("constant2").withConstant("SUB").build();
        FixedSizeFieldDescriptor field2 = FixedSizeFieldDescriptor.newInstance("field2").withFieldSize(6).build();

        CompositeItemDescriptor composite2 = CompositeItemDescriptor.newInstance("sub")
                .addComponentItemDescriptor(constant2)
                .addComponentItemDescriptor(field2)
                .build();

        ConstantFieldDescriptor constant1 = ConstantFieldDescriptor.newInstance("constant1").withConstant("TOP").build();
        FixedSizeFieldDescriptor field1 = FixedSizeFieldDescriptor.newInstance("field1").withFieldSize(6).build();

        CompositeItemDescriptor composite1 = CompositeItemDescriptor.newInstance("element")
                .addComponentItemDescriptor(constant1)
                .addComponentItemDescriptor(field1)
                .addComponentItemDescriptor(composite2)
                .build();

        SequenceItemDescriptor sequence = SequenceItemDescriptor.newInstance("sequence")
                .withElementItemDescriptor(composite1)
                .build();

        Reader reader = new StringReader("TOPfield1SUBfield2TOPfield3SUBfield4");

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        PullReadIterator pullReadIterator = sequence.pullReadFrom(reader);

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
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertTrue(pullReadIterator.hasNextEvent());
        pullReadIterator.nextEvent(streamReadHandlerMock);
        assertFalse(pullReadIterator.hasNextEvent());

        InOrder inOrder = inOrder(streamReadHandlerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(sequence);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite1);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant1, "TOP");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field1, "field1");
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite2);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant2, "SUB");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field2, "field2");
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite2);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite1);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite1);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant1, "TOP");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field1, "field3");
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite2);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant2, "SUB");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field2, "field4");
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite2);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite1);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(sequence);

        verifyNoMoreInteractions(streamReadHandlerMock);
    }

    @Test
    void Pull_read_sequence_with_count_field() {

        ObservableFieldDescriptor countField = ObservableFieldDescriptor.newInstance(
                FixedSizeFieldDescriptor.newInstance("counter").withFieldSize(1).build()).build();

        ConstantFieldDescriptor constant2 = ConstantFieldDescriptor.newInstance("constant2").withConstant("SUB").build();
        FixedSizeFieldDescriptor field2 = FixedSizeFieldDescriptor.newInstance("field2").withFieldSize(6).build();

        CompositeItemDescriptor composite2 = CompositeItemDescriptor.newInstance("sub")
                .addComponentItemDescriptor(constant2)
                .addComponentItemDescriptor(field2)
                .build();

        ConstantFieldDescriptor constant1 = ConstantFieldDescriptor.newInstance("constant1").withConstant("TOP").build();
        FixedSizeFieldDescriptor field1 = FixedSizeFieldDescriptor.newInstance("field1").withFieldSize(6).build();

        CompositeItemDescriptor composite1 = CompositeItemDescriptor.newInstance("element")
                .addComponentItemDescriptor(constant1)
                .addComponentItemDescriptor(field1)
                .addComponentItemDescriptor(composite2)
                .build();

        SequenceItemDescriptor sequence = SequenceItemDescriptor.newInstance("sequence")
                .withElementItemDescriptor(composite1)
                .withCountFieldReference(FieldReference.<Integer>newInstance(countField)
                        .usingValueConverter(new IntegerFieldValueConverter()).build())
                .build();

        CompositeItemDescriptor record = CompositeItemDescriptor.newInstance("record")
                .addComponentItemDescriptor(countField).addComponentItemDescriptor(sequence).build();

        Reader reader = new StringReader("2TOPfield1SUBfield2TOPfield3SUBfield4");

        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        PullReadIterator pullReadIterator = record.pullReadFrom(reader);

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

        InOrder inOrder = inOrder(streamReadHandlerMock);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(record);
        inOrder.verify(streamReadHandlerMock).onFieldItem(countField, "2");
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(sequence);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite1);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant1, "TOP");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field1, "field1");
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite2);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant2, "SUB");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field2, "field2");
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite2);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite1);
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite1);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant1, "TOP");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field1, "field3");
        inOrder.verify(streamReadHandlerMock).onStructureItemStart(composite2);
        inOrder.verify(streamReadHandlerMock).onFieldItem(constant2, "SUB");
        inOrder.verify(streamReadHandlerMock).onFieldItem(field2, "field4");
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite2);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(composite1);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(sequence);
        inOrder.verify(streamReadHandlerMock).onStructureItemEnd(record);

        verifyNoMoreInteractions(streamReadHandlerMock);
    }
}
