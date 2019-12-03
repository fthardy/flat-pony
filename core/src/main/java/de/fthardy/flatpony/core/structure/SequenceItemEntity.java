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
import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.util.TypedFieldDecorator;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of the sequence item entity type.
 *
 * @author Frank Timothy Hardy
 */
public class SequenceItemEntity extends AbstractFlatDataItemEntity<SequenceItemDescriptor>
        implements FlatDataStructure<SequenceItemDescriptor> {

    private static List<FlatDataItemEntity<?>> createElementList(
            FlatDataItemDescriptor<? extends FlatDataItemEntity<?>> descriptor, int size) {
        List<FlatDataItemEntity<?>> elements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            elements.add(descriptor.createItem());
        }
        return elements;
    }

    private final List<FlatDataItemEntity<?>> elementItems;
    private final TypedFieldDecorator<Integer> countField;

    /**
     * Creates a new instance of this item entity.
     *
     * @param descriptor the descriptor which created this instance.
     * @param countField the count field entity instance or {@code null}.
     */
    SequenceItemEntity(SequenceItemDescriptor descriptor, TypedFieldDecorator<Integer> countField) {
        this(
                descriptor,
                createElementList(
                        descriptor.getElementItemDescriptor(),
                        countField == null ? 0 : countField.getTypedValue()),
                countField);
    }

    /**
     * Creates a new instance of this item entity.
     *
     * @param descriptor the descriptor which created this instance.
     * @param countField the count field entity instance or {@code null}.
     * @param elementItems the list of the element items.
     */
    SequenceItemEntity(
            SequenceItemDescriptor descriptor,
            List<FlatDataItemEntity<?>> elementItems,
            TypedFieldDecorator<Integer> countField) {
        super(descriptor);
        this.elementItems = new ArrayList<>(elementItems);
        this.countField = countField;
    }

    @Override
    public List<FlatDataItemEntity<?>> getChildItems() {
        return Collections.unmodifiableList(this.elementItems);
    }

    @Override
    public int getLength() {
        return this.elementItems.stream().mapToInt(FlatDataItemEntity::getLength).sum();
    }

    @Override
    public void writeTo(Writer target) {
        SequenceItemDescriptor.Multiplicity multiplicity = this.getDescriptor().getMultiplicity();
        if (multiplicity == null || multiplicity.isSizeWithinBounds(this.elementItems.size())) {
            this.elementItems.forEach(e -> e.writeTo(target));
        } else {
            throw new FlatDataWriteException(SequenceItemDescriptor.MSG_Multiplicity_constraint_violated(
                    this.getDescriptor().getName(), this.getDescriptor().getMultiplicity()));
        }
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataStructure.Handler) {
            ((FlatDataStructure.Handler) handler).handleSequenceItem(this);
        } else {
            handler.handleFlatDataItem(this);
        }
    }

    /**
     * Discard a particular element item from the list.
     *
     * @param elementItem the element item to discard.
     */
    public void discardElement(FlatDataItemEntity<?> elementItem) {
        if (!this.elementItems.remove(elementItem)) {
            throw new IllegalArgumentException("Invalid element item!");
        }
        this.updateCountField();
    }

    /**
     * Discard all element items.
     */
    public void discardAllElementItems() {
        this.elementItems.clear();
        this.updateCountField();
    }

    /**
     * Add an element item.
     *
     * @param elementItem the element item to add. Must have the same descriptor as all other element items.
     *
     * @throws IllegalArgumentException when the given element item entity already exists or doesn't have the element
     *                                  item descriptor from this optional items descriptor.
     */
    public void addElementItem(FlatDataItemEntity<?> elementItem) {
        if (!this.elementItems.contains(Objects.requireNonNull(elementItem, "Undefined element item entity!"))
                || elementItem.getDescriptor() != this.getDescriptor().getElementItemDescriptor()) {
            throw new IllegalArgumentException("Invalid element item!");
        }
        this.elementItems.add(elementItem);
        this.updateCountField();
    }

    /**
     * Create and add a new element item to the list and return it.
     *
     * @return the new element item.
     */
    public FlatDataItemEntity<?> addNewElementItem() {
        FlatDataItemEntity<?> newElementItem = this.getDescriptor().getElementItemDescriptor().createItem();
        this.addElementItem(newElementItem);
        return newElementItem;
    }

    private void updateCountField() {
        if (this.countField != null) {
            this.countField.setTypedValue(this.elementItems.size());
        }
    }
}
