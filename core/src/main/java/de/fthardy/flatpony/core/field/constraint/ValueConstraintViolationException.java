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

import de.fthardy.flatpony.core.FlatDataIOException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This runtime exception is thrown when a fields value constraints are violated.
 *
 * @author Frank Timothy Hardy
 */
public final class ValueConstraintViolationException extends FlatDataIOException {

    private static String message(String fieldName, String value, Set<String> constraintNames) {
        return String.format(
                "Setting the value [%s] at field '%s' violates the following constraints: %s",
                value, fieldName, String.join(", ", constraintNames));
    }

    private final String fieldName;
    private final String value;
    private final Set<String> constraintNames;

    /**
     * Creates a new instance of this runtime exception.
     *
     * @param fieldName the name of the field.
     * @param value the value which violates constraints.
     * @param constraintNames the violated constraints.
     */
    public ValueConstraintViolationException(String fieldName, String value, Set<String> constraintNames) {
        super(message(fieldName, value, constraintNames));

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
