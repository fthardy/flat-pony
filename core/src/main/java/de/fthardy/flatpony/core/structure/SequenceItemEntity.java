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
import java.util.List;
import java.util.Objects;

/**
 * The implementation of the sequence item entity type.
 *
 * @author Frank Timothy Hardy
 */
public final class SequenceItemEntity extends AbstractFlatDataItemEntity<SequenceItemDescriptor>
        implements FlatDataStructure<SequenceItemDescriptor> {

    private static List<FlatDataItemEntity<?>> createElementList(
            SequenceItemDescriptor descriptor, TypedFieldDecorator<Integer> countField) {
        int size = countField == null ? 
                descriptor.getMultiplicity().getMinOccurrences() :
                countField.getTypedValue();
        List<FlatDataItemEntity<?>> elements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            elements.add(descriptor.getElementItemDescriptor().createItemEntity());
        }
        return elements;
    }

    private final List<FlatDataItemEntity<?>> elements;
    private final TypedFieldDecorator<Integer> countField;

    /**
     * Creates a new instance of this item entity.
     * <p>
     * This constructor is called by the descriptor when a new item entity instance is created. 
     * </p>
     *
     * @param descriptor the descriptor which created this instance.
     * @param countField the count field entity instance or {@code null}.
     */
    SequenceItemEntity(SequenceItemDescriptor descriptor, TypedFieldDecorator<Integer> countField) {
        this(descriptor, createElementList(descriptor, countField), countField);
    }

    /**
     * Creates a new instance of this item entity.
     * <p>
     * This constructor is called by the descriptor when a item entity instance is read from a source stream. 
     * </p>
     *
     * @param descriptor the descriptor which created this instance.
     * @param elements the list of the read element item entities.
     * @param countField the count field entity instance or {@code null}.
     */
    SequenceItemEntity(
            SequenceItemDescriptor descriptor,
            List<FlatDataItemEntity<?>> elements,
            TypedFieldDecorator<Integer> countField) {
        super(descriptor);
        this.elements = new ArrayList<>(elements);
        this.countField = countField;
    }

    @Override
    public int getLength() {
        return this.elements.stream().mapToInt(FlatDataItemEntity::getLength).sum();
    }

    @Override
    public void writeTo(Writer target) {
        SequenceItemDescriptor.Multiplicity multiplicity = this.getDescriptor().getMultiplicity();
        if (multiplicity == null || multiplicity.isSizeWithinBounds(this.elements.size())) {
            this.elements.forEach(e -> e.writeTo(target));
        } else {
            throw new FlatDataWriteException(SequenceItemDescriptor.MSG_Multiplicity_constraint_violated(
                    this.getDescriptor().getName(), this.getDescriptor().getMultiplicity()));
        }
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataStructure.Handler) {
            ((FlatDataStructure.Handler) handler).handleSequenceItemEntity(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }

    /**
     * @return a new list with the element item entities.
     */
    public List<FlatDataItemEntity<?>> getElements() {
        return new ArrayList<>(this.elements);
    }

    /**
     * Create and add a new element item to the list.
     * 
     * @return the newly created element item entity.
     */
    public FlatDataItemEntity<?> createAndAddNewElement() {
        FlatDataItemEntity<?> element = this.getDescriptor().getElementItemDescriptor().createItemEntity();
        this.addElement(element);
        return element;
    }

    /**
     * Add an element item entity.
     *
     * @param elementItem the element item entity to add. Must have the same descriptor as all other element item 
     *                    entities.
     *
     * @throws IllegalArgumentException when the given element item entity already exists or doesn't have the element
     *                                  item descriptor from this optional items descriptor.
     */
    public void addElement(FlatDataItemEntity<?> elementItem) {
        if (!this.elements.contains(Objects.requireNonNull(elementItem, "Undefined element item entity!"))
                || elementItem.getDescriptor() != this.getDescriptor().getElementItemDescriptor()) {
            throw new IllegalArgumentException("Invalid element item entity!");
        }
        this.elements.add(elementItem);
        this.updateCountField();
    }

    /**
     * Discard a particular element item entity from the list.
     *
     * @param elementItem the element item to discard.
     */
    public void discardElement(FlatDataItemEntity<?> elementItem) {
        if (!this.elements.remove(elementItem)) {
            throw new IllegalArgumentException("Invalid element item entity!");
        }
        this.updateCountField();
    }

    /**
     * Discard all element item entities.
     */
    public void discardAllElements() {
        this.elements.clear();
        this.updateCountField();
    }

    private void updateCountField() {
        if (this.countField != null) {
            this.countField.setTypedValue(this.elements.size());
        }
    }
}
