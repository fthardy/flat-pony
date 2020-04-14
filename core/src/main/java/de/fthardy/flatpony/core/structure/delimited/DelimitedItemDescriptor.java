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
package de.fthardy.flatpony.core.structure.delimited;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.streamio.StructureItemPullReadIteratorBase;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.structure.FlatDataStructureDescriptor;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * The implementation of a delimited item descriptor.
 * <p>
 * A delimited item is a wrapper item which that encapsulates a target item. Just like a delimited field a delimited
 * item has a delimiter character which marks its end. This type of item is a special one which is mainly intended for
 * modeling entire record sets in fixed length format where each record is usually separated by a delimiter (which is
 * mostly the new line character).
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public class DelimitedItemDescriptor implements FlatDataStructureDescriptor<DelimitedItemEntity> {

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
        FlatDataItemDescriptor<?> getTargetItemDescriptor();
        int getDelimiter();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<DelimitedItemDescriptor> 
            implements DefineDelimiter, BuildParams {
        
        private FlatDataItemDescriptor<?> targetItemDescriptor;
        private int delimiter = DEFAULT_DELIMITER;
        
        BuilderImpl(FlatDataItemDescriptor<?> targetItemDescriptor) {
            super(targetItemDescriptor.getName());
            this.targetItemDescriptor = 
                    Objects.requireNonNull(targetItemDescriptor, "Undefined target item descriptor!");
        }

        @Override
        public ObjectBuilder<DelimitedItemDescriptor> withDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        @Override
        public FlatDataItemDescriptor<?> getTargetItemDescriptor() {
            return this.targetItemDescriptor;
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

    static String MSG_No_delimiter_found(String itemName) {
        return MSG_Read_failed(itemName) + " No delimiter after item.";
    }
    
    /**
     * Create a builder instance to configure and create a new {@link DelimitedItemDescriptor} instance.
     * 
     * @param targetItemDescriptor the target item descriptor.
     *             
     * @return the builder instance.
     */
    public static DefineDelimiter newInstance(FlatDataItemDescriptor<?> targetItemDescriptor) {
        return new BuilderImpl(targetItemDescriptor);
    }

    private final int delimiter;
    private final FlatDataItemDescriptor<?> targetItemDescriptor;

    private DelimitedItemDescriptor(BuildParams params) {
        this.delimiter = params.getDelimiter();
        this.targetItemDescriptor = params.getTargetItemDescriptor();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this) +
                "[target-item-descriptor=" + this.targetItemDescriptor.toString() + "]";
    }

    @Override
    public String getName() {
        return this.targetItemDescriptor.getName();
    }

    @Override
    public int getMinLength() {
        return this.targetItemDescriptor.getMinLength();
    }

    @Override
    public DelimitedItemEntity createItemEntity() {
        return new DelimitedItemEntity(this, this.targetItemDescriptor.createItemEntity());
    }

    @Override
    public DelimitedItemEntity readItemEntityFrom(Reader source) {
        FlatDataItemEntity<?> item = this.targetItemDescriptor.readItemEntityFrom(source);
        
        this.readDelimiter(source);
        
        return new DelimitedItemEntity(this, item);
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {
        handler.onStructureItemStart(this);
        this.targetItemDescriptor.pushReadFrom(source, handler);
        this.readDelimiter(source);
        handler.onStructureItemEnd(this);
    }

    @Override
    public PullReadIterator pullReadFrom(Reader reader) {
        return new StructureItemPullReadIteratorBase<DelimitedItemDescriptor>(this, reader) {
            
            PullReadIterator targetItemStreamIterator;

            @Override
            protected boolean handleContent(StreamReadHandler handler) {
                if (!targetItemStreamIterator.hasNextEvent()) {
                    return true;
                }
                targetItemStreamIterator.nextEvent(handler);
                return false;
            }

            @Override
            protected void fetchContent() {
                targetItemStreamIterator = targetItemDescriptor.pullReadFrom(reader);
            }
        };
    }

    @Override
    public <H extends FlatDataItemDescriptor.Handler> H applyHandler(H handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleDelimitedItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
        return handler;
    }

    /**
     * @return the delimiter character value.
     */
    public char getDelimiter() {
        return (char) this.delimiter;
    }

    /**
     * @return the descriptor of the (delimited) item. 
     */
    public FlatDataItemDescriptor<?> getTargetItemDescriptor() {
        return targetItemDescriptor;
    }
    
    private void readDelimiter(Reader source) {
        try {
            int i = source.read();
            if (i != -1 && i != this.delimiter) {
                throw new FlatDataReadException(MSG_No_delimiter_found(this.targetItemDescriptor.getName()));
            }
        } catch (IOException e) {
            throw new FlatDataReadException(MSG_Read_failed(this.getName()), e);
        }
    }
}
