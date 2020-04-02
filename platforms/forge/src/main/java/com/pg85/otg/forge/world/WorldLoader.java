package com.pg85.otg.forge.world;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfigFinder;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.io.FileSettingsWriter;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.DefaultBiome;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.configuration.world.WorldConfig.DefaulWorldData;
import com.pg85.otg.forge.biomes.ForgeMojangSettings;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.helpers.FileHelper;

/**
 * Responsible for loading and unloading the world.
 *
 */
public final class WorldLoader
{	
	private final Object worldsMapsLock = new Object();
	private final File configsDir;
    private final HashMap<String, ForgeWorld> worldsByName = new HashMap<String, ForgeWorld>();
    private final HashMap<ServerWorld, ForgeWorld> worldsByWorld = new HashMap<ServerWorld, ForgeWorld>();
    private final HashMap<String, ForgeWorld> unloadedWorldsByName = new HashMap<String, ForgeWorld>();
    private final HashMap<String, ForgeWorld> unloadedWorldsByWorld = new HashMap<String, ForgeWorld>();

    public WorldLoader()
    {
        File dataFolder = new File(Minecraft.getInstance().gameDir, "mods" + File.separator + PluginStandardValues.PLUGIN_NAME);
        this.configsDir = dataFolder;
    }
    
    public File getConfigsFolder()
    {
        return this.configsDir;
    }

    private File getWorldDir(String worldName)
    {
        return new File(this.configsDir, PluginStandardValues.PresetsDirectoryName + File.separator + worldName);
    }
    
    public void registerBiomesForPreset(File presetDir)
    {
        if (presetDir == null || !presetDir.exists())
        {
            // OpenTerrainGenerator is not enabled for this world
            return;
        }

        String worldName = presetDir.getName();
        ForgeWorld world = this.getWorld(worldName);
        if (world == null)
        {           	
            world = new ForgeWorld(worldName);
            OTG.log(LogMarker.DEBUG, "Loading biomes for preset \"{}\"..", worldName);

    		File worldDir = new File(Minecraft.getInstance().gameDir + File.separator + "saves" + File.separator + worldName);
            
            ServerConfigProvider config = new ServerConfigProvider(presetDir, world, worldDir);            
            world.provideConfigs(config);
            OTG.getEngine().unregisterOTGBiomeIdsForWorld(worldName);
            
            OTG.log(LogMarker.DEBUG, "Completed loading biomes for preset \"{}\"..", worldName);
        }
    }

    public ForgeWorld getOrCreateForgeWorld(ServerWorld mcWorld, String worldName)
    {
    	File worldConfigsFolder = null;
    	
    	worldConfigsFolder = this.getWorldDir(OTG.getDimensionsConfig().getDimensionConfig(worldName).PresetName);    	    	
        if (worldConfigsFolder == null || !worldConfigsFolder.exists())
        {
            // OpenTerrainGenerator is not enabled for this world
            return null;
        }

        ForgeWorld world = this.getWorld(worldName);
        if (world == null)
        {           	
            world = new ForgeWorld(worldName);
            OTG.log(LogMarker.DEBUG, "Loading configs for world \"{}\"..", world.getName());

    		File worldDir;
    		if(((ServerWorld)mcWorld).getSaveHandler() != null)
    		{
    			worldDir = ((ServerWorld)mcWorld).getSaveHandler().getWorldDirectory();
    		} else {
    			worldDir = new File(Minecraft.getInstance().gameDir + File.separator + "saves" + File.separator + mcWorld.getWorldInfo().getWorldName());
    		}
            
            ServerConfigProvider config = new ServerConfigProvider(worldConfigsFolder, world, worldDir);            
            world.provideConfigs(config);
            
            OTG.log(LogMarker.DEBUG, "Completed loading configs for world \"{}\"..", world.getName());
        }
        if (world != null && world.getWorld() == null)
        {
        	world.provideWorldInstance(mcWorld);
        }
        
        synchronized(worldsMapsLock)
        {
    		this.worldsByName.put(worldName, world);
    		this.unloadedWorldsByName.remove(worldName);
    		this.worldsByWorld.put(mcWorld, world);
    		this.unloadedWorldsByWorld.remove(mcWorld);
        }
        
        return world;
    }
    
