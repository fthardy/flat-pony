package de.fthardy.flatpony.core.field.constraint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegExValueConstraintTest extends AbstractValueConstraintTest {

    @Test
    void Cannot_create_with_null_expression() {
        assertThrows(NullPointerException.class, () -> new RegExValueConstraint("TEST", null));
    }
    
    @Test
    void Check_value() {
        RegExValueConstraint constraint = new RegExValueConstraint("TEST", ".+");
        
        assertTrue(constraint.acceptValue("f"));
        assertTrue(constraint.acceptValue("bar"));
        assertFalse(constraint.acceptValue(""));
    }
    
    @Override
    protected AbstractValueConstraint createInstance(String name) {
        return new RegExValueConstraint(name, ".*");
    }
}