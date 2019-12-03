package de.fthardy.flatpony.core.structure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Frank Timothy Hardy
 */
public class MultiplicityTest {

    @Test
    void Negative_bounds_are_not_allowed() {
        assertThrows(IllegalArgumentException.class, () -> new SequenceItemDescriptor.Multiplicity(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SequenceItemDescriptor.Multiplicity(0, -1));
    }

    @Test
    void A_multiplicity_of_0_to_0_is_not_allowed() {
        assertThrows(IllegalArgumentException.class, () -> new SequenceItemDescriptor.Multiplicity(0, 0));
    }

    @Test
    void The_order_of_the_bounds_doesnt_matter() {
        SequenceItemDescriptor.Multiplicity multiplicity1 = new SequenceItemDescriptor.Multiplicity(1, 0);
        SequenceItemDescriptor.Multiplicity multiplicity2 = new SequenceItemDescriptor.Multiplicity(0, 1);
        assertThat(multiplicity1).isEqualTo(multiplicity2);
        assertThat(multiplicity2).isEqualTo(multiplicity1);
        assertThat(multiplicity1.getMinOccurrences()).isEqualTo(0);
        assertThat(multiplicity1.getMaxOccurrences()).isEqualTo(1);
        assertThat(multiplicity2.getMinOccurrences()).isEqualTo(0);
        assertThat(multiplicity2.getMaxOccurrences()).isEqualTo(1);
    }

    @Test
    void Bounds_test() {
        SequenceItemDescriptor.Multiplicity multiplicity = new SequenceItemDescriptor.Multiplicity(4, 0);

        assertThat(multiplicity.getMaxOccurrences()).isEqualTo(4);
        assertThat(multiplicity.getMinOccurrences()).isEqualTo(0);

        assertTrue(multiplicity.isSizeWithinBounds(0));
        assertTrue(multiplicity.isSizeWithinBounds(1));
        assertTrue(multiplicity.isSizeWithinBounds(2));
        assertTrue(multiplicity.isSizeWithinBounds(3));
        assertTrue(multiplicity.isSizeWithinBounds(4));

        assertFalse(multiplicity.isSizeWithinBounds(-1));
        assertFalse(multiplicity.isSizeWithinBounds(5));

        assertFalse(multiplicity.isSizeNotWithinBounds(0));
        assertFalse(multiplicity.isSizeNotWithinBounds(1));
        assertFalse(multiplicity.isSizeNotWithinBounds(2));
        assertFalse(multiplicity.isSizeNotWithinBounds(3));
        assertFalse(multiplicity.isSizeNotWithinBounds(4));

        assertTrue(multiplicity.isSizeNotWithinBounds(-1));
        assertTrue(multiplicity.isSizeNotWithinBounds(5));
    }
}
