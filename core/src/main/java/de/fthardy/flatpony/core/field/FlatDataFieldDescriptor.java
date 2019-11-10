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
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldDescriptor;

import java.util.Set;

/**
 * The interface for a flat data field descriptor.
 * <p>
 * A flat data field is the most atomic part of flat data. Any other type of item which is not a field is or represents
 * only structure.
 * </p>
 *
 * @param <T> the type of the field created by the field descriptor.
 *
 * @author Frank Timothy Hardy
 */
public interface FlatDataFieldDescriptor<T extends FlatDataField<?>> extends FlatDataItemDescriptor<T> {

    /**
     * The interface for a handler which can handle the various field descriptor type implementations provided by the core
     * package.
     *
     * @author Frank Timothy Hardy.
     */
    interface Handler extends FlatDataItemDescriptor.Handler {

        /**
         * Handle a constant field descriptor.
         *
         * @param descriptor the descriptor to be handled by the receiving instance.
         */
        void handleConstantFieldDescriptor(ConstantFieldDescriptor descriptor);

        /**
         * Handle a delimited field descriptor.
         *
         * @param descriptor the descriptor to be handled by the receiving instance.
         */
        void handleDelimitedFieldDescriptor(DelimitedFieldDescriptor descriptor);

        /**
         * Handle a fixed size field descriptor.
         *
         * @param descriptor the descriptor to be handled by the receiving instance.
         */
        void handleFixedSizeFieldDescriptor(FixedSizeFieldDescriptor descriptor);
    }

    /**
     * Get the default value for the content of a new field instance.
     *
     * @return the default value for the field.
     */
    String getDefaultValue();

    /**
     * Check a given value if it violates any constraints defined by the receiving descriptor instance.
     *
     * @param value the field value to check.
     *
     * @return the list of the violated constraint names.
     */
    Set<String> determineConstraintViolationsFor(String value);
}
