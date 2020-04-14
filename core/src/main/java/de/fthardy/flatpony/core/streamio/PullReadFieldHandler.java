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
import de.fthardy.flatpony.core.field.constant.ConstantField;
import de.fthardy.flatpony.core.field.constrained.ConstrainedField;
import de.fthardy.flatpony.core.field.delimited.DelimitedField;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeField;
import de.fthardy.flatpony.core.field.observable.ObservableField;
import de.fthardy.flatpony.core.field.typed.TypedField;

/**
 * A field entity handler (visitor) implementation which is used during a pull read process in a trial and error
 * scenario to handle field entities from a {@link ItemEntityStructureFlattener flattened} pre-read item entity
 * structure.
 * 
 * @author Frank Timothy Hardy
 * 
 * @see ItemEntityStructureFlattener
 */
public final class PullReadFieldHandler implements FlatDataField.Handler {
    
    static String MSG_Unsupported_item_entity(FlatDataItemEntity<?> itemEntity) {
        return String.format("Unsupported item entity '%s' [%s]!", 
                itemEntity.getDescriptor().getName(), itemEntity.getClass().getName());
    }

    private final StreamReadHandler handler;
    
    public PullReadFieldHandler(StreamReadHandler handler) {
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
    public void handleObservableField(ObservableField field) {
        field.getObservedField().applyHandler(this);
    }

    @Override
    public void handleFlatDataItemEntity(FlatDataItemEntity<?> itemEntity) {
        throw new IllegalStateException(MSG_Unsupported_item_entity(itemEntity));
    }
}
