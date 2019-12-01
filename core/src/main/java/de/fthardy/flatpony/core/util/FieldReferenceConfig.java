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
import de.fthardy.flatpony.core.field.converter.FieldValueConverter;

import java.util.Objects;

/**
 * A configuration for linking with another field.
 * <p>
 * This is only a helper class which is usually not held by a client. It takes an
 * {@link ObservableFieldDescriptorDecorator observable field} and a {@link FieldValueConverter} of the fields value
 * type. When {@link #linkWithField()} is called an observer is created and added to the observable field the observer
 * uses a thread local object to store a read or created field item entity instance.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class FieldReferenceConfig<T> {

    private final ObservableFieldDescriptorDecorator observableFieldDescriptor;
    private final FieldValueConverter<T> valueConverter;

    /**
     * Create a new instance of this config.
     *
     * @param fieldDescriptor the descriptor of the field which represents the field.
     * @param valueConverter the converter which converts the field value.
     */
    public FieldReferenceConfig(
            FlatDataFieldDescriptor<? extends FlatDataMutableField<? extends FlatDataFieldDescriptor<?>>> fieldDescriptor,
            FieldValueConverter<T> valueConverter) {
        this.observableFieldDescriptor = new ObservableFieldDescriptorDecorator(
                Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!"));
        this.valueConverter = Objects.requireNonNull(valueConverter, "Undefined value converter!");
    }

    /**
     * @return the observable field descriptor decorating the descriptor of the referenced field.
     */
    public ObservableFieldDescriptorDecorator getObservableFieldDescriptor() {
        return this.observableFieldDescriptor;
    }

    /**
     * Link with the field.
     * <p>
     * Creates an observer instance and adds it to the observable field descriptor. The observer will create a
     * {@link TypedFieldDecorator typed field} for any field entity which has been read or created by the observed field
     * descriptor and stores it into a thread local which is returned by this method.
     * </p>
     *
     * @return the thread local which will contain the field item entity to be linked with.
     */
    public ThreadLocal<TypedFieldDecorator<T>> linkWithField() {
        final ThreadLocal<TypedFieldDecorator<T>> threadLocalField = new ThreadLocal<>();

        observableFieldDescriptor.addObserver(new ObservableFieldDescriptorDecorator.FieldObserver() {
            @Override
            public void newFieldCreated(FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> field) {
                threadLocalField.set(new TypedFieldDecorator<T>(field, valueConverter));
            }

            @Override
            public void fieldRead(FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> field) {
                this.newFieldCreated(field);
            }
        });
        return threadLocalField;
    }
}
