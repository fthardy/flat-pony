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
import de.fthardy.flatpony.core.streamio.StreamReadHandler;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A decorator implementation for a field descriptor which allows to be observed.
 *
 * @author Frank Timothy Hardy
 *
 * @see Observer
 */
public final class ObservableFieldDescriptorDecorator implements FlatDataFieldDescriptor<FlatDataMutableField<?>> {

    /**
     * The observer interface which allows to get informed about a newly created or a read field entities.
     *
     * @author Frank Timothy Hardy
     */
    public interface Observer {

        /**
         * Is called when a new field is created.
         *
         * @param field the new field entity instance.
         */
        // TODO Does the field has to be mutabel - don't think so
        void onFieldEntityCreated(FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> field);

        /**
         * Is called when a field entity instance has been read from a source stream.
         *
         * @param field the field entity instance read from a source stream.
         */
        // TODO Does the field has to be mutabel - don't think so
        void onFieldEntityRead(FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> field);
    }

    private final FlatDataFieldDescriptor<? extends FlatDataMutableField<? extends FlatDataFieldDescriptor<?>>> observedFieldDescriptor;
    private final List<Observer> observers = new ArrayList<>();

    /**
     * Creates a new instance of this decorator.
     *
     * @param fieldDescriptor the field descriptor to observe.
     */
    public ObservableFieldDescriptorDecorator(
            FlatDataFieldDescriptor<? extends FlatDataMutableField<? extends FlatDataFieldDescriptor<?>>> fieldDescriptor) {
        if (fieldDescriptor instanceof ObservableFieldDescriptorDecorator) {
            throw new IllegalArgumentException("It doesn't make sense to observe an observable field descriptor!");
        }
        this.observedFieldDescriptor = Objects.requireNonNull(fieldDescriptor, "No field descriptor defined!");
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
    public FlatDataMutableField<?> createItemEntity() {
        FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> newField = this.observedFieldDescriptor.createItemEntity();
        this.observers.forEach(o -> o.onFieldEntityCreated(newField));
        return newField;
    }

    @Override
    public FlatDataMutableField<?> readItemEntityFrom(Reader source) {
        FlatDataMutableField<? extends FlatDataFieldDescriptor<?>> readField = 
                this.observedFieldDescriptor.readItemEntityFrom(source);
        this.observers.forEach(o -> o.onFieldEntityRead(readField));
        return readField;
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {
        this.observedFieldDescriptor.pushReadFrom(source, handler);
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
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }
}
