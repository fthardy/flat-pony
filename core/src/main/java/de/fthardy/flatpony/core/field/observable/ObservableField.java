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
package de.fthardy.flatpony.core.field.observable;

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataMutableField;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation for an observable field.
 * <p>
 * An observable field is a wrapper field item which allows to register observers which are going to be informed about
 * value changes at the underlying field.   
 * </p>
 * 
 * @see Observer
 * 
 * @author Frank Timothy Hardy
 */
public class ObservableField extends AbstractFlatDataItemEntity<ObservableFieldDescriptor>
        implements FlatDataMutableField<ObservableFieldDescriptor> {

    /**
     * The interface definition for an observer of a mutable flat data field. 
     * 
     * @author Frank Timothy Hardy
     */
    public interface Observer {

        /**
         * Is called when the value of the observed field changes.
         * 
         * @param field the observable field which calls this method.
         * @param value the actual value.
         * @param newValue the new value.
         */
        void onValueChange(ObservableField field, String value, String newValue);
    }
    
    private final FlatDataMutableField<?> observedField;
    private final List<Observer> observers = Collections.synchronizedList(new ArrayList<>()); 

    /**
     * Create a new instance of this field wrapper.
     *
     * @param descriptor the descriptor which created this item.
     * @param observedField the field to be observed.
     */
    ObservableField(ObservableFieldDescriptor descriptor, FlatDataMutableField<?> observedField) {
        super(descriptor);
        this.observedField = Objects.requireNonNull(observedField);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this) +
                "[descriptor=" + this.getDescriptor().toString() +
                ", observed-field=" + this.observedField.toString() + "]";
    }

    @Override
    public void setValue(String value) {
        String currentValue = this.observedField.getValue();
        this.observedField.setValue(value);
        synchronized (this.observers) {
            this.observers.forEach(o -> o.onValueChange(this, currentValue, value));
        }
    }

    @Override
    public String getValue() {
        return this.observedField.getValue();
    }

    @Override
    public FlatDataMutableField<ObservableFieldDescriptor> asMutableField() {
        return this;
    }

    @Override
    public int getLength() {
        return this.observedField.getLength();
    }

    @Override
    public void writeTo(Writer target) {
        this.observedField.writeTo(target);
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataField.Handler) {
            ((FlatDataField.Handler) handler).handleObservableField(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }

    /**
     * @return the observed field.
     */
    public FlatDataMutableField<?> getObservedField() {
        return this.observedField;
    }

    /**
     * Add an observer.
     * 
     * @param observer the observer to add.
     */
    public void addObserver(Observer observer) {
        if (this.observers.contains(observer)) {
            throw new IllegalArgumentException(ObservableFieldDescriptor.MSG_OBSERVER_ALREADY_ADDED);
        }
        this.observers.add(observer);
    }

    /**
     * Remove an observer.
     * 
     * @param observer the observer to remove.
     */
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    /**
     * @return the observer list.
     */
    List<Observer> getObservers() {
        return Collections.unmodifiableList(this.observers);
    }
}
