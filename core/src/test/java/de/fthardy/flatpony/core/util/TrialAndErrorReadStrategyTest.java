package de.fthardy.flatpony.core.util;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.ConstantField;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class TrialAndErrorReadStrategyTest {

    @Test
    void Cannot_create_with_null_item_name() {
        assertThrows(NullPointerException.class, () -> new TrialAndErrorReadStrategy(null, null));
    }

    @Test
    void Cannot_create_with_null_descriptor() {
        assertThrows(NullPointerException.class, () -> new TrialAndErrorReadStrategy("Foo", null));
    }

    @Test
    void Mark_not_supported_by_reader() {
        Reader readerMock = mock(Reader.class);

        TrialAndErrorReadStrategy strategy = new TrialAndErrorReadStrategy(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        when(readerMock.markSupported()).thenReturn(false);

        assertThrows(FlatDataReadException.class, () -> strategy.readItemFrom(readerMock));
    }

    @Test
    void Mark_throws_exception() throws IOException {
        Reader readerMock = mock(Reader.class);

        TrialAndErrorReadStrategy strategy = new TrialAndErrorReadStrategy(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        when(readerMock.markSupported()).thenReturn(true);
        IOException ioException = new IOException();
        doThrow(ioException).when(readerMock).mark(3);

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> strategy.readItemFrom(readerMock));
        assertThat(exception.getMessage()).isEqualTo(
                TrialAndErrorReadStrategy.MSG_Failed_to_mark_stream("Test"));
    }

    @Test
    void Reading_the_source_stream_totally_fails() throws IOException {
        Reader readerMock = mock(Reader.class);

        TrialAndErrorReadStrategy strategy = new TrialAndErrorReadStrategy(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        when(readerMock.markSupported()).thenReturn(true);
        IOException ioException = new IOException();
        doThrow(ioException).when(readerMock).read(Mockito.any(char[].class));
        doThrow(ioException).when(readerMock).reset();

        FlatDataReadException exception =
                assertThrows(FlatDataReadException.class, () -> strategy.readItemFrom(readerMock));
        assertThat(exception.getMessage()).isEqualTo(
                TrialAndErrorReadStrategy.MSG_Failed_to_reset_stream("Test"));
    }

    @Test
    void Reading_the_item_from_the_source_stream_fails() throws IOException {
        Reader readerMock = mock(Reader.class);

        TrialAndErrorReadStrategy strategy = new TrialAndErrorReadStrategy(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        when(readerMock.markSupported()).thenReturn(true);
        IOException ioException = new IOException();
        doThrow(ioException).when(readerMock).read(Mockito.any(char[].class));

        FlatDataItemEntity<?> item = strategy.readItemFrom(readerMock);
        assertThat(item).isNull();
    }

    @Test
    void Reading_the_item_succeeds() throws IOException {
        StringReader reader = new StringReader("BAR");

        TrialAndErrorReadStrategy strategy = new TrialAndErrorReadStrategy(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        FlatDataItemEntity<?> item = strategy.readItemFrom(reader);
        assertThat(item).isNotNull();
        assertThat(((ConstantField)item).getValue()).isEqualTo("BAR");
    }
}