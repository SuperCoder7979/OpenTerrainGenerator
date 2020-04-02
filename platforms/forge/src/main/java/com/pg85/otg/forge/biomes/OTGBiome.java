package com.pg85.otg.forge.biomes;

import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.MineshaftConfig;
import net.minecraft.world.gen.feature.structure.MineshaftStructure;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

import java.awt.*;

/**
 * Used for all custom biomes.
 */
public class OTGBiome extends Biome// implements IOTGASMBiome
{
    OTGBiome(BiomeConfig config, ResourceLocation registryKey)
    {
		super(GetBiomeBuilder(config));
		this.func_226711_a_(Feature.VILLAGE.func_225566_b_(new VillageConfig("village/plains/town_centers", 6)));
		this.func_226711_a_(Feature.PILLAGER_OUTPOST.func_225566_b_(IFeatureConfig.NO_FEATURE_CONFIG));
		this.func_226711_a_(Feature.MINESHAFT.func_225566_b_(new MineshaftConfig(0.004D, MineshaftStructure.Type.NORMAL)));
		this.func_226711_a_(Feature.STRONGHOLD.func_225566_b_(IFeatureConfig.NO_FEATURE_CONFIG));
		DefaultBiomeFeatures.addCarvers(this);
		DefaultBiomeFeatures.addStructures(this);
		DefaultBiomeFeatures.addLakes(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.func_222283_Y(this);
		DefaultBiomeFeatures.addStoneVariants(this);
		DefaultBiomeFeatures.addOres(this);
		DefaultBiomeFeatures.addSedimentDisks(this);
		DefaultBiomeFeatures.func_222299_R(this);
		DefaultBiomeFeatures.addMushrooms(this);
		DefaultBiomeFeatures.addReedsAndPumpkins(this);
		DefaultBiomeFeatures.addSprings(this);
		DefaultBiomeFeatures.addFreezeTopLayer(this);
		this.addSpawn(EntityClassification.CREATURE, new Biome.SpawnListEntry(EntityType.SHEEP, 12, 4, 4));
		this.addSpawn(EntityClassification.CREATURE, new Biome.SpawnListEntry(EntityType.PIG, 10, 4, 4));
		this.addSpawn(EntityClassification.CREATURE, new Biome.SpawnListEntry(EntityType.CHICKEN, 10, 4, 4));
		this.addSpawn(EntityClassification.CREATURE, new Biome.SpawnListEntry(EntityType.COW, 8, 4, 4));
		this.addSpawn(EntityClassification.CREATURE, new Biome.SpawnListEntry(EntityType.HORSE, 5, 2, 6));
		this.addSpawn(EntityClassification.CREATURE, new Biome.SpawnListEntry(EntityType.DONKEY, 1, 1, 3));
		this.addSpawn(EntityClassification.AMBIENT, new Biome.SpawnListEntry(EntityType.BAT, 10, 8, 8));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SPIDER, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ZOMBIE, 95, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SKELETON, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.CREEPER, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SLIME, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ENDERMAN, 10, 1, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.WITCH, 5, 1, 1));
		
		this.setRegistryName(registryKey);
    }
    
    private static Biome.Builder GetBiomeBuilder(BiomeConfig biomeConfig)
    {
    	Biome.Builder biomeBuilder = new Biome.Builder();    	
		biomeBuilder.surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG); //new SurfaceBuilderConfig(RED_SAND, WHITE_TERRACOTTA, GRAVEL)
		biomeBuilder.category(Biome.Category.NONE);
		biomeBuilder.depth(biomeConfig.biomeHeight).scale(biomeConfig.biomeVolatility);
		biomeBuilder.temperature(biomeConfig.biomeTemperature);
		if(biomeConfig.biomeWetness > 0.0001)
		{
			if(biomeConfig.biomeTemperature <= WorldStandardValues.SNOW_AND_ICE_TEMP)
			{
				biomeBuilder.precipitation(Biome.RainType.SNOW);	
			} else {
				biomeBuilder.precipitation(Biome.RainType.RAIN);
			}			
			biomeBuilder.downfall(biomeConfig.biomeWetness);
		} else {
			biomeBuilder.precipitation(Biome.RainType.NONE);
			biomeBuilder.downfall(0.0f);			
		}
		
		//water color is multiplier, multiply with vanilla's default value
		//TODO: check if this is the intended behavior

        int multiplier = biomeConfig.waterColor;
		//multiply channels seperately to replicate old behavior
        int r = (((multiplier & 0xFF0000) >> 16) / 255) * ((4159204 & 0xFF0000) >> 16);
        int g = (((multiplier & 0xFF00) >> 8) / 255) * ((4159204 & 0xFF00) >> 8);
        int b = (((multiplier & 0xFF)) / 255) * ((4159204 & 0xFF));
		int finalColor = ((r & 0xFF) << 16) |
				((g & 0xFF) << 8)  |
				((b & 0xFF));

		biomeBuilder.waterColor(finalColor); //4159204
		biomeBuilder.waterFogColor(329011);	
		biomeBuilder.parent(null);
    	return biomeBuilder;
    }
}
