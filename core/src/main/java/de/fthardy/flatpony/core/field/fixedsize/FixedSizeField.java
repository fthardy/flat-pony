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
package de.fthardy.flatpony.core.field.fixedsize;

import de.fthardy.flatpony.core.AbstractFlatDataItem;
import de.fthardy.flatpony.core.FlatDataItemHandler;
import de.fthardy.flatpony.core.FlatDataWriteException;
import de.fthardy.flatpony.core.field.FlatDataField;
import de.fthardy.flatpony.core.field.FlatDataFieldHandler;

import java.io.IOException;
import java.io.Writer;

/**
 * The implementation of a fixed size field.
 *
 * @author Frank Timothy Hardy
 */
public final class FixedSizeField extends AbstractFlatDataItem<FixedSizeFieldDescriptor>
        implements FlatDataField<FixedSizeFieldDescriptor> {

    static String MSG_Write_failed(String fieldName) {
        return String.format("Failed to write fixed size field '%s' to target stream!", fieldName);
    }

    private String value;

    /**
     * Create a new fixed size field instance.
     *
     * @param descriptor the descriptor which is creating this field instance.
     */
    FixedSizeField(FixedSizeFieldDescriptor descriptor) {
        super(descriptor);
        this.value = descriptor.getDefaultValue();
    }

    @Override
    public int getLength() {
        return this.getDescriptor().getFieldSize();
    }

    @Override
    public void writeTo(Writer target) {
        try {
            target.write(this.getContent());
        } catch (IOException e) {
            throw new FlatDataWriteException(MSG_Write_failed(this.getDescriptor().getName()), e);
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = this.getDescriptor().checkForConstraintViolation(value);
    }

    @Override
    public void applyHandler(FlatDataItemHandler handler) {
        if (handler instanceof FlatDataFieldHandler) {
            ((FlatDataFieldHandler) handler).handleFixedSizeField(this);
        } else {
            handler.handleFlatDataItem(this);
        }
    }

    private String getContent() {
        return this.getDescriptor().makeContentFromValue(value);
    }
}
