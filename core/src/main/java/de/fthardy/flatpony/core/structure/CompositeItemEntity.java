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

import de.fthardy.flatpony.core.AbstractFlatDataItemEntity;
import de.fthardy.flatpony.core.FlatDataItemEntity;

import java.io.Writer;
import java.util.*;

/**
 * The implementation of the composite item entity type.
 * <p>
 * A composite item is a composition of several other flat data items. Because composite items can contain other
 * composite items it is possible to build complex, nested, tree structures with this kind of item.
 * </p>
 *
 * @author Frank Timothy Hardy
 */
public final class CompositeItemEntity extends AbstractFlatDataItemEntity<CompositeItemDescriptor>
        implements FlatDataStructure<CompositeItemDescriptor> {
    
    private static Map<String, FlatDataItemEntity<?>> mapByName(List<FlatDataItemEntity<?>> items) {
        Map<String, FlatDataItemEntity<?>> map = new LinkedHashMap<>();
        for (FlatDataItemEntity<?> entity : items) {
            map.put(entity.getDescriptor().getName(), entity);
        }
        return Collections.unmodifiableMap(map);
    }

    private final Map<String, FlatDataItemEntity<?>> elementItemEntityMap;

    /**
     * Creates a new instance of this composite item.
     *
     * @param descriptor the descriptor which is creating this item instance.
     * @param items the list of items which make up this composite item.
     */
    CompositeItemEntity(CompositeItemDescriptor descriptor, List<FlatDataItemEntity<?>> items) {
        super(descriptor);
        this.elementItemEntityMap = mapByName(items);
    }

    @Override
    public int getLength() {
        return this.elementItemEntityMap.values().stream().mapToInt(FlatDataItemEntity::getLength).sum();
    }

    @Override
    public void writeTo(Writer target) {
        this.elementItemEntityMap.values().forEach(dataItem -> dataItem.writeTo(target));
    }

    @Override
    public void applyHandler(FlatDataItemEntity.Handler handler) {
        if (handler instanceof FlatDataStructure.Handler) {
            ((FlatDataStructure.Handler) handler).handleCompositeItemEntity(this);
        } else {
            handler.handleFlatDataItemEntity(this);
        }
    }

    /**
     * Get a particular element item entity by its name.
     * 
     * @param name the name of the element item entity to get.
     *             
     * @return the item entity.
     * 
     * @throws NoSuchElementException when there is no element item entity for the given name.
     */
    public FlatDataItemEntity<?> getElementItemEntityByName(String name) {
        if (this.elementItemEntityMap.containsKey(name)) {
            return this.elementItemEntityMap.get(name);
        } else {
            throw new NoSuchElementException(name);
        }
    }
}
