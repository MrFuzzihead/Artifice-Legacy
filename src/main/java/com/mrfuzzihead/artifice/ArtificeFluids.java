package com.mrfuzzihead.artifice;

import java.util.Set;
import java.util.TreeSet;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * The four fluids Artifice knows about. Each one is registered only if no other mod has registered that name first;
 * otherwise the existing {@link Fluid} is adopted, which is what makes a tank of BuildCraft oil behave like a tank of
 * Artifice oil (and why {@code ArtificeBlocks} then reuses that fluid's block rather than registering a second one).
 *
 * <p>
 * The distinction this class keeps is <em>who created the fluid</em>. It matters because a bucket is two
 * registrations - {@code FluidContainerRegistry} (fluid to container, for tanks and machines) and CoFH's
 * {@code BucketHandler} (fluid block to filled bucket, for right-clicking) - and neither has a notion of precedence:
 * {@code FluidContainerRegistry.fillFluidContainer} returns the first entry for the fluid that matches, so whichever
 * mod registers last or first wins by load order. Adopting a fluid therefore does <em>not</em> mean adopting its
 * bucket; the mod that created the fluid owns it, and {@link #owns} is how {@code ArtificeItems} asks
 * (CODE_REVIEW 9.7).
 * </p>
 */
public class ArtificeFluids {

    public static Fluid fluidOil;
    public static Fluid fluidFuel;
    public static Fluid fluidCreosote;
    public static Fluid fluidBitumen;

    /** Names Artifice registered itself. Everything else was already in the registry when we looked. */
    private static final Set<String> created = new TreeSet<>();

    public static void initFluids() {
        // Densities and viscosities are the values 1.1.x shipped with, unchanged: fuel never set a viscosity, so it
        // keeps Fluid's default rather than gaining one by accident here.
        fluidOil = getOrCreate("oil", 850, 2000);
        fluidFuel = getOrCreate("fuel", 750, -1);
        fluidCreosote = getOrCreate("creosote", 1100, 1100);
        fluidBitumen = getOrCreate("bitumen", 1100, 10000);

        report();
    }

    /**
     * Whether Artifice created this fluid, and therefore whether it is ours to give a bucket.
     *
     * <p>
     * A null fluid answers {@code false}: nothing was registered under that name at all, so there is no fluid for a
     * bucket to hold.
     * </p>
     */
    public static boolean owns(Fluid fluid) {
        return fluid != null && created.contains(fluid.getName());
    }

    private static Fluid getOrCreate(String name, int density, int viscosity) {
        Fluid existing = FluidRegistry.getFluid(name);
        if (existing != null) {
            return existing;
        }

        Fluid fluid = new Fluid(name).setDensity(density);
        if (viscosity > 0) {
            fluid.setViscosity(viscosity);
        }

        FluidRegistry.registerFluid(fluid);
        created.add(name);
        return fluid;
    }

    /**
     * One line, once, so a pack author can see which of the four came from somewhere else - the bucket rules follow
     * from this list, and "why does Artifice not have an oil bucket here" is otherwise invisible.
     */
    private static void report() {
        Set<String> adopted = new TreeSet<>();
        addIfAdopted(adopted, fluidOil);
        addIfAdopted(adopted, fluidFuel);
        addIfAdopted(adopted, fluidCreosote);
        addIfAdopted(adopted, fluidBitumen);

        ArtificeCore.modLog()
            .info(
                "Fluids: created " + (created.isEmpty() ? "none" : created.toString())
                    + ", adopted from another mod "
                    + (adopted.isEmpty() ? "none" : adopted.toString())
                    + ". A fluid we adopted keeps its own mod's bucket.");
    }

    private static void addIfAdopted(Set<String> adopted, Fluid fluid) {
        if (fluid != null && !owns(fluid)) {
            adopted.add(fluid.getName());
        }
    }
}
