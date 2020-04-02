package com.pg85.otg.forge.terraingen;

import net.minecraft.world.gen.OverworldGenSettings;

// net.minecraft.world.gen.OverworldGenSettings
//TODO: Don't inherit from OverworldGenSettings after replacing LayerUtil.func_227474_a_ in OTGBiomeProvider
public class OTGGenSettings extends OverworldGenSettings 
{
   @Override
   public int getBiomeSize()
   {
      return 4;
   }

   @Override
   public int getRiverSize()
   {
      return 4;
   }

   @Override
   public int getBiomeId()
   {
      return -1;
   }

   @Override
   public int getBedrockFloorHeight()
   {
      return 0;
   }
}
