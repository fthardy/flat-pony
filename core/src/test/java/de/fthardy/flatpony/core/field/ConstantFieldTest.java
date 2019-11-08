package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataWriteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConstantFieldTest {

    @Test
    void Field_length_equals_length_of_constant() {
        assertEquals(3, new ConstantFieldDescriptor("Test", "Foo").createItem().getLength());
    }

    @Test
    void Field_value_equals_constant() {
        assertEquals("Foo", new ConstantFieldDescriptor("Test", "Foo").createItem().getValue());
    }

    @Test
    void Field_is_immutable() {
        assertThrows(UnsupportedOperationException.class, () ->
                new ConstantFieldDescriptor("Test", "Foo").createItem().setValue("Bar"));
    }

    @Test
    void Writing_causes_an_IOException() throws IOException {
        Writer writerMock = mock(Writer.class);

        IOException ioException = new IOException();
        doThrow(ioException).when(writerMock).write("Foo");

        FlatDataWriteException exception = assertThrows(FlatDataWriteException.class, () ->
                new ConstantFieldDescriptor("Test", "Foo").createItem().writeTo(writerMock));

        assertEquals(ConstantField.MSG_Write_failed("Test"), exception.getMessage());
        assertSame(ioException, exception.getCause());

        verify(writerMock).write("Foo");
        verifyNoMoreInteractions(writerMock);
    }

    @Test
    void Write_constant_to_target_stream() {
        StringWriter writer = new StringWriter();

        new ConstantFieldDescriptor("Test", "Foo").createItem().writeTo(writer);

        assertEquals("Foo", writer.getBuffer().toString());
    }
}