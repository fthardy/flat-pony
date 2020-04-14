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
package de.fthardy.flatpony.core.structure.optional;

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.structure.FlatDataStructure;
import de.fthardy.flatpony.core.util.FieldReference;

import java.io.Writer;
import java.util.Optional;

/**
 * The implementation of the optional item entity type.
 *
 * @author Frank Timothy Hardy
 */
public class OptionalItemEntity extends AbstractFlatDataItemEntity<OptionalItemDescriptor>
        implements FlatDataStructure<OptionalItemDescriptor> {

    private final FieldReference.ReferencedField<Boolean> flagField;
    private FlatDataItemEntity<?> targetItem;

    /**
     * Creates a non-empty instance of this option item entity.
     *
     * @param descriptor the descriptor which created this item entity.
     * @param targetItem the target item entity.
     */
    OptionalItemEntity(
            OptionalItemDescriptor descriptor,
            FlatDataItemEntity<?> targetItem,
            FieldReference.ReferencedField<Boolean> flagField) {
        super(descriptor);
        this.targetItem = targetItem;
        this.flagField = flagField;
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
        if (handler instanceof FlatDataStructure.Handler) {
            ((FlatDataStructure.Handler) handler).handleOptionalItemEntity(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }

    /**
     * @return an optional providing the target item entity or is empty.
     */
    public Optional<FlatDataItemEntity<?>> getTargetItem() {
        return Optional.ofNullable(this.targetItem);
    }

    /**
     * Set a particular target item entity or discard the current target item entity.
     *
     * @param targetItem the target item entity to set or {@code null} to discard the current target item entity.
     *
     * @throws IllegalArgumentException when this optional item entity already has a target item entity or the given
     *                                  target item entity doesn't have the same descriptor as the target item
     *                                  descriptor of this optional items descriptor.
     */
    public void setTargetItem(FlatDataItemEntity<?> targetItem) {
        if (targetItem != null && targetItem.getDescriptor() != this.getDescriptor().getTargetItemDescriptor()) {
            throw new IllegalArgumentException(
                    "Invalid target item! Descriptor is not the target item descriptor of this optional items descriptor.");
        }
        this.targetItem = targetItem;
        updateFlagField();
    }

    /**
     * Create a new target item entity.
     *
     * @return the new target item entity.
     *
     * @throws IllegalArgumentException when this optional item entity already has a target item entity.
     */
    public FlatDataItemEntity<?> newTargetItem() {
        this.setTargetItem(this.getDescriptor().getTargetItemDescriptor().createItemEntity());
        return this.targetItem;
    }

    private void updateFlagField() {
        if (this.flagField != null) {
            this.flagField.setValue(this.targetItem != null);
        }
    }
}
