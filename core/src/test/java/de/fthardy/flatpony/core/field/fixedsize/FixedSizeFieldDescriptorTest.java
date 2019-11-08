package de.fthardy.flatpony.core.field.fixedsize;

import de.fthardy.flatpony.core.FlatDataReadException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FixedSizeFieldDescriptorTest {

    @Test
    void Cannot_create_with_a_field_size_smaller_than_1() {
        assertThrows(IllegalArgumentException.class, () -> new FixedSizeFieldDescriptor("Foo", 0));
    }

    @Test
    void Cannot_create_with_null_ContentValueTransformer() {
        assertThrows(NullPointerException.class, () ->
                new FixedSizeFieldDescriptor(
                        "Foo", 1, "J", null, Collections.emptySet()));
    }

    @Test
    void New_field_has_default_value() {
        FixedSizeField field = new FixedSizeFieldDescriptor("Foo", 10).createItem();
        assertThat(field.getValue()).isEqualTo("          ");
    }

    @Test
    void Field_is_read_from_source_stream() {
        Reader reader = new StringReader("FooBar    ");
        FixedSizeField field = new FixedSizeFieldDescriptor(
                "Foo",
                10,
                "",
                new DefaultFieldContentValueTransformer(' ', true),
                Collections.emptySet()).readItemFrom(reader);
        assertThat(field.getValue()).isEqualTo("FooBar");
    }

    @Test
    void IOException_during_read() throws IOException {
        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read(any(char[].class))).thenThrow(ioException);

        FlatDataReadException exception = assertThrows(FlatDataReadException.class, () ->
                new FixedSizeFieldDescriptor("Foo", 10).readItemFrom(readerMock));
        assertThat(exception.getMessage()).isEqualTo(FixedSizeFieldDescriptor.MSG_Read_failed("Foo"));
        assertThat(exception.getCause()).isSameAs(ioException);

        verify(readerMock).read(any(char[].class));
    }
}