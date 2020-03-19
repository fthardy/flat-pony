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

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.streamio.FieldPullReadIterator;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.structure.DelimitedItemDescriptor;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * The implementation of the descriptor for a delimited field.
 * <p>
 * A delimited field has an end marker which is defined by the delimiter character. Because of that the length of the
 * field content is not restricted unlike for a fixed length field. When reading the value from a source stream the
 * end of the field is detected when the delimiter character is read or the end of the source stream is reached. The
 * delimiter char is ignored and not part of the field value.
 * </p>
 * <p>
 * WARNING:
 * Actually this implementation doesn't support escaping of the delimiter. This means that the delimiter character
 * cannot be used in the field content.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class DelimitedFieldDescriptor extends AbstractFlatDataFieldDescriptor<DelimitedField>
        implements FlatDataFieldDescriptor<DelimitedField> {

    /**
     * The builder which creates the new field descriptor instance.
     * 
     * @author Frank Timothy Hardy
     */
    public interface Builder extends ObjectBuilder<DelimitedFieldDescriptor> {
        // no additional methods
    }

    /**
     * Allows to define a default value for the field.
     * <p>
     * By default the default value is an empty string.
     * </p>
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineDefaultValue extends DefineDelimiter {

        /**
         * Define a different default value.
         * 
         * @param defaultValue the default value for the field.
         *                     
         * @return the builder for further configuration or instance creation.
         */
        DefineDelimiter withDefaultValue(String defaultValue);
    }

    /**
     * Allows to define a delimiter.
     * <p>
     * By default the delimiter is {@link DelimitedItemDescriptor#DEFAULT_DELIMITER}.
     * </p>
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineDelimiter extends Builder {

        /**
         * Define a different delimiter character.
         * 
         * @param delimiter the delimiter character.
         *                  
         * @return the builder for further configuration or instance creation.
         */
        Builder withDelimiter(char delimiter);
    }
    
    private interface BuildParams {
        
        String getDescriptorName();
        String getDefaultValue();
        int getDelimiter();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<DelimitedFieldDescriptor>
            implements DefineDefaultValue, DefineDelimiter, BuildParams {
        
        private String defaultValue = "";
        private int delimiter = DEFAULT_DELIMITER;
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
        }

        @Override
        public DefineDelimiter withDefaultValue(String defaultValue) {
            this.defaultValue = Objects.requireNonNull(defaultValue);
            return this;
        }

        @Override
        public Builder withDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        @Override
        public String getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public int getDelimiter() {
            return this.delimiter;
        }

        @Override
        protected DelimitedFieldDescriptor createItemDescriptorInstance() {
            return new DelimitedFieldDescriptor(this);
        }
    }

    /** The default delimiter definition used by this implementation. */
    public static final char DEFAULT_DELIMITER = ',';

    static String MSG_Read_failed(String fieldName) {
        return String.format("Failed to read separated field '%s' from source stream!", fieldName);
    }

    /**
     * Create a builder to configure and create a new instance of this field descriptor.
     * 
     * @param name the name for this new field descriptor.
     *             
     * @return the builder instance.
     */
    public static DefineDefaultValue newInstance(String name) {
        return new BuilderImpl(name);
    }

    private final int delimiter;
    
    private DelimitedFieldDescriptor(BuildParams params) {
        super(params.getDescriptorName(), params.getDefaultValue());
        this.delimiter = params.getDelimiter();
    }

    @Override
    public DelimitedField createItemEntity() {
        return new DelimitedField(this);
    }

    @Override
    public DelimitedField readItemEntityFrom(Reader source) {
        DelimitedField field = this.createItemEntity();
        field.setValue(this.readValue(source));
        return field;
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {
        handler.onFieldItem(this, this.readValue(source));
    }

    @Override
    public PullReadIterator pullReadFrom(Reader source) {
        return new FieldPullReadIterator<>(this, source);
    }

    @Override
    public String readValue(Reader source) {
        StringBuilder valueBuilder = new StringBuilder();
        try {
            int charValue = source.read();
            while (charValue != -1 && charValue != delimiter) {
                valueBuilder.append((char) charValue);
                charValue = source.read();
            }
        } catch (IOException e) {
            throw new FlatDataReadException(MSG_Read_failed(this.getName()), e);
        }
        return valueBuilder.toString();
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataFieldDescriptor.Handler) {
            ((FlatDataFieldDescriptor.Handler) handler).handleDelimitedFieldDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    /**
     * Get the delimiter character used by this descriptor.
     * 
     * @return the delimiter character.
     */
    public char getDelimiter() {
        return (char) this.delimiter;
    }
}
