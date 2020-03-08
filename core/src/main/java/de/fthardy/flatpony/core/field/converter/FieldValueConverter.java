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

/**
 * The interface for a converter which can convert from a field value string to a value with a particular target type
 * and vice versa convert a value of the target type into a field value string.
 *
 * @param <T> the target type to and from which the field value string can be converted.
 *
 * @author Frank Timothy Hardy
 */
public interface FieldValueConverter<T> {

    /**
     * Get the target type.
     * 
     * @return the class of the target type.
     */
    Class<T> getTargetType();

    /**
     * Convert the field value to a value with the target type.
     *
     * @param fieldValue the field value to convert.
     *
     * @return the value in the target type.
     * 
     * @throws FieldValueConvertException when the conversion of a field value to the target type fails.
     */
    T convertFromFieldValue(String fieldValue) throws FieldValueConvertException;

    /**
     * Convert a value to a field value.
     *
     * @param value the value to convert.
     *
     * @return the field value.
     *
     * @throws FieldValueConvertException when the conversion of a field value from the target type fails.
     */
    String convertToFieldValue(T value);
}
