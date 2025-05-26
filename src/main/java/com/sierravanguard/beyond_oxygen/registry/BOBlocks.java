package com.sierravanguard.beyond_oxygen.registry;

import ca.weblite.objc.Proxy;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.BubbleGeneratorBlock;
import com.sierravanguard.beyond_oxygen.blocks.CryoBedBlock;
import com.sierravanguard.beyond_oxygen.blocks.VentBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BOBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BeyondOxygen.MODID);

    public static final RegistryObject<Block> VENT = BLOCKS.register("vent",()->new VentBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().explosionResistance(6f).strength(30f)));
    public static final RegistryObject<Block> BUBBLE_GENERATOR = BLOCKS.register("bubble_generator", () -> new BubbleGeneratorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().explosionResistance(6f).strength(30f)));
    public static final RegistryObject<Block> CRYO_BED = BLOCKS.register("cryo_bed",
            () -> new CryoBedBlock(Block.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops().explosionResistance(6f).strength(30f)));
}
