package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.converter.FieldValueConvertException;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.Writer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TypedFieldItemTest {

    @Test
    void Cannot_create_descriptor_with_null_field_descriptor() {
        assertThrows(NullPointerException.class, () -> TypedFieldDescriptor.newInstance(null));
    }

    @Test
    void Cannot_create_descriptor_with_null_converter() {
        assertThrows(NullPointerException.class, () -> TypedFieldDescriptor
                .newInstance(mock(FlatDataFieldDescriptor.class)).withFieldValueConverter(null));
    }

    @Test
    void Create_descriptor_instance() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.getDefaultValue()).thenReturn("Default value");
        when(fieldDescriptorMock.getMinLength()).thenReturn(42);

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);
        
        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();

        assertThat(descriptor.getDecoratedFieldDescriptor()).isSameAs(fieldDescriptorMock);
        assertThat(descriptor.getFieldValueConverter()).isSameAs(converterMock);
        
        assertThat(descriptor.getName()).isEqualTo("Field");
        assertThat(descriptor.getDefaultValue()).isEqualTo("Default value");
        assertThat(descriptor.getMinLength()).isEqualTo(42);
        
        verify(fieldDescriptorMock, times(2)).getName();
        verify(fieldDescriptorMock).getDefaultValue();
        verify(fieldDescriptorMock).getMinLength();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verifyZeroInteractions(converterMock);
    }

    @Test
    void Apply_handler_to_descriptor() {
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);

        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();

        FlatDataItemDescriptor.Handler handlerMock = mock(FlatDataItemDescriptor.Handler.class);
        FlatDataFieldDescriptor.Handler fieldDescriptorHandlerMock = mock(FlatDataFieldDescriptor.Handler.class);

        descriptor.applyHandler(handlerMock);
        descriptor.applyHandler(fieldDescriptorHandlerMock);

        verify(handlerMock).handleFlatDataItemDescriptor(descriptor);
        
        verifyNoMoreInteractions(handlerMock);
        
        verify(fieldDescriptorHandlerMock).handleTypedFieldDescriptor(descriptor);
        
        verifyNoMoreInteractions(fieldDescriptorHandlerMock);

        verify(fieldDescriptorMock).getName();

        verifyNoMoreInteractions(fieldDescriptorMock);

        verifyZeroInteractions(converterMock);
    }
    
    @Test
    void Create_field_and_set_new_value() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.asMutableField()).then(invocation -> fieldMock);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("Value");
        
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.createItemEntity()).then(invocation -> fieldMock);

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);
        when(converterMock.convertFromFieldValue(anyString())).then(invocation -> invocation.getArgument(0));
        when(converterMock.convertToFieldValue(any())).then(invocation -> invocation.getArgument(0));

        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();

        TypedField<Object> field = descriptor.createItemEntity();
        
        assertThat(field.getDecoratedField()).isSameAs(fieldMock);
        
        assertThat(field.asMutableField()).isSameAs(field);
        
        assertThat(field.getLength()).isEqualTo(42);
        assertThat(field.getValue()).isEqualTo("Value");
        assertThat(field.getValueAsTargetType()).isEqualTo("Value");
        
        field.setValue("New value");
        field.setValueAsTargetType("Other value");
        
        verify(fieldMock, times(2)).asMutableField();
        verify(fieldMock).getLength();
        verify(fieldMock, times(3)).getValue();
        verify(fieldMock).setValue("New value");
        verify(fieldMock).setValue("Other value");
        
        verifyNoMoreInteractions(fieldMock);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).createItemEntity();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verify(converterMock, times(2)).convertFromFieldValue("Value");
        verify(converterMock).convertFromFieldValue("New value");
        verify(converterMock).convertToFieldValue("New value");
        verify(converterMock).convertToFieldValue("Other value");
        
        verifyNoMoreInteractions(converterMock);
    }
    
    @Test
    void Create_field_and_set_invalid_value() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.asMutableField()).then(invocation -> fieldMock);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("Value");
        
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.createItemEntity()).then(invocation -> fieldMock);

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);
        when(converterMock.convertFromFieldValue(anyString())).then(invocation -> invocation.getArgument(0));
        when(converterMock.convertToFieldValue(any())).then(invocation -> invocation.getArgument(0));
        when(converterMock.getTargetType()).thenReturn(Object.class);

        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();

        TypedField<Object> field = descriptor.createItemEntity();
        
        assertThat(field.getDecoratedField()).isSameAs(fieldMock);
        
        assertThat(field.asMutableField()).isSameAs(field);
        
        assertThat(field.getLength()).isEqualTo(42);
        assertThat(field.getValue()).isEqualTo("Value");
        assertThat(field.getValueAsTargetType()).isEqualTo("Value");

        FieldValueConvertException exception = new FieldValueConvertException("TEST");
        when(converterMock.convertToFieldValue("Bad value")).thenThrow(exception);
        ClassCastException classCastException = new ClassCastException("TEST");
        when(converterMock.convertToFieldValue("Other very bad value!")).thenThrow(classCastException);
        
        FieldValueConvertException ex = 
                assertThrows(FieldValueConvertException.class, () -> field.setValueAsTargetType("Bad value"));
        assertThat(ex).isSameAs(exception);
        
        ex = assertThrows(FieldValueConvertException.class, () -> field.setValueAsTargetType("Other very bad value!"));
        assertThat(ex.getMessage()).isEqualTo(
                TypedField.MSG_Convert_from_target_type_failed("Other very bad value!", Object.class.getName()));
        assertThat(ex.getCause()).isSameAs(classCastException);
        
        verify(fieldMock, times(2)).asMutableField();
        verify(fieldMock).getLength();
        verify(fieldMock, times(3)).getValue();
        
        verifyNoMoreInteractions(fieldMock);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).createItemEntity();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verify(converterMock, times(2)).convertFromFieldValue("Value");
        verify(converterMock).convertToFieldValue("Bad value");
        verify(converterMock).convertToFieldValue("Other very bad value!");
        verify(converterMock).getTargetType();
        
        verifyNoMoreInteractions(converterMock);
    }
    
    @Test
    void Create_field_with_initial_invalid_value_Converter_throws_FieldConvertException() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("Bad value");
        
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.createItemEntity()).then(invocation -> fieldMock);

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);
        when(converterMock.convertFromFieldValue(anyString())).then(invocation -> invocation.getArgument(0));
        when(converterMock.convertToFieldValue(any())).then(invocation -> invocation.getArgument(0));
        FieldValueConvertException exception = new FieldValueConvertException("TEST");
        when(converterMock.convertFromFieldValue("Bad value")).thenThrow(exception);

        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();

        FieldValueConvertException ex = assertThrows(FieldValueConvertException.class, descriptor::createItemEntity);
        assertThat(ex).isSameAs(exception);
        
        verify(fieldMock).getValue();
        
        verifyNoMoreInteractions(fieldMock);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).createItemEntity();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verify(converterMock).convertFromFieldValue("Bad value");
        
        verifyNoMoreInteractions(converterMock);
    }
    
    @Test
    void Create_field_with_initial_invalid_value_Converter_throws_RuntimeException() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getLength()).thenReturn(42);
        when(fieldMock.getValue()).thenReturn("Bad value");
        
        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.createItemEntity()).then(invocation -> fieldMock);

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);
        when(converterMock.convertFromFieldValue(anyString())).then(invocation -> invocation.getArgument(0));
        when(converterMock.convertToFieldValue(any())).then(invocation -> invocation.getArgument(0));
        when(converterMock.getTargetType()).thenReturn(Object.class);
        
        ClassCastException classCastException = new ClassCastException("TEST");
        when(converterMock.convertFromFieldValue("Bad value")).thenThrow(classCastException);

        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();

        FieldValueConvertException ex = assertThrows(FieldValueConvertException.class, descriptor::createItemEntity);
        assertThat(ex.getMessage()).isEqualTo(
                TypedField.MSG_Convert_to_target_type_failed("Bad value", Object.class.getName()));
        assertThat(ex.getCause()).isSameAs(classCastException);
        
        verify(fieldMock, times(2)).getValue();
        
        verifyNoMoreInteractions(fieldMock);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).createItemEntity();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verify(converterMock).convertFromFieldValue("Bad value");
        verify(converterMock).getTargetType();
        
        verifyNoMoreInteractions(converterMock);
    }

    @Test
    void Apply_handler_to_field() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getValue()).thenReturn("Value");

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.createItemEntity()).then(invocation -> fieldMock);

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);
        when(converterMock.convertFromFieldValue(anyString())).then(invocation -> invocation.getArgument(0));
        when(converterMock.convertToFieldValue(any())).then(invocation -> invocation.getArgument(0));

        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();

        TypedField<Object> field = descriptor.createItemEntity();

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataField.Handler fieldHandlerMock = mock(FlatDataField.Handler.class);

        field.applyHandler(handlerMock);
        field.applyHandler(fieldHandlerMock);

        verify(handlerMock).handleFlatDataItemEntity(field);
        
        verifyNoMoreInteractions(handlerMock);
        
        verify(fieldHandlerMock).handleTypedField(field);
        
        verifyNoMoreInteractions(fieldHandlerMock);
        
        verify(fieldMock).getValue();
        
        verifyNoMoreInteractions(fieldMock);
        
        verify(fieldDescriptorMock).getName();
        verify(fieldDescriptorMock).createItemEntity();
        
        verifyNoMoreInteractions(fieldDescriptorMock);
        
        verify(converterMock).convertFromFieldValue("Value");
        
        verifyNoMoreInteractions(converterMock);
    }

    @Test
    void Read_from_source_stream_and_write_back_to_target_stream() {
        FlatDataMutableField<?> fieldMock = mock(FlatDataMutableField.class);
        when(fieldMock.getValue()).thenReturn("Value");

        Reader readerMock = mock(Reader.class);

        FlatDataFieldDescriptor<?> fieldDescriptorMock = mock(FlatDataFieldDescriptor.class);
        when(fieldDescriptorMock.getName()).thenReturn("Field");
        when(fieldDescriptorMock.readItemEntityFrom(readerMock)).then(invocation -> fieldMock);

        FieldValueConverter<Object> converterMock = mock(FieldValueConverter.class);
        when(converterMock.convertFromFieldValue(anyString())).then(invocation -> invocation.getArgument(0));
        when(converterMock.convertToFieldValue(any())).then(invocation -> invocation.getArgument(0));

        TypedFieldDescriptor<Object> descriptor = TypedFieldDescriptor
                .newInstance(fieldDescriptorMock).withFieldValueConverter(converterMock).build();
        TypedField<Object> field = descriptor.readItemEntityFrom(readerMock);

        Writer writerMock = mock(Writer.class);
        field.writeTo(writerMock);

        verify(fieldDescriptorMock, times(1)).getName();
        verify(fieldDescriptorMock).readItemEntityFrom(readerMock);

        verifyNoMoreInteractions(fieldDescriptorMock);

        verify(fieldMock).getValue();
        verify(fieldMock).writeTo(writerMock);

        verifyNoMoreInteractions(fieldMock);

        verify(converterMock).convertFromFieldValue("Value");

        verifyNoMoreInteractions(converterMock);

        verifyZeroInteractions(readerMock);
        verifyZeroInteractions(writerMock);
    }
}
