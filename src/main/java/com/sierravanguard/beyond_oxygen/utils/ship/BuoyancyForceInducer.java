package com.sierravanguard.beyond_oxygen.utils.ship;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public class BuoyancyForceInducer implements ShipForcesInducer {

    private final BuoyancyData data;

    public BuoyancyForceInducer(BuoyancyData data) {
        this.data = data;
    }
    @Override
    public void applyForces(PhysShip physShip) {
        if (data.totalVolume <= 0) return;
        float buoyance = (float) (0.1 * data.totalVolume);
        physShip.setBuoyantFactor(buoyance);
    }
    public static BuoyancyForceInducer tickOnShip(final LoadedServerShip ship, double sealedVolume) {
        BuoyancyForceInducer attachment = ship.getAttachment(BuoyancyForceInducer.class);
        if (attachment == null) {
            attachment = new BuoyancyForceInducer(new BuoyancyData());
            ship.setAttachment(BuoyancyForceInducer.class, attachment);
        }

        attachment.data.totalVolume = sealedVolume;

        return attachment;
    }
}

