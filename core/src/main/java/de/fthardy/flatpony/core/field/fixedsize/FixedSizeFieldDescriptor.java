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
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

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

    /**
     * Allows to define a field size.
     * <p>
     * By default the field size is 1.
     * </p>
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineFieldSize extends DefineDefaultValue {

        /**
         * Define a field size.
         * 
         * @param size the size. Must be 1 or greater.
         *             
         * @return the builder for further configuration or instance creation.
         */
        DefineDefaultValue withFieldSize(int size);
    }

    /**
     * Allows to define a default value.
     * <p>
     * By default the default value is an empty string.
     * </p>
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineDefaultValue extends DefineContentValueTransformer {

        /**
         * Define a default value.
         * 
         * @param defaultValue the default value.
         *
         * @return the builder for further configuration or instance creation.
         */
        DefineContentValueTransformer withDefaultValue(String defaultValue);
    }

    /**
     * Allows to define a content value transformer.
     * <p>
     * By default a content value transformer with a blank as fill character and left padding is used.
     * </p>
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineContentValueTransformer extends ObjectBuilder<FixedSizeFieldDescriptor> {

        /**
         * Define a content value transformer to be used by the descriptor.
         * 
         * @param contentValueTransformer the content value transformer.
         *
         * @return the builder for further configuration or instance creation.
         */
        ObjectBuilder<FixedSizeFieldDescriptor> useContentValueTransformer(
                ContentValueTransformer contentValueTransformer);
    }
    
    private interface BuildParams {
        String getDescriptorName();
        int getFieldSize();
        String getDefaultValue();
        ContentValueTransformer getContentValueTransformer();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<FixedSizeFieldDescriptor>
            implements DefineFieldSize, DefineDefaultValue, BuildParams {
        
        private int fieldSize = 1;
        private String defaultValue = "";
        private ContentValueTransformer contentValueTransformer;
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
        }

        @Override
        public DefineDefaultValue withFieldSize(int size) {
            if (size < 1) {
                throw new IllegalArgumentException("Field size must be at least 1!");
            }
            this.fieldSize = size;
            return this;
        }

        @Override
        public DefineContentValueTransformer withDefaultValue(String defaultValue) {
            this.defaultValue = Objects.requireNonNull(defaultValue);
            return this;
        }

        @Override
        public ObjectBuilder<FixedSizeFieldDescriptor> useContentValueTransformer(
                ContentValueTransformer contentValueTransformer) {
            this.contentValueTransformer = Objects.requireNonNull(contentValueTransformer);
            return this;
        }

        @Override
        public int getFieldSize() {
            return this.fieldSize;
        }

        @Override
        public String getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public ContentValueTransformer getContentValueTransformer() {
            return this.contentValueTransformer == null ? 
                    new DefaultFieldContentValueTransformer(' ', true) : this.contentValueTransformer;
        }

        @Override
        protected FixedSizeFieldDescriptor createItemDescriptorInstance() {
            return new FixedSizeFieldDescriptor(this);
        }
    }

    /**
     * Create a builder to configure and create a new instance of this field descriptor.
     * 
     * @param name the name for the new field descriptor instance. 
     * 
     * @return the builder instance.
     */
    public static DefineFieldSize newInstance(String name) {
        return new BuilderImpl(name);
    }

    static String MSG_Read_failed(String fieldName) {
        return String.format("Failed to read value of fixed size field '%s' from source stream!", fieldName);
    }

    private final int fieldSize;
    private final ContentValueTransformer contentValueTransformer;
    
    private FixedSizeFieldDescriptor(BuildParams params) {
        super(params.getDescriptorName(), params.getDefaultValue());
        this.fieldSize = params.getFieldSize();
        this.contentValueTransformer = params.getContentValueTransformer();
    }

    @Override
    public int getMinLength() {
        return this.fieldSize;
    }

    @Override
    public FixedSizeField createItemEntity() {
        return new FixedSizeField(this);
    }

    @Override
    public FixedSizeField readItemEntityFrom(Reader source) {
        try {
            char[] chars = new char[fieldSize];
            int len = source.read(chars);
            assert len == fieldSize;

            FixedSizeField field = this.createItemEntity();

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
