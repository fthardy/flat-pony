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

import java.io.Writer;

/**
 * Represents the model for flat data which can be read from or written to a character stream.
 * <p>
 * A flat data item represents and encapsulates a particular data section of a character stream (flat data stream). Each
 * item implementation has a pendant descriptor implementation. Both implementations are tied together and form a unit.
 * Each item has a reference to the descriptor which created it. By using {@link #writeTo(Writer)} the data represented
 * by an item instance can be written to a given character stream.
 * </p>
 *
 * @param <T> the type of the descriptor.
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataItem<T extends FlatDataItemDescriptor<?>> {

    /**
     * Get the descriptor of the flat data item.
     *
     * @return the descriptor which produced the receiving flat data item instance.
     */
    T getDescriptor();

    /**
     * Get the length of the flat data item.
     *
     * @return the length (of characters) of the receiving flat data item instance.
     */
    int getLength();

    /**
     * Write the data of the item to an output target.
     *
     * @param target the writer providing write access to the output target for writing the data of the receiving item
     *               instance to it.
     */
    void writeTo(Writer target);

    /**
     * Apply a handler to the receiving item instance.
     *
     * @param handler the handler to be applied.
     */
    void applyHandler(FlatDataItemHandler handler);
}
