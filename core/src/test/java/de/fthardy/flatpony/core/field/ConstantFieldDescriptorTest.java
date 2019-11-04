package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataReadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConstantFieldDescriptorTest {

    @Test
    void Cannot_create_with_null_constant() {
        assertThrows(NullPointerException.class, () -> new ConstantFieldDescriptor("Test", null));
    }

    @Test
    void Cannot_create_with_empty_constant() {
        assertThrows(IllegalArgumentException.class, () -> new ConstantFieldDescriptor("Test", ""));
    }

    @Test
    void Determine_constraint_violations_with_throw_UnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () ->
                new ConstantFieldDescriptor("Test", "Foo").determineConstraintViolationsFor("Bar"));
    }

    @Test
    void Default_value_is_constant_value() {
        assertEquals("Foo", new ConstantFieldDescriptor("Test", "Foo").getDefaultValue());
    }

    @Test
    void Read_from_stream_which_is_too_short() {
        ConstantFieldDescriptor descriptor = new ConstantFieldDescriptor("Test", "Foo");

        StringReader reader = new StringReader("Fu");

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemFrom(reader));

        assertEquals(ConstantFieldDescriptor.MSG_Invalid_length(descriptor.getName()), exception.getMessage());
    }

    @Test
    void Read_from_stream_which_contains_wrong_value() {
        ConstantFieldDescriptor descriptor = new ConstantFieldDescriptor("Test", "Foo");

        String value = "Fuu";
        StringReader reader = new StringReader(value);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemFrom(reader));

        assertEquals(ConstantFieldDescriptor.MSG_Invalid_value(
                descriptor.getName(), value, descriptor.getDefaultValue()), exception.getMessage());
    }

    @Test
    void Read_from_stream_which_throws_an_IOException() throws IOException {
        ConstantFieldDescriptor descriptor = new ConstantFieldDescriptor("Test", "Foo");

        Reader readerMock = mock(Reader.class);

        IOException ioException = new IOException();
        when(readerMock.read(any(char[].class))).thenThrow(ioException);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> descriptor.readItemFrom(readerMock));

        assertEquals(ConstantFieldDescriptor.MSG_Read_failed(descriptor.getName()), exception.getMessage());
        assertSame(ioException, exception.getCause());

        verify(readerMock).read(any(char[].class));
        verifyNoMoreInteractions(readerMock);
    }

    @Test
    void Returns_always_the_same_field_instance() {
        ConstantFieldDescriptor descriptor = new ConstantFieldDescriptor("Test", "Foo");
        assertSame(descriptor.createItem(), descriptor.createItem());
        StringReader reader = new StringReader("Foo");
        assertSame(descriptor.createItem(), descriptor.readItemFrom(reader));
    }
}