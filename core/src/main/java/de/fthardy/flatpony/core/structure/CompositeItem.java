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

import de.fthardy.flatpony.core.AbstractFlatDataItem;
import de.fthardy.flatpony.core.FlatDataItem;
import de.fthardy.flatpony.core.FlatDataItemHandler;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of a composite item.
 * <p>
 * A composite item is a composition of several other flat data items.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class CompositeItem extends AbstractFlatDataItem<CompositeItemDescriptor>
        implements FlatDataStructure<CompositeItemDescriptor> {

    private final List<FlatDataItem<?>> items;

    /**
     * Creates a new instance of this composite item.
     *
     * @param descriptor the descriptor which is creating this item instance.
     * @param items the list of items which make up this composite item.
     */
    CompositeItem(CompositeItemDescriptor descriptor, List<FlatDataItem<?>> items) {
        super(descriptor);
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    @Override
    public int getLength() {
        return items.stream().mapToInt(FlatDataItem::getLength).sum();
    }

    @Override
    public void writeTo(Writer target) {
        items.forEach(dataItem -> dataItem.writeTo(target));
    }

    @Override
    public List<FlatDataItem<?>> getChildItems() {
        return this.items;
    }

    @Override
    public void applyHandler(FlatDataItemHandler handler) {
        if (handler instanceof FlatDataStructureHandler) {
            ((FlatDataStructureHandler) handler).handleCompositeItem(this);
        } else {
            handler.handleFlatDataItem(this);
        }
    }
}
