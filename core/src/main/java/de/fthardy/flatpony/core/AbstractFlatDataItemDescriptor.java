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
package de.fthardy.flatpony.core;

import java.util.Objects;

/**
 * An abstract base implementation for flat data item descriptor implementations.
 *
 * @param <T> the item type created by the descriptor type.
 *
 * @author Frank Timothy Hardy
 */
public abstract class AbstractFlatDataItemDescriptor<T extends FlatDataItemEntity<?>> implements FlatDataItemDescriptor<T> {

    private final String name;

    /**
     * Initialise a new instance of a flat data item descriptor.
     *
     * @param name the name of the descriptor. Must not be empty.
     */
    protected AbstractFlatDataItemDescriptor(String name) {
        if (Objects.requireNonNull(name, "Undefined descriptor name!").isEmpty()) {
            throw new IllegalArgumentException("Descriptor name cannot be empty!");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this) + 
                "[name=" + this.getName() + "]";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getMinLength() {
        return 0;
    }
}
