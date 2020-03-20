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

import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;

import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A pull read iterator implementation for field items.
 * 
 * @param <T> the type of the field represented by its descriptor implementation class.
 *     
 * @author Frank Timothy Hardy
 */
public final class FieldPullReadIterator<T extends FlatDataFieldDescriptor<?>> implements PullReadIterator {

    static String MSG_No_pull_read_event(FlatDataFieldDescriptor<?> descriptor) {
        return String.format(
                "Field-Item '%s' [%s] has no further pull read event!",
                descriptor.getName(),
                descriptor.getClass().getName());
    }

    private final T descriptor;
    private final Reader source;
    
    private boolean nextEvent = true;

    /**
     * Creates a new instance of this iterator.
     * 
     * @param descriptor the descriptor of the field.
     * @param source the source stream to read from.
     */
    public FieldPullReadIterator(T descriptor, Reader source) {
        this.descriptor = Objects.requireNonNull(descriptor);
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public boolean hasNextEvent() {
        return this.nextEvent;
    }

    @Override
    public void nextEvent(StreamReadHandler handler) {
        if (this.nextEvent) {
            handler.onFieldItem(this.descriptor, this.descriptor.readValue(this.source));
            this.nextEvent = false;
        } else {
            throw new NoSuchElementException(MSG_No_pull_read_event(this.descriptor));
        }
    }
}
