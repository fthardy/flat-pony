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
package de.fthardy.flatpony.core.field.constrained;

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntityHandler;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataMutableField;

import java.io.Writer;

/**
 * The implementation of the constrained field.
 * <p>
 * A constrained field is decorating a field entity that is constrained.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public class ConstrainedField extends AbstractFlatDataItemEntity<ConstrainedFieldDescriptor>
        implements FlatDataMutableField<ConstrainedFieldDescriptor> {

    private final FlatDataField<?> decoratedField;

    /**
     * Creates a new instance of a constant field.
     *
     * @param descriptor the descriptor which is creating this field instance.
     * @param decoratedField the decorated field entity instance.
     */
    ConstrainedField(ConstrainedFieldDescriptor descriptor, FlatDataField<?> decoratedField) {
        super(descriptor);
        descriptor.checkForConstraintViolation(decoratedField.getValue());
        this.decoratedField = decoratedField;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this) +
                "[descriptor=" + this.getDescriptor().toString() +
                ", decorated-field=" + this.decoratedField.toString() + "]";
    }

    @Override
    public int getLength() {
        return this.decoratedField.getLength();
    }

    @Override
    public void writeTo(Writer target) {
        this.decoratedField.writeTo(target);
    }

    @Override
    public <H extends FlatDataItemEntityHandler> H applyHandler(H handler) {
        if (handler instanceof ConstrainedFieldHandler) {
            ((ConstrainedFieldHandler) handler).handleConstrainedField(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
        return handler;
    }

    @Override
    public String getValue() {
        return this.decoratedField.getValue();
    }

    @Override
    public void setValue(String value) {
        this.getDescriptor().checkForConstraintViolation(value);
        this.decoratedField.asMutableField().setValue(value);
    }

    @Override
    public FlatDataMutableField<ConstrainedFieldDescriptor> asMutableField() {
        return this;
    }

    /**
     * Get the field decorated by this decorator.
     *
     * @return the decorated field.
     */
    public FlatDataField<?> getDecoratedField() {
        return this.decoratedField;
    }
}
