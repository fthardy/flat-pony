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

import java.util.Objects;
import java.util.function.Predicate;


/**
 * An adapter implementation for a value constraint to adapt a {@link Predicate}.
 * 
 * @author Frank Timothy Hardy
 */
public final class PredicateAdaptingValueConstraint extends AbstractValueConstraint {
    
    private final Predicate<String> predicate;

    /**
     * Creates a new instance of this value constraint implementation.
     *
     * @param name the name of this constraint.  
     * @param predicate the predicate to be adapted.
     */
    public PredicateAdaptingValueConstraint(String name, Predicate<String> predicate) {
        super(name);
        this.predicate = Objects.requireNonNull(predicate, "Undefined predicate!");
    }

    @Override
    public boolean acceptValue(String value) {
        return this.predicate.test(value);
    }
}
