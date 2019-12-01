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
package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.AbstractFlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.util.FieldReferenceConfig;
import de.fthardy.flatpony.core.util.ItemEntityReadStrategy;
import de.fthardy.flatpony.core.util.TrialAndErrorReadStrategy;
import de.fthardy.flatpony.core.util.TypedFieldDecorator;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The descriptor implementation for an optional item.
 * <p>
 * An optional item represents a target item with might be present or not. The optional item can optionally be linked
 * with a field that contains/represents a flag indicating the presence and/or absence of the target item. Such a field
 * is expected to be defined before (upstream) the optional item. If no reference flag field is defined then the read
 * strategy is trial and error which means the read algorithm tries to read the target item and if it fails it stops
 * reading. For further details see {@link TrialAndErrorReadStrategy}.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class OptionalItemDescriptor extends AbstractFlatDataItemDescriptor<OptionalItemEntity>
        implements FlatDataStructureDescriptor<OptionalItemEntity> {

    private final FlatDataItemDescriptor<? extends FlatDataItemEntity<?>> targetItemDescriptor;
    private final ThreadLocal<TypedFieldDecorator<Boolean>> threadLocalFlagField;

    /**
     * Create a new instance of this descriptor with no flag field reference.
     *
     * @param name the name of the descriptor.
     * @param targetItemDescriptor the target item descriptor.
     */
    public OptionalItemDescriptor(
            String name,
            FlatDataItemDescriptor<? extends FlatDataItemEntity<?>> targetItemDescriptor) {
        this(name, targetItemDescriptor, null);
    }

    /**
     * Create a new instance of this descriptor with a custom read ahead limit.
     *
     * @param name the name of the descriptor.
     * @param targetItemDescriptor the target item descriptor.
     * @param fieldReferenceConfig the config for the flag reference field.
     */
    public OptionalItemDescriptor(
            String name,
            FlatDataItemDescriptor<? extends FlatDataItemEntity<?>> targetItemDescriptor,
            FieldReferenceConfig<Boolean> fieldReferenceConfig) {
        super(name);
        this.targetItemDescriptor = Objects.requireNonNull(
                targetItemDescriptor, "Undefined target item descriptor!");
        this.threadLocalFlagField = fieldReferenceConfig == null ?
                null : fieldReferenceConfig.linkWithField();
    }

    @Override
    public OptionalItemEntity createItem() {
        return new OptionalItemEntity(
                this,
                null,
                this.threadLocalFlagField == null ? null : this.threadLocalFlagField.get());
    }

    @Override
    public OptionalItemEntity readItemFrom(Reader source) {
        TypedFieldDecorator<Boolean> flagField = this.threadLocalFlagField == null ?
                null : this.threadLocalFlagField.get();

        ItemEntityReadStrategy strategy = flagField == null ?
                new TrialAndErrorReadStrategy(this.getName(), this.targetItemDescriptor) :
                (Reader reader) -> flagField.getTypedValue() ?
                        this.targetItemDescriptor.readItemFrom(reader) : null;

        FlatDataItemEntity<?> itemEntity = strategy.readItemFrom(source);

        return new OptionalItemEntity(this, itemEntity, flagField);
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleOptionalItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    @Override
    public List<FlatDataItemDescriptor<?>> getChildDescriptors() {
        return Collections.singletonList(this.targetItemDescriptor);
    }

    /**
     * @return the descriptor of the target item.
     */
    public FlatDataItemDescriptor<? extends FlatDataItemEntity<?>> getTargetItemDescriptor() {
        return this.targetItemDescriptor;
    }
}
