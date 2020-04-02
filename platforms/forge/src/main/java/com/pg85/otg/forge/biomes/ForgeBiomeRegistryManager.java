package com.pg85.otg.forge.biomes;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.DefaultBiome;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.helpers.StringHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeBiomeRegistryManager
{
	public static ForgeBiome getOrCreateBiome(BiomeConfig biomeConfig, int otgBiomeId, String presetName, ConfigProvider configProvider)
	{
		// NOTE: Let's hope noone uses dots in their folder names... (Biome Bundle and Biome.Bundle would be the same to OTG)
	    String biomeNameForRegistry = presetName.toLowerCase().replace(' ', '.') + "_" + StringHelper.toComputerFriendlyName(biomeConfig.getName());
	    String resourceDomain = PluginStandardValues.PLUGIN_NAME_LOWER_CASE;
	    ResourceLocation registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);
	
	    // Check if registered earlier
	    Biome biome = ForgeRegistries.BIOMES.getValue(registryKey);
	    if (biome == null)
	    {
	        // No existing biome, create new one
	    	biome = new OTGBiome(biomeConfig, presetName, registryKey);
	    	registerForgeBiome(biome);
		    ForgeBiomeRegistryManager.registerBiomeInBiomeDictionary(biome, biomeConfig, configProvider);
	    }
	    
	    return new ForgeBiome(biome, otgBiomeId, biomeConfig);
	}

	private static void registerBiomeInBiomeDictionary(Biome biome, BiomeConfig biomeConfig, ConfigProvider configProvider)
	{
	    // Add inherited BiomeDictId's for replaceToBiomeName. Biome dict id's are stored twice,
	    // there is 1 list of biomedict types per biome id and one list of biomes (not id's) per biome dict type.
	
	    ArrayList<Type> types = new ArrayList<Type>();
        if(biomeConfig.biomeDictId != null && biomeConfig.biomeDictId.trim().length() > 0)
        {
        	types = ForgeBiomeRegistryManager.getTypesList(biomeConfig.biomeDictId.split(","));
        }
	
		Type[] typeArr = new Type[types.size()];
		types.toArray(typeArr);
	
		if(!ForgeRegistries.BIOMES.containsValue(biome))
		{
			OTG.log(LogMarker.WARN, "Biome " + biome.getRegistryName() + " could not be found in the registry. This could be because it is a virtual biome (id > 255) but does not have a ReplaceToBiomeName configured.");
		}
	
		BiomeDictionary.addTypes(biome, typeArr);
	}

	private static ArrayList<Type> getTypesList(String[] typearr)
	{
		ArrayList<Type> types = new ArrayList<Type>();
		for(String typeString : typearr)
		{
			if(typeString != null && typeString.trim().length() > 0)
			{
		        Type type = null;
				typeString = typeString.trim();
		        try
		        {
		        	type = Type.getType(typeString, null);
		        }
		        catch(Exception ex)
		        {
		        	OTG.log(LogMarker.WARN, "Can't find BiomeDictId: \"" + typeString + "\".");
		        }
		        if(type != null)
		        {
		        	types.add(type);
		        }
			}
		}
		return types;
	}
    	
    private static void registerForgeBiome(Biome biome)
    {
    	OTG.log(LogMarker.DEBUG, "Registering biome " + biome.getRegistryName().toString());
//			System.out.println("DUMB THING: " + new ResourceLocation(biome.getTranslationKey()).toString());
    	Registry.register(Registry.BIOME, new ResourceLocation(biome.getTranslationKey()), biome);
    }
    
	public static List<BiomeLoadInstruction> getDefaultBiomes()
	{
        // Loop through all default biomes and create the default
        // settings for them
        List<BiomeLoadInstruction> standardBiomes = new ArrayList<BiomeLoadInstruction>();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(ForgeMojangSettings.fromResourceLocation(ResourceLocation.tryCreate(defaultBiome.resourceLocation)), ForgeWorld.STANDARD_WORLD_HEIGHT);
            standardBiomes.add(instruction);
        }

        return standardBiomes;
	}
}
