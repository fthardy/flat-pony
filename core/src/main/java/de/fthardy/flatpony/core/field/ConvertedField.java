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
package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;

import java.io.Writer;

/**
 * The implementation of the converted field.
 * <p>
 * A converted field provides the raw field value in a particular converted type. 
 * </p>
 * 
 * @param <T> the target type for the field value.
 *           
 * @author Frank Timothy Hardy
 */
public final class ConvertedField<T> extends AbstractFlatDataItemEntity<ConvertedFieldDescriptor<T>>
        implements FlatDataMutableField<ConvertedFieldDescriptor<T>> {

    private final FlatDataField<?> decoratedField;

    /**
     * Creates a new instance of a constant field.
     *
     * @param descriptor the descriptor which is creating this field instance.
     * @param decoratedField the decorated field entity instance.
     */
    ConvertedField(ConvertedFieldDescriptor<T> descriptor, FlatDataField<?> decoratedField) {
        super(descriptor);
        this.decoratedField = decoratedField;
    }

    @Override
    public String getValue() {
        return this.decoratedField.getValue();
    }

    @Override
    public void setValue(String value) {
        this.decoratedField.asMutableField().setValue(value); 
    }
    
    @Override
    public FlatDataMutableField<ConvertedFieldDescriptor<T>> asMutableField() {
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
            ((FlatDataField.Handler) handler).handleConvertedField(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }

    public FlatDataField<?> getDecoratedField() {
        return decoratedField;
    }

    /**
     * Get the field value as the converted target type.
     * 
     * @return the field value.
     */
    public T getConvertedValue() {
        return this.getDescriptor().getFieldValueConverter().convertFromFieldValue(this.decoratedField.getValue());
    }

    /**
     * Set the field value as the converted target type which is then converted back into the raw field value.
     * 
     * @param convertedValue the field value to set.
     */
    public void setConvertedValue(T convertedValue) {
        this.decoratedField.asMutableField().setValue(
                this.getDescriptor().getFieldValueConverter().convertToFieldValue(convertedValue));
    }
}
