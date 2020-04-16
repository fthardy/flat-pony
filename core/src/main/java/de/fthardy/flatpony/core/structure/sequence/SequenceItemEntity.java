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
package de.fthardy.flatpony.core.structure.sequence;

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntityHandler;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.structure.FlatDataStructure;
import de.fthardy.flatpony.core.util.FieldReference;

import java.io.Writer;
import java.util.ArrayList;
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
            SequenceItemDescriptor descriptor, FieldReference.ReferencedField<Integer> countField) {
        int size = countField == null ? descriptor.getMultiplicity().getMinOccurrences() : countField.getValue();
        List<FlatDataItemEntity<?>> elements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            elements.add(descriptor.getElementItemDescriptor().createItemEntity());
        }
        return elements;
    }
    
    static String MSG_Element_count_is_not_equal_to_count_field_value(String itemName, int countFieldValue) {
        return String.format("The element count of sequence item '%s' doesn't equal the count field value of %d!",
                itemName, countFieldValue);
    }

    private final List<FlatDataItemEntity<?>> elementItemEntities;
    private final FieldReference.ReferencedField<Integer> countField;

    /**
     * Creates a new instance of this item entity.
     * <p>
     * This constructor is called by the descriptor when a new item entity instance is created. 
     * </p>
     *
     * @param descriptor the descriptor which created this instance.
     * @param countField the count field entity instance or {@code null}.
     */
    SequenceItemEntity(SequenceItemDescriptor descriptor, FieldReference.ReferencedField<Integer> countField) {
        this(descriptor, createElementList(descriptor, countField), countField);
    }

    /**
     * Creates a new instance of this item entity.
     * <p>
     * This constructor is called by the descriptor when a item entity instance is read from a source stream. 
     * </p>
     *
     * @param descriptor the descriptor which created this instance.
     * @param elementItemEntities the list of the read element item entities.
     * @param countField the count field entity instance or {@code null}.
     */
    SequenceItemEntity(
            SequenceItemDescriptor descriptor,
            List<FlatDataItemEntity<?>> elementItemEntities,
            FieldReference.ReferencedField<Integer> countField) {
        super(descriptor);
        this.elementItemEntities = new ArrayList<>(elementItemEntities);
        this.countField = countField;
    }

    @Override
    public int getLength() {
        return this.elementItemEntities.stream().mapToInt(FlatDataItemEntity::getLength).sum();
    }

    @Override
    public void writeTo(Writer target) {
        if (this.getDescriptor().getMultiplicity().isSizeWithinBounds(this.elementItemEntities.size())) {
            Integer countFieldValue = this.countField == null ? null : this.countField.getValue();
            if (countFieldValue != null && countFieldValue != this.elementItemEntities.size()) {
                throw new FlatDataWriteException(SequenceItemEntity.MSG_Element_count_is_not_equal_to_count_field_value(
                        this.getDescriptor().getName(), countFieldValue));
            }
            this.elementItemEntities.forEach(e -> e.writeTo(target));
        } else {
            throw new FlatDataWriteException(SequenceItemDescriptor.MSG_Multiplicity_constraint_violated(
                    this.getDescriptor().getName(), this.getDescriptor().getMultiplicity()));
        }
    }

    @Override
    public <H extends FlatDataItemEntityHandler> H applyHandler(H handler) {
        if (handler instanceof SequenceItemEntityHandler) {
            ((SequenceItemEntityHandler) handler).handleSequenceItemEntity(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
        return handler;
    }

    /**
     * @return a new list with the element item entities.
     */
    public List<FlatDataItemEntity<?>> getElementItemEntities() {
        return new ArrayList<>(this.elementItemEntities);
    }

    /**
     * Create and add a new element item to the list.
     * 
     * @return the newly created element item entity.
     */
    public FlatDataItemEntity<?> createAndAddNewElementItemEntity() {
        FlatDataItemEntity<?> element = this.getDescriptor().getElementItemDescriptor().createItemEntity();
        this.addElementItemEntity(element);
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
    public void addElementItemEntity(FlatDataItemEntity<?> elementItem) {
        if (this.elementItemEntities.contains(Objects.requireNonNull(elementItem, "Undefined element item entity!"))
                || elementItem.getDescriptor() != this.getDescriptor().getElementItemDescriptor()) {
            throw new IllegalArgumentException("Invalid element item entity!");
        }
        this.elementItemEntities.add(elementItem);
        this.updateCountField();
    }

    /**
     * Discard a particular element item entity from the list.
     *
     * @param elementItem the element item to discard.
     */
    public void discardElementItemEntity(FlatDataItemEntity<?> elementItem) {
        if (!this.elementItemEntities.remove(elementItem)) {
            throw new IllegalArgumentException("Invalid element item entity!");
        }
        this.updateCountField();
    }

    /**
     * Discard all element item entities.
     */
    public void discardAllElementItemEntities() {
        this.elementItemEntities.clear();
        this.updateCountField();
    }

    private void updateCountField() {
        if (this.countField != null) {
            this.countField.setValue(this.elementItemEntities.size());
        }
    }
}
