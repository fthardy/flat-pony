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
package de.fthardy.flatpony.core.field.constraint;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ChoiceValueConstraintTest extends AbstractValueConstraintTest {

    @Test
    void Cannot_create_with_a_null_value_set() {
        assertThrows(NullPointerException.class, () -> new ChoiceValueConstraint("TEST", null));
    }

    @Test
    void Cannot_create_with_an_empty_value_set() {
        assertThrows(IllegalArgumentException.class, () -> new ChoiceValueConstraint("TEST", new HashSet<>()));
    }
    
    @Test
    void Accepted_values() {
        AbstractValueConstraint constraint = createInstance("TEST");
        
        assertTrue(constraint.acceptValue("Foo"));
        assertTrue(constraint.acceptValue("Bar"));
        assertFalse(constraint.acceptValue("Baz"));
    }


    @Test
    void Non_accepted_values() {
        ChoiceValueConstraint constraint = new ChoiceValueConstraint(
                "TEST", new HashSet<>(Arrays.asList("Foo", "Bar")), false);

        assertFalse(constraint.acceptValue("Foo"));
        assertFalse(constraint.acceptValue("Bar"));
        assertTrue(constraint.acceptValue("Baz"));
    }

    @Override
    protected AbstractValueConstraint createInstance(String name) {
        return new ChoiceValueConstraint(name, new HashSet<>(Arrays.asList("Foo", "Bar")));
    }
}