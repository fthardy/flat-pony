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

/**
 * A content value transformer has the task to make a field content from a given value and vice versa to extract a value
 * from a given field content.
 * <p>
 * There are two terms which have different meanings in the context of fixed size fields: value and content. The term
 * content describes the whole content of a fixed size field. The length of the content is always equal to the size of
 * the field. The term value describes the data which has to be placed into the content of a fixed size field. The
 * length of the value is not restricted. It might be shorter or longer than a fields size. However, in the latter case
 * the value has to be truncated to the field size.
 * </p>
 * <p>
 * A content value converter handles the transformation of a value to a content string and vice versa. If a value is
 * going to be written to a target stream it must be "tailored" to the size of that field. And the other way around if a
 * fixed size field content is read from a source stream the essential value must be somewhat extracted from the content
 * string. An implementation of a content value transformer defines the logic for this.
 * </p>
 *
 * @see FixedSizeFieldDescriptor
 *
 * @author Frank Timothy Hardy
 */
public interface ContentValueTransformer {

    /**
     * Make the content string for a fixed length field from a given value string.
     *
     * @param value the value string to convert.
     * @param fieldLength the length of the field.
     *
     * @return the content string for the field.
     */
    String makeContentFromValue(String value, int fieldLength);

    /**
     * Extract the value string from a given content string.
     *
     * @param content the content string which contains the value.
     *
     * @return the value string.
     */
    String extractValueFromContent(String content);
}
