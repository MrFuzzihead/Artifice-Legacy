package com.mrfuzzihead.artifice;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import com.mrfuzzihead.artifice.command.CommandCalc;
import com.mrfuzzihead.artifice.compat.AE2;
import com.mrfuzzihead.artifice.compat.Buildcraft;
import com.mrfuzzihead.artifice.compat.CarpentersBlocks;
import com.mrfuzzihead.artifice.compat.EE3;
import com.mrfuzzihead.artifice.compat.FMP;
import com.mrfuzzihead.artifice.compat.Forestry;
import com.mrfuzzihead.artifice.compat.ICompat;
import com.mrfuzzihead.artifice.compat.IMC;
import com.mrfuzzihead.artifice.compat.MFR;
import com.mrfuzzihead.artifice.compat.Vanilla;
import com.mrfuzzihead.artifice.event.ArtificeEventHandler;
import com.mrfuzzihead.artifice.event.ArtificeTickHandler;
import com.mrfuzzihead.artifice.gui.ArtificeCreativeTab;
import com.mrfuzzihead.artifice.net.CommonProxy;
import com.mrfuzzihead.artifice.recipe.ArtificeRecipes;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry.Type;

@Mod(
    modid = "Artifice",
    name = "Artifice",
    version = "1.7.10R1.1.4",
    dependencies = "required-after:CoFHCore;after:BuildCraft|Core;after:EE3;after:Forestry;after:MineFactoryReloaded;after:Thaumcraft;after:Railcraft")
public class ArtificeCore {

    @SidedProxy(
        clientSide = "com.mrfuzzihead.artifice.net.ClientProxy",
        serverSide = "com.mrfuzzihead.artifice.net.CommonProxy")
    public static CommonProxy proxy;
    public static final String modID = "Artifice";
    public static final String modName = "Artifice";
    public static final String modChannel = "Artifice";
    public static final String modVersion = "1.7.10R1.1.4";
    public static Logger logger;
    public static ArtificeEventHandler eventHandler;
    public static ArtificeTickHandler tickHandler;
    public static final ArtificeCreativeTab mainTab = new ArtificeCreativeTab("Artifice");
    public static final ArtificeCreativeTab worldTab = new ArtificeCreativeTab("Artifice Worldgen");
    @Instance("Artifice")
    public static ArtificeCore instance;
    private ArrayList<ICompat> compats = new ArrayList<>();

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new CommandCalc());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        this.compats.add(new Buildcraft());
        this.compats.add(new EE3());
        this.compats.add(new FMP());
        this.compats.add(new Forestry());
        this.compats.add(new CarpentersBlocks());
        this.compats.add(new MFR());
        this.compats.add(new AE2());
        this.compats.add(new Vanilla());
        logger = evt.getModLog();
        MinecraftForge.EVENT_BUS.register(eventHandler = new ArtificeEventHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(tickHandler = new ArtificeTickHandler());
        ArtificeConfig.initClient(evt);
        ArtificeConfig.initCommon(evt);
        ArtificeFluids.initFluids();
        ArtificeBlocks.initBlocks();
        // Before anything can query the ore dictionary, and before ArtificeRecipes#registerOreRecipes runs in init:
        // registering the variants only in postInit meant every one of those smelting recipes matched "Unknown" and
        // was never added at all (CODE_REVIEW 9.2 / row 14 of the TL;DR).
        ArtificeBlocks.registerOreVariants();
        ArtificeTiles.initTiles();
        ArtificeItems.initItems();
        ArtificeEnchants.initEnchants();
        ArtificeWorld.initWorldGen();
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        ArtificeTooltips.initTooltips();
        ArtificeRecipes.registerRecipes();
        proxy.init();

        for (ICompat c : this.compats) {
            if (c.getModID() == null || Loader.isModLoaded(c.getModID())) {
                logger.debug(
                    "Loading compat " + c.getClass()
                        .getName());
                c.load();
            }
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        ArtificeBlocks.initOreMappings();
        ArtificeBlocks.initOreSet();
        ArtificeBlocks.registerBasaltsAndMarbles();
        if (ArtificeConfig.floraBoneMeal) {
            for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
                if (biome != null) {
                    for (int i = 0; i < 4; i++) {
                        biome.addFlower(ArtificeBlocks.blockFlora, i, 10);
                    }
                }
            }
        }
    }

    @EventHandler
    public void processIMCRequests(IMCEvent event) {
        IMC.processIMC(event);
    }

    @EventHandler
    public void missingMappings(FMLMissingMappingsEvent event) {
        for (MissingMapping m : event.get()) {
            if (m.name.contains("dobule")) {
                if (m.type == Type.BLOCK) {
                    m.remap((Block) Block.blockRegistry.getObject(m.name.replace("dobule", "double")));
                } else if (m.type == Type.ITEM) {
                    m.remap((Item) Item.itemRegistry.getObject(m.name.replace("dobule", "double")));
                }
            }
        }
    }
}
