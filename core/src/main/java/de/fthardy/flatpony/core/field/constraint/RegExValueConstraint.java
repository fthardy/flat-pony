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
import java.util.regex.Pattern;

/**
 * A value constraint implementation which uses a regular expression for validating a value.
 * 
 * @author Frank Timothy Hardy.
 */
public final class RegExValueConstraint extends AbstractValueConstraint {

    private final Pattern regExPattern;
    
    /**
     * Create a new instance of this value constraint implementation.
     *
     * @param name the name of the constraint.
     * @param expression the regular expression to use.
     */
    public RegExValueConstraint(String name, String expression) {
        super(name);
        this.regExPattern = Pattern.compile(
                Objects.requireNonNull(expression, "Undefined regular expression!"));
    }

    @Override
    public boolean acceptValue(String value) {
        return this.regExPattern.matcher(value).matches();
    }
}
