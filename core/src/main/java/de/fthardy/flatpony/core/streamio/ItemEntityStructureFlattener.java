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
package de.fthardy.flatpony.core.streamio;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.*;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeField;
import de.fthardy.flatpony.core.structure.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An item entity handler implementation which is used during a pull read process in a trial and error scenario to
 * flatten the structure of a pre-read item entity so that it can be processed in a pull read fashion by using a
 * {@link PullReadFieldHandler} for this purpose.
 * 
 * @author Frank Timothy Hardy
 * 
 * @see PullReadFieldHandler
 */
public final class ItemEntityStructureFlattener implements FlatDataField.Handler, FlatDataStructure.Handler {

    static String MSG_Unsupported_item_entity(FlatDataItemEntity<?> itemEntity) {
        return String.format("Unsupported item entity '%s' [%s]!",
                itemEntity.getDescriptor().getName(), itemEntity.getClass().getName());
    }

    private final List<FlatDataItemEntity<?>> flattenedItemEntities = new ArrayList<>();
    
    @Override
    public void handleConstantField(ConstantField field) {
        flattenedItemEntities.add(field);
    }

    @Override
    public void handleDelimitedField(DelimitedField field) {
        flattenedItemEntities.add(field);
    }

    @Override
    public void handleFixedSizeField(FixedSizeField field) {
        flattenedItemEntities.add(field);
    }

    @Override
    public void handleConstrainedField(ConstrainedField field) {
        field.getDecoratedField().applyHandler(this);
    }

    @Override
    public void handleTypedField(TypedField<?> field) {
        field.getDecoratedField().applyHandler(this);
    }

    @Override
    public void handleObservableField(ObservableField field) {
        field.getObservedField().applyHandler(this);
    }

    @Override
    public void handleCompositeItemEntity(CompositeItemEntity item) {
        flattenedItemEntities.add(item);
        item.getComponentItemEntities().forEach(i -> i.applyHandler(this));
        flattenedItemEntities.add(item);
    }

    @Override
    public void handleDelimitedItemEntity(DelimitedItemEntity item) {
        flattenedItemEntities.add(item);
        item.getTargetItem().applyHandler(this);
        flattenedItemEntities.add(item);
    }

    @Override
    public void handleOptionalItemEntity(OptionalItemEntity item) {
        flattenedItemEntities.add(item);
        item.getTargetItem().ifPresent(i -> i.applyHandler(this));
        flattenedItemEntities.add(item);
    }

    @Override
    public void handleSequenceItemEntity(SequenceItemEntity item) {
        flattenedItemEntities.add(item);
        item.getElementItemEntities().forEach(i -> i.applyHandler(this));
        flattenedItemEntities.add(item);
    }

    @Override
    public void handleFlatDataItemEntity(FlatDataItemEntity<?> itemEntity) {
        throw new IllegalStateException(MSG_Unsupported_item_entity(itemEntity));
    }

    /**
     * @return the list of the collected, flattened item entities.
     */
    public List<FlatDataItemEntity<?>> getFlattenedItemEntities() {
        return Collections.unmodifiableList(flattenedItemEntities);
    }
}
