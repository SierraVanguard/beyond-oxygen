package com.sierravanguard.beyond_oxygen.compat.valkyrienskies;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipPhysicsListener;
import org.valkyrienskies.core.api.world.PhysLevel;

public final class BuoyancyForceInducer implements ShipPhysicsListener {

    private final BuoyancyData data;

    public BuoyancyForceInducer(BuoyancyData data) {
        this.data = data;
    }
    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        if (data.totalVolume <= 0) return;
        float buoyance = (float) (0.1 * data.totalVolume);
        physShip.setBuoyantFactor(buoyance);
    }
    public static BuoyancyForceInducer tickOnShip(final LoadedServerShip ship, double sealedVolume) {
        BuoyancyForceInducer attachment = ship.getOrPutAttachment(BuoyancyForceInducer.class, () -> new BuoyancyForceInducer(new BuoyancyData()));

        attachment.data.totalVolume = sealedVolume;

        return attachment;
    }
}

