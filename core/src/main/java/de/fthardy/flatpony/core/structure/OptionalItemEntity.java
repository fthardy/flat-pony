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

import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The entity implementation for an optional item.
 *
 * @see OptionalItemDescriptor
 *
 * @author Frank Timothy Hardy
 */
public final class OptionalItemEntity extends AbstractFlatDataItemEntity<OptionalItemDescriptor>
        implements FlatDataStructure<OptionalItemDescriptor> {

    private FlatDataItemEntity<?> targetItem;

    /**
     * Creates an empty instance of this optional item entity.
     *
     * @param descriptor the descriptor which created this item entity.
     */
    OptionalItemEntity(OptionalItemDescriptor descriptor) {
        this(descriptor, null);
    }

    /**
     * Creates a non-empty instance of this option item entity.
     *
     * @param descriptor the descriptor which created this item entity.
     * @param targetItem the target item entity.
     */
    OptionalItemEntity(OptionalItemDescriptor descriptor, FlatDataItemEntity<?> targetItem) {
        super(descriptor);
        this.targetItem = targetItem;
    }

    @Override
    public int getLength() {
        return targetItem == null ? 0 : targetItem.getLength();
    }

    @Override
    public void writeTo(Writer target) {
        if (this.targetItem != null) {
            this.targetItem.writeTo(target);
        }
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        // TODO Implementation
    }

    @Override
    public List<FlatDataItemEntity<?>> getChildItems() {
        return this.targetItem == null ? Collections.emptyList() : Collections.singletonList(this.targetItem);
    }

    /**
     * @return {@code true} if this optional item has currently no target item entity. Otherwise {@code false} is
     * returned.
     */
    public boolean isEmpty() {
        return this.targetItem == null;
    }

    /**
     * @return an optional providing the target item entity or is empty.
     */
    public Optional<FlatDataItemEntity<?>> getTargetItem() {
        return Optional.of(this.targetItem);
    }

    /**
     * Discard the current referenced target item entity.
     * <p>
     * Discarding a target item entity doesn't destroy the target item entity. Only the internal reference to the item
     * is set to {@code null}. If the target item entity is still referenced somewhere it continues to exist. However,
     * it cannot be reattached to this item.
     * </p>
     */
    public void discardTargetItem() {
        if (this.targetItem != null) {
            this.targetItem = null;
        }
    }

    /**
     * Create a new target item entity. This item has to be empty otherwise an {@link IllegalStateException} is
     * thrown.
     *
     * @see #isEmpty()
     */
    public void newTargetItem() {
        if (this.targetItem != null) {
            throw new IllegalStateException("There is already a target item entity referenced. " +
                    "Make sure to clear the existing item before calling this method!");
        }
        this.targetItem = this.getDescriptor().createNewTargetItem();
    }
}
