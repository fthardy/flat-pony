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
 * An abstract base implementation for a flat data item implementation.
 *
 * @param <T> the descriptor type which creates the item type.
 *
 * @author Frank Timothy Hardy
 */
public abstract class AbstractFlatDataItemEntity<T extends FlatDataItemDescriptor<?>> implements FlatDataItemEntity<T> {

    private final T descriptor;

    /**
     * Initialise a new instance of this flat data item.
     *
     * @param descriptor the descriptor which created this item.
     */
    protected AbstractFlatDataItemEntity(T descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "Undefined descriptor!");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this) + 
                "[descriptor=" + this.getDescriptor().toString() + "]";
    }

    @Override
    public T getDescriptor() {
        return this.descriptor;
    }
}
