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
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompositeItemTest {

    @Test
    void Calls_correct_handler_method() {
        CompositeItemEntity item = CompositeItemDescriptor.newInstance("Record").addItemDescriptor(
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
    void Length_is_sum_of_all_item_lengths() {
        ConstantFieldDescriptor constantFieldDescriptor = ConstantFieldDescriptor.newInstance("ID")
                .withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addItemDescriptors(Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor))
                .build();

        Reader reader = new StringReader("FOOTest1123456789");

        CompositeItemEntity compositeItem = descriptor.readItemEntityFrom(reader);
        assertThat(compositeItem.getLength()).isEqualTo(17);
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
                .addItemDescriptors(Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor))
                .build();

        String data = "FOOTest1123456789";

        Reader reader = new StringReader(data);

        CompositeItemEntity compositeItem = descriptor.readItemEntityFrom(reader);

        StringWriter writer = new StringWriter();
        compositeItem.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo(data);
    }
}