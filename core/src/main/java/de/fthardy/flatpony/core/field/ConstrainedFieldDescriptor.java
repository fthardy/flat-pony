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
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraintViolationException;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The implementation of a descriptor for a field which should have value constraints.
 * <p>
 * This descriptor is used as a decorator for any field descriptor. It allows to add {@link ValueConstraint}s. The given
 * constraints are used to validate the value of any field entity created by that decorated descriptor. Any field entity
 * provided by the decorated descriptor is decorated by an instance of {@link ConstrainedField}. 
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class ConstrainedFieldDescriptor implements FlatDataFieldDescriptor<ConstrainedField> {

    /**
     * Demands the definition of a value constraint.
     * 
     * @author Frank Timothy Hardy
     */
    public interface AddConstraint {

        /**
         * Add a new value constraint.
         * <p>
         * The constraint must have a unique name.
         * </p>
         * 
         * @param constraint the constraint to add.
         *                   
         * @return the builder instance for adding further constraints or instance creation.
         */
        AddFurtherConstraints addConstraint(ValueConstraint constraint);
    }

    /**
     * Allows to add further value constraints.
     * 
     * @author Frank Timothy Hardy
     */
    public interface AddFurtherConstraints 
            extends AddConstraint, ObjectBuilder<ConstrainedFieldDescriptor> {
        // Aggregator interface with no further method definitions
    }
    
    private interface BuildParams {

        FlatDataFieldDescriptor<?> getFieldDescriptor();
        Set<ValueConstraint> getConstraints();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<ConstrainedFieldDescriptor> 
            implements AddFurtherConstraints, BuildParams {

        private final FlatDataFieldDescriptor<?> fieldDescriptor;
        private final Map<String, ValueConstraint> constraintsByName = new LinkedHashMap<>();

        BuilderImpl(FlatDataFieldDescriptor<?> fieldDescriptor) {
            super("none");
            this.fieldDescriptor = Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!");
        }

        @Override
        public AddFurtherConstraints addConstraint(ValueConstraint constraint) {
            if (this.constraintsByName.containsKey(
                    Objects.requireNonNull(constraint, "Undefined constraint!").getName())) {
                throw new IllegalArgumentException("The name of the constraints must be unique!");
            }
            this.constraintsByName.put(constraint.getName(), constraint);
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
     * Creates a builder for configuration and creation of an instance of this descriptor decorator.
     * 
     * @param fieldDescriptor the field descriptor to decorate.
     *                        
     * @return the builder instance.
     */
    public static AddConstraint newInstance(FlatDataFieldDescriptor<?> fieldDescriptor) {
        return new BuilderImpl(fieldDescriptor);
    }

    private final FlatDataFieldDescriptor<?> fieldDescriptor;
    private final Set<ValueConstraint> constraints;

    private ConstrainedFieldDescriptor(BuildParams params) {
        this.fieldDescriptor = params.getFieldDescriptor();
        this.constraints = params.getConstraints();
        this.checkForConstraintViolation(fieldDescriptor.getDefaultValue());
    }

    @Override
    public String getName() {
        return this.fieldDescriptor.getName();
    }

    @Override
    public String getDefaultValue() {
        return this.fieldDescriptor.getDefaultValue();
    }

    @Override
    public int getMinLength() {
        return this.fieldDescriptor.getMinLength();
    }

    @Override
    public ConstrainedField createItemEntity() {
        return new ConstrainedField(this, this.fieldDescriptor.createItemEntity().asMutableField());
    }

    @Override
    public ConstrainedField readItemEntityFrom(Reader source) {
        return new ConstrainedField(this, this.fieldDescriptor.readItemEntityFrom(source).asMutableField());
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataFieldDescriptor.Handler) {
            ((FlatDataFieldDescriptor.Handler) handler).handleConstrainedFieldDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    /**
     * Get the decorated field descriptor.
     *
     * @return the decorated field descriptor.
     */
    public FlatDataFieldDescriptor<?> getFieldDescriptor() {
        return this.fieldDescriptor;
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
        return constraints.stream().filter(c -> c.test(value)).map(ValueConstraint::getName).collect(
                Collectors.toCollection(LinkedHashSet::new));
    }
}
