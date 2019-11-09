package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CompositeItemTest {

    @Test
    void Length_is_sum_of_all_item_lengths() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("ID", "FOO");
        FixedSizeFieldDescriptor field1Descriptor = new FixedSizeFieldDescriptor("Field1", 5);
        FixedSizeFieldDescriptor field2Descriptor = new FixedSizeFieldDescriptor("Field2", 9);

        CompositeItemDescriptor descriptor = new CompositeItemDescriptor(
                "Record", Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor));

        Reader reader = new StringReader("FOOTest1123456789");

        CompositeItem compositeItem = descriptor.readItemFrom(reader);
        assertThat(compositeItem.getLength()).isEqualTo(17);
    }

    @Test
    void What_is_read_is_what_is_written() {
        ConstantFieldDescriptor constantFieldDescriptor = new ConstantFieldDescriptor("ID", "FOO");
        FixedSizeFieldDescriptor field1Descriptor = new FixedSizeFieldDescriptor("Field1", 5);
        FixedSizeFieldDescriptor field2Descriptor = new FixedSizeFieldDescriptor("Field2", 9);

        CompositeItemDescriptor descriptor = new CompositeItemDescriptor(
                "Record", Arrays.asList(constantFieldDescriptor, field1Descriptor, field2Descriptor));

        String data = "FOOTest1123456789";

        Reader reader = new StringReader(data);

        CompositeItem compositeItem = descriptor.readItemFrom(reader);

        StringWriter writer = new StringWriter();
        compositeItem.writeTo(writer);

        assertThat(writer.getBuffer().toString()).isEqualTo(data);
    }
}