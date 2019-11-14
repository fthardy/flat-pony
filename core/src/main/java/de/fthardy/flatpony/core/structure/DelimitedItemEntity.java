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

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataWriteException;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of a delimited item entity.
 * <p>
 * A delimited item entity represents an item entity which has an extra delimiter at its end.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class DelimitedItemEntity extends AbstractFlatDataItemEntity<DelimitedItemDescriptor>
        implements FlatDataStructure<DelimitedItemDescriptor> {

    static String MSG_Write_failed(String itemName) {
        return String.format("Failed to write delimited item '%s' to target stream!", itemName);
    }

    private final FlatDataItemEntity<?> item;

    /**
     * Creates a new instance of this item.
     *
     * @param descriptor the descriptor which is creating this item.
     * @param item the inner item.
     */
    DelimitedItemEntity(DelimitedItemDescriptor descriptor, FlatDataItemEntity<?> item) {
        super(descriptor);
        this.item = item;
    }

    @Override
    public int getLength() {
        return item.getLength();
    }

    @Override
    public void writeTo(Writer target) {
        try {
            item.writeTo(target);
            target.write(this.getDescriptor().getDelimiter());
        } catch (IOException e) {
            throw new FlatDataWriteException(MSG_Write_failed(this.getDescriptor().getName()), e);
        }
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataStructure.Handler) {
            ((FlatDataStructure.Handler) handler).handleDelimitedItem(this);
        } else {
            handler.handleFlatDataItem(this);
        }
    }

    @Override
    public List<FlatDataItemEntity<?>> getChildItems() {
        return Collections.singletonList(this.item);
    }
}
