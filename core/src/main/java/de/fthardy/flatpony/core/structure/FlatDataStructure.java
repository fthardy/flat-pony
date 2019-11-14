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
package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItemEntity;

import java.util.List;

/**
 * The interface for a flat data structure.
 * <p>
 * A flat data structure is a flat data item entity which represents a particular data structure.
 * </p>
 *
 * @param <T> the type of the structure descriptor which creates the structure type.
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataStructure<T extends FlatDataStructureDescriptor<?>> extends FlatDataItemEntity<T> {

    /**
     * The interface for a handler which can handle the various structure type implementations provided by the core package.
     *
     * @author Frank Timothy Hardy.
     */
    interface Handler extends FlatDataItemEntity.Handler {

        /**
         * Handle a composite item.
         *
         * @param item the item to be handled by the receiving instance.
         */
        void handleCompositeItem(CompositeItemEntity item);

        /**
         * Handle a delimited item.
         *
         * @param item the item to be handled by the receiving instance.
         */
        void handleDelimitedItem(DelimitedItemEntity item);
    }

    /**
     * @return the list of child items.
     */
    List<FlatDataItemEntity<?>> getChildItems();
}
