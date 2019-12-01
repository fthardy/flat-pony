package de.fthardy.flatpony.core.structure;

import de.fthardy.flatpony.core.FlatDataItemEntity;
import de.fthardy.flatpony.core.field.ConstantField;
import de.fthardy.flatpony.core.field.ConstantFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionalItemEntityTest {

    @Test
    void Set_invalid_target_item() {
        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        OptionalItemEntity optionalItemEntity = descriptor.createItem();
        assertFalse(optionalItemEntity.isTargetItemPresent());
        assertThrows(IllegalArgumentException.class, () -> optionalItemEntity.setTargetItem(
                new ConstantFieldDescriptor("Bla", "blo").createItem()));
    }

    @Test
    void Discard_and_Set_same_target_item() {
        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        StringReader reader = new StringReader("BAROMETER");

        OptionalItemEntity optionalItemEntity = descriptor.readItemFrom(reader);

        ConstantField itemEntity = (ConstantField) optionalItemEntity.getChildItems().get(0);
        assertEquals("BAR", itemEntity.getValue());
        assertSame(itemEntity, optionalItemEntity.getTargetItem().get());

        optionalItemEntity.setTargetItem(null);

        assertFalse(optionalItemEntity.isTargetItemPresent());

        optionalItemEntity.setTargetItem(itemEntity);

        assertTrue(optionalItemEntity.isTargetItemPresent());
        assertSame(itemEntity, optionalItemEntity.getTargetItem().get());
    }

    @Test
    void Discard_and_Set_new_target_item() {
        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        StringReader reader = new StringReader("BAROMETER");

        OptionalItemEntity optionalItemEntity = descriptor.readItemFrom(reader);

        ConstantField itemEntity = (ConstantField) optionalItemEntity.getChildItems().get(0);

        optionalItemEntity.setTargetItem(null);
        assertFalse(optionalItemEntity.isTargetItemPresent());

        FlatDataItemEntity<?> newTargetItem = optionalItemEntity.newTargetItem();
        assertTrue(optionalItemEntity.isTargetItemPresent());
    }

    @Test
    void Calls_correct_handler_method() {
        OptionalItemDescriptor descriptor = new OptionalItemDescriptor(
                "Test", new ConstantFieldDescriptor("Foo", "BAR"));

        StringReader reader = new StringReader("BAROMETER");

        OptionalItemEntity item = descriptor.readItemFrom(reader);

        FlatDataItemEntity.Handler handlerMock = mock(FlatDataItemEntity.Handler.class);
        FlatDataStructure.Handler structureHandlerMock = mock(FlatDataStructure.Handler.class);

        item.applyHandler(handlerMock);
        item.applyHandler(structureHandlerMock);

        verify(handlerMock).handleFlatDataItem(item);
        verifyNoMoreInteractions(handlerMock);
        verify(structureHandlerMock).handleOptionalItem(item);
        verifyNoMoreInteractions(structureHandlerMock);
    }
}