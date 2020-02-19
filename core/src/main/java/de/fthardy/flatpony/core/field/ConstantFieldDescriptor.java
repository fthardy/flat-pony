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
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * The implementation of a descriptor for a field which represents an immutable constant value.
 *
 * @author Frank Timothy Hardy
 */
public final class ConstantFieldDescriptor extends AbstractFlatDataFieldDescriptor<ConstantField>
        implements FlatDataFieldDescriptor<ConstantField> {

    /**
     * Demand the definition of the constant value for the item.
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineConstant {

        /**
         * Define the constant for this item.
         * 
         * @param constant the constant value.
         *                 
         * @return the builder instance to create the new instance.
         */
        ObjectBuilder<ConstantFieldDescriptor> withConstant(String constant);
    }
    
    private interface BuildParams {
        
        String getDescriptorName();
        String getConstant();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<ConstantFieldDescriptor> 
            implements DefineConstant, ObjectBuilder<ConstantFieldDescriptor>, BuildParams {
        
        private String constant;
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
        }

        @Override
        public ObjectBuilder<ConstantFieldDescriptor> withConstant(String constant) {
            this.constant = Objects.requireNonNull(constant, "Undefined constant value!");
            if (this.constant.isEmpty()) {
                throw new IllegalArgumentException("The constant value cannot be empty!");
            }
            return this;
        }

        @Override
        public String getConstant() {
            return this.constant;
        }

        @Override
        protected ConstantFieldDescriptor createItemDescriptorInstance() {
            return new ConstantFieldDescriptor(this);
        }
    }
    
    static String MSG_Read_failed(String fieldName) {
        return String.format("Failed to read constant field '%s' from source stream!", fieldName);
    }

    static String MSG_Invalid_length(String fieldName) {
        return MSG_Read_failed(fieldName) + " Source stream is not long enough!";
    }

    static String MSG_Invalid_value(String fieldName, String value, String constant) {
        return MSG_Read_failed(fieldName) + String.format(
                " The read value is not equal to the constant: [%s] != [%s]!", value, constant);
    }

    /**
     * Create a builder for configuration and creation of a new instance of this item descriptor.
     * 
     * @param name the name of the descriptor.
     *             
     * @return the builder instance.
     */
    public static DefineConstant newInstance(String name) {
        return new BuilderImpl(name);
    }

    private final ConstantField fieldInstance;

    private ConstantFieldDescriptor(BuildParams params) {
        super(params.getDescriptorName(), params.getConstant());
        this.fieldInstance = new ConstantField(this);
    }

    @Override
    public int getMinLength() {
        return this.getDefaultValue().length();
    }

    @Override
    public ConstantField createItemEntity() {
        return fieldInstance;
    }

    @Override
    public ConstantField readItemEntityFrom(Reader source) {
        char[] charsToRead = new char[this.getDefaultValue().length()];
        try {
            int length = source.read(charsToRead);
            if (length != charsToRead.length) {
                throw new FlatDataReadException(MSG_Invalid_length(this.getName()));
            }
        } catch (IOException e) {
            throw new FlatDataReadException(MSG_Read_failed(this.getName()), e);
        }

        String value = new String(charsToRead);
        if (this.getDefaultValue().equals(value)) {
            return fieldInstance;
        } else {
            throw new FlatDataReadException(MSG_Invalid_value(this.getName(), value, this.getDefaultValue()));
        }
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataFieldDescriptor.Handler) {
            ((FlatDataFieldDescriptor.Handler) handler).handleConstantFieldDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }
}
