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
package de.fthardy.flatpony.core.field.constraint;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * The default implementation of a value constraint which simply takes a {@link Predicate} to which it delegates.
 *
 * @author Frank Timothy Hardy
 */
public final class DefaultValueConstraint extends AbstractValueConstraint {

    private final String name;
    private final Predicate<String> predicate;

    /**
     * Create a new instance of a value constraint.
     *
     * @param name the name of the constraint.
     * @param predicate the constraint predicate.
     */
    public DefaultValueConstraint(String name, Predicate<String> predicate) {
        if (Objects.requireNonNull(name, "Undefined constraint name!").isEmpty()) {
            throw new IllegalArgumentException("Constraint name cannot be empty!");
        }
        this.name = name;
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
