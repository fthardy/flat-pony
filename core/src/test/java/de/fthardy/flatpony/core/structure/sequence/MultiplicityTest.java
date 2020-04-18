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
package de.fthardy.flatpony.core.structure.sequence;

import de.fthardy.flatpony.core.structure.sequence.SequenceItemDescriptor;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

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
    
    @Test
    void Is_a_value_object() {
        SequenceItemDescriptor.Multiplicity multiplicity1 = new SequenceItemDescriptor.Multiplicity(4, 0);
        SequenceItemDescriptor.Multiplicity multiplicity2 = new SequenceItemDescriptor.Multiplicity(0, 4);
        SequenceItemDescriptor.Multiplicity multiplicity3 = new SequenceItemDescriptor.Multiplicity(4, 2);
        
        assertThat(multiplicity1).isNotSameAs(multiplicity2);
        assertEquals(multiplicity1, multiplicity2);
        assertNotEquals(multiplicity1, multiplicity3);
        assertNotEquals(multiplicity2, multiplicity3);
        
        assertEquals(multiplicity1.hashCode(), multiplicity2.hashCode());
        assertNotEquals(multiplicity1.hashCode(), multiplicity3.hashCode());
        assertNotEquals(multiplicity2.hashCode(), multiplicity3.hashCode());

        Set<SequenceItemDescriptor.Multiplicity> set = new HashSet<>();
        assertTrue(set.add(multiplicity1));
        assertFalse(set.add(multiplicity2));
        assertTrue(set.add(multiplicity3));
        assertThat(set).hasSize(2);
    }
}
