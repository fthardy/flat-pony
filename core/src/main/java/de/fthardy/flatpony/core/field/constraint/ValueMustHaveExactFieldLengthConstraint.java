package de.fthardy.flatpony.core.field.constraint;

/**
 * A value constraint which expects the value to have the same length as the field at which it is set.
 *
 * @author Frank Timothy Hardy
 */
public final class ValueMustHaveExactFieldLengthConstraint extends AbstractValueConstraint {

    private final int length;

    /**
     * Create a new instance of this constraint.
     *
     * @param length the length of the field.
     */
    public ValueMustHaveExactFieldLengthConstraint(int length) {
        this.length = length;
    }

    @Override
    public boolean test(String value) {
        return value.length() != this.length;
    }
}
