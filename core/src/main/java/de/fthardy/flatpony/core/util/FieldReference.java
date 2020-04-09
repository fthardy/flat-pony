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

import de.fthardy.flatpony.core.field.ObservableField;
import de.fthardy.flatpony.core.field.ObservableFieldDescriptor;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;

import java.util.Objects;

/**
 * Represents a reference to an observable flat data field.
 * <p>
 * A field reference encapsulates an {@link ObservableFieldDescriptor}. It provides type specific access to the field
 * entity or value of the observed field depending on the method which is used to create or read the field. If a new
 * instance of the observed field is created or read the field entity is accessible through 
 * {@link #getReferencedField()}. If the observed field is read by a stream read method (push or pull read) then the
 * read field value is accessible through {@link #getFieldValue()}. 
 * </p>
 *
 * @param <T> the type of the field value.
 *           
 * @see de.fthardy.flatpony.core.structure.OptionalItemDescriptor
 * @see de.fthardy.flatpony.core.structure.SequenceItemDescriptor
 *     
 * @author Frank Timothy Hardy
 */
public class FieldReference<T> {

    /**
     * Demands the definition of a value converter for the field value.
     * 
     * @param <T> the target type for the field value.
     *     
     * @author Frank Timothy Hardy
     */
    public interface DefineValueConverter<T> {

        /**
         * Define the value converter.
         * 
         * @param valueConverter the value converter to use.
         *                       
         * @return the builder instance for further configuration.
         */
        ObjectBuilder<FieldReference<T>> usingValueConverter(FieldValueConverter<T> valueConverter);
    }
    
    /**
     * A field entity adapter implementation which allows to access the field value of a referenced field in a given type.
     *
     * @author Frank Timothy Hardy
     */
    public static class ReferencedField<T> {

        private final ObservableField referencedField;
        private final FieldValueConverter<T> valueConverter;

        /**
         * Create a new instance of this field adapter.
         *
         * @param referencedField the field entity instance to adapt.
         * @param valueConverter the boolean value converter.
         */
        ReferencedField(ObservableField referencedField, FieldValueConverter<T> valueConverter) {
            this.referencedField = Objects.requireNonNull(referencedField, "Undefined referenced field!");
            this.valueConverter = Objects.requireNonNull(valueConverter, "Undefined value converter!");
        }

        /**
         * @return the flag value.
         */
        public T getValue() {
            return this.valueConverter.convertFromFieldValue(this.referencedField.getValue());
        }

        /**
         * @param value the new flag value.
         */
        public void setValue(T value) {
            this.referencedField.setValue(this.valueConverter.convertToFieldValue(value));
        }
    }
    
    private interface BuildParams<T> {
        ObservableFieldDescriptor getFieldDescriptor();
        FieldValueConverter<T> getValueConverter();
    }
    
    private static final class BuilderImpl<T> 
            implements DefineValueConverter<T>, ObjectBuilder<FieldReference<T>>, BuildParams<T> {

        private final ObservableFieldDescriptor fieldDescriptor;
        private FieldValueConverter<T> valueConverter;

        BuilderImpl(ObservableFieldDescriptor fieldDescriptor) {
            this.fieldDescriptor = Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!");
        }

        @Override
        public ObjectBuilder<FieldReference<T>> usingValueConverter(final FieldValueConverter<T> valueConverter) {
            this.valueConverter = Objects.requireNonNull(valueConverter, "Undefined value converter!");
            return this;
        }

        @Override
        public ObservableFieldDescriptor getFieldDescriptor() {
            return this.fieldDescriptor;
        }

        @Override
        public FieldValueConverter<T> getValueConverter() {
            return this.valueConverter;
        }

        @Override
        public FieldReference<T> build() {
            return new FieldReference<>(this);
        }
    }

    /**
     * Create a builder to configure and create a new field reference instance.
     * 
     * @param fieldDescriptor the descriptor of the field to be referred to.
     *                        
     * @param <T> the type of the field value.
     *           
     * @return a new builder instance.
     */
    public static <T> DefineValueConverter<T> newInstance(ObservableFieldDescriptor fieldDescriptor) {
        return new BuilderImpl<>(fieldDescriptor);
    }

    private final FieldValueConverter<T> valueConverter;
    private final BufferingFieldDescriptorObserver observer = new BufferingFieldDescriptorObserver();
    
    private FieldReference(BuildParams<T> params) {
        this.valueConverter = params.getValueConverter();
        params.getFieldDescriptor().addObserver(this.observer);
    }

    /**
     * @return a new field adapter instance adapting the referenced field if one exists. Otherwise {@code null}.
     */
    public ReferencedField<T> getReferencedField() {
        ObservableField bufferedField = this.observer.getBufferedField();
        return bufferedField == null ? null : new ReferencedField<>(bufferedField, valueConverter);
    }

    /**
     * @return the field value of the referenced field if one exists. Otherwise {@code null}.
     */
    public T getFieldValue() {
        String fieldValue = this.observer.getBufferedFieldValue();
        return fieldValue == null ? null : this.valueConverter.convertFromFieldValue(fieldValue);
    }
}
