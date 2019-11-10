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

import de.fthardy.flatpony.core.FlatDataItemDescriptorHandler;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.AbstractFlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptor;
import de.fthardy.flatpony.core.field.FlatDataFieldDescriptorHandler;
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueMustHaveExactFieldLengthConstraint;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * The implementation of a descriptor for a fixed size field.
 *
 * @author Frank Timothy Hardy
 */
public final class FixedSizeFieldDescriptor extends AbstractFlatDataFieldDescriptor<FixedSizeField>
        implements FlatDataFieldDescriptor<FixedSizeField> {

    // This transformer is used when no content value transformer is defined
    private static final ContentValueTransformer DUMMY_TRANSFORMER = new ContentValueTransformer() {

        @Override
        public String makeContentFromValue(String value, int fieldLength) {
            return value;
        }

        @Override
        public String extractValueFromContent(String content) {
            return content;
        }
    };

    static String MSG_Read_failed(String fieldName) {
        return String.format("Failed to read value of fixed size field '%s' from source stream!", fieldName);
    }

    /**
     * Creates a default value in case when no content value transformer is used.
     *
     * @param fieldSize the size of the field.
     *
     * @return the default value which is a string of blanks in the given size.
     */
    static String makeDefaultValue(int fieldSize) {
        char[] content = new char[fieldSize];
        Arrays.fill(content, ' ');
        return String.valueOf(content);
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
        this(name, fieldSize, makeDefaultValue(fieldSize), DUMMY_TRANSFORMER,
                Collections.singleton(new ValueMustHaveExactFieldLengthConstraint(fieldSize)));
    }

    /**
     * Create a new instance of this descriptor.
     *
     * @param name the name for this field.
     * @param fieldSize the size for this field.
     * @param defaultValue the default value for this field.
     * @param contentValueTransformer the content value transformer.
     * @param constraints the constraints.
     */
    public FixedSizeFieldDescriptor(
            String name, int fieldSize, String defaultValue,
            ContentValueTransformer contentValueTransformer,
            Set<ValueConstraint> constraints) {

        super(name, defaultValue, constraints);

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
    public void applyHandler(FlatDataItemDescriptorHandler handler) {
        if (handler instanceof FlatDataFieldDescriptorHandler) {
            ((FlatDataFieldDescriptorHandler) handler).handleFixedSizeFieldDescriptor(this);
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
