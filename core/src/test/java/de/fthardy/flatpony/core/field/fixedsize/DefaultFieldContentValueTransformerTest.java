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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFieldContentValueTransformerTest {

    @Test
    void Get_value_which_is_pad_to_the_left() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer(' ', true);

        assertThat(transformer.extractValueFromContent("Foo    ")).isEqualTo("Foo");
    }

    @Test
    void Get_value_which_is_pad_to_the_right() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer('0', false);

        assertThat(transformer.extractValueFromContent("000001")).isEqualTo("1");
    }

    @Test
    void Make_left_pad_content_from_value_which_has_exact_field_size() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer(' ', true);

        assertThat(transformer.makeContentFromValue("Foo", 3)).isEqualTo("Foo");
    }

    @Test
    void Make_right_pad_content_from_value_which_has_exact_field_size() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer('0', false);

        assertThat(transformer.makeContentFromValue("1", 1)).isEqualTo("1");
    }

    @Test
    void Make_left_pad_content_from_value_which_is_shorter_than_the_field() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer(' ', true);

        assertThat(transformer.makeContentFromValue("Foo", 10)).isEqualTo("Foo       ");
    }

    @Test
    void Make_right_pad_content_from_value_which_is_shorter_than_the_field() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer('0', false);

        assertThat(transformer.makeContentFromValue("1", 10)).isEqualTo("0000000001");
    }

    @Test
    void Make_left_pad_content_from_value_which_is_longer_than_the_field() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer(' ', true);

        assertThat(transformer.makeContentFromValue("Foo", 1)).isEqualTo("F");
    }

    @Test
    void Make_right_pad_content_from_value_which_is_longer_than_the_field() {
        DefaultFieldContentValueTransformer transformer =
                new DefaultFieldContentValueTransformer('0', false);

        assertThat(transformer.makeContentFromValue("12345", 3)).isEqualTo("345");
    }
}