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

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A decorator implementation to extend a field descriptor implementation with a set of constraints that are applied on
 * the field value.
 *
 * @author Frank Timothy Hardy
 */
public final class ConstrainedFieldDescriptor implements FlatDataFieldDescriptor<ConstrainedField> {

    /**
     * Checks if the constraints have all unambiguous names.
     * If any value constraints are ambiguous an {@code IllegalArgumentException} is thrown.
     *
     * @param constraints the set of value constraints.
     *
     * @return an unmodifiable set with the given constraints. The order of the elements is preserved.
     */
    private static Set<ValueConstraint> makeUnmodifiableSetFrom(Set<ValueConstraint> constraints) {
        if (Objects.requireNonNull(constraints, "Undefined value constraints!").isEmpty()) {
            throw new IllegalArgumentException("No value constraints defined!");
        } else {
            List<String> names = constraints.stream().map(ValueConstraint::getName).collect(Collectors.toList());
            Set<String> uniqueNames = new HashSet<>(names);
            if (uniqueNames.size() != names.size()) {
                uniqueNames.forEach(names::remove);
                throw new IllegalArgumentException("Value constraints with ambiguous names: " +
                        String.join(", ", names));
            }
            return Collections.unmodifiableSet(new LinkedHashSet<>(constraints));
        }
    }

    private final FlatDataFieldDescriptor<?> fieldDescriptor;
    private final Set<ValueConstraint> constraints;

    /**
     * Creates a new instance of this descriptor.
     *
     * @param fieldDescriptor the descriptor of the field to be extended with this decorator.
     * @param constraints the set of value constraints. The order of the elements in the given set is preserved. The
     *                    constraint check will test in this given order.
     */
    public ConstrainedFieldDescriptor(FlatDataFieldDescriptor<?> fieldDescriptor, Set<ValueConstraint> constraints) {
        this.fieldDescriptor = Objects.requireNonNull(fieldDescriptor, "Undefined field descriptor!");
        this.constraints = makeUnmodifiableSetFrom(constraints);
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
    public ConstrainedField createItem() {
        return new ConstrainedField(this, this.fieldDescriptor.createItem().asMutableField());
    }

    @Override
    public ConstrainedField readItemFrom(Reader source) {
        return new ConstrainedField(this, this.fieldDescriptor.readItemFrom(source).asMutableField());
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
