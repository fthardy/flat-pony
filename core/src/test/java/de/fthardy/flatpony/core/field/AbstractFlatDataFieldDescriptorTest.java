package de.fthardy.flatpony.core.field;

import de.fthardy.flatpony.core.field.constraint.DefaultValueConstraint;
import de.fthardy.flatpony.core.field.constraint.ValueConstraint;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.*;

class AbstractFlatDataFieldDescriptorTest {

    @Test
    void makeUnmodifiableSetFrom_Constraints_cannot_be_null() {
        assertThrows(NullPointerException.class, () ->
                AbstractFlatDataFieldDescriptor.makeUnmodifiableSetFrom(null));
    }

    @Test
    void makeUnmodifiableSetFrom_No_constraints_return_empty_set() {
        assertThat(AbstractFlatDataFieldDescriptor.makeUnmodifiableSetFrom(new HashSet<>())).isEmpty();
    }

    @Test
    void makeUnmodifiableSetFrom_Cannot_add_constraints_with_ambiguous_names() {

        ValueConstraint c1 = new DefaultValueConstraint("empty", String::isEmpty);
        ValueConstraint c2 = new DefaultValueConstraint("empty", String::isEmpty);
        ValueConstraint c3 = new DefaultValueConstraint("length", v -> v.length() > 0);

        Set<ValueConstraint> constraints = new LinkedHashSet<>(
                Arrays.asList(c1, c2, c3));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                AbstractFlatDataFieldDescriptor.makeUnmodifiableSetFrom(constraints));

        assertThat(exception.getMessage()).endsWith("empty");
    }

    @Test
    void makeUnmodifiableSetFrom_Resulting_set_preserves_order_and_is_unmodifiable() {

        ValueConstraint c1 = new DefaultValueConstraint("empty", String::isEmpty);
        ValueConstraint c2 = new DefaultValueConstraint("length", v -> v.length() > 0);

        Set<ValueConstraint> constraints = new LinkedHashSet<>(Arrays.asList(c1, c2));

        Set<ValueConstraint> result =
                AbstractFlatDataFieldDescriptor.makeUnmodifiableSetFrom(constraints);
        assertThat(result).isNotSameAs(constraints);
        assertThat(result).containsExactlyElementsOf(constraints);

        ValueConstraint c3 = new DefaultValueConstraint("false", v -> false);
        assertThrows(UnsupportedOperationException.class, () -> result.add(c3));
    }
}