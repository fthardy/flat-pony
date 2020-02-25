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
package de.fthardy.flatpony.core.field.fixedsize;

import java.util.Arrays;

/**
 * The default implementation for the content value transformer.
 * <p>
 * Uses a fill character for filling remaining space of a fields content when the values length is shorter that the
 * field size. Allows to pad the value to the left or right side of the field. If the value is longer thant the field
 * size it is cut off depending on the pad orientation. When pad to left is set then the value is cut at the right end.
 * Otherwise it is cut at the left end.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class DefaultFieldContentValueTransformer implements FieldContentValueTransformer {

    private final char fillChar;
    private final boolean padToLeft;

    /**
     * Create a new instance of this transformer.
     *
     * @param fillChar the fill char to use for filling remaining space.
     * @param padToLeft {@code true} to pad the value to the left. Otherwise the value is pad to the right.
     */
    public DefaultFieldContentValueTransformer(char fillChar, boolean padToLeft) {
        this.fillChar = fillChar;
        this.padToLeft = padToLeft;
    }

    @Override
    public String makeContentFromValue(String value, int fieldLength) {
        String content;
        int valueLength = value.length();;
        if (valueLength < fieldLength) {
            content = padValue(value, fieldLength);
        } else if (valueLength > fieldLength) {
            content = padToLeft ? value.substring(0, fieldLength) : value.substring(value.length() - fieldLength);
        } else {
            content = value;
        }
        return content;
    }

    @Override
    public String extractValueFromContent(String content) {
        int index = padToLeft ? content.length() - 1 : 0;
        while (index != -1 && content.charAt(index) == fillChar) {
            index += padToLeft ? -1 : 1;
        }
        return padToLeft ? content.substring(0, index + 1) : content.substring(index);
    }

    /**
     * Pad a given string to the left or right side.
     *
     * @param string the string to be padded.
     * @param length the total length of the resulting string. Must be greater then the length of the string.
     *
     * @return the padded string value which has the given length.
     */
    private String padValue(String string, int length) {
        if (string.length() > length) {
            throw new IllegalArgumentException("Value string length must be shorter than or equal the field length!");
        }
        if (string.length() < length) {
            char[] fillChars = new char[length - string.length()];
            Arrays.fill(fillChars, fillChar);
            String fillString = String.valueOf(fillChars);
            return padToLeft ? string + fillString : fillString + string;
        } else {
            return string;
        }
    }
}
