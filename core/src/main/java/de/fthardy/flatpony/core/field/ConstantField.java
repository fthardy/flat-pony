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

import de.fthardy.flatpony.core.AbstractFlatDataItem;
import de.fthardy.flatpony.core.FlatDataItemHandler;
import de.fthardy.flatpony.core.FlatDataWriteException;

import java.io.IOException;
import java.io.Writer;

/**
 * The implementation of a constant field which is a special, immutable field with a constant value.
 *
 * @author Frank Timothy Hardy
 */
public final class ConstantField extends AbstractFlatDataItem<ConstantFieldDescriptor>
        implements FlatDataField<ConstantFieldDescriptor> {

    static String MSG_Write_failed(String fieldName) {
        return String.format("Failed to write constant field '%s' to target stream!", fieldName);
    }

    /**
     * Creates a new instance of a constant field.
     *
     * @param descriptor the descriptor which is creating this field instance.
     */
    ConstantField(ConstantFieldDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public int getLength() {
        return getDescriptor().getDefaultValue().length();
    }

    @Override
    public void writeTo(Writer target) {
        try {
            target.write(this.getDescriptor().getDefaultValue());
        } catch (IOException e) {
            throw new FlatDataWriteException(MSG_Write_failed(this.getDescriptor().getName()), e);
        }
    }

    @Override
    public String getValue() {
        return this.getDescriptor().getDefaultValue();
    }

    @Override
    public void setValue(String value) {
        throw new UnsupportedOperationException("A constant field is immutable!");
    }

    @Override
    public void applyHandler(FlatDataItemHandler handler) {
        if (handler instanceof FlatDataFieldHandler) {
            ((FlatDataFieldHandler) handler).handleConstantField(this);
        } else {
            handler.handleFlatDataItem(this);
        }
    }
}
