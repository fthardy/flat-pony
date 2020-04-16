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

import de.fthardy.flatpony.core.FlatDataItemEntityHandler;
import de.fthardy.flatpony.core.field.constant.ConstantFieldHandler;
import de.fthardy.flatpony.core.field.constrained.ConstrainedFieldHandler;
import de.fthardy.flatpony.core.field.delimited.DelimitedFieldHandler;
import de.fthardy.flatpony.core.field.fixedsize.FixedSizeFieldHandler;
import de.fthardy.flatpony.core.field.observable.ObservableFieldHandler;
import de.fthardy.flatpony.core.field.typed.TypedFieldHandler;

/**
 * The interface for a handler which can handle the various field type implementations provided by the core package.
 *
 * @author Frank Timothy Hardy.
 */
public interface FlatDataFieldHandler 
        extends FlatDataItemEntityHandler, ConstantFieldHandler, ConstrainedFieldHandler, DelimitedFieldHandler,
        FixedSizeFieldHandler, ObservableFieldHandler, TypedFieldHandler {
    
    // Nothing, this is just an aggregate interface definition for convenience.
}
