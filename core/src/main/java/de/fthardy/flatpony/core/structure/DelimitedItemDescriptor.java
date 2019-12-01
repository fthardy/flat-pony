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

    /** The definition of the default delimiter character. */
    public static final char DEFAULT_DELIMITER = '\n';

    static String MSG_Read_failed(String itemName) {
        return String.format("Failed to read delimited item '%s' from source stream!", itemName);
    }

    static String MSG_No_delimiter_found(String itemName, String innerItemName) {
        return MSG_Read_failed(itemName) + String.format(" No delimiter after inner item '%s'.", innerItemName);
    }

    private final int delimiter;
    private final FlatDataItemDescriptor<?> itemDescriptor;

    /**
     * Creates a new instance of this descriptor using the {@link #DEFAULT_DELIMITER}.
     *
     * @param name the name for the item.
     * @param itemDescriptor the descriptor of the delimited item.
     */
    public DelimitedItemDescriptor(String name, FlatDataItemDescriptor<?> itemDescriptor) {
        this(name, DEFAULT_DELIMITER, itemDescriptor);
    }

    /**
     * Create a new instance of this descriptor.
     *
     * @param name the name of the item.
     * @param delimiter the delimiter character to use.
     * @param itemDescriptor the item descriptor.
     */
    public DelimitedItemDescriptor(String name, char delimiter, FlatDataItemDescriptor<?> itemDescriptor) {
        super(name);
        this.delimiter = delimiter;
        this.itemDescriptor = Objects.requireNonNull(itemDescriptor, "Undefined item!");
    }

    @Override
    public int getMinLength() {
        return this.itemDescriptor.getMinLength();
    }

    @Override
    public DelimitedItemEntity createItem() {
        return new DelimitedItemEntity(this, this.itemDescriptor.createItem());
    }

    @Override
    public DelimitedItemEntity readItemFrom(Reader source) {
        try {
            FlatDataItemEntity<?> item = this.itemDescriptor.readItemFrom(source);

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
    public List<FlatDataItemDescriptor<?>> getChildDescriptors() {
        return Collections.singletonList(this.itemDescriptor);
    }

    int getDelimiter() {
        return this.delimiter;
    }
}
