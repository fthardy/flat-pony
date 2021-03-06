package de.fthardy.flatpony.core.field.constrained;

import de.fthardy.flatpony.core.FlatDataItemDescriptorHandler;
import de.fthardy.flatpony.core.FlatDataItemEntityHandler;
import de.fthardy.flatpony.core.field.*;
import de.fthardy.flatpony.core.field.constrained.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constrained.constraint.ValueConstraintViolationException;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ConstrainedFieldItemTest {

    @Test
    void Cannot_create_descriptor_with_null_field_descriptor() {
        assertThrows(NullPointerException.class, () -> ConstrainedFieldDescriptor.newInstance(null));
    }
    
    @Test
    void Cannot_create_descriptor_with_null_constraint() {
        assertThrows(NullPointerException.class, () -> ConstrainedFieldDescriptor.newInstance(
                mock(FlatDataFieldDescriptor.class)).addConstraint(null));
    }

    @Test
    void Cannot_create_descriptor_adding_constraints_with_ambiguous_names() {
        String name = "Constraint3";
        
        ValueConstraint constraintMock1 = mock(ValueConstraint.class);
        ValueConstraint constraintMock2 = mock(ValueConstraint.class);
        ValueConstraint constraintMock3 = mock(ValueConstraint.class);
        ValueConstraint constraintMock4 = mock(ValueConstraint.class);
        
        when(constraintMock1.getName()).thenReturn("Constraint1");
        when(constraintMock2.getName()).thenReturn("Constraint2");
        when(constraintMock3.getName()).thenReturn(name);
        when(constraintMock4.getName()).thenReturn(name);
        
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        
        assertThrows(IllegalArgumentException.class, () -> ConstrainedFieldDescriptor
                .newInstance(fieldDescriptorMock)
                .addConstraints(Arrays.asList(constraintMock1, constraintMock2))
                .addConstraints(constraintMock3, constraintMock4));
        
        verify(fieldDescriptorMock).getName();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verify(constraintMock1, times(2)).getName();
        verify(constraintMock2, times(2)).getName();
        verify(constraintMock3, times(2)).getName();
        verify(constraintMock4).getName();
        
        verifyNoMoreInteractions(constraintMock1);
        verifyNoMoreInteractions(constraintMock2);
        verifyNoMoreInteractions(constraintMock3);
        verifyNoMoreInteractions(constraintMock4);
    }

    @Test
    void Default_value_violates_constraint() {
        ValueConstraint constraintMock = mock(ValueConstraint.class);
        when(constraintMock.getName()).thenReturn("Constraint");
        when(constraintMock.acceptValue(anyString())).thenReturn(false);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");
        
        assertThrows(ValueConstraintViolationException.class, () -> ConstrainedFieldDescriptor
                .newInstance(fieldDescriptorMock).addConstraint(constraintMock).build());
        
        verify(fieldDescriptorMock, times(2)).getName();
        verify(fieldDescriptorMock).getDefaultValue();

        verifyNoMoreInteractions(fieldDescriptorMock);

        verify(constraintMock, times(3)).getName();
        verify(constraintMock).acceptValue("Default value");

        verifyNoMoreInteractions(constraintMock);
    }
    
    @Test
    void Create_descriptor_instance() {
        ValueConstraint constraintMock = mock(ValueConstraint.class);
        when(constraintMock.getName()).thenReturn("Constraint");
        when(constraintMock.acceptValue(anyString())).thenReturn(true);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");
        when(fieldDescriptorMock.getMinLength()).thenReturn(42);

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(constraintMock).build();
        assertThat(descriptor.toString()).startsWith(ConstrainedFieldDescriptor.class.getSimpleName());
        
        assertThat(descriptor.getDecoratedFieldDescriptor()).isSameAs(fieldDescriptorMock);
        
        assertThat(descriptor.getName()).isEqualTo("Field");
        assertThat(descriptor.getDefaultValue()).isEqualTo("Default value");
        assertThat(descriptor.getMinLength()).isEqualTo(42);
        
        verify(fieldDescriptorMock, times(2)).getName();
        verify(fieldDescriptorMock, times(2)).getDefaultValue();
        verify(fieldDescriptorMock).getMinLength();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verify(constraintMock, times(2)).getName();
        verify(constraintMock).acceptValue("Default value");
        
        verifyNoMoreInteractions(constraintMock);
    }

    @Test
    void Apply_handler_to_descriptor() {
        ValueConstraint constraintMock = mock(ValueConstraint.class);
        when(constraintMock.getName()).thenReturn("Constraint");
        when(constraintMock.acceptValue(anyString())).thenReturn(true);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(constraintMock).build();

        FlatDataItemDescriptorHandler handlerMock = mock(FlatDataItemDescriptorHandler.class);
        FlatDataFieldDescriptorHandler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptorHandler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();

        verifyNoMoreInteractions(fieldDescriptorMock);

        verify(constraintMock, times(2)).getName();
        verify(constraintMock).acceptValue("Default value");
        verifyNoMoreInteractions(constraintMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        
        verifyNoMoreInteractions(handlerMock);
        
        verify(fieldDescriptorHandlerMock).handleConstrainedFieldDescriptor(descriptor);
        
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);
    }

    @Test
    void New_value_at_field_violates_constraint() {
        ValueConstraint constraintMock = mock(ValueConstraint.class);
        when(constraintMock.getName()).thenReturn("Constraint");
        when(constraintMock.acceptValue(anyString())).thenReturn(true, true, false);

        FlatDataField<?> fieldMock = mock(FlatDataField.class);
        when(fieldMock.getValue()).thenReturn("Value on create");
        
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");
        when(fieldDescriptorMock.createItemEntity()).thenAnswer(i -> fieldMock);

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(constraintMock).build();

        ConstrainedField field = descriptor.createItemEntity();
        assertThat(field.toString()).startsWith(ConstrainedField.class.getSimpleName());

        assertThrows(ValueConstraintViolationException.class, () -> field.setValue("Bad value"));

        verify(fieldDescriptorMock, times(2)).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).createItemEntity();

        verify(constraintMock, times(3)).getName();
        verify(constraintMock).acceptValue("Default value");
        verify(constraintMock).acceptValue("Value on create");
        verify(constraintMock).acceptValue("Bad value");
        
        verify(fieldMock).getValue();
        
        verifyNoMoreInteractions(constraintMock, fieldMock, fieldDescriptorMock);
    }

    @Test
    void Set_new_valid_value_at_field() {
        ValueConstraint constraintMock = mock(ValueConstraint.class);
        when(constraintMock.getName()).thenReturn("Constraint");
        when(constraintMock.acceptValue(anyString())).thenReturn(true, true, true);
        
        final FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("Value");
        when(fieldMock.asMutableField()).then((Answer<FlatDataField<?>>) invocation -> fieldMock);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");
        when(fieldDescriptorMock.createItemEntity()).then((Answer<FlatDataField<?>>) invocation -> fieldMock);

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(constraintMock).build();
        
        ConstrainedField field = descriptor.createItemEntity();
        
        assertThat(field.getDecoratedField()).isSameAs(fieldMock);
        assertThat(field.getLength()).isEqualTo(42);
        assertThat(field.getValue()).isEqualTo("Value");
        assertThat(field.asMutableField()).isSameAs(field);
        
        field.setValue("Good value");
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).createItemEntity();

        verifyNoMoreInteractions(fieldDescriptorMock);

        verify(fieldMock).getLength();
        verify(fieldMock, times(2)).getValue();
        verify(fieldMock).setValue("Good value");
        verify(fieldMock).asMutableField();
        
        verifyNoMoreInteractions(fieldMock);

        verify(constraintMock, times(2)).getName();
        verify(constraintMock).acceptValue("Default value");
        verify(constraintMock).acceptValue("Value");
        verify(constraintMock).acceptValue("Good value");
        
        verifyNoMoreInteractions(constraintMock);
    }

    @Test
    void Apply_handler_to_field() {
        ValueConstraint constraintMock = mock(ValueConstraint.class);
        when(constraintMock.getName()).thenReturn("Constraint");
        when(constraintMock.acceptValue(anyString())).thenReturn(true, true, false);
        
        FlatDataField<?> fieldMock = mock(FlatDataField.class);
        when(fieldMock.getValue()).thenReturn("Value");

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");
        when(fieldDescriptorMock.createItemEntity()).thenAnswer(i -> fieldMock);

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(constraintMock).build();

        ConstrainedField field = descriptor.createItemEntity();

        FlatDataItemEntityHandler handlerMock = mock(FlatDataItemEntityHandler.class);
        FlatDataFieldHandler fieldHandlerMock = mock(FlatDataFieldHandler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        
        verify(fieldHandlerMock).handleConstrainedField(field);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).createItemEntity();
        
        verify(fieldMock).getValue();
        
        verify(constraintMock, times(2)).getName();
        verify(constraintMock).acceptValue("Default value");
        verify(constraintMock).acceptValue("Value");

        verifyNoMoreInteractions(handlerMock, fieldHandlerMock, fieldMock, fieldDescriptorMock, constraintMock);
    }
    
    @Test
    void Read_from_source_stream_and_write_back_to_target_stream() {
        ValueConstraint constraintMock = mock(ValueConstraint.class);
        when(constraintMock.getName()).thenReturn("Constraint");
        when(constraintMock.acceptValue(anyString())).thenReturn(true, true, true);

        final FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("Value");
        when(fieldMock.asMutableField()).then((Answer<FlatDataField<?>>) invocation -> fieldMock);

        Reader readerMock = mock(Reader.class);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");
        when(fieldDescriptorMock.readItemEntityFrom(readerMock)).then((Answer<FlatDataField<?>>) invocation -> fieldMock);

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(constraintMock).build();
        
        ConstrainedField field = descriptor.readItemEntityFrom(readerMock);
        
        Writer writerMock = mock(Writer.class);
        field.writeTo(writerMock);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).readItemEntityFrom(readerMock);

        verify(fieldMock).writeTo(writerMock);
        verify(fieldMock).getValue();
        
        verify(constraintMock, times(2)).getName();
        verify(constraintMock).acceptValue("Default value");
        verify(constraintMock).acceptValue("Value");

        verifyNoMoreInteractions(fieldDescriptorMock, fieldMock, constraintMock);
        
        verifyZeroInteractions(readerMock);
        verifyZeroInteractions(writerMock);
    }
    
    @Test
    void Push_read() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(new ValueConstraint() {
                    @Override
                    public String getName() {
                        return "Dummy";
                    }

                    @Override
                    public boolean acceptValue(String value) {
                        return true;
                    }
                }).build();
        
        Reader readerMock = mock(Reader.class);
        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);
        
        descriptor.pushReadFrom(readerMock, streamReadHandlerMock);

        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).pushReadFrom(readerMock, streamReadHandlerMock);
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verifyZeroInteractions(readerMock);
        
        verifyZeroInteractions(streamReadHandlerMock);
    }

    @Test
    void Pull_read() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(new ValueConstraint() {
                    @Override
                    public String getName() {
                        return "Dummy";
                    }

                    @Override
                    public boolean acceptValue(String value) {
                        return true;
                    }
                }).build();

        Reader readerMock = mock(Reader.class);
        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        descriptor.pullReadFrom(readerMock);

        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).pullReadFrom(readerMock);
        verifyNoMoreInteractions(fieldDescriptorMock);

        verifyZeroInteractions(readerMock);

        verifyZeroInteractions(streamReadHandlerMock);
    }

    @Test
    void Read_value() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");

        ConstrainedFieldDescriptor descriptor =
                ConstrainedFieldDescriptor.newInstance(fieldDescriptorMock).addConstraint(new ValueConstraint() {
                    @Override
                    public String getName() {
                        return "Dummy";
                    }

                    @Override
                    public boolean acceptValue(String value) {
                        return true;
                    }
                }).build();

        Reader readerMock = mock(Reader.class);
        StreamReadHandler streamReadHandlerMock = mock(StreamReadHandler.class);

        descriptor.readValue(readerMock);

        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).readValue(readerMock);
        verifyNoMoreInteractions(fieldDescriptorMock);

        verifyZeroInteractions(readerMock);

        verifyZeroInteractions(streamReadHandlerMock);
    }
}
