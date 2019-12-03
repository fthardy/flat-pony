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
package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;

import java.io.Writer;

/**
 * The implementation of the constrained field.
 * <p>
 * A constrained field is wrapping a target field that is to be constrained.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class ConstrainedField extends AbstractFlatDataItemEntity<ConstrainedFieldDescriptor>
        implements FlatDataMutableField<ConstrainedFieldDescriptor>{

    private final FlatDataMutableField<?> field;

    /**
     * Creates a new instance of this field decorator.
     *
     * @param descriptor the descriptor which created this field decorator.
     * @param field the mutable field to be decorated by this field decorator.
     */
    ConstrainedField(ConstrainedFieldDescriptor descriptor, FlatDataMutableField<?> field) {
        super(descriptor);
        this.field = field;
    }

    @Override
    public int getLength() {
        return this.field.getLength();
    }

    @Override
    public void writeTo(Writer target) {
        this.field.writeTo(target);
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataField.Handler) {
            ((FlatDataField.Handler) handler).handleConstrainedField(this);
        } else {
            handler.handleFlatDataItem(this);
        }
    }

    @Override
    public String getValue() {
        return this.field.getValue();
    }

    @Override
    public void setValue(String value) {
        this.getDescriptor().checkForConstraintViolation(value);
        this.field.setValue(value);
    }

    @Override
    public FlatDataMutableField<ConstrainedFieldDescriptor> asMutableField() {
        return this;
    }

    /**
     * Get the decorated field instance.
     *
     * @return the decorated field instance.
     */
    public FlatDataMutableField<?> getField() {
        return this.field;
    }
}