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
package de.fthardy.flatpony.core.field.typed;

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.typed.converter.FieldValueConvertException;
import de.fthardy.flatpony.core.field.typed.converter.FieldValueConverter;

import java.io.Writer;

/**
 * The implementation of the converted field.
 * <p>
 * A type converter field allows to get and set the field value in a particular type 
 * A converted field provides the raw field value in a particular converted type. 
 * </p>
 * 
 * @param <T> the target type for the field value.
 *           
 * @author Frank Timothy Hardy
 */
public class TypedField<T> extends AbstractFlatDataItemEntity<TypedFieldDescriptor<T>>
        implements FlatDataMutableField<TypedFieldDescriptor<T>> {
    
    static String MSG_Convert_to_target_type_failed(String fieldValue, String targetTypeName) {
        return String.format("Failed to convert field value [%s] to target type [%s] !", fieldValue, targetTypeName);
    }
    
    static String MSG_Convert_from_target_type_failed(Object fieldValue, String targetTypeName) {
        return String.format("Failed to convert value [%s] from target type [%s] to field value!",
                fieldValue, targetTypeName);
    }

    private final FlatDataField<?> decoratedField;

    /**
     * Creates a new instance of a constant field.
     *
     * @param descriptor the descriptor which is creating this field instance.
     * @param decoratedField the decorated field entity instance.
     *                       
     * @throws FieldValueConvertException when the value of the decorated field cannot be converted into the target type.
     */
    TypedField(TypedFieldDescriptor<T> descriptor, FlatDataField<?> decoratedField) {
        super(descriptor);
        this.decoratedField = decoratedField;
        getValueAsTargetType();
    }

    @Override
    public String getValue() {
        return this.decoratedField.getValue();
    }

    @Override
    public void setValue(String value) {
        FieldValueConverter<T> converter = this.getDescriptor().getFieldValueConverter();
        this.setValueAsTargetType(converter.convertFromFieldValue(value));
    }
    
    @Override
    public FlatDataMutableField<TypedFieldDescriptor<T>> asMutableField() {
        return this;
    }

    @Override
    public int getLength() {
        return this.decoratedField.getLength();
    }

    @Override
    public void writeTo(Writer target) {
        this.decoratedField.writeTo(target);
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataField.Handler) {
            ((FlatDataField.Handler) handler).handleTypedField(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }

    /**
     * Get the field decorated by this decorator.
     * 
     * @return the decorated field.
     */
    public FlatDataField<?> getDecoratedField() {
        return this.decoratedField;
    }

    /**
     * Get the field value as the target type.
     * 
     * @return the field value converted into the target type.
     * 
     * @throws FieldValueConvertException when the field value cannot be converted into the target type. 
     */
    public T getValueAsTargetType() {
        FieldValueConverter<T> converter = this.getDescriptor().getFieldValueConverter();
        try {
            return converter.convertFromFieldValue(this.decoratedField.getValue());
        } catch (FieldValueConvertException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new FieldValueConvertException(
                    MSG_Convert_to_target_type_failed(this.getValue(), converter.getTargetType().getName()), e);
        }
    }

    /**
     * Set the field value as the converted target type which is then converted back into the raw field value.
     * 
     * @param value the field value to set.
     *
     * @throws FieldValueConvertException when the given value cannot be converted into a string for the field value. 
     */
    public void setValueAsTargetType(T value) {
        FlatDataMutableField<?> field = this.decoratedField.asMutableField();
        FieldValueConverter<T> converter = this.getDescriptor().getFieldValueConverter();
        try {
            field.setValue(converter.convertToFieldValue(value));
        } catch (FieldValueConvertException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new FieldValueConvertException(
                    MSG_Convert_from_target_type_failed(value, converter.getTargetType().getName()), e);
        }
    }
}
