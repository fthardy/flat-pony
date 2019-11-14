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

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeField;

/**
 * The interface definition for a flat data field.
 * <p>
 * A flat data field is an item that represents some specific data and is for fixed size field and delimited formats the
 * most atomic part. Any other type of item which is not a field is structure. Structure entities are represented by an
 * implementation instance of a {@link de.fthardy.flatpony.core.structure.FlatDataStructure}.
 * </p>
 *
 * @param <T> the type of the field descriptor which creates the field type.
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataField<T extends FlatDataFieldDescriptor<?>> extends FlatDataItemEntity<T> {

    /**
     * The interface for a handler which can handle the various field  type implementations provided by the core package.
     *
     * @author Frank Timothy Hardy.
     */
    interface Handler extends FlatDataItemEntity.Handler {

        /**
         * Handle a constant field descriptor.
         *
         * @param item the item to be handled by the receiving instance.
         */
        void handleConstantField(ConstantField item);

        /**
         * Handle a delimited field descriptor.
         *
         * @param item the item to be handled by the receiving instance.
         */
        void handleDelimitedField(DelimitedField item);

        /**
         * Handle a fixed size field descriptor.
         *
         * @param item the item to be handled by the receiving instance.
         */
        void handleFixedSizeField(FixedSizeField item);
    }

    /**
     * @return the length of the field.
     */
    int getLength();

    /**
     * Get the current value from the field content.
     *
     * @return the value from the field content.
     */
    String getValue();

    /**
     * Set a new value for the field content.
     *
     * @param value the value to set.
     */
    void setValue(String value);
}
