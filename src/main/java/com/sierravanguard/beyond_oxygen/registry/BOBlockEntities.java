package com.sierravanguard.beyond_oxygen.registry;

import ca.weblite.objc.Proxy;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.CryoBedBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.OxygenHarvesterBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BOBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BeyondOxygen.MODID);

    public static final RegistryObject<BlockEntityType<VentBlockEntity>> VENT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("vent",()->BlockEntityType.Builder.of(VentBlockEntity::new, BOBlocks.VENT.get()).build(null));
    public static final RegistryObject<BlockEntityType<BubbleGeneratorBlockEntity>> BUBBLE_GENERATOR =
            BLOCK_ENTITY_TYPES.register("bubble_generator", () -> BlockEntityType.Builder.of(
                    BubbleGeneratorBlockEntity::new,
                    BOBlocks.BUBBLE_GENERATOR.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<CryoBedBlockEntity>> CRYO_BED = BLOCK_ENTITY_TYPES.register("cryo_bed",
            () -> BlockEntityType.Builder.of(CryoBedBlockEntity::new, BOBlocks.CRYO_BED.get()).build(null));
    public static final RegistryObject<BlockEntityType<OxygenHarvesterBlockEntity>> OXYGEN_HARVESTER =
            BLOCK_ENTITY_TYPES.register("oxygen_harvester", () ->
                    BlockEntityType.Builder.of(
                            OxygenHarvesterBlockEntity::new,
                            BOBlocks.OXYGEN_HARVESTER.get()  // Assuming you have a BOBlocks.OXYGEN_HARVESTER registered block
                    ).build(null)
            );
}
