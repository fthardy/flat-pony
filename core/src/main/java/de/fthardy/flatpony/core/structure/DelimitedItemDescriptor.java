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
package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.AbstractFlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of delimited item descriptor.
 * <p>
 * A delimited item is a decorator for an item which has to end with a delimiter.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class DelimitedItemDescriptor extends AbstractFlatDataItemDescriptor<DelimitedItemEntity>
        implements FlatDataStructureDescriptor<DelimitedItemEntity> {

    /**
     * Demands the definition of the descriptor for the item to delimit.
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineItemDescriptor {

        /**
         * Define the descriptor of the item to delimit.
         * 
         * @param itemDescriptor the item descriptor.
         *                       
         * @return the builder instance for further configuration or instance creation.
         */
        DefineDelimiter withItemDescriptor(FlatDataItemDescriptor<?> itemDescriptor);
    }

    /**
     * Allows to define a different delimiter character.
     * <p>
     * By default the delimiter is {@link DelimitedItemDescriptor#DEFAULT_DELIMITER}
     * </p>
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineDelimiter extends ObjectBuilder<DelimitedItemDescriptor> {

        /**
         * Define a different delimiter character.
         * 
         * @param delimiter the delimiter character to use.
         *                  
         * @return the builder instance to create the new item descriptor instance.
         */
        ObjectBuilder<DelimitedItemDescriptor> withDelimiter(char delimiter);
    }
    
    private interface BuildParams {
        
        String getDescriptorName();
        FlatDataItemDescriptor<?> getItemDescriptor();
        int getDelimiter();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<DelimitedItemDescriptor> 
            implements DefineItemDescriptor, DefineDelimiter, BuildParams {
        
        private FlatDataItemDescriptor<?> itemDescriptor;
        private int delimiter = DEFAULT_DELIMITER;
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
        }

        @Override
        public DefineDelimiter withItemDescriptor(FlatDataItemDescriptor<?> itemDescriptor) {
            this.itemDescriptor = Objects.requireNonNull(itemDescriptor, "Undefined item descriptor!");
            return this;
        }

        @Override
        public ObjectBuilder<DelimitedItemDescriptor> withDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        @Override
        public FlatDataItemDescriptor<?> getItemDescriptor() {
            return this.itemDescriptor;
        }

        @Override
        public int getDelimiter() {
            return this.delimiter;
        }

        @Override
        protected DelimitedItemDescriptor createItemDescriptorInstance() {
            return new DelimitedItemDescriptor(this);
        }
    }
    
    /** The definition of the default delimiter character. */
    public static final char DEFAULT_DELIMITER = '\n';

    static String MSG_Read_failed(String itemName) {
        return String.format("Failed to read delimited item '%s' from source stream!", itemName);
    }

    static String MSG_No_delimiter_found(String itemName, String innerItemName) {
        return MSG_Read_failed(itemName) + String.format(" No delimiter after inner item '%s'.", innerItemName);
    }

    /**
     * Create a builder instance to configure and create a new {@link DelimitedItemDescriptor} instance.
     * 
     * @param name the name for the new item descriptor.
     *             
     * @return the builder instance.
     */
    public static DefineItemDescriptor newInstance(String name) {
        return new BuilderImpl(name);
    }

    private final int delimiter;
    private final FlatDataItemDescriptor<?> itemDescriptor;

    private DelimitedItemDescriptor(BuildParams params) {
        super(params.getDescriptorName());
        this.delimiter = params.getDelimiter();
        this.itemDescriptor = params.getItemDescriptor();
    }

    @Override
    public int getMinLength() {
        return this.itemDescriptor.getMinLength();
    }

    @Override
    public DelimitedItemEntity createItemEntity() {
        return new DelimitedItemEntity(this, this.itemDescriptor.createItemEntity());
    }

    @Override
    public DelimitedItemEntity readItemEntityFrom(Reader source) {
        try {
            FlatDataItemEntity<?> item = this.itemDescriptor.readItemEntityFrom(source);

            int i = source.read();
            if (i != -1 && i != this.delimiter) {
                throw new FlatDataReadException(MSG_No_delimiter_found(this.getName(), this.itemDescriptor.getName()));
            }

            return new DelimitedItemEntity(this, item);
        } catch (IOException e) {
            throw new FlatDataReadException(MSG_Read_failed(this.getName()), e);
        }
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleDelimitedItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    @Override
    public List<FlatDataItemDescriptor<?>> getChildren() {
        return Collections.singletonList(this.itemDescriptor);
    }

    int getDelimiter() {
        return this.delimiter;
    }
}
