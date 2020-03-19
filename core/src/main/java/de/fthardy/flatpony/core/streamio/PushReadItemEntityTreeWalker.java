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

/**
 * An item entity handler (visitor) implementation which is used during a push read process to iterate over an item
 * entity structure that has been pre-read in a trial and error scenario.
 * 
 * @author Frank Timothy Hardy
 */
public class PushReadItemEntityTreeWalker implements FlatDataField.Handler, FlatDataStructure.Handler {

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
        throw new IllegalStateException("Unhandled item entity type: " + item.getClass().getName());
    }
}
