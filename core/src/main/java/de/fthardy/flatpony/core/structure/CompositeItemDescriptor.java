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
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The implementation of a composite item descriptor.
 * <p>
 * A composite item is a composition of several other flat data items. Because composite items can contain other
 * composite items it is possible to build complex, nested, tree structures with this kind of item. However, the
 * composition of a composite item is not dynamic i.e. cannot be changed once it has been defined.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class CompositeItemDescriptor extends AbstractFlatDataItemDescriptor<CompositeItemEntity>
        implements FlatDataStructureDescriptor<CompositeItemEntity> {

    /**
     * Demands the addition of at least one item descriptor for the composition.
     * <p>
     * There is no limitation on how many items can be added to the composite item but any item descriptor instance can
     * only be added once and the names of the items have to be unique. If a particular item descriptor instance is
     * added a second time or another item descriptor with the same name has already been added then an
     * {@link IllegalArgumentException} is going to be thrown. The order in which the item descriptors are added is the
     * order in which the items are expected when reading from a source stream and written to a target stream. 
     * </p>
     * 
     * @author Frank Timothy Hardy
     */
    public interface AddItemDescriptors {

        /**
         * Add a single item descriptor to the composite item.
         * 
         * @param itemDescriptor the item descriptor to add.
         *                       
         * @return the builder instance for further configuration or instance creation.
         */
        AddFurtherItemDescriptors addItemDescriptor(FlatDataItemDescriptor<?> itemDescriptor);

        /**
         * Add a bunch of item descriptors to the composite item.
         * 
         * @param itemDescriptors the item descriptors to add.
         *                        
         * @return the builder instance for further configuration or instance creation.
         */
        AddFurtherItemDescriptors addItemDescriptors(FlatDataItemDescriptor<?>... itemDescriptors);

        /**
         * Add a bunch of item descriptors to the composite item provided by an iterable.
         *
         * @param itemDescriptors the iterable providing the item descriptors to add.
         *
         * @return the builder instance for further configuration or instance creation.
         */
        AddFurtherItemDescriptors addItemDescriptors(Iterable<FlatDataItemDescriptor<?>> itemDescriptors);
    }

    /**
     * Allows to add further item descriptors.
     * 
     * @author Frank Timothy Hardy
     */
    public interface AddFurtherItemDescriptors extends AddItemDescriptors, ObjectBuilder<CompositeItemDescriptor> {
        // Aggregate interface with no further method definitions 
    }
    
    private interface BuildParams {

        String getDescriptorName();
        List<FlatDataItemDescriptor<?>> getItemDescriptors();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<CompositeItemDescriptor>
            implements AddFurtherItemDescriptors, BuildParams {
        
        private final List<FlatDataItemDescriptor<?>> itemDescriptors = new ArrayList<>();
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
        }

        @Override
        public AddFurtherItemDescriptors addItemDescriptor(FlatDataItemDescriptor<?> itemDescriptor) {
            if (this.itemDescriptors.contains(
                    Objects.requireNonNull(itemDescriptor, "Undefined item descriptor!"))) {
                throw new IllegalArgumentException("Cannot add the same item descriptor instance twice!");
            }
            this.itemDescriptors.add(itemDescriptor);
            return this;
        }

        @Override
        public AddFurtherItemDescriptors addItemDescriptors(FlatDataItemDescriptor<?>... itemDescriptors) {
            this.addItemDescriptors(Arrays.asList(itemDescriptors));
            return this;
        }

        @Override
        public AddFurtherItemDescriptors addItemDescriptors(Iterable<FlatDataItemDescriptor<?>> itemDescriptors) {
            Objects.requireNonNull(itemDescriptors, "Undefined item descriptors!").forEach(
                    this::addItemDescriptor);
            return this;
        }

        @Override
        public List<FlatDataItemDescriptor<?>> getItemDescriptors() {
            return Collections.unmodifiableList(itemDescriptors);
        }

        @Override
        protected CompositeItemDescriptor createItemDescriptorInstance() {
            return new CompositeItemDescriptor(this);
        }
    }

    /**
     * Create a builder to configure and create a new instance of this structure item descriptor.
     * 
     * @param name the name for the new item descriptor.
     *             
     * @return the builder instance to configure and create the new instance.
     */
    public static AddItemDescriptors newInstance(String name) {
        return new BuilderImpl(name);
    }

    // TODO Make a name-mapping 
    private final List<FlatDataItemDescriptor<?>> descriptors;

    private CompositeItemDescriptor(BuildParams params) {
        super(params.getDescriptorName());
        this.descriptors = params.getItemDescriptors();
    }

    @Override
    public int getMinLength() {
        return this.descriptors.stream().mapToInt(FlatDataItemDescriptor::getMinLength).sum();
    }

    @Override
    public CompositeItemEntity createItemEntity() {
        return new CompositeItemEntity(this,
                this.descriptors.stream().map(FlatDataItemDescriptor::createItemEntity).collect(Collectors.toList()));
    }

    @Override
    public CompositeItemEntity readItemEntityFrom(Reader source) {
        return new CompositeItemEntity(this, this.descriptors.stream().map(descriptor ->
                descriptor.readItemEntityFrom(source)).collect(Collectors.toList()));
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleCompositeItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }
    
    // TODO add methods to get the descriptors
}
