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

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.Reader;
import java.util.Objects;

/**
 * The implementation of a descriptor for a field which can provide its value in a particular target type.
 * <p>
 * A converted field is a wrapper for other field types. It allows to get and set the value of the wrapped field in a
 * particular data type. For this purpose this field wrapper needs a {@link FieldValueConverter converter}
 * implementation that converts the field value into the target type and vice versa. 
 * </p>
 *     
 * @param <T> the target type for the field value. 
 * 
 * @author Frank Timothy Hardy
 */
public final class TypedFieldDescriptor<T> implements FlatDataFieldDescriptor<TypedField<T>> {

    /**
     * Demands the definition of a field value converter.
     * 
     * @param <T> the target type for the field value.
     *
     * @author Frank Timothy Hardy
     */
    public interface DefineFieldValueConverter<T> {

        /**
         * Define the field value converter.
         * 
         * @param converter the converter to be used to convert the field value.
         *                  
         * @return the builder instance for creating the descriptor instance.
         */
        ObjectBuilder<TypedFieldDescriptor<T>> withFieldValueConverter(FieldValueConverter<T> converter);
    }

    private interface BuildParams<T> {
        FlatDataFieldDescriptor<?> getFieldDescriptor();
        FieldValueConverter<T> getFieldValueConverter();
    }
    
    private static final class BuilderImpl<T> extends AbstractItemDescriptorBuilder<TypedFieldDescriptor<T>>
            implements DefineFieldValueConverter<T>, BuildParams<T> {
        
        private final FlatDataFieldDescriptor<?> fieldDescriptor;
        private FieldValueConverter<T> fieldValueConverter;
        
        /**
         * Create a new instance of this builder implementation.
         *
         * @param fieldDescriptor the descriptor of the field to decorate.
         */
        BuilderImpl(FlatDataFieldDescriptor<?> fieldDescriptor) {
            super(fieldDescriptor.getName());
            this.fieldDescriptor = Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!");
        }

        @Override
        public FlatDataFieldDescriptor<?> getFieldDescriptor() {
            return this.fieldDescriptor;
        }

        @Override
        public FieldValueConverter<T> getFieldValueConverter() {
            return this.fieldValueConverter;
        }

        @Override
        public ObjectBuilder<TypedFieldDescriptor<T>> withFieldValueConverter(FieldValueConverter<T> converter) {
            this.fieldValueConverter = Objects.requireNonNull(converter, "Undefined field value converter!");
            return this;
        }

        @Override
        protected TypedFieldDescriptor<T> createItemDescriptorInstance() {
            return new TypedFieldDescriptor<>(this);
        }
    }
    
    public static <T> DefineFieldValueConverter<T> newInstance(FlatDataFieldDescriptor<?> fieldDescriptor) {
        return new BuilderImpl<T>(fieldDescriptor);
    }

    private final FlatDataFieldDescriptor<?> decoratedFieldDescriptor;
    private final FieldValueConverter<T> fieldValueConverter;

    public TypedFieldDescriptor(BuildParams<T> builder) {
        this.decoratedFieldDescriptor = builder.getFieldDescriptor();
        this.fieldValueConverter = builder.getFieldValueConverter();
    }
    
    @Override
    public String getDefaultValue() {
        return this.decoratedFieldDescriptor.getDefaultValue();
    }

    @Override
    public String getName() {
        return this.decoratedFieldDescriptor.getName();
    }

    @Override
    public int getMinLength() {
        return this.decoratedFieldDescriptor.getMinLength();
    }

    @Override
    public TypedField<T> createItemEntity() {
        return new TypedField<T>(this, this.decoratedFieldDescriptor.createItemEntity());
    }

    @Override
    public TypedField<T> readItemEntityFrom(Reader source) {
        return new TypedField<T>(this, this.decoratedFieldDescriptor.readItemEntityFrom(source));
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataFieldDescriptor.Handler) {
            ((FlatDataFieldDescriptor.Handler) handler).handleConvertedFieldDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    /**
     * Get the field descriptor decorated by this descriptor instance.
     * 
     * @return the target field descriptor of this decorator. 
     */
    public FlatDataFieldDescriptor<?> getDecoratedFieldDescriptor() {
        return this.decoratedFieldDescriptor;
    }

    /**
     * Get the field value converter.
     * 
     * @return the field value converter instance.
     */
    public FieldValueConverter<T> getFieldValueConverter() {
        return this.fieldValueConverter;
    }
}
