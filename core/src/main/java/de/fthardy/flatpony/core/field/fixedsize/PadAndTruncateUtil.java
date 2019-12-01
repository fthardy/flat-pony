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
 * This class contains static functions to pad and truncate field values.
 *
 * @author Frank Timothy Hardy
 */
final class PadAndTruncateUtil {

    /**
     * Pad a given string to the left or right side.
     *
     * @param string the string to be padded.
     * @param length the total length of the resulting string. Must be greater then the length of the string.
     * @param toLeft {@code} if the string should be padded to the left. Otherwise the string is padded to the right.
     * @param padChar the character to use for padding.
     *
     * @return the padded string value which has the given length.
     */
    static String padString(String string, int length, boolean toLeft, char padChar) {
        if (string.length() >= length) {
            throw new IllegalArgumentException("String length is greater or equal the target length. Must be smaller!");
        }
        char[] fillChars = new char[length - string.length()];
        Arrays.fill(fillChars, padChar);
        String fillString = String.valueOf(fillChars);
        return toLeft ? string  + fillString : fillString + string;
    }

    /**
     * Trim off any padding character from a given string.
     *
     * @param string the string from which the padding characters have to be trimmed off.
     * @param fromRight trim off from the right side. Otherwise trim off form the left side.
     * @param padChar the pad character.
     *
     * @return the trimmed string.
     */
    static String truncateString(String string, boolean fromRight, char padChar) {
        int index = fromRight ? string.length() - 1 : 0;
        while (index != -1 && string.charAt(index) == padChar) {
            index += fromRight ? -1 : 1;
        }
        return fromRight ? string.substring(0, index + 1) : string.substring(index);
    }

    // No instances
    private PadAndTruncateUtil() {}
}
