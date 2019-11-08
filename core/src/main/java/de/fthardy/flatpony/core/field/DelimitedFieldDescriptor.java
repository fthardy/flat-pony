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

import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraintViolationException;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * The implementation of the descriptor for a delimited field.
 * <p>
 * A delimited field has an end marker which is defined by the delimiter character. Because of that the length of the
 * field content is not restricted unlike (for a fixed length field). When reading the value from a source stream the
 * end of the field is detected when the delimiter character is read or the end of the source stream is reached. The
 * delimiter char is ignored and not part of the field value.
 * This implementation doesn't support escaping of the delimiter. This means that the delimiter character cannot be used
 * in the field content.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class DelimitedFieldDescriptor extends AbstractFlatDataFieldDescriptor<DelimitedField>
        implements FlatDataFieldDescriptor<DelimitedField> {

    /** The default delimiter definition used by this implementation. */
    public static final char DEFAULT_DELIMITER = ',';

    static String MSG_Read_failed(String fieldName) {
        return String.format("Failed to read separated field '%s' from source stream!", fieldName);
    }

    private final int delimiter;

    /**
     * Creates a new delimited field descriptor which has no constraints, uses the {@link #DEFAULT_DELIMITER} and has an
     * empty string as default value.
     *
     * @param name the name of the field.
     */
    public DelimitedFieldDescriptor(String name) {
        this(name, "");
    }

    /**
     * Creates a new delimited field descriptor which has no constraints and uses {@link #DEFAULT_DELIMITER}.
     *
     * @param name the name of the field.
     * @param defaultValue a default value for the field.
     */
    public DelimitedFieldDescriptor(String name, String defaultValue) {
        this(name, DEFAULT_DELIMITER, defaultValue, Collections.emptySet());
    }

    /**
     * Creates a new delimited field descriptor.
     *
     * @param name the name of the field.
     * @param delimiter the delimiter which delimits the end of the field data.
     * @param defaultValue a default value for the field.
     * @param constraints the constraints.
     */
    public DelimitedFieldDescriptor(
            String name, char delimiter, String defaultValue, Set<ValueConstraint> constraints) {

        super(name, defaultValue, constraints);

        this.delimiter = delimiter;
    }

    @Override
    public DelimitedField createItem() {
        return new DelimitedField(this);
    }

    @Override
    public DelimitedField readItemFrom(Reader source) {
        StringBuilder valueBuilder = new StringBuilder();
        try {
            int i = source.read();
            while (i != -1 && i != delimiter) {
                valueBuilder.append((char) i);
                i = source.read();
            }
        } catch (IOException e) {
            throw new FlatDataReadException(MSG_Read_failed(this.getName()), e);
        }

        DelimitedField field = this.createItem();
        try {
            field.setValue(valueBuilder.toString());
        } catch (ValueConstraintViolationException e) {
            throw new FlatDataReadException(MSG_Read_failed(this.getName()), e);
        }
        return field;
    }

    int getDelimiter() {
        return delimiter;
    }
}
