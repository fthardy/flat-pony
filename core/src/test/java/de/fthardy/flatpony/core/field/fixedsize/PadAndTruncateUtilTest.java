package de.fthardy.flatpony.core.field.fixedsize;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PadAndTruncateUtilTest {

    @Test
    void padString_to_left() {
        Assertions.assertEquals("test      ",
                PadAndTruncateUtil.padString("test", 10, true, ' '));
    }
    @Test
    void padString_to_left_length_1() {
        Assertions.assertEquals(" ",
                PadAndTruncateUtil.padString("", 1, true, ' '));
    }

    @Test
    void padString_to_right() {
        Assertions.assertEquals("0000012345",
                PadAndTruncateUtil.padString("12345", 10, false, '0'));
    }

    @Test
    void padString_to_right_length_1() {
        Assertions.assertEquals(" ",
                PadAndTruncateUtil.padString("", 1, false, ' '));
    }

    @Test
    void truncateString_from_right() {
        assertEquals("test", PadAndTruncateUtil.truncateString("test      ", true, ' '));
    }

    @Test
    void truncateString_from_left() {
        assertEquals("12345", PadAndTruncateUtil.truncateString("0000012345", false, '0'));
    }
}