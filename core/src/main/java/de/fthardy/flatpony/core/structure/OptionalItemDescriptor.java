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

import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.FieldReference;
import de.fthardy.flatpony.core.util.ObjectBuilder;
import de.fthardy.flatpony.core.util.TypedFieldDecorator;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.function.Function;

/**
 * The descriptor implementation for an optional item.
 * <p>
 * An optional item is a wrapper item that encapsulates a target item which might be present or absent within the data.
 * This descriptor can be optionally linked with a field which has to be located before this item in the data. Such a
 * field should represent an indicator indicating the presence or absence of the target item. If no link to a field is
 * defined then the read strategy is "trial and error" which means the read algorithm tries to read the target item and
 * when it fails it assumes the target item to be absent and resets the read pointer to the position where the read of
 * the item started.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class OptionalItemDescriptor implements FlatDataStructureDescriptor<OptionalItemEntity> {

    /**
     * Allows to optionally define a reference to a flag field.
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineFlagFieldReference extends ObjectBuilder<OptionalItemDescriptor> {

        /**
         * Define a reference to a flag field.
         *
         * @param fieldReference the field reference.
         *
         * @return the builder instance for creating the new instance.
         */
        ObjectBuilder<OptionalItemDescriptor> withFlagFieldReference(FieldReference<Boolean> fieldReference);
    }

    private interface BuildParams {
        FlatDataItemDescriptor<?> getTargetItemDescriptor();
        FieldReference<Boolean> getFieldReference();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<OptionalItemDescriptor>
            implements DefineFlagFieldReference, BuildParams {
        
        private final FlatDataItemDescriptor<?> targetItemDescriptor;
        private FieldReference<Boolean> fieldReference;
        
        BuilderImpl(FlatDataItemDescriptor<?> targetItemDescriptor) {
            super(targetItemDescriptor.getName());
            this.targetItemDescriptor = Objects.requireNonNull(
                    targetItemDescriptor, "Undefined target item descriptor!");
        }

        @Override
        public FlatDataItemDescriptor<?> getTargetItemDescriptor() {
            return this.targetItemDescriptor;
        }

        @Override
        public FieldReference<Boolean> getFieldReference() {
            return this.fieldReference;
        }

        @Override
        public ObjectBuilder<OptionalItemDescriptor> withFlagFieldReference(
                FieldReference<Boolean> fieldReference) {
            
            if (this.fieldReference != null) {
                throw new IllegalStateException("A flag field reference has been already defined!");
            }
            this.fieldReference = Objects.requireNonNull(fieldReference);
            return this;
        }

        @Override
        protected OptionalItemDescriptor createItemDescriptorInstance() {
            return new OptionalItemDescriptor(this);
        }
    }

    /**
     * Create a new instance of this item descriptor.
     * 
     * @param targetItemDescriptor the target item descriptor.
     *             
     * @return a builder instance to configure and create a new item descriptor instance. 
     */
    public static DefineFlagFieldReference newInstance(FlatDataItemDescriptor<?> targetItemDescriptor) {
        return new BuilderImpl(targetItemDescriptor);
    }

    static String MSG_Failed_to_mark_stream(String itemName) {
        return MSG_Read_failed(itemName) + " Failed to mark the input source stream.";
    }

    static String MSG_Failed_to_reset_stream(String itemName) {
        return MSG_Read_failed(itemName) + " Failed to reset the input source stream.";
    }

    static String MSG_Mark_not_supported(String itemName) {
        return MSG_Read_failed(itemName) + " The input source stream doesn't support marking which is essential for " +
                "this item to function.";
    }

    static String MSG_Read_failed(String itemName) {
        return String.format("Failed to read optional item '%s' from source stream!", itemName);
    }

    static String MSG_No_flag_field(String itemName) {
        return String.format(
                "Expected a flag field for optional item '%s' because it has a field reference defined!",
                itemName);
    }

    private final FlatDataItemDescriptor<?> targetItemDescriptor;
    private final FieldReference<Boolean> flagFieldReference;

    private OptionalItemDescriptor(BuildParams params) {
        this.targetItemDescriptor = params.getTargetItemDescriptor();
        this.flagFieldReference = params.getFieldReference();
    }

    @Override
    public String getName() {
        return this.targetItemDescriptor.getName();
    }

    @Override
    public int getMinLength() {
        return 0; // an optional element may be absent so the minimum length is 0
    }

    @Override
    public OptionalItemEntity createItemEntity() {

        assertFlagFieldExistsWhenFlagFieldIsReferenced();

        return new OptionalItemEntity(
                this,
                null,
                this.flagFieldReference == null ? null : this.flagFieldReference.getReferencedField());
    }

    @Override
    public OptionalItemEntity readItemEntityFrom(Reader source) {

        assertFlagFieldExistsWhenFlagFieldIsReferenced();
        
        OptionalItemEntity optionalItemEntity;
        if (this.flagFieldReference == null) {
            optionalItemEntity = new OptionalItemEntity(
                    this,
                    this.readByTrialAndError(source, this.targetItemDescriptor::readItemEntityFrom),
                    null);
        } else {
            TypedFieldDecorator<Boolean> flagField = this.flagFieldReference.getReferencedField();
            FlatDataItemEntity<?> itemEntity = flagField.getTypedValue() ?
                    this.targetItemDescriptor.readItemEntityFrom(source) : null;
            optionalItemEntity = new OptionalItemEntity(this, itemEntity, flagField);
        }
        return optionalItemEntity;
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {

        assertFlagFieldExistsWhenFlagFieldIsReferenced();

        handler.onStructureItemStart(this);
        
        if (this.flagFieldReference == null) {
            this.readByTrialAndError(source, s ->  { 
                    this.targetItemDescriptor.pushReadFrom(s, handler);
                    return null; 
                });
        } else {
            if (this.flagFieldReference.getReferencedField().getTypedValue()) {
                this.targetItemDescriptor.pushReadFrom(source, handler);
            }
        }
        
        handler.onStructureItemEnd(this);
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleOptionalItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    /**
     * @return the descriptor of the target item.
     */
    public FlatDataItemDescriptor<?> getTargetItemDescriptor() {
        return this.targetItemDescriptor;
    }

    private void assertFlagFieldExistsWhenFlagFieldIsReferenced() {
        if (this.flagFieldReference != null && this.flagFieldReference.getReferencedField() == null) {
            throw new IllegalStateException(MSG_No_flag_field(this.getName()));
        }
    }

    private <R> R readByTrialAndError(Reader source, Function<Reader, R> readFunction) {
        if (source.markSupported()) {
            try {
                source.mark(this.targetItemDescriptor.getMinLength());
            } catch (IOException e) {
                throw new FlatDataReadException(MSG_Failed_to_mark_stream(this.getName()), e);
            }

            try {
                return readFunction.apply(source);
            } catch (Exception e) {
                try {
                    source.reset();
                } catch (IOException ex) {
                    throw new FlatDataReadException(MSG_Failed_to_reset_stream(this.getName()), ex);
                }
                return null;
            }
        } else {
            throw new FlatDataReadException(MSG_Mark_not_supported(this.getName()));
        }
    }
}
