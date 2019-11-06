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

import de.fthardy.flatpony.core.AbstractFlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataIOException;
import de.fthardy.flatpony.core.FlatDataReadException;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract base implementation for flat data field descriptors.
 *
 * @param <T> the field type created by the descriptor.
 *
 * @author Frank Timothy Hardy
 */
public abstract class AbstractFlatDataFieldDescriptor<T extends FlatDataField<?>>
        extends AbstractFlatDataItemDescriptor<T> implements FlatDataFieldDescriptor<T> {

    /**
     * An extended predicate which represents a field value constraint.
     * <p>
     * The semantics of this predicate is that it checks if a given value is violating the constraint represented by the
     * receiving implementation instance. If so {@link #test(Object)} must return {@code true}. Otherwise, if the value
     * is valid {@code false} must be returned.
     * </p>
     *
     * @author Frank Timothy Hardy
     */
    public interface ValueConstraint extends Predicate<String> {

        /**
         * Get the name of the value constraint.
         * <p>
         * The name of a constraint serves as an identifier which might be used as a simple name for direct display or
         * as a key for resolving a (probably localized) message.
         * </p>
         *
         * @return the name of the constraint.
         */
        String getName();
    }

    /**
     * The default implementation of a value constraint.
     *
     * @author Frank Timothy Hardy
     */
    public static final class DefaultValueConstraint implements ValueConstraint {

        private final String name;
        private final Predicate<String> predicate;

        /**
         * Create a new instance of a value constraint.
         *
         * @param name the name of the constraint.
         * @param predicate the constraint predicate.
         */
        public DefaultValueConstraint(String name, Predicate<String> predicate) {
            this.name = Objects.requireNonNull(name, "Undefined value constraint name!");
            this.predicate = Objects.requireNonNull(predicate, "Undefined value constraint predicate!");
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public boolean test(String value) {
            return this.predicate.test(value);
        }
    }

    /**
     * This runtime exception is thrown when a fields value constraints are violated.
     *
     * @author Frank Timothy Hardy
     */
    public static final class ValueConstraintViolationException extends FlatDataIOException {

        private final String fieldName;
        private final String value;
        private final Set<String> constraintNames;

        ValueConstraintViolationException(String fieldName, String value, Set<String> constraintNames) {
            this.fieldName = fieldName;
            this.value = value;
            this.constraintNames = Collections.unmodifiableSet(new LinkedHashSet<>(constraintNames));
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getValue() {
            return value;
        }

        public Set<String> getConstraintNames() {
            return constraintNames;
        }
    }

    /**
     * Checks if the constraints have all unambiguous names.
     * If any value constraints are ambiguous an {@code IllegalArgumentException} is thrown.
     *
     * @param constraints the set of value constraints.
     *
     * @return an unmodifiable set with the given constraints. The order of the elements is preserved.
     */
    static Set<ValueConstraint> makeUnmodifiableSetFrom(Set<ValueConstraint> constraints) {
        if (Objects.requireNonNull(constraints, "Undefined value constraints!").isEmpty()) {
            return Collections.emptySet();
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

    private final Set<ValueConstraint> constraints;

    /**
     * Initialise a new instance of a field descriptor.
     *
     * @param name the name of the field.
     */
    protected AbstractFlatDataFieldDescriptor(String name) {
        this(name, Collections.emptySet());
    }

    /**
     * Initialise a new instance of a field descriptor.
     *
     * @param name the name of the field.
     * @param constraints the set of value constraints. The order of the elements in the given set is preserved. The
     *                    constraint check will test in this given order.
     */
    protected AbstractFlatDataFieldDescriptor(String name, Set<ValueConstraint> constraints) {
        super(name);
        this.constraints = makeUnmodifiableSetFrom(constraints);
    }

    @Override
    public Set<String> determineConstraintViolationsFor(String value) {
        return constraints.stream().filter(c -> c.test(value)).map(ValueConstraint::getName).collect(
                Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Check if a given value violates any of the constraints.
     * <p>
     * If so a {@link ValueConstraintViolationException} is thrown.
     * </p>
     *
     * @param value the value to check. Might be {@code null}.
     *
     * @return the given value.
     */
    protected String checkForConstraintViolation(String value) {
        Set<String> constraintViolations = this.determineConstraintViolationsFor(value);
        if (constraintViolations.isEmpty()) {
            return value;
        } else {
            throw new ValueConstraintViolationException(this.getName(), value, constraintViolations);
        }
    }
}
