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
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CompositeItemDescriptorTest {

    @Test
    void Cannot_add_null() {
        assertThrows(NullPointerException.class, () -> CompositeItemDescriptor.newInstance("Foo")
                .addItemDescriptor(null));
    }

    @Test
    void Read_from_a_source_stream() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addItemDescriptors(constantFieldDescriptor, field1Descriptor, field2Descriptor)
                .build();

        Reader reader = new StringReader("FOOTest1123456789");

        CompositeItemEntity compositeItem = descriptor.readItemEntityFrom(reader);
        assertThat(compositeItem).isNotNull();
    }
    @Test
    void Min_length_is_sum_of_lengths() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addItemDescriptors(Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor))
                .build();

        assertThat(descriptor.getMinLength()).isEqualTo(17);
    }

    @Test
    void Create_new_item() {
        ConstantFieldDescriptor constantFieldDescriptor =
                ConstantFieldDescriptor.newInstance("ID").withConstant("FOO").build();
        FixedSizeFieldDescriptor field1Descriptor = FixedSizeFieldDescriptor.newInstance("Field1")
                .withFieldSize(5).build();
        FixedSizeFieldDescriptor field2Descriptor = FixedSizeFieldDescriptor.newInstance("Field2")
                .withFieldSize(9).build();

        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addItemDescriptor(constantFieldDescriptor)
                .addItemDescriptor(field1Descriptor)
                .addItemDescriptor(field2Descriptor)
                .build();

        CompositeItemEntity compositeItem = descriptor.createItemEntity();
        assertThat(compositeItem.getLength()).isEqualTo(17);
    }

    @Test
    void Calls_correct_handler_method() {
        CompositeItemDescriptor descriptor = CompositeItemDescriptor.newInstance("Record")
                .addItemDescriptor(ConstantFieldDescriptor.newInstance("ID").withConstant("Foo").build())
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
}