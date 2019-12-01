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
package de.fthardy.flatpony.core.util;

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * A utility class which implements a "trial and error" read strategy.
 * <p>
 * In order to be able to read with this strategy the reader must support marking. If it doesn't support marking a
 * {@link FlatDataReadException} is going to be thrown. Otherwise the algorithm tries to read the item by delegating to
 * the item descriptors {@link FlatDataItemDescriptor#readItemFrom(Reader) read-method}. If a
 * {@link FlatDataReadException} is thrown which has a causing exception then {@code null} is returned. Otherwise the
 * read item entity is returned.
 * </p>
 * <p>
 * For this to work, the item must be as uniquely identifiable as possible. This is most likely guaranteed when the item
 * is a constant field or at least contains a constant field (the more the better).
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public class TrialAndErrorReadStrategy implements ItemEntityReadStrategy {

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

    private final String itemName;
    private final FlatDataItemDescriptor<?> itemDescriptor;

    /**
     * Creates a new instance of this read strategy.
     *
     * @param itemName the name of the controlling item.
     * @param itemDescriptor the descriptor to be used for reading the item entities.
     */
    public TrialAndErrorReadStrategy(String itemName, FlatDataItemDescriptor<?> itemDescriptor) {
        this.itemName = Objects.requireNonNull(itemName, "Undefined item name!");
        this.itemDescriptor = Objects.requireNonNull(itemDescriptor, "Undefined item descriptor!");
    }

    @Override
    public FlatDataItemEntity<?> readItemFrom(Reader source) {
        if (source.markSupported()) {
            try {
                source.mark(this.itemDescriptor.getMinLength());
            } catch (IOException e) {
                throw new FlatDataReadException(MSG_Failed_to_mark_stream(itemName), e);
            }

            try {
                return itemDescriptor.readItemFrom(source);
            } catch (Exception e) {
                try {
                    source.reset();
                } catch (IOException ex) {
                    throw new FlatDataReadException(MSG_Failed_to_reset_stream(itemName), ex);
                }
                return null;
            }
        } else {
            throw new FlatDataReadException(MSG_Mark_not_supported(itemName));
        }
    }
}
