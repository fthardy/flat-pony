package de.fthardy.flatpony.core.field.constraint;

/**
 * A value constraint which expects the value to be not empty.
 *
 * @author Frank Timothy Hardy
 */
public final class ValueCannotBeEmptyConstraint extends AbstractValueConstraint {

    /** The instance of this constraint implementation. */
    public static final ValueCannotBeEmptyConstraint INSTANCE = new ValueCannotBeEmptyConstraint();

    // No instances
    private ValueCannotBeEmptyConstraint() {}

    @Override
    public boolean test(String value) {
        return value.isEmpty();
    }
}
