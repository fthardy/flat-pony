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

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.AbstractFlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * The implementation of a descriptor for a fixed size field.
 *
 * @author Frank Timothy Hardy
 */
public final class FixedSizeFieldDescriptor extends AbstractFlatDataFieldDescriptor<FixedSizeField>
        implements FlatDataFieldDescriptor<FixedSizeField> {

    static String MSG_Read_failed(String fieldName) {
        return String.format("Failed to read value of fixed size field '%s' from source stream!", fieldName);
    }

    private final int fieldSize;
    private final ContentValueTransformer contentValueTransformer;

    /**
     * Create a new instance of this descriptor.
     *
     * @param name the name for this field.
     * @param fieldSize the length for this field.
     */
    public FixedSizeFieldDescriptor(String name, int fieldSize) {
        this(name, fieldSize, "", new DefaultFieldContentValueTransformer(' ', true));
    }

    /**
     * Create a new instance of this descriptor.
     *
     * @param name the name for this field.
     * @param fieldSize the size for this field.
     * @param defaultValue the default value for this field.
     * @param contentValueTransformer the content value transformer.
     */
    public FixedSizeFieldDescriptor(
            String name, int fieldSize, String defaultValue, ContentValueTransformer contentValueTransformer) {

        super(name, defaultValue);

        if (fieldSize < 1) {
            throw new IllegalArgumentException("Field size must be at least 1!");
        }
        this.fieldSize = fieldSize;

        this.contentValueTransformer = Objects.requireNonNull(contentValueTransformer);
    }

    @Override
    public FixedSizeField createItem() {
        return new FixedSizeField(this);
    }

    @Override
    public FixedSizeField readItemFrom(Reader source) {
        try {
            char[] chars = new char[fieldSize];
            int len = source.read(chars);
            assert len == fieldSize;

            FixedSizeField field = this.createItem();

            field.setValue(contentValueTransformer.extractValueFromContent(new String(chars)));

            return field;
        } catch (IOException e) {
            throw new FlatDataReadException(MSG_Read_failed(this.getName()), e);
        }
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataFieldDescriptor.Handler) {
            ((FlatDataFieldDescriptor.Handler) handler).handleFixedSizeFieldDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    int getFieldSize() {
        return fieldSize;
    }

    String makeContentFromValue(String value) {
        return contentValueTransformer.makeContentFromValue(value, this.fieldSize);
    }
}
