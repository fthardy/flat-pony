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

import java.util.function.Predicate;

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
