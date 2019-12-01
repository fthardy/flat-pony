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
import de.fthardy.flatpony.core.util.FieldReferenceConfig;
import de.fthardy.flatpony.core.util.TypedFieldDecorator;
import de.fthardy.flatpony.core.util.TrialAndErrorReadStrategy;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of a descriptor for a sequence item.
 * <p>
 * A sequence item represents a sequence of element items where each element item is of the same item type. A sequence
 * can optionally be linked with a field that contains/represents the count of the elements. Such a field is expected to
 * be defined before (upstream) the sequence item. If no reference count field is defined then the read strategy is
 * trial and error which means the read algorithm tries to read an element item and if it fails it stops reading element
 * items. For further details see {@link TrialAndErrorReadStrategy}.
 * </p>
 * <p>
 * Additionally it is possible to define a multiplicity for the element items. If the number of the read element items
 * is not withing the bounds of the defined multiplicity the a {@link FlatDataReadException} is thrown. Conversely if
 * the sequence is going to be written to a target stream and the number of element items to write is not withing the
 * bounds of the defined multiplicity then a {@link FlatDataWriteException} is thrown.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public class SequenceItemDescriptor extends AbstractFlatDataItemDescriptor<SequenceItemEntity>
        implements FlatDataStructureDescriptor<SequenceItemEntity> {

    /**
     * A multiplicity for the elements of a sequence.
     *
     * @author Frank Timothy Hardy
     */
    public static final class Multiplicity {

        private final int minOccurrences;
        private final int maxOccurrences;

        /**
         * Creates a new instance of this multiplicity.
         * <p>
         * The order of the bounds doesn't matter. Negative value are not allowed. The maximum is not allowed to be 0
         * which makes a multiplicity of 0 to 0 impossible. A multiplicity of 0 to 1 is allowed but however usually
         * doesn't make sense because then an {@link OptionalItemDescriptor optional item} is the one you are searching
         * for.
         * </p>
         *
         * @param bound1 the first bound.
         * @param bound2 the second bound.
         */
        public Multiplicity(int bound1, int bound2) {
            if (bound1 < 0 || bound2 < 0) {
                throw new IllegalArgumentException("Negative values are not allowed!");
            }
            this.maxOccurrences = Math.max(bound1, bound2);
            this.minOccurrences = Math.min(bound1, bound2);
            if (maxOccurrences == 0 || maxOccurrences < minOccurrences) {
                throw new IllegalArgumentException(
                        "Maximum occurrences must be at least 1 and greater than minimum occurrences!");
            }
        }

        public String toString() {
            return String.format("%d to %d", this.minOccurrences, this.maxOccurrences);
        }

        public boolean isSizeWithinBounds(int size) {
            return size >= this.minOccurrences && size <= this.maxOccurrences;
        }
    }

    static String MSG_Multiplicity_constraint_violated(String sequenceName, Multiplicity multiplicity) {
        return String.format(
                "The number of elements in the sequence [%s] doesn't match the required multiplicity [%s]!",
                sequenceName, multiplicity);
    }

    private final FlatDataItemDescriptor<FlatDataItemEntity<?>> elementItemDescriptor;
    private final ThreadLocal<TypedFieldDecorator<Integer>> threadLocalCountField;
    private final Multiplicity multiplicity;

    /**
     * Creates a new instance of this sequence item descriptor without a reference field for the element count and no
     * multiplicity.
     *
     * @param name the name of this sequence item.
     * @param elementItemDescriptor the descriptor of the sequence elements.
     */
    public SequenceItemDescriptor(String name, FlatDataItemDescriptor<FlatDataItemEntity<?>> elementItemDescriptor) {
        this(name, elementItemDescriptor, null, null);
    }

    /**
     * Creates a new instance of this sequence item descriptor.
     *
     * @param name the name of this sequence item.
     * @param elementItemDescriptor the descriptor of the sequence element items.
     * @param countFieldReferenceConfig the field reference config for the related count field. Can be {@code null}.
     * @param multiplicity the multiplicity definition for the element item count. Can be {@code null}.
     */
    public SequenceItemDescriptor(
            String name,
            FlatDataItemDescriptor<FlatDataItemEntity<?>> elementItemDescriptor,
            FieldReferenceConfig<Integer> countFieldReferenceConfig,
            Multiplicity multiplicity) {
        super(name);
        this.elementItemDescriptor = Objects.requireNonNull(
                elementItemDescriptor, "Undefined sequence descriptor!");
        this.threadLocalCountField = countFieldReferenceConfig == null ?
                null : countFieldReferenceConfig.linkWithField();
        this.multiplicity = multiplicity;
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
    public List<FlatDataItemDescriptor<?>> getChildDescriptors() {
        return Collections.singletonList(this.elementItemDescriptor);
    }

    @Override
    public SequenceItemEntity createItem() {
        return new SequenceItemEntity(
                this, this.threadLocalCountField == null ? null : this.threadLocalCountField.get());
    }

    @Override
    public SequenceItemEntity readItemFrom(Reader source) {
        Integer count = this.threadLocalCountField == null ? null : this.threadLocalCountField.get().getTypedValue();

        List<FlatDataItemEntity<?>> elementItems = new ArrayList<>();
        if (count == null) {
            TrialAndErrorReadStrategy readStrategy =
                    new TrialAndErrorReadStrategy(this.getName(), this.elementItemDescriptor);

            FlatDataItemEntity<?> itemEntity = null;
            do {
                itemEntity = readStrategy.readItemFrom(source);
                if (itemEntity != null) {
                    elementItems.add(itemEntity);
                }
            } while(itemEntity != null);
        } else {
            for (int i = 0; i < count; i++) {
                elementItems.add(this.elementItemDescriptor.readItemFrom(source));
            }
        }

        if (multiplicity == null || multiplicity.isSizeWithinBounds(elementItems.size())) {
            return new SequenceItemEntity(
                    this,
                    elementItems,
                    this.threadLocalCountField == null ? null : this.threadLocalCountField.get());
        } else {
            throw new FlatDataReadException(MSG_Multiplicity_constraint_violated(this.getName(), this.multiplicity));
        }
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
    public FlatDataItemDescriptor<FlatDataItemEntity<?>> getElementItemDescriptor() {
        return this.elementItemDescriptor;
    }

    /**
     * @return the multiplicity for the element items.
     */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }
}
