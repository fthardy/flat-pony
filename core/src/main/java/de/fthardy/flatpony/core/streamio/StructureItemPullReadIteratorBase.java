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
package de.fthardy.flatpony.core.streamio;

import de.fthardy.flatpony.core.structure.FlatDataStructureDescriptor;

import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Abstract base implementation for pull read iterators for structure items.
 * 
 * @author Frank Timothy Hardy
 */
public abstract class StructureItemPullReadIteratorBase<T extends FlatDataStructureDescriptor<?>>
        implements PullReadIterator {

    public static String MSG_No_pull_read_event(String itemName, String descriptorClassName) {
        return String.format("Structure-Item '%s' [%s] has no further pull read event!", itemName, descriptorClassName);
    }

    protected final T descriptor;
    protected final Reader source;

    private boolean startEventSent;
    private boolean endEventSent;

    /**
     * Initialise a new instance of this iterator.
     *
     * @param descriptor the descriptor instance representing the item to read.
     * @param source the source stream to read from.
     */
    protected StructureItemPullReadIteratorBase(T descriptor, Reader source) {
        this.source = Objects.requireNonNull(source);
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    @Override
    public boolean hasNextEvent() {
        return !this.endEventSent;
    }

    @Override
    public void nextEvent(StreamReadHandler handler) {
        if (this.endEventSent) {
            throw new NoSuchElementException(MSG_No_pull_read_event(this.descriptor.getName(), this.descriptor.getClass().getSimpleName()));
        } else if (this.startEventSent) {
            if (handleContent(handler)) {
                handler.onStructureItemEnd(this.descriptor);
                this.endEventSent = true;
            }
        } else {
            handler.onStructureItemStart(this.descriptor);
            fetchContent();
            this.startEventSent = true;
        }
    }

    /**
     * Is called to handle the content of the structure item.
     * As long this method is returns {@code false} it is called with the {@link #nextEvent(StreamReadHandler)}
     * invocation again. 
     * 
     * @param handler the stream read handler.
     *                
     * @return {@code true} if the content has been read and the end of the structure item is reached.
     */
    protected abstract boolean handleContent(StreamReadHandler handler);

    /**
     * Is called to determine the content of the structure item.
     * Will be called directly after the start event has been sent.
     */
    protected abstract void fetchContent();
}