    public void createDefaultOTGWorld(String worldName)
    {
    	File worldDir = new File(OTG.getEngine().getWorldsDirectory() + File.separator + worldName);
    	if(!worldDir.exists())
    	{
	        List<File> worldDirs = new ArrayList<File>(2);
	        worldDirs.add(new File(worldDir, WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME));
	        worldDirs.add(new File(worldDir, WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME));
	        FileHelper.makeFolders(worldDirs);		
			
			// World config
			DefaulWorldData defaultWorldData = WorldConfig.createDefaultOTGWorldConfig(worldDir, worldName);
			SettingsMap settingsMap = defaultWorldData.settingsMap;
			WorldConfig defaultWorldConfig = defaultWorldData.worldConfig;			
	    	FileSettingsWriter.writeToFile(settingsMap, new File(worldDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME), WorldConfig.ConfigMode.WriteAll);
	    	
	    	// Biome configs
	        // Build a set of all biomes to load
	        Collection<BiomeLoadInstruction> biomesToLoad = new HashSet<BiomeLoadInstruction>();
	
	        // Loop through all default biomes and create the default
	        // settings for them
	        List<BiomeLoadInstruction> defaultBiomes = new ArrayList<BiomeLoadInstruction>();
	        for (DefaultBiome defaultBiome : DefaultBiome.values())
	        {
	            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(ForgeMojangSettings.fromResourceLocation(ResourceLocation.create(defaultBiome.resourceLocation, ':')), ForgeWorld.STANDARD_WORLD_HEIGHT);
	            defaultBiomes.add(instruction);
	        }
	        
	        // If we're creating a new world with new configs then add the default biomes
	        for (BiomeLoadInstruction defaultBiome : defaultBiomes)
	        {
	    		biomesToLoad.add(new BiomeLoadInstruction(defaultBiome.getBiomeName(), defaultBiome.getBiomeTemplate()));
	        }
	        
	        List<File> biomeDirs = new ArrayList<File>(1);
	        biomeDirs.add(new File(worldDir, WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME));
	        
	        // Load all files
	        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(OTG.getPluginConfig().biomeConfigExtension);
	        Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(defaultWorldConfig, defaultWorldConfig.worldHeightScale, biomeDirs, biomesToLoad);
	        
	        // Write all biomes
	
	        for (BiomeConfigStub biomeConfigStub : biomeConfigStubs.values())
	        {
	            // Settings reading
	            BiomeConfig biomeConfig = new BiomeConfig(biomeConfigStub.getLoadInstructions(), biomeConfigStub, biomeConfigStub.getSettings(), defaultWorldConfig);
	
	            // Settings writing
	            File writeFile = biomeConfigStub.getFile();
	            if (!biomeConfig.biomeExtends.isEmpty())
	            {
	                writeFile = new File(writeFile.getAbsolutePath() + ".inherited");
	            }
	            FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile, defaultWorldConfig.settingsMode);
	        }
    	}
    }
    
    public ForgeWorld getWorld(String name)
    {
    	if(name == null)
    	{
    		return null;
    	}
    	if(name.equals("overworld"))
    	{
    		return getOverWorld();
    	}

    	ForgeWorld forgeWorld = null;        
        synchronized(worldsMapsLock)
        {
        	forgeWorld = this.worldsByName.get(name);
        }
        
        return forgeWorld;
    }
    
    public ForgeWorld getWorld(ServerWorld world)
    {
    	if(world == null)
    	{
    		return null;
    	}

    	ForgeWorld forgeWorld = null;        
        synchronized(worldsMapsLock)
        {
        	forgeWorld = this.worldsByName.get(world);
        }
        
        return forgeWorld;
    }
    
    public ForgeWorld getOverWorld()
    {
		ArrayList<LocalWorld> allWorlds = getAllWorlds();
		for(LocalWorld world : allWorlds)
		{
			// Overworld can be null for MP clients who are in a dimension, try 'overworld'
			if(
				(
					((ForgeWorld)world).getWorld() != null && 
					((ForgeWorld)world).getWorld().getDimension().getType().getId() == 0 
				) || (
						((ForgeWorld)world).getName().equals("overworld")
				)				
			)
			{
				return (ForgeWorld)world;
			}
		}
		return null;
    }
    
    public ArrayList<LocalWorld> getAllWorlds()
    {
    	ArrayList<LocalWorld> allWorlds = new ArrayList<LocalWorld>();
    	synchronized(worldsMapsLock)
    	{
			allWorlds.addAll(worldsByName.values());
			allWorlds.addAll(unloadedWorldsByName.values());
    	}
    	return allWorlds;
    }
}
