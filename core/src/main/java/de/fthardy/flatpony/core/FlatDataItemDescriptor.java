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

import java.io.Reader;

/**
 * Represents a descriptor of a flat data item.
 * <p>
 * A flat data item descriptor describes a particular data section of a character stream (flat data stream). Each
 * descriptor implementation has a pendant item implementation. Both implementations are tied together and form a unit.
 * The descriptor serves as a factory for item instances. With {@link #createItem()} new item instances can be created
 * or with {@link #readItemFrom(Reader)} an item can be created by reading flat data from a given character stream.
 * </p>
 *
 * @param <T> the type of the item produced by the descriptor.
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataItemDescriptor<T extends FlatDataItem<?>> {

    /**
     * The name of the descriptor.
     *
     * @return the name of the receiving descriptor instance.
     */
    String getName();

    /**
     * Create a new item.
     *
     * @return the new item instance.
     */
    T createItem();

    /**
     * Read the data from a given input source and provide an item instance that encapsulates the read data.
     *
     * @param source the reader providing the data from the input source.
     *
     * @return a new item instance with the read data.
     */
    T readItemFrom(Reader source);
}
