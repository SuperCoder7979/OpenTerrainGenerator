package com.pg85.otg.forge.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;

import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.biomes.OTGBiomeProvider;
import com.pg85.otg.forge.biomes.OTGBiomeProviderSettings;
import com.pg85.otg.forge.terraingen.OTGChunkGenerator;
import com.pg85.otg.forge.terraingen.OTGGenSettings;

public class OTGWorldType extends WorldType
{
    public OTGWorldType()
    {
        super(PluginStandardValues.PLUGIN_NAME_SHORT);
    }
	
	public OTGWorldType(String name)
	{
		super(name);
	}
	
    @Override
	public ChunkGenerator<?> createChunkGenerator(World world)
    {
		ChunkGeneratorType<OTGGenSettings, OTGChunkGenerator> chunkgeneratortype4 = OTGPlugin.OtgChunkGeneratorType;
		OTGGenSettings otggensettings = chunkgeneratortype4.createSettings();
		OTGBiomeProviderSettings overworldbiomeprovidersettings = new OTGBiomeProviderSettings(world.getWorldInfo()).setGeneratorSettings(otggensettings);
		return chunkgeneratortype4.create(world, new OTGBiomeProvider(overworldbiomeprovidersettings), otggensettings);
    }
}
