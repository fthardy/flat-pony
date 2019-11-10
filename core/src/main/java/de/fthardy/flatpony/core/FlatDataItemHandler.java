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

/**
 * The interface definition for a handler which handles item instances.
 * <p>
 * This handler interface is part of the visitor pattern and takes the role of the visitor. The items are the visitable
 * elements and provide for this purpose the method
 * {@link FlatDataItem#applyHandler(FlatDataItemHandler)} which allows to apply an implementation instance of this
 * interface type.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataItemHandler {

    /**
     * Handle a flat data item.
     *
     * @param item the item to be handled by the receiving instance.
     */
    void handleFlatDataItem(FlatDataItem<?> item);
}
