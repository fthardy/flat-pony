package de.fthardy.flatpony.core.streamio;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.*;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeField;
import de.fthardy.flatpony.core.structure.*;

/**
 * An item entity handler (visitor) implementation which is used during a push read process to iterate over an item
 * entity (tree) structure that has been pre-read during a trial and error scenario.
 * 
 * @author Frank Timothy Hardy
 */
public final class PushReadItemEntityTreeWalker implements FlatDataField.Handler, FlatDataStructure.Handler {
    
    static String MSG_Unsupported_item_entity(FlatDataItemEntity itemEntity) {
        return String.format("Unsupported item entity '%s' [%s]!",
                itemEntity.getDescriptor().getName(), itemEntity.getClass().getName());
    }

    private final StreamReadHandler handler;
    
    public PushReadItemEntityTreeWalker(StreamReadHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public void handleConstantField(ConstantField field) {
        this.handler.onFieldItem(field.getDescriptor(), field.getValue());
    }

    @Override
    public void handleDelimitedField(DelimitedField field) {
        this.handler.onFieldItem(field.getDescriptor(), field.getValue());
    }

    @Override
    public void handleFixedSizeField(FixedSizeField field) {
        this.handler.onFieldItem(field.getDescriptor(), field.getValue());
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
    public void handleCompositeItemEntity(CompositeItemEntity item) {
        this.handler.onStructureItemStart(item.getDescriptor());
        item.getElementItemEntities().forEach(elementItem -> elementItem.applyHandler(this));
        this.handler.onStructureItemEnd(item.getDescriptor());
    }

    @Override
    public void handleDelimitedItemEntity(DelimitedItemEntity item) {
        this.handler.onStructureItemStart(item.getDescriptor());
        item.getTargetItem().applyHandler(this);
        this.handler.onStructureItemEnd(item.getDescriptor());
    }

    @Override
    public void handleOptionalItemEntity(OptionalItemEntity item) {
        this.handler.onStructureItemStart(item.getDescriptor());
        item.getTargetItem().ifPresent(targetItem -> targetItem.applyHandler(this));
        this.handler.onStructureItemEnd(item.getDescriptor());
    }

    @Override
    public void handleSequenceItemEntity(SequenceItemEntity item) {
        this.handler.onStructureItemStart(item.getDescriptor());
        item.getElementItemEntities().forEach(elementItem -> elementItem.applyHandler(this));
        this.handler.onStructureItemEnd(item.getDescriptor());
    }

    @Override
    public void handleFlatDataItemEntity(FlatDataItemEntity<?> item) {
        throw new IllegalStateException(MSG_Unsupported_item_entity(item));
    }
}
