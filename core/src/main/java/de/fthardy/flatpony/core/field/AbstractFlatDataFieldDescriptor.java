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

import java.util.Objects;

/**
 * Abstract base implementation for flat data field descriptors.
 *
 * @param <T> the field type created by the descriptor.
 *
 * @author Frank Timothy Hardy
 */
public abstract class AbstractFlatDataFieldDescriptor<T extends FlatDataField<?>>
        extends AbstractFlatDataItemDescriptor<T> implements FlatDataFieldDescriptor<T> {

    private final String defaultValue;

    /**
     * Initialise a new instance of a field descriptor.
     *
     * @param name the name of the field.
     * @param defaultValue a default value.
     */
    protected AbstractFlatDataFieldDescriptor(String name, String defaultValue) {
        super(name);
        this.defaultValue = Objects.requireNonNull(defaultValue, "Undefined default value!");
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}
