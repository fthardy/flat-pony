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
package de.fthardy.flatpony.core.util;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;

import java.io.Writer;
import java.util.Objects;

/**
 * A decorator implementation for a field which adds methods to get and set the field value as a defined target type.
 *
 * @param <T> the target type of the field value.
 *
 * @author Frank Timothy Hardy
 */
public final class TypedFieldDecorator<T> implements FlatDataMutableField<FlatDataFieldDescriptor<?>> {

    private final FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> decoratedField;
    private final FieldValueConverter<T> typeConverter;

    /**
     * Creates a new instance of this field decorator.
     *
     * @param decoratedField the field to be decorated.
     * @param typeConverter the type converter.
     */
    public TypedFieldDecorator(
            FlatDataField<? extends FlatDataFieldDescriptor<?>> decoratedField, FieldValueConverter<T> typeConverter) {
        this.decoratedField = Objects.requireNonNull(decoratedField, "Undefined field to decorate!").asMutableField();
        this.typeConverter = Objects.requireNonNull(typeConverter, "Undefined type converter!");
    }

    @Override
    public void setValue(String value) {
        this.decoratedField.setValue(value);
    }

    @Override
    public FlatDataFieldDescriptor<?> getDescriptor() {
        return this.decoratedField.getDescriptor();
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
        this.decoratedField.applyHandler(handler);
    }

    @Override
    public String getValue() {
        return this.decoratedField.getValue();
    }

    @Override
    public FlatDataMutableField<FlatDataFieldDescriptor<?>> asMutableField() {
        return this;
    }

    /**
     * Get the field value converted into the target type.
     *
     * @return the value.
     */
    public T getTypedValue() {
        return this.typeConverter.convertFromFieldValue(this.decoratedField.getValue());
    }

    /**
     * Set a new value.
     *
     * @param value the new value.
     */
    public void setTypedValue(T value) {
        this.decoratedField.setValue(this.typeConverter.convertToFieldValue(value));
    }
}
