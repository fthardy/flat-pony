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

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataMutableField;
import de.fthardy.flatpony.core.streamio.FieldPullReadIterator;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.Reader;
import java.util.*;

/**
 * The implementation of a descriptor for a field which can be observed.
 * <p>
 * This descriptor is a decorator implementation which can be used to extend any type of a mutable field.
 * An observable field allows to register {@link Observer}s. These observers are then notified whenever either a new 
 * field entity is created or read or a field value is read by a stream read method. 
 * </p>
 *
 * @see Observer
 *
 * @author Frank Timothy Hardy
 */
public class ObservableFieldDescriptor implements FlatDataFieldDescriptor<ObservableField> {
    
    /**
     * The interface definition for an observer of a field descriptor.
     *
     * @author Frank Timothy Hardy
     */
    public interface Observer {

        /**
         * Is called when a new field is created.
         *
         * @param field the new observable field.
         */
        void onFieldEntityCreated(ObservableField field);

        /**
         * Is called when a field has been read from a source stream.
         *
         * @param field the observable field read from a source stream.
         */
        void onFieldEntityRead(ObservableField field);

        /**
         * Is called when a field value is read from a source stream.
         * 
         * @param descriptor the descriptor which is calling this method.
         * @param value the read field value.
         */
        void onFieldValueRead(ObservableFieldDescriptor descriptor, String value);
    }

    /**
     * Allows to add observers to the descriptor.
     * 
     * @author Frank Timothy Hardy
     */
    public interface AddObservers extends ObjectBuilder<ObservableFieldDescriptor> {

        /**
         * Add a single observer.
         * 
         * @param observer the observer to add.
         *                 
         * @return the builder instance to add further observers or to create a new descriptor instance.
         */
        AddObservers addObserver(Observer observer);

        /**
         * Add several observers.
         * 
         * @param observers the observers to add.
         *
         * @return the builder instance to add further observers or to create a new descriptor instance.
         */
        AddObservers addObservers(Observer... observers);

        /**
         * Add several observers.
         * 
         * @param observers the observers to add.
         *                  
         * @return the builder instance to add further observers or to create a new descriptor instance.
         */
        AddObservers addObservers(Iterable<Observer> observers);
    }
    
    private interface BuildParams {
        FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> getFieldDescriptor();
        List<Observer> getObservers();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<ObservableFieldDescriptor>
            implements AddObservers, BuildParams {

        private final FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptor;
        private List<Observer> observers = new ArrayList<>();
        
        BuilderImpl(FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptor) {
            super(fieldDescriptor.getName());
            this.fieldDescriptor = Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!");
        }

        @Override
        public AddObservers addObserver(Observer observer) {
            if (this.observers.contains(observer)) {
                throw new IllegalArgumentException(MSG_OBSERVER_ALREADY_ADDED);
            }
            this.observers.add(Objects.requireNonNull(observer, "Undefined observer!"));
            return this;
        }

        @Override
        public AddObservers addObservers(Observer... observers) {
            return this.addObservers(Arrays.asList(Objects.requireNonNull(observers, "Undefined observers!")));
        }

        @Override
        public AddObservers addObservers(Iterable<Observer> observers) {
            Objects.requireNonNull(observers, "Undefined observers!").forEach(this::addObserver);
            return this;
        }

        @Override
        protected ObservableFieldDescriptor createItemDescriptorInstance() {
            return new ObservableFieldDescriptor(this);
        }

        @Override
        public FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> getFieldDescriptor() {
            return this.fieldDescriptor;
        }

        @Override
        public List<Observer> getObservers() {
            return this.observers;
        }
    }
    
    static final String MSG_OBSERVER_ALREADY_ADDED = "The given observer instance has already been added!";

    /**
     * Create a builder to configure and create a new instance of this field descriptor decorator.
     * 
     * @param fieldDescriptor the field descriptor to be observed.
     *                        
     * @return the builder instance.
     */
    public static AddObservers newInstance(FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> fieldDescriptor) {
        return new BuilderImpl(fieldDescriptor);
    }

    private final FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> observedFieldDescriptor;
    private final List<Observer> observers = Collections.synchronizedList(new ArrayList<>());
    
    private ObservableFieldDescriptor(BuildParams params) {
        this.observedFieldDescriptor = params.getFieldDescriptor();
        this.observers.addAll(params.getObservers());
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
    public ObservableField createItemEntity() {
        ObservableField newField = new ObservableField(this, this.observedFieldDescriptor.createItemEntity());
        synchronized (this.observers) {
            this.observers.forEach(o -> o.onFieldEntityCreated(newField));
        }
        return newField;
    }

    @Override
    public ObservableField readItemEntityFrom(Reader source) {
        ObservableField readField = new ObservableField(
                this, this.observedFieldDescriptor.readItemEntityFrom(source));
        synchronized (this.observers) {
            this.observers.forEach(o -> o.onFieldEntityRead(readField));
        }
        return readField; 
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {
        handler.onFieldItem(this, this.readValue(source));
    }

    @Override
    public PullReadIterator pullReadFrom(Reader source) {
        return new FieldPullReadIterator<>(this, source);
    }

    @Override
    public String readValue(Reader source) {
        String value = this.observedFieldDescriptor.readValue(source);
        synchronized (this.observers) {
            this.observers.forEach(o -> o.onFieldValueRead(this, value));
        }
        return value;
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataFieldDescriptor.Handler) {
            ((FlatDataFieldDescriptor.Handler) handler).handleObservableFieldDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    /**
     * @return the observed field descriptor.
     */
    public FlatDataFieldDescriptor<? extends FlatDataMutableField<?>> getObservedFieldDescriptor() {
        return this.observedFieldDescriptor;
    }

    /**
     * Add a new observer.
     *
     * @param observer the observer to add.
     */
    public void addObserver(Observer observer) {
        if (this.observers.contains(observer)) {
            throw new IllegalArgumentException(MSG_OBSERVER_ALREADY_ADDED);
        }
        this.observers.add(observer);
    }

    /**
     * Remove a particular observer.
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
