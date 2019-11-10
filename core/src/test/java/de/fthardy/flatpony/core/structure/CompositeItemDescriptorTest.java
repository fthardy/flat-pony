package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItemDescriptorHandler;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.DelimitedFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptorHandler;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompositeItemDescriptorTest {

    @Test
    void Cannot_create_when_child_list_is_null() {
        assertThrows(NullPointerException.class, () -> new CompositeItemDescriptor("Foo", null));
    }

    @Test
    void Cannot_create_when_child_list_is_empty() {
        assertThrows(IllegalArgumentException.class, () ->
                new CompositeItemDescriptor("Foo", Collections.emptyList()));
    }

    @Test
    void Read_from_a_source_stream() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("ID", "FOO");
        FixedSizeFieldDescriptor field1Descriptor = new FixedSizeFieldDescriptor("Field1", 5);
        FixedSizeFieldDescriptor field2Descriptor = new FixedSizeFieldDescriptor("Field2", 9);

        CompositeItemDescriptor descriptor = new CompositeItemDescriptor(
                "Record", Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor));

        Reader reader = new StringReader("FOOTest1123456789");

        CompositeItem compositeItem = descriptor.readItemFrom(reader);
        assertThat(compositeItem).isNotNull();
    }

    @Test
    void Create_new_item() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("ID", "FOO");
        FixedSizeFieldDescriptor field1Descriptor = new FixedSizeFieldDescriptor("Field1", 5);
        FixedSizeFieldDescriptor field2Descriptor = new FixedSizeFieldDescriptor("Field2", 9);

        CompositeItemDescriptor descriptor = new CompositeItemDescriptor(
                "Record", Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor));

        CompositeItem compositeItem = descriptor.createItem();
        assertThat(compositeItem.getLength()).isEqualTo(17);
    }

    @Test
    void Calls_correct_handler_method() {
        CompositeItemDescriptor descriptor = new CompositeItemDescriptor(
                "Record", Collections.singletonList(new ConstantFieldDescriptor("ID", "Foo")));

        FlatDataItemDescriptorHandler handlerMock = mock(FlatDataItemDescriptorHandler.class);
        FlatDataStructureDescriptorHandler descriptorHandlerMock = mock(FlatDataStructureDescriptorHandler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(descriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        verifyNoMoreInteractions(handlerMock);
        verify(descriptorHandlerMock).handleCompositeItemDescriptor(descriptor);
        verifyNoMoreInteractions(descriptorHandlerMock);
    }
}