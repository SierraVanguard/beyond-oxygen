package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BOBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BeyondOxygen.MODID);

    public static final RegistryObject<BlockEntityType<VentBlockEntity>> VENT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("vent",()->BlockEntityType.Builder.of(VentBlockEntity::new, BOBlocks.VENT.get()).build(null));
}
