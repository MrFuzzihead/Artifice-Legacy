package com.mrfuzzihead.artifice.item;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.util.BlockCoord;

public class DispenserBehaviorSledge extends BehaviorDefaultDispenseItem {

    public ItemStack dispenseStack(IBlockSource dispenser, ItemStack stack) {
        World world = dispenser.getWorld();
        Random rand = world.rand;
        EnumFacing face = BlockDispenser.func_149937_b(dispenser.getBlockMetadata());
        BlockCoord target = new BlockCoord(dispenser.getXInt(), dispenser.getYInt(), dispenser.getZInt());
        target.offset(face.ordinal());

        // Read the ingredients once, before the block is gone. The second read used to happen after setBlockToAir,
        // and getIngredientsForBlock starts with an isAirBlock test, so it returned null and the for-each threw an
        // NPE on every dispense (CODE_REVIEW 5.2).
        ArrayList<ItemStack> drops = ItemSledge.getIngredientsForBlock(world, target.x, target.y, target.z, stack);
        if (drops == null) {
            return stack;
        }

        world.setBlockToAir(target.x, target.y, target.z);

        for (ItemStack drop : drops) {
            EntityItem item = new EntityItem(world, target.x + 0.5, target.y + 0.5, target.z + 0.5, drop.copy());
            double xVel = -0.1 + 0.2 * rand.nextDouble();
            double zVel = -0.1 + 0.2 * rand.nextDouble();
            // Entity.setVelocity is @SideOnly(CLIENT) - it is stripped from the server jar, so calling it here
            // threw NoSuchMethodError on a dedicated server, right after the NPE above was out of the way.
            item.motionX = xVel;
            item.motionY = 0.1;
            item.motionZ = zVel;
            world.spawnEntityInWorld(item);
        }

        // A dispenser has no player to damage the tool, so go through the same path a player would: Unbreaking is
        // honoured and a tool that reaches max damage is consumed instead of running past it.
        if (stack.attemptDamageItem(1, rand)) {
            stack.stackSize = 0;
        }

        return stack;
    }

    protected void playDispenseSound(IBlockSource dispenser) {
        dispenser.getWorld()
            .playAuxSFX(1002, dispenser.getXInt(), dispenser.getYInt(), dispenser.getZInt(), 0);
    }
}
