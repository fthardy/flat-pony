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
import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The descriptor implementation for an optional item.
 * <p>
 * This optional item implementation relies on the good old trial-and-error method. Hence when reading the item from a
 * source stream a given reader has to support {@link Reader#markSupported() marking}. If it doesn't the descriptor will
 * throw a {@link FlatDataReadException} because otherwise this implementation won't work! However, if so, the reading
 * is delegated to the target item descriptor. If this throws a {@link FlatDataReadException} during reading and the
 * exception has a causing exception the {@link FlatDataReadException} is re-thrown. Otherwise it is interpreted as
 * "error" in the sense of trial-and-error leading to the creation of an empty item entity (ignoring the caught
 * exception).
 * </p>
 * <p>
 * The expectation and the use case for this to work is that the target item is either a
 * {@link de.fthardy.flatpony.core.field.ConstantField constant field} or any {@link FlatDataStructure} which contains
 * at least one constant field. In this case the read ahead limit should reach to the end of the first constant field
 * otherwise it might not work.
 * </p>
 *
 * @see OptionalItemDescriptor
 *
 * @author Frank Timothy Hardy
 */
public final class OptionalItemDescriptor extends AbstractFlatDataItemDescriptor<OptionalItemEntity>
        implements FlatDataStructureDescriptor<OptionalItemEntity> {

    private static final int DEFAULT_READ_AHEAD_LIMIT = 42; // That figures!

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

    private static String MSG_Read_failed(String itemName) {
        return String.format("Failed to read optional item '%s' from source stream!", itemName);
    }

    private final FlatDataItemDescriptor<?> targetItemDescriptor;
    private final int readAheadLimit;

    /**
     * Create a new instance of this descriptor with the default read ahead limit.
     *
     * @param name the name of the descriptor.
     * @param targetItemDescriptor the target item descriptor.
     */
    public OptionalItemDescriptor(String name, FlatDataItemDescriptor<?> targetItemDescriptor) {
        this(name, targetItemDescriptor, DEFAULT_READ_AHEAD_LIMIT);
    }

    /**
     * Create a new instance of this descriptor with a custom read ahead limit.
     *
     * @param name the name of the descriptor.
     * @param targetItemDescriptor the target item descriptor.
     * @param readAheadLimit the read ahead limit. Must be at least 1.
     */
    public OptionalItemDescriptor(String name, FlatDataItemDescriptor<?> targetItemDescriptor, int readAheadLimit) {
        super(name);
        this.targetItemDescriptor = Objects.requireNonNull(
                targetItemDescriptor, "Undefined target item descriptor!");
        if (readAheadLimit < 1) {
            throw new IllegalArgumentException("The read ahead limit must be at least 1!");
        }
        this.readAheadLimit = readAheadLimit;
    }

    @Override
    public OptionalItemEntity createItem() {
        return new OptionalItemEntity(this);
    }

    @Override
    public OptionalItemEntity readItemFrom(Reader source) {
        if (source.markSupported()) {
            try {
                source.mark(this.readAheadLimit);
            } catch (IOException e) {
                throw new FlatDataReadException(MSG_Failed_to_mark_stream(this.getName()), e);
            }

            try {
                return new OptionalItemEntity(this, this.targetItemDescriptor.readItemFrom(source));
            } catch (FlatDataReadException e) {
                if (e.getCause() != null) {
                    throw e;
                }

                try {
                    source.reset();
                } catch (IOException ex) {
                    throw new FlatDataReadException(MSG_Failed_to_reset_stream(this.getName()), ex);
                }

                return this.createItem();
            }
        } else {
            throw new FlatDataReadException(MSG_Mark_not_supported(this.getName()));
        }
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        // TODO
    }

    @Override
    public List<FlatDataItemDescriptor<?>> getChildDescriptors() {
        return Collections.singletonList(this.targetItemDescriptor);
    }

    /**
     * Internal factory method used by the item entities of this descriptor to create target item entities.
     *
     * @return a new target entity.
     */
    FlatDataItemEntity<?> createNewTargetItem() {
        return this.targetItemDescriptor.createItem();
    }
}
