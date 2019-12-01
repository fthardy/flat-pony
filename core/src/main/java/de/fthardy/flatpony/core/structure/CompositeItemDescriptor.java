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

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The implementation of a composite item descriptor.
 * <p>
 * A composite item is a composition of several other flat data items. Because composite items can contain other
 * composite items it is possible to build complex, nested, tree structures with this kind of item.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class CompositeItemDescriptor extends AbstractFlatDataItemDescriptor<CompositeItemEntity>
        implements FlatDataStructureDescriptor<CompositeItemEntity> {

    private final List<FlatDataItemDescriptor<?>> descriptors;

    /**
     * Create a new instance of this composite item descriptor.
     *
     * @param name the name of this composite item descriptor.
     * @param descriptors the list of the descriptors which make up this composite descriptor.
     */
    public CompositeItemDescriptor(String name, List<FlatDataItemDescriptor<?>> descriptors) {
        super(name);
        if (Objects.requireNonNull(descriptors, "Undefined descriptors!").isEmpty()) {
            throw new IllegalArgumentException("At least one descriptor has to be defined!");
        }
        this.descriptors = Collections.unmodifiableList(new ArrayList<>(descriptors));
    }

    @Override
    public int getMinLength() {
        return this.descriptors.stream().mapToInt(FlatDataItemDescriptor::getMinLength).sum();
    }

    @Override
    public CompositeItemEntity createItem() {
        return new CompositeItemEntity(this,
                this.descriptors.stream().map(FlatDataItemDescriptor::createItem).collect(Collectors.toList()));
    }

    @Override
    public CompositeItemEntity readItemFrom(Reader source) {
        return new CompositeItemEntity(this, this.descriptors.stream().map(descriptor ->
                descriptor.readItemFrom(source)).collect(Collectors.toList()));
    }

    @Override
    public List<FlatDataItemDescriptor<?>> getChildDescriptors() {
        return this.descriptors;
    }

    @Override
    public void applyHandler(FlatDataItemDescriptor.Handler handler) {
        if (handler instanceof FlatDataStructureDescriptor.Handler) {
            ((FlatDataStructureDescriptor.Handler) handler).handleCompositeItemDescriptor(this);
        } else {
            handler.handleFlatDataItemDescriptor(this);
        }
    }
}
