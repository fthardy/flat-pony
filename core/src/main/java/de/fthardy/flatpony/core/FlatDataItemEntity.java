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
 * The interface for a flat data item entity.
 * <p>
 * A flat data item entity represents and encapsulates a particular data section of a character (flat data) stream. Each
 * item entity implementation has its corresponding descriptor implementation. Both implementations are tightly coupled
 * and represent the item as a unit. In other words: a (flat data) item is only a conceptual term which is represented
 * by a pair of a descriptor and entity implementation.
 * </p>
 * <p>
 * While an item entity is representing concrete data it provides access to the descriptor which created it and its
 * length in characters. Concrete sub types may allow to change the data of the entity and will provide a corresponding
 * API for this purpose. However, every item entity can be written to a given character stream by calling
 * {@link #writeTo(Writer)}.
 * </p>
 *
 * @param <T> the type of the descriptor.
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataItemEntity<T extends FlatDataItemDescriptor<?>> {

    /**
     * The interface definition for a handler which handles item instances.
     * <p>
     * This handler interface is part of the visitor pattern and takes the role of the visitor. The items are the
     * visitable elements and provide for this purpose the method {@link FlatDataItemEntity#applyHandler(Handler)} which
     * allows to apply an implementation instance of this interface type.
     * </p>
     *
     * @author Frank Timothy Hardy
     */
    interface Handler {

        /**
         * Handle a flat data item entity.
         *
         * @param item the item entity to be handled by the receiving instance.
         */
        void handleFlatDataItemEntity(FlatDataItemEntity<?> item);
    }

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
    void applyHandler(Handler handler);
}
