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

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataWriteException;

import java.io.IOException;
import java.io.Writer;

/**
 * The implementation of the delimited field.
 * <p>
 * A delimited field has a variable content length. The end of the field is recognized by a delimiter character.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class DelimitedField extends AbstractFlatDataMutableField<DelimitedFieldDescriptor> {

    static String MSG_Write_failed(String fieldName) {
        return String.format("Failed to write delimited field '%s' to target stream!", fieldName);
    }

    /**
     * Create a new instance of a delimited field.
     *
     * @param descriptor the descriptor which is creating this field instance.
     */
    DelimitedField(DelimitedFieldDescriptor descriptor) {
        super(descriptor);
        this.setValue(descriptor.getDefaultValue());
    }

    @Override
    public int getLength() {
        return this.getValue().length();
    }

    @Override
    public void writeTo(Writer target) {
        try {
            target.write(this.getValue());
            target.write(this.getDescriptor().getDelimiter());
        } catch (IOException e) {
            throw new FlatDataWriteException(MSG_Write_failed(this.getDescriptor().getName()), e);
        }
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataField.Handler) {
            ((FlatDataField.Handler) handler).handleDelimitedField(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }
}
