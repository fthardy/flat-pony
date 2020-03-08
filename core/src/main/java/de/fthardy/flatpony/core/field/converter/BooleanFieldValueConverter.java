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
package de.fthardy.flatpony.core.field.converter;

import java.util.Objects;

/**
 * Converts field values to Boolean and vice versa.
 * 
 * @author Frank Timothy Hardy
 */
public final class BooleanFieldValueConverter implements FieldValueConverter<Boolean> {
    
    static String MSG_Invalid_value(String value, String trueValue, String falseValue) {
        return String.format(
                "Cannot convert field value [%s] to boolean value. Expecting [%s] for true and [%s] for false.",
                value, trueValue, falseValue);
    }
    
    private final String trueValue;
    private final String falseValue;
    
    public BooleanFieldValueConverter(String trueValue, String falseValue) {
        this.trueValue = Objects.requireNonNull(trueValue, "Undefined string value for true-value!");
        this.falseValue = Objects.requireNonNull(falseValue, "Undefined string value for false-value!");
    }

    @Override
    public Class<Boolean> getTargetType() {
        return Boolean.class;
    }

    @Override
    public Boolean convertFromFieldValue(String fieldValue) {
        if (fieldValue.equals(this.falseValue)) {
            return Boolean.FALSE;
        } else if (fieldValue.equals(this.trueValue)) {
            return Boolean.TRUE;
        } else {
            throw new FieldValueConvertException(MSG_Invalid_value(fieldValue, this.trueValue, this.falseValue));
        }
    }

    @Override
    public String convertToFieldValue(Boolean value) {
        return value ? this.trueValue : this.falseValue;
    }
}
