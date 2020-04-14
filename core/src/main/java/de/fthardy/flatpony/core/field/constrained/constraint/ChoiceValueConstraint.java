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
package de.fthardy.flatpony.core.field.constrained.constraint;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A value constraint implementation which checks a value against a given set of values. By default a checked value has
 * to equal to one of the values of the given value set (including). However, this logic can be inverted so that the set
 * of values defines the non-allowed (excluding) values.
 * 
 * @author Frank Timothy Hardy
 */
public final class ChoiceValueConstraint extends AbstractValueConstraint {
    
    private final Set<String> values;
    private final boolean allowed;

    /**
     * Creates a new instance of this value constraint implementation
     * 
     * @param name the name of the constraint.
     * @param allowedValues the set of the allowed values.
     */
    public ChoiceValueConstraint(String name, Set<String> allowedValues) {
        this(name, allowedValues, true);
    }
    
    /**
     * Creates a new instance of this value constraint implementation.
     *
     * @param name the name of the constraint.
     * @param values the set of values.
     * @param valuesAreAllowed set to {@code true} when the set of values defines the allowed values. Otherwise if
     * {@code false} is set the value set defines the values which are NOT allowed.
     */
    public ChoiceValueConstraint(String name, Set<String> values, boolean valuesAreAllowed) {
        super(name);
        this.values = new HashSet<>(Objects.requireNonNull(values, "Undefined values!"));
        if (this.values.isEmpty()) {
            throw new IllegalArgumentException("No values defined!");
        }
        this.allowed = valuesAreAllowed;
    }

    @Override
    public boolean acceptValue(String value) {
        return this.values.contains(value) == this.allowed;
    }
}
