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

import de.fthardy.flatpony.core.field.observable.ObservableField;
import de.fthardy.flatpony.core.field.observable.ObservableFieldDescriptor;

/**
 * An observer implementation for an {@link ObservableFieldDescriptor} which buffers the notified field entity instance
 * or field value (in case of a stream read).
 * 
 * @author Frank Timothy Hardy
 */
public class BufferingFieldDescriptorObserver implements ObservableFieldDescriptor.Observer {
    
    private ObservableField bufferedField;
    private String bufferedFieldValue;
    
    @Override
    public void onFieldEntityCreated(ObservableField field) {
        this.bufferedField = field;
    }

    @Override
    public void onFieldEntityRead(ObservableField field) {
        this.bufferedField = field;
    }

    @Override
    public void onFieldValueRead(ObservableFieldDescriptor descriptor, String value) {
        this.bufferedFieldValue = value;
    }

    /**
     * @return the buffered field entity instance if one has been buffered. Otherwise {@code null}.
     */
    public ObservableField getBufferedField() {
        return this.bufferedField;
    }

    /**
     * @return the buffered field value if one has been buffered. Otherwise {@code null}.
     */
    public String getBufferedFieldValue() {
        return this.bufferedFieldValue;
    }
}
