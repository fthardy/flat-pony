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