package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.registry.BOOxygenSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class OxygenSource<T> implements Comparable<OxygenSource<?>> {
    public static OxygenSource<ItemStack> forItems(int priority, Function<LivingEntity, Stream<ItemStack>> getItems) {
        return new ItemSource(priority, getItems, (stack, entity) -> true);
    }

    public static OxygenSource<ItemStack> forItems(int priority, Function<LivingEntity, Stream<ItemStack>> getItems, BiPredicate<ItemStack, LivingEntity> validateItem) {
        return new ItemSource(priority, getItems, validateItem);
    }

    private int priority;

    public OxygenSource(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        BOOxygenSources.markDirty();
    }

    public boolean enabled() {
        return this.priority != Integer.MIN_VALUE;
    }

    @Override
    public int compareTo(@NotNull OxygenSource<?> o) {
        return Integer.compare(o.priority, this.priority);
    }

    //you are allowed and encouraged to perform simple filtering for your stream here, if it's faster than a lookup from a HashSet that may contain objects of other types.
    public abstract Stream<T> getSourceObjects(LivingEntity entity);

    //this second area of possible validation is for if your source objects may require additional, expensive validation, and you don't want to risk running this validation multiple times for the same object from different sources.
    public abstract boolean isSourceObjectValid(T sourceObject, LivingEntity entity);

    public abstract int consumeOxygen(T sourceObject, LivingEntity entity, int maxConsume, IFluidHandler.FluidAction action);

    public abstract int getStoredOxygen(T sourceObject, LivingEntity entity);

    public static class ItemSource extends OxygenSource<ItemStack> {
        private final Function<LivingEntity, Stream<ItemStack>> getItems;
        private final BiPredicate<ItemStack, LivingEntity> validateItem;

        public ItemSource(int priority, Function<LivingEntity, Stream<ItemStack>> getItems, BiPredicate<ItemStack, LivingEntity> validateItem) {
            super(priority);
            this.getItems = getItems;
            this.validateItem = validateItem;
        }

        @Override
        public Stream<ItemStack> getSourceObjects(LivingEntity entity) {
            return getItems.apply(entity).filter(stack -> !stack.isEmpty());
        }

        @Override
        public boolean isSourceObjectValid(ItemStack sourceObject, LivingEntity entity) {
            return validateItem.test(sourceObject, entity);
        }

        @Override
        public int consumeOxygen(ItemStack sourceObject, LivingEntity entity, int maxConsume, IFluidHandler.FluidAction action) {
            return OxygenManager.drainOxygen(sourceObject, maxConsume, action);
        }

        @Override
        public int getStoredOxygen(ItemStack sourceObject, LivingEntity entity) {
            return OxygenManager.getContainedOxygen(sourceObject);
        }
    }
}
