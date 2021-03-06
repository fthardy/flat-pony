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
package de.fthardy.flatpony.core.field.constrained;

import de.fthardy.flatpony.core.FlatDataItemDescriptorHandler;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.constrained.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constrained.constraint.ValueConstraintViolationException;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The implementation of a descriptor for a field which has value constraints.
 * <p>
 * This descriptor is a decorator implementation which can be used to extend any type of field.
 * A constrained field has one or more {@link ValueConstraint}s which are used to validate the field value when it is
 * read, set or modified. Be aware that the default value of a decorated field is also validated by the constraints
 * hence the default value must conform to these constraints.
 * </p>
 *
 * @author Frank Timothy Hardy
 * 
 * @see ValueConstraint
 */
public class ConstrainedFieldDescriptor implements FlatDataFieldDescriptor<ConstrainedField> {
    
    /**
     * Demands the addition of at least one value constraint.
     * <p>
     * There is no limitation on how many value constraints can be added but any value constraint instance can only be
     * added once and the names of the value constraints are unique. If a particular value constraint instance is added
     * a second time or another value constraint with the same name has already been added then an
     * {@link IllegalArgumentException} is going to be thrown. The order in which the constraints are added is the order
     * in which a field value is passed to the value constraint instances during a validation.
     * </p>
     * 
     * @see ConstrainedFieldDescriptor
     * 
     * @author Frank Timothy Hardy
     */
    public interface AddConstraints extends ObjectBuilder<ConstrainedFieldDescriptor> {

        /**
         * Add a new value constraint.
         * 
         * @param constraint the constraint to add.
         *                   
         * @return the builder instance for adding further constraints or instance creation.
         */
        AddConstraints addConstraint(ValueConstraint constraint);

        /**
         * Add several value constraints.
         * 
         * @param constraints the constraints to add.
         *
         * @return the builder instance for adding further constraints or instance creation.
         */
        AddConstraints addConstraints(ValueConstraint... constraints);

        /**
         * Add several value constraints.
         *
         * @param constraints the constraints to add.
         *
         * @return the builder instance for adding further constraints or instance creation.
         */
        AddConstraints addConstraints(Iterable<ValueConstraint> constraints);
    }
    
    private interface BuildParams {
        FlatDataFieldDescriptor<?> getFieldDescriptor();
        Set<ValueConstraint> getConstraints();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<ConstrainedFieldDescriptor> 
            implements AddConstraints, BuildParams {

        private final FlatDataFieldDescriptor<?> fieldDescriptor;
        private final Map<String, ValueConstraint> constraintsByName = new LinkedHashMap<>();

        BuilderImpl(FlatDataFieldDescriptor<?> fieldDescriptor) {
            super(fieldDescriptor.getName());
            this.fieldDescriptor = Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!");
        }

        @Override
        public AddConstraints addConstraint(ValueConstraint constraint) {
            if (this.constraintsByName.containsKey(
                    Objects.requireNonNull(constraint, "Undefined constraint!").getName())) {
                throw new IllegalArgumentException("Constraints must be unique!");
            }
            this.constraintsByName.put(constraint.getName(), constraint);
            return this;
        }

        @Override
        public AddConstraints addConstraints(ValueConstraint... constraints) {
            return this.addConstraints(Arrays.asList(Objects.requireNonNull(
                    constraints, "Undefined value constraints!")));
        }

        @Override
        public AddConstraints addConstraints(Iterable<ValueConstraint> constraints) {
            Objects.requireNonNull(constraints, "Undefined value constraints!").forEach(this::addConstraint);
            return this;
        }

        @Override
        public FlatDataFieldDescriptor<?> getFieldDescriptor() {
            return this.fieldDescriptor;
        }

        @Override
        public Set<ValueConstraint> getConstraints() {
            return Collections.unmodifiableSet(new LinkedHashSet<>(this.constraintsByName.values()));
        }

        @Override
        protected ConstrainedFieldDescriptor createItemDescriptorInstance() {
            return new ConstrainedFieldDescriptor(this);
        }
    }

    /**
     * Creates a builder for configuration and creation of an instance of this descriptor implementation.
     * 
     * @param fieldDescriptor the field descriptor to be constrained.
     *                        
     * @return the builder instance.
     */
    public static AddConstraints newInstance(FlatDataFieldDescriptor<?> fieldDescriptor) {
        return new BuilderImpl(fieldDescriptor);
    }

    private final FlatDataFieldDescriptor<?> decoratedFieldDescriptor;
    private final Set<ValueConstraint> constraints;

    private ConstrainedFieldDescriptor(BuildParams params) {
        this.decoratedFieldDescriptor = params.getFieldDescriptor();
        this.constraints = params.getConstraints();
        this.checkForConstraintViolation(decoratedFieldDescriptor.getDefaultValue());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this) +
                "[decorated-descriptor=" + this.decoratedFieldDescriptor.toString() + "]";
    }

    @Override
    public String getName() {
        return this.decoratedFieldDescriptor.getName();
    }

    @Override
    public String getDefaultValue() {
        return this.decoratedFieldDescriptor.getDefaultValue();
    }

    @Override
    public int getMinLength() {
        return this.decoratedFieldDescriptor.getMinLength();
    }

    @Override
    public ConstrainedField createItemEntity() {
        return new ConstrainedField(this, this.decoratedFieldDescriptor.createItemEntity());
    }

    @Override
    public ConstrainedField readItemEntityFrom(Reader source) {
        return new ConstrainedField(this, this.decoratedFieldDescriptor.readItemEntityFrom(source));
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {
        this.decoratedFieldDescriptor.pushReadFrom(source, handler);
    }

    @Override
    public PullReadIterator pullReadFrom(Reader source) {
        return this.decoratedFieldDescriptor.pullReadFrom(source);
    }

    @Override
    public String readValue(Reader source) {
        return this.decoratedFieldDescriptor.readValue(source);
    }

    @Override
    public <H extends FlatDataItemDescriptorHandler> H applyHandler(H handler) {
        if (handler instanceof ConstrainedFieldDescriptorHandler) {
            ((ConstrainedFieldDescriptorHandler) handler).handleConstrainedFieldDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
        return handler;
    }

    /**
     * Get the field descriptor decorated by this descriptor.
     *
     * @return the decorated field descriptor.
     */
    public FlatDataFieldDescriptor<?> getDecoratedFieldDescriptor() {
        return this.decoratedFieldDescriptor;
    }

    /**
     * Check if a given value violates any of the constraints.
     * <p>
     * If so a {@link ValueConstraintViolationException} is thrown.
     * </p>
     *
     * @param value the value to check. Might be {@code null}.
     */
    void checkForConstraintViolation(String value) {
        Set<String> constraintViolations = this.determineConstraintViolationsFor(value);
        if (!constraintViolations.isEmpty()) {
            throw new ValueConstraintViolationException(this.getName(), value, constraintViolations);
        }
    }

    /**
     * Check a given value if it violates any constraints defined by the receiving descriptor instance.
     *
     * @param value the field value to check.
     *
     * @return the list of the violated constraint names.
     */
    private Set<String> determineConstraintViolationsFor(String value) {
        return constraints.stream().filter(c -> !c.acceptValue(value)).map(ValueConstraint::getName).collect(
                Collectors.toCollection(LinkedHashSet::new));
    }
}
