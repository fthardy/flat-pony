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
package de.fthardy.flatpony.core.structure.composite;

import de.fthardy.flatpony.core.AbstractFlatDataItemDescriptor;
import de.fthardy.flatpony.core.FlatDataItemDescriptor;
import de.fthardy.flatpony.core.streamio.PullReadIterator;
import de.fthardy.flatpony.core.streamio.StreamReadHandler;
import de.fthardy.flatpony.core.streamio.StructureItemPullReadIteratorBase;
import de.fthardy.flatpony.core.structure.FlatDataStructureDescriptor;
import de.fthardy.flatpony.core.util.AbstractItemDescriptorBuilder;
import de.fthardy.flatpony.core.util.ObjectBuilder;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The implementation of a composite item descriptor.
 * <p>
 * A composite item is a composition of several other flat data items. Because composite items can contain other
 * composite items as well it is possible to build any kind of complex structures with this type of item. However, the
 * composition of a composite item is not dynamic i.e. cannot be changed once it has been defined.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public class CompositeItemDescriptor extends AbstractFlatDataItemDescriptor<CompositeItemEntity>
        implements FlatDataStructureDescriptor<CompositeItemEntity> {

    /**
     * Demands the addition of at least one component item descriptor for the composition.
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
    public interface AddComponentItemDescriptors {

        /**
         * Add a single component item descriptor to the composite item.
         * 
         * @param itemDescriptor the component item descriptor to add.
         *                       
         * @return the builder instance for further configuration or instance creation.
         */
        AddFurtherComponentItemDescriptors addComponentItemDescriptor(FlatDataItemDescriptor<?> itemDescriptor);

        /**
         * Add a bunch of component item descriptors to the composite item.
         * 
         * @param itemDescriptors the component item descriptors to add.
         *                        
         * @return the builder instance for further configuration or instance creation.
         */
        AddFurtherComponentItemDescriptors addComponentItemDescriptors(FlatDataItemDescriptor<?>... itemDescriptors);

        /**
         * Add a bunch of component item descriptors to the composite item provided by an iterable.
         *
         * @param itemDescriptors the iterable providing the component item descriptors to add.
         *
         * @return the builder instance for further configuration or instance creation.
         */
        AddFurtherComponentItemDescriptors addComponentItemDescriptors(
                Iterable<FlatDataItemDescriptor<?>> itemDescriptors);
    }

    /**
     * Allows to add further component item descriptors.
     * 
     * @author Frank Timothy Hardy
     */
    public interface AddFurtherComponentItemDescriptors 
            extends AddComponentItemDescriptors, ObjectBuilder<CompositeItemDescriptor> {
        // Aggregate interface with no further method definitions 
    }
    
    private interface BuildParams {

        String getDescriptorName();
        Map<String, FlatDataItemDescriptor<?>> getComponentItemDescriptorMap();
    }
    
    private static final class BuilderImpl extends AbstractItemDescriptorBuilder<CompositeItemDescriptor>
            implements AddFurtherComponentItemDescriptors, BuildParams {
        
        private final Map<String, FlatDataItemDescriptor<?>> componentItemDescriptorMap = new LinkedHashMap<>();
        
        BuilderImpl(String descriptorName) {
            super(descriptorName);
        }

        @Override
        public AddFurtherComponentItemDescriptors addComponentItemDescriptor(FlatDataItemDescriptor<?> itemDescriptor) {
            if (this.componentItemDescriptorMap.containsValue(Objects.requireNonNull(itemDescriptor,
                    "Undefined item descriptor!"))) {
                throw new IllegalArgumentException("Tried to add the same item descriptor again!");
            }
            if (this.componentItemDescriptorMap.containsKey(itemDescriptor.getName())) {
                throw new IllegalArgumentException(String.format(
                        "Another item descriptor with the name [%s] already exists!", itemDescriptor.getName()));
            }
            this.componentItemDescriptorMap.put(itemDescriptor.getName(), itemDescriptor);
            return this;
        }

        @Override
        public AddFurtherComponentItemDescriptors addComponentItemDescriptors(FlatDataItemDescriptor<?>... itemDescriptors) {
            this.addComponentItemDescriptors(Arrays.asList(itemDescriptors));
            return this;
        }

        @Override
        public AddFurtherComponentItemDescriptors addComponentItemDescriptors(Iterable<FlatDataItemDescriptor<?>> itemDescriptors) {
            Objects.requireNonNull(itemDescriptors, "Undefined item descriptors!").forEach(
                    this::addComponentItemDescriptor);
            return this;
        }

        @Override
        public Map<String, FlatDataItemDescriptor<?>> getComponentItemDescriptorMap() {
            return Collections.unmodifiableMap(this.componentItemDescriptorMap);
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
    public static AddComponentItemDescriptors newInstance(String name) {
        return new BuilderImpl(name);
    }

    private final Map<String, FlatDataItemDescriptor<?>> elementItemDescriptorMap;

    private CompositeItemDescriptor(BuildParams params) {
        super(params.getDescriptorName());
        this.elementItemDescriptorMap = params.getComponentItemDescriptorMap();
    }

    @Override
    public int getMinLength() {
        return this.elementItemDescriptorMap.values().stream().mapToInt(FlatDataItemDescriptor::getMinLength).sum();
    }

    @Override
    public CompositeItemEntity createItemEntity() {
        return new CompositeItemEntity(this,
                this.elementItemDescriptorMap.values().stream().map(FlatDataItemDescriptor::createItemEntity).collect(
                        Collectors.toList()));
    }

    @Override
    public CompositeItemEntity readItemEntityFrom(Reader source) {
        return new CompositeItemEntity(this, this.elementItemDescriptorMap.values().stream().map(descriptor ->
                descriptor.readItemEntityFrom(source)).collect(Collectors.toList()));
    }

    @Override
    public void pushReadFrom(Reader source, StreamReadHandler handler) {
        handler.onStructureItemStart(this);
        this.elementItemDescriptorMap.values().forEach(d -> d.pushReadFrom(source, handler));
        handler.onStructureItemEnd(this);
    }
    
    @Override
    public PullReadIterator pullReadFrom(Reader source) {
        return new StructureItemPullReadIteratorBase<CompositeItemDescriptor>(this, source) {

            Iterator<FlatDataItemDescriptor<?>> elementItemIterator;
            PullReadIterator currentElementStreamIterator;

            @Override
            protected boolean handleContent(StreamReadHandler handler) {
                if (elementItemIterator.hasNext()) {
                    if (!currentElementStreamIterator.hasNextEvent()) {
                        currentElementStreamIterator = elementItemIterator.next().pullReadFrom(source);
                    }
                    currentElementStreamIterator.nextEvent(handler);
                } else {
                    if (currentElementStreamIterator.hasNextEvent()) {
                        currentElementStreamIterator.nextEvent(handler);
                    } else {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void fetchContent() {
                elementItemIterator = elementItemDescriptorMap.values().iterator();
                currentElementStreamIterator = elementItemIterator.next().pullReadFrom(source);
            }
        };
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleCompositeItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }

    /**
     * Get a particular element item descriptor by its name.
     * 
     * @param name the name of the element item descriptor to get.
     *             
     * @return the descriptor.
     * 
     * @throws NoSuchElementException when no element item descriptor is found for the given name.
     */
    public FlatDataItemDescriptor<?> getComponentItemDescriptorByName(String name) {
        if (this.elementItemDescriptorMap.containsKey(name)) {
            return this.elementItemDescriptorMap.get(name);
        } else {
            throw new NoSuchElementException(name);
        }
    }
}
