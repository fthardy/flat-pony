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

import de.fthardy.flatpony.core.streamio.StreamReadHandler;

import java.io.Reader;

/**
 * The interface for a flat data item descriptor.
 * <p>
 * A flat data item descriptor describes a particular data section of a character (flat data) stream. Each descriptor
 * implementation has a corresponding item entity implementation. Both implementations are tightly coupled and represent
 * the item as a unit. In other words: a (flat data) item is only a conceptual term which is represented by a pair of a
 * descriptor and entity implementation.
 * </p>
 * <p>
 * The descriptor serves as a factory for creating new instances of the entity. A descriptor can be used to create as
 * many entities as desired. Each of the entity instances has a reference to the descriptor instance that created it.
 * There are two ways to create an entity. Either create a new entity instance by calling {@link #createItemEntity()} or by
 * reading from a given character stream by calling {@link #readItemEntityFrom(Reader)}.
 * </p>
 *
 * @param <T> the type of the item produced by the descriptor.
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataItemDescriptor<T extends FlatDataItemEntity<?>> {

    /**
     * The interface definition for a handler which handles descriptor instances.
     * <p>
     * This handler interface is part of the visitor pattern and takes the role of the visitor. The descriptors are the
     * visitable elements and provide for this purpose the method
     * {@link FlatDataItemDescriptor#applyHandler(Handler)} which allows to apply an implementation
     * instance of this interface type.
     * </p>
     *
     * @author Frank Timothy Hardy
     */
    interface Handler {

        /**
         * Handles an item descriptor.
         *
         * @param descriptor the descriptor instance to be handled by the receiving instance.
         */
        void handleFlatDataItemDescriptor(FlatDataItemDescriptor<?> descriptor);
    }

    /**
     * The name of the descriptor.
     *
     * @return the name of the receiving descriptor instance.
     */
    String getName();

    /**
     * Get the minimum length of the item.
     *
     * @return the minimum length of the item. If the minimum length is not known return 0.
     */
    int getMinLength();

    /**
     * Create a new item entity instance.
     *
     * @return the new item entity instance which represents the root of an item entity model (IEM).
     */
    T createItemEntity();

    /**
     * Read the data from a given source stream and provide an item entity instance that encapsulates the read data.
     *
     * @param source the reader providing the data from the source stream.
     *
     * @return a new item entity instance which represents the root of an item entity model (IEM).
     */
    T readItemEntityFrom(Reader source);

    /**
     * Start to read the data of the receiving item from a given source stream in a push fashion.
     * <p>
     * When starting a push read the reading client has to provide an implementation of a {@link StreamReadHandler}
     * which gets constantly pushed read events by the read control algorithm. This means the reading client has no
     * control over the reading process itself. It is just processing read events which are being pushed by the read
     * control algorithm until the read process is finished.
     * </p>
     * 
     * @param source the reader of the source stream.
     * @param handler the push event handler.
     */
    void pushReadFrom(Reader source, StreamReadHandler handler);

    /**
     * Start to puĺl read from a given source stream.
     * 
     * @param source the reader of the source stream.
     *               
     * @return the iterator to pull read events an control the read process.
     */
    // TODO PullReadIterator pullReadFromSourceStream(Reader source);

    /**
     * Start a stream write to a given target stream.
     * 
     * @param writer the writer to write the data to.
     * @param provider the provider which provides the field data to write.
     */
    // TODO void writeToTargetStream(Writer writer, FieldValueProvider provider);

    /**
     * Apply a handler to the receiving descriptor.
     *
     * @param handler the handler instance.
     */
    void applyHandler(Handler handler);
}
