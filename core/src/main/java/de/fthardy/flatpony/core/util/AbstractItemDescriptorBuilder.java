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
package de.fthardy.flatpony.core.util;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;

import java.util.Objects;

/**
 * An abstract base implementation for an item descriptor builder.
 * <p>
 * Allows to invoke {@link #build()} only once. This implementation is not thread safe!
 * </p>
 * 
 * @param <T> the type of the item descriptor to be configured and created by the builder.
 *     
 * @author Frank Timothy Hardy
 */
public abstract class AbstractItemDescriptorBuilder<T extends FlatDataItemDescriptor<?>> implements ObjectBuilder<T> {
    
    private final String descriptorName;
    
    private boolean buildHasBeenInvoked;

    /**
     * Initialise a new instance of this builder implementation.
     * 
     * @param descriptorName the name for the new item descriptor.
     */
    protected AbstractItemDescriptorBuilder(String descriptorName) {
        this.descriptorName = Objects.requireNonNull(descriptorName, "Undefined name for the item descriptor!");
        if (this.descriptorName.isEmpty()) {
            throw new IllegalArgumentException("Item descriptor name cannot be empty!");
        }
    }

    @Override
    public T build() {
        if (buildHasBeenInvoked) {
            throw new IllegalStateException(
                    "The item descriptor instance has already been created by this builder instance!");
        }
        buildHasBeenInvoked = true;
        
        return this.createItemDescriptorInstance();
    }

    /**
     * @return the name of the descriptor.
     */
    public String getDescriptorName() {
        return this.descriptorName;
    }

    /**
     * Create a new instance of the item descriptor.
     * 
     * @return the new item descriptor instance.
     */
    protected abstract T createItemDescriptorInstance();
}
