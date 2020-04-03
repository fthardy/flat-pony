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

import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.field.ObservableField;
import de.fthardy.flatpony.core.field.ObservableFieldDescriptor;
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;

import java.util.Objects;

/**
 * Represents a reference to a field which is sometimes necessary to read particular structure items correctly.
 *
 * @param <T> the type of the field value.
 *           
 * @see de.fthardy.flatpony.core.structure.OptionalItemDescriptor
 * @see de.fthardy.flatpony.core.structure.SequenceItemDescriptor
 *     
 * @author Frank Timothy Hardy
 */
public final class FieldReference<T> {

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
        Builder<T> usingValueConverter(FieldValueConverter<T> valueConverter);
    }

    /**
     * Builds the field reference.
     * 
     * @param <T> the type of the field value.
     *     
     * @author Frank Timothy Hardy
     */
    public interface Builder<T> {

        /**
         * @return a new field reference instance.
         */
        FieldReference<T> build();
    }

    private interface BuildParams<T> {
        ObservableFieldDescriptor getFieldDescriptorDecorator();
        ThreadLocal<TypedFieldDecorator<T>> getThreadLocalFieldReference();
    }
    
    private static final class BuilderImpl<T> implements DefineValueConverter<T>, Builder<T>, BuildParams<T> {

        private final ObservableFieldDescriptor fieldDescriptorDecorator;
        private final ThreadLocal<TypedFieldDecorator<T>> fieldReference = new ThreadLocal<>();

        BuilderImpl(FlatDataFieldDescriptor<? extends FlatDataMutableField<? extends FlatDataFieldDescriptor<?>>> fieldDescriptor) {
            this.fieldDescriptorDecorator = ObservableFieldDescriptor.newInstance(
                    Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!")).build();
        }

        @Override
        public Builder<T> usingValueConverter(final FieldValueConverter<T> valueConverter) {
            fieldDescriptorDecorator.addObserver(new ObservableFieldDescriptor.Observer() {
                @Override
                public void onFieldEntityCreated(ObservableField field) {
                    fieldReference.set(new TypedFieldDecorator<T>(
                            field, Objects.requireNonNull(valueConverter, "Undefined value converter!")));
                }

                @Override
                public void onFieldEntityRead(ObservableField field) {
                    this.onFieldEntityCreated(field);
                }

                @Override
                public void onFieldValueRead(ObservableFieldDescriptor descriptor, String value) {
                    // TODO
                }
            });

            return this;
        }

        @Override
        public ObservableFieldDescriptor getFieldDescriptorDecorator() {
            return this.fieldDescriptorDecorator;
        }

        @Override
        public ThreadLocal<TypedFieldDecorator<T>> getThreadLocalFieldReference() {
            return this.fieldReference;
        }

        @Override
        public FieldReference<T> build() {
            return new FieldReference<>(this);
        }
    }

    /**
     * Create a builder to configure and create a new field reference instance.
     * 
     * @param fieldDescriptor the descriptor of the field to reference.
     *                        
     * @param <T> the target type of the field value.
     *           
     * @return a new builder instance.
     */
    public static <T> DefineValueConverter<T> newInstance(
            FlatDataFieldDescriptor<? extends FlatDataMutableField<? extends FlatDataFieldDescriptor<?>>> fieldDescriptor) {
        return new BuilderImpl<>(fieldDescriptor);
    }

    private final ObservableFieldDescriptor fieldDescriptorDecorator;
    private final ThreadLocal<TypedFieldDecorator<T>> threadLocalFieldReference;
    
    private FieldReference(BuildParams<T> params) {
        this.fieldDescriptorDecorator = params.getFieldDescriptorDecorator();
        this.threadLocalFieldReference = params.getThreadLocalFieldReference();
    }

    /**
     * Get the observable decorator of the fields descriptor.
     * 
     * @return the descriptor observable decorator.
     */
    public ObservableFieldDescriptor getFieldDescriptorDecorator() {
        return this.fieldDescriptorDecorator;
    }

    /**
     * Get the reference field for the current thread.
     * 
     * @return the reference field.
     */
    public TypedFieldDecorator<T> getReferencedField() {
        return this.threadLocalFieldReference.get();
    }
}
