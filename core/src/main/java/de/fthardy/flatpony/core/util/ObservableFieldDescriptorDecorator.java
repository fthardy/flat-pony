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

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A decorator implementation for a field descriptor which allows to be observed.
 *
 * @author Frank Timothy Hardy
 *
 * @see FieldObserver
 */
public class ObservableFieldDescriptorDecorator implements FlatDataFieldDescriptor<FlatDataMutableField<?>> {

    /**
     * The observer interface which allows to get informed about a new created field or a read field.
     *
     * @author Frank Timothy Hardy
     */
    public interface FieldObserver {

        /**
         * Is called by the observable field descriptor decorator when a new field is created.
         *
         * @param field the new field entity instance.
         */
        void newFieldCreated(FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> field);

        /**
         * Is called by the observable field descriptor decorator when a field has been read from a source stream.
         *
         * @param field the field read from a source stream.
         */
        void fieldRead(FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> field);
    }

    private final FlatDataFieldDescriptor<? extends FlatDataMutableField<? extends FlatDataFieldDescriptor<?>>> observedFieldDescriptor;
    private final List<FieldObserver> fieldObservers = new ArrayList<>();

    /**
     * Creates a new instance of this descriptor decorator.
     *
     * @param observedFieldDescriptor the field descriptor to observe.
     */
    public ObservableFieldDescriptorDecorator(
            FlatDataFieldDescriptor<? extends FlatDataMutableField<? extends FlatDataFieldDescriptor<?>>> observedFieldDescriptor) {
        this.observedFieldDescriptor = Objects.requireNonNull(observedFieldDescriptor);
    }

    @Override
    public String getName() {
        return this.observedFieldDescriptor.getName();
    }

    @Override
    public String getDefaultValue() {
        return this.observedFieldDescriptor.getDefaultValue();
    }

    @Override
    public int getMinLength() {
        return this.observedFieldDescriptor.getMinLength();
    }

    @Override
    public FlatDataMutableField<?> createItem() {
        FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> newField = this.observedFieldDescriptor.createItem();
        this.fieldObservers.forEach(o -> o.newFieldCreated(newField));
        return newField;
    }

    @Override
    public FlatDataMutableField<?> readItemFrom(Reader source) {
        FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> readField = this.observedFieldDescriptor.readItemFrom(source);
        this.fieldObservers.forEach(o -> o.fieldRead(readField));
        return readField;
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        this.observedFieldDescriptor.applyHandler(handler);
    }

    /**
     * Add a new observer.
     *
     * @param observer the observer to add.
     */
    public void addObserver(FieldObserver observer) {
        this.fieldObservers.add(observer);
    }
}
