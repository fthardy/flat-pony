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

/**
 * The implementation of the delimited item entity type.
 * <p>
 * A delimited item entity represents an item entity which has an extra delimiter at its end.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public class DelimitedItemEntity extends AbstractFlatDataItemEntity<DelimitedItemDescriptor>
        implements FlatDataStructure<DelimitedItemDescriptor> {

    static String MSG_Write_failed(String itemName) {
        return String.format("Failed to write delimited item '%s' to target stream!", itemName);
    }

    private final FlatDataItemEntity<?> targetItem;

    /**
     * Creates a new instance of this item.
     *
     * @param descriptor the descriptor which is creating this item.
     * @param targetItem the (delimited) item.
     */
    DelimitedItemEntity(DelimitedItemDescriptor descriptor, FlatDataItemEntity<?> targetItem) {
        super(descriptor);
        this.targetItem = targetItem;
    }

    @Override
    public int getLength() {
        return targetItem.getLength();
    }

    @Override
    public void writeTo(Writer target) {
        try {
            targetItem.writeTo(target);
            target.write(this.getDescriptor().getDelimiter());
        } catch (IOException e) {
            throw new FlatDataWriteException(MSG_Write_failed(this.getDescriptor().getName()), e);
        }
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataStructure.Handler) {
            ((FlatDataStructure.Handler) handler).handleDelimitedItemEntity(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }

    /**
     * @return the inner (delimited) item entity.
     */
    public FlatDataItemEntity<?> getTargetItem() {
        return this.targetItem;
    }
}
