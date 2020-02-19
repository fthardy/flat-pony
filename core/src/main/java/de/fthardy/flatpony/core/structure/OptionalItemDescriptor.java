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
import de.fthardy.flatpony.core.FlatDataReadException;
import de.fthardy.flatpony.core.util.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The descriptor implementation for an optional item.
 * <p>
 * An optional item represents some target item which might be present or absent. This descriptor can be optionally
 * linked with a field which is before (upstream) the optional item. Such a field might represent a flag indicating the
 * presence or absence of the target item represented by this descriptor. If no linked to a flag field is defined then
 * the read strategy is "trial and error" which means the read algorithm tries to read the target item and if it fails
 * it stops reading, resets to the position where the item read started assuming that there is no target item.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class OptionalItemDescriptor extends AbstractFlatDataItemDescriptor<OptionalItemEntity>
        implements FlatDataStructureDescriptor<OptionalItemEntity> {

    /**
     * Demands the definition of a target item descriptor.
     * 
     * @author Frank Timothy Hardy
     */
    public interface DefineTargetItemDescriptor {

        /**
         * Define the descriptor for the target item which might be present or absent.
         * 
         * @param targetItemDescriptor the target item descriptor.
         *                             
         * @return the builder instance for further configuration or instance creation.
         */
        DefineFlagFieldReference withTargetItemDescriptor(FlatDataItemDescriptor<?> targetItemDescriptor);
    }

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

        String getDescriptorName();
        FlatDataItemDescriptor<?> getTargetItemDescriptor();
        FieldReference<Boolean> getFieldReference();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<OptionalItemDescriptor>
            implements DefineTargetItemDescriptor, DefineFlagFieldReference, BuildParams {
        
        private FlatDataItemDescriptor<?> targetItemDescriptor;
        private FieldReference<Boolean> fieldReference;
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
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
        public DefineFlagFieldReference withTargetItemDescriptor(FlatDataItemDescriptor<?> targetItemDescriptor) {
            this.targetItemDescriptor = Objects.requireNonNull(
                    targetItemDescriptor, "Undefined target item descriptor!");
            return this;
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
     * @param name the name of the item descriptor.
     *             
     * @return a builder instance to configure and create a new item descriptor instance. 
     */
    public static DefineTargetItemDescriptor newInstance(String name) {
        return new BuilderImpl(name);
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

    private static String MSG_Read_failed(String itemName) {
        return String.format("Failed to read optional item '%s' from source stream!", itemName);
    }

    private final FlatDataItemDescriptor<?> targetItemDescriptor;
    private final FieldReference<Boolean> flagFieldReference;

    private OptionalItemDescriptor(BuildParams params) {
        super(params.getDescriptorName());
        this.targetItemDescriptor = params.getTargetItemDescriptor();
        this.flagFieldReference = params.getFieldReference();
    }

    @Override
    public OptionalItemEntity createItemEntity() {

        assertCountFieldExistsWhenCountFieldIsReferenced();

        return new OptionalItemEntity(
                this,
                null,
                this.flagFieldReference == null ? null : this.flagFieldReference.getReferencedField());
    }

    @Override
    public OptionalItemEntity readItemEntityFrom(Reader source) {

        assertCountFieldExistsWhenCountFieldIsReferenced();
        
        OptionalItemEntity optionalItemEntity;
        if (this.flagFieldReference == null) {
            optionalItemEntity = new OptionalItemEntity(
                    this, this.readItemEntityByTrialAndErrorFrom(source), null);
        } else {
            TypedFieldDecorator<Boolean> flagField = this.flagFieldReference.getReferencedField();
            FlatDataItemEntity<?> itemEntity = flagField.getTypedValue() ?
                    this.targetItemDescriptor.readItemEntityFrom(source) : null;
            optionalItemEntity = new OptionalItemEntity(this, itemEntity, flagField);
        }
        return optionalItemEntity;
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
    public List<FlatDataItemDescriptor<?>> getChildren() {
        return Collections.singletonList(this.targetItemDescriptor);
    }

    /**
     * @return the descriptor of the target item.
     */
    public FlatDataItemDescriptor<?> getTargetItemDescriptor() {
        return this.targetItemDescriptor;
    }

    private void assertCountFieldExistsWhenCountFieldIsReferenced() {
        if (this.flagFieldReference != null && this.flagFieldReference.getReferencedField() == null) {
            throw new IllegalStateException("Expected a flag field because flag field reference has been defined!");
        }
    }

    private FlatDataItemEntity<?> readItemEntityByTrialAndErrorFrom(Reader source) {
        if (source.markSupported()) {
            try {
                source.mark(this.targetItemDescriptor.getMinLength());
            } catch (IOException e) {
                throw new FlatDataReadException(MSG_Failed_to_mark_stream(this.getName()), e);
            }

            try {
                return this.targetItemDescriptor.readItemEntityFrom(source);
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
