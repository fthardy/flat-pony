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

import de.fthardy.flatpony.core.*;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.FieldReference;
import de.fthardy.flatpony.core.util.ObjectBuilder;
import de.fthardy.flatpony.core.util.TypedFieldDecorator;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The implementation of a descriptor for a sequence item.
 * <p>
 * A sequence item represents a sequence of element items where each element item is of the same item type. A sequence
 * can optionally be linked with a field that contains the count of the elements. Such a field is expected to be located
 * before the sequence item. If no reference count field is defined then the read strategy is "trial and error" which
 * means the read algorithm tries to read an element item until it fails.
 * </p>
 * <p>
 * Additionally it is possible to define a multiplicity for the element items. If the number of the read element items
 * is not within the bounds of the defined multiplicity a {@link FlatDataReadException} is thrown. Conversely if the
 * sequence is going to be written to a target stream and the number of element items to write is not within the bounds
 * of the defined multiplicity then a {@link FlatDataWriteException} is thrown.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class SequenceItemDescriptor extends AbstractFlatDataItemDescriptor<SequenceItemEntity>
        implements FlatDataStructureDescriptor<SequenceItemEntity> {

    /**
     * Demands the definition of the element item descriptor for the sequence item.
     * 
     * @author Frank Timothy Hardy 
     */
    public interface DefineElementItemDescriptor {

        /**
         * Define the item descriptor for the sequence elements. 
         * 
         * @param elementItemDescriptor an item descriptor.
         * 
         * @return the builder instance for further configuration or instance creation.
         */
        DefineCountFieldReference withElementItemDescriptor(FlatDataItemDescriptor<?> elementItemDescriptor);
    }

    /**
     * Allows to optionally define a count field reference for the sequence item.
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineCountFieldReference extends DefineMultiplicity {

        /**
         * Define the count field reference.
         * 
         * @param countFieldReference the count field reference instance.
         * 
         * @return the builder instance for further configuration or instance creation.
         */
        DefineMultiplicity withCountFieldReference(FieldReference<Integer> countFieldReference);
    }

    /**
     * Allows to optionally define a multiplicity for the sequence items element item entities.
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineMultiplicity extends ObjectBuilder<SequenceItemDescriptor> {

        /**
         * Define a multiplicity for the sequence items element item entities.
         * <p>
         * The bound range is inclusive. Negative values and a multiplicity of 0 to 0 are not allowed.
         * </p>
         * 
         * @param bound1 the upper or lower bound.
         * @param bound2 the upper bound if {@code bound1} is the lower bound or vice versa.               
         * 
         * @return the builder instance for creating the new instance.
         */
        ObjectBuilder<SequenceItemDescriptor> withMultiplicity(int bound1, int bound2);
    }
    
    private interface BuildParams {
        
        String getDescriptorName();
        FlatDataItemDescriptor<?> getElementItemDescriptor();
        FieldReference<Integer> getCountFieldReference();
        Multiplicity getMultiplicity();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<SequenceItemDescriptor> 
            implements DefineElementItemDescriptor, DefineCountFieldReference, BuildParams {
        
        private FlatDataItemDescriptor<?> elementItemDescriptor;
        private FieldReference<Integer> countFieldReference;
        private Multiplicity multiplicity;
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
        }

        @Override
        public DefineCountFieldReference withElementItemDescriptor(FlatDataItemDescriptor<?> elementItemDescriptor) {
            this.elementItemDescriptor = 
                    Objects.requireNonNull(elementItemDescriptor, "Undefined element item descriptor!");
            return this;
        }

        @Override
        public DefineMultiplicity withCountFieldReference(FieldReference<Integer> countFieldReference) {
            this.countFieldReference =
                    Objects.requireNonNull(countFieldReference, "Undefined count field reference!");
            return this;
        }

        @Override
        public ObjectBuilder<SequenceItemDescriptor> withMultiplicity(int bound1, int bound2) {
            this.multiplicity = new Multiplicity(bound1, bound2);
            return this;
        }

        @Override
        public FlatDataItemDescriptor<?> getElementItemDescriptor() {
            return this.elementItemDescriptor;
        }

        @Override
        public FieldReference<Integer> getCountFieldReference() {
            return this.countFieldReference;
        }

        @Override
        public Multiplicity getMultiplicity() {
            return this.multiplicity == null ? new Multiplicity() : this.multiplicity;
        }

        @Override
        protected SequenceItemDescriptor createItemDescriptorInstance() {
            return new SequenceItemDescriptor(this);
        }
    }

    /**
     * A multiplicity definition.
     * 
     * @author Frank Timothy Hardy
     */
    public static final class Multiplicity {

        private final int minOccurrences;
        private final int maxOccurrences;
        
        Multiplicity() {
            this(0, Integer.MAX_VALUE);
        }

        Multiplicity(int bound1, int bound2) {
            if (bound1 < 0 || bound2 < 0) {
                throw new IllegalArgumentException("Negative values are not allowed!");
            }
            this.maxOccurrences = Math.max(bound1, bound2);
            this.minOccurrences = Math.min(bound1, bound2);
            if (maxOccurrences == 0) {
                throw new IllegalArgumentException("A multiplicity of 0 to 0 is not allowed!");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Multiplicity that = (Multiplicity) o;
            return minOccurrences == that.minOccurrences &&
                    maxOccurrences == that.maxOccurrences;
        }

        @Override
        public int hashCode() {
            return Objects.hash(minOccurrences, maxOccurrences);
        }

        @Override
        public String toString() {
            return String.format("Multiplicity: %d to %d items", this.minOccurrences, this.maxOccurrences);
        }

        public int getMinOccurrences() {
            return this.minOccurrences;
        }

        public int getMaxOccurrences() {
            return this.maxOccurrences;
        }

        public boolean isSizeWithinBounds(int size) {
            return size >= this.minOccurrences && size <= this.maxOccurrences;
        }

        public boolean isSizeNotWithinBounds(int size) {
            return !this.isSizeWithinBounds(size);
        }
    }

    static String MSG_Multiplicity_constraint_violated(String sequenceName, Multiplicity multiplicity) {
        return String.format(
                "The number of elements in the sequence [%s] doesn't match the required multiplicity [%s]!",
                sequenceName, multiplicity);
    }

    static String MSG_Failed_to_mark_stream(String itemName) {
        return MSG_Read_failed(itemName) + " Failed to mark the input source stream.";
    }

    static String MSG_Failed_to_reset_stream(String itemName) {
        return MSG_Read_failed(itemName) + " Failed to reset the input source stream.";
    }

    static String MSG_Mark_not_supported(String itemName) {
        return MSG_Read_failed(itemName) + " The input source stream doesn't support marking which is essential for " +
                "this item to function.";
    }

    static String MSG_Read_failed(String itemName) {
        return String.format("Failed to read sequence item '%s' from source stream!", itemName);
    }
    
    static String MSG_No_count_field(String itemName) {
        return String.format(
                "Expected a count field for sequence item '%s' because it has a field reference defined!",
                itemName);
    }

    /**
     * Create a builder to construct a new instance of this item descriptor.
     * 
     * @param name the name for the new item descriptor.
     * 
     * @return the builder instance to configure and create the new item descriptor instance.
     */
    public static DefineElementItemDescriptor newInstance(String name) {
        return new BuilderImpl(name);
    }

    private final FlatDataItemDescriptor<?> elementItemDescriptor;
    private final FieldReference<Integer> countFieldReference;
    private final Multiplicity multiplicity;
    
    private SequenceItemDescriptor(BuildParams params) {
        super(params.getDescriptorName());
        this.elementItemDescriptor = params.getElementItemDescriptor();
        this.countFieldReference = params.getCountFieldReference();
        this.multiplicity = params.getMultiplicity();
    }

    @Override
    public int getMinLength() {
        int minLength = 0;
        if (this.multiplicity != null) {
            minLength = this.elementItemDescriptor.getMinLength() * this.multiplicity.minOccurrences;
        }
        return minLength;
    }

    @Override
    public SequenceItemEntity createItemEntity() {

        assertCountFieldExistsWhenCountFieldIsReferenced();

        return new SequenceItemEntity(this, this.countFieldReference == null ?
                null : this.countFieldReference.getReferencedField());
    }

    @Override
    public SequenceItemEntity readItemEntityFrom(Reader source) {

        assertCountFieldExistsWhenCountFieldIsReferenced();

        TypedFieldDecorator<Integer> countField =
                this.countFieldReference == null ? null : this.countFieldReference.getReferencedField();

        List<FlatDataItemEntity<?>> elementItems = countField == null ?
                this.readElementsByTrialAndErrorFrom(source) : this.readWithCountField(source, countField.getTypedValue());

        if (multiplicity != null && multiplicity.isSizeNotWithinBounds(elementItems.size())) {
            throw new FlatDataReadException(MSG_Multiplicity_constraint_violated(this.getName(), this.multiplicity));
        }
        
        return new SequenceItemEntity(this, elementItems, countField);
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {

        assertCountFieldExistsWhenCountFieldIsReferenced();

        TypedFieldDecorator<Integer> countField =
                this.countFieldReference == null ? null : this.countFieldReference.getReferencedField();
        
        handler.onStructureItemStart(this);

        int elementCount = countField == null ? 
                this.pushReadElementsByTrialAndErrorFrom(source, handler) : 
                this.pushReadWithCountField(source, handler, countField.getTypedValue());

        if (multiplicity != null && multiplicity.isSizeNotWithinBounds(elementCount)) {
            throw new FlatDataReadException(MSG_Multiplicity_constraint_violated(this.getName(), this.multiplicity));
        }

        handler.onStructureItemEnd(this);
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleSequenceItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    /**
     * @return the descriptor of the element items.
     */
    public FlatDataItemDescriptor<?> getElementItemDescriptor() {
        return this.elementItemDescriptor;
    }

    /**
     * @return the multiplicity for the element items.
     */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    private void assertCountFieldExistsWhenCountFieldIsReferenced() {
        if (this.countFieldReference != null && this.countFieldReference.getReferencedField() == null) {
            throw new IllegalStateException(MSG_No_count_field(this.getName()));
        }
    }

    private List<FlatDataItemEntity<?>> readElementsByTrialAndErrorFrom(Reader source) {
        List<FlatDataItemEntity<?>> elementItems = new ArrayList<>();
        FlatDataItemEntity<?> itemEntity;
        do {
            itemEntity = readByTrialAndErrorFrom(
                    source, this.elementItemDescriptor::readItemEntityFrom, null);
            if (itemEntity != null) {
                elementItems.add(itemEntity);
            }
        } while(itemEntity != null);
        return elementItems;
    }

    private List<FlatDataItemEntity<?>> readWithCountField(Reader source, Integer count) {
        List<FlatDataItemEntity<?>> elementItems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            elementItems.add(this.elementItemDescriptor.readItemEntityFrom(source));
        }
        return elementItems;
    }

    private int pushReadElementsByTrialAndErrorFrom(Reader source, StreamReadHandler handler) {
        int elementCount = 0;
        boolean itemFound;
        do {
            itemFound = readByTrialAndErrorFrom(
                    source, s -> { 
                        this.elementItemDescriptor.pushReadFrom(s, handler);
                        return true; },
                    false); 
            if (itemFound) {
                elementCount++;
            }
        } while (itemFound);
        
        return elementCount;
    }

    private int pushReadWithCountField(Reader source, StreamReadHandler handler, Integer count) {
        for (int i = 0; i < count; i++) {
            this.elementItemDescriptor.pushReadFrom(source, handler);
        }
        return count;
    }

    private <R> R readByTrialAndErrorFrom(Reader source, Function<Reader, R> readFunction, R resultOnReadFail) {
        if (source.markSupported()) {
            try {
                source.mark(this.elementItemDescriptor.getMinLength());
            } catch (IOException e) {
                throw new FlatDataReadException(MSG_Failed_to_mark_stream(getName()), e);
            }

            try {
                return readFunction.apply(source);
            } catch (Exception e) {
                try {
                    source.reset();
                } catch (IOException ex) {
                    throw new FlatDataReadException(MSG_Failed_to_reset_stream(getName()), ex);
                }
                return resultOnReadFail;
            }
        } else {
            throw new FlatDataReadException(MSG_Mark_not_supported(getName()));
        }
    }
}
