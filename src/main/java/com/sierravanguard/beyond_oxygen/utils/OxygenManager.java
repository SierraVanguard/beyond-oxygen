package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BOServerConfig;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.registry.BOFluids;
import com.sierravanguard.beyond_oxygen.registry.BOOxygenSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OxygenManager {
    private static Stream<OxygenSourcePair<?, ?>> getSourcePairs(LivingEntity entity) {
        Set<Object> seenSourceObjects = new HashSet<>();
        return Arrays
                .stream(BOOxygenSources.getSources())
                .flatMap(source -> processSource(entity, source, seenSourceObjects));
    }

    private static <T> Stream<OxygenSourcePair<T, ? extends OxygenSource<T>>> processSource(LivingEntity entity, OxygenSource<T> source, Set<Object> seenSourceObjects) {
        return source
                .getSourceObjects(entity)
                .filter(sourceObject -> !seenSourceObjects.contains(sourceObject) && source.isSourceObjectValid(sourceObject, entity))
                .map(sourceObject -> {
                    seenSourceObjects.add(sourceObject);
                    return new OxygenSourcePair<>(sourceObject, source);
                });
    }
    
    public static void consumeOxygen(LivingEntity entity) {
        if (!SpaceSuitHandler.isWearingFullSuit(entity)) return;
        int mbToDrain = 1;
        IntReference needs = new IntReference(mbToDrain);
        IntReference drained = new IntReference(0);
        getSourcePairs(entity).filter(oxygenSourcePair -> {
            int thisDrained = oxygenSourcePair.consumeOxygen(entity, needs.value, IFluidHandler.FluidAction.EXECUTE);
            if (thisDrained > 0) {
                needs.value -= thisDrained;
                drained.value += thisDrained;
                if (needs.value <= 0) return true;
            }
            return false;
        }).findFirst(); //combined with filter() allows us to terminate the stream early once we have consumed enough oxygen


        if (needs.value <= 0) { //TODO what to do if we got some but not enough?
            entity.addEffect(new MobEffectInstance(
                    BOEffects.OXYGEN_SATURATION.get(),
                    BOConfig.getTimeToImplode(),
                    0,
                    false,
                    false
            ));
        }
    }

    
    public static int getTotalOxygen(LivingEntity entity) {
        return getSourcePairs(entity).mapToInt(oxygenSourcePair -> oxygenSourcePair.getStoredOxygen(entity)).sum();
    }


    public static int getContainedOxygen(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .lazyMap(handler -> {
                    int tanks = handler.getTanks();
                    if (tanks <= 0) return 0;
                    int contained = 0;
                    for (int i = 0; i < tanks; ++i) {
                        FluidStack fluidStack = handler.getFluidInTank(i);
                        if (BOFluids.isOxygen(fluidStack)) contained += fluidStack.getAmount();
                    }
                    return contained;
                }).orElse(0);
    }

    
    public static int drainOxygen(ItemStack stack, int mb, IFluidHandler.FluidAction action) {
        //TODO find better way to do this
        if (stack.isEmpty()) return 0;
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .lazyMap(handler -> {
                    int toDrain = mb;
                    int drained = 0;
                    int tanks = handler.getTanks();
                    if (handler.getTanks() <= 0) return 0;
                    for (int i = 0; i < tanks; ++i) {
                        FluidStack fluidStack = handler.getFluidInTank(i);
                        if (BOFluids.isOxygen(fluidStack)) {
                            int thisDrain = Math.min(fluidStack.getAmount(), toDrain);
                            FluidStack drain = new FluidStack(fluidStack.getFluid(), thisDrain, fluidStack.getTag());
                            int thisDrained = handler.drain(drain, action).getAmount();
                            if (thisDrained > 0) {
                                toDrain -= thisDrain;
                                drained += thisDrained;
                                if (toDrain <= 0) break;
                            }
                        }
                    }
                    return drained;
                }).orElse(0);
    }

    record OxygenSourcePair<T, U extends OxygenSource<T>>(T sourceObject, U source) {
        int getStoredOxygen(LivingEntity entity) {
            return source.getStoredOxygen(sourceObject, entity);
        }

        int consumeOxygen(LivingEntity entity, int maxConsume, IFluidHandler.FluidAction action) {
            return source.consumeOxygen(sourceObject, entity, maxConsume, action);
        }
    }
}
