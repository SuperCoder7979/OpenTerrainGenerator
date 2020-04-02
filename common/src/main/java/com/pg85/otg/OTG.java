package com.pg85.otg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.PluginConfig;
import com.pg85.otg.configuration.biome.settings.BiomeResourcesManager;
import com.pg85.otg.configuration.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.io.FileSettingsReader;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.events.EventHandler;
import com.pg85.otg.events.EventPriority;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.terraingen.ChunkBuffer;
import com.pg85.otg.terraingen.biome.BiomeModeManager;
import com.pg85.otg.terraingen.resource.Resource;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.CompressionUtils;

public class OTG
{
    private OTG() { }
	
    private static OTGEngine Engine;
    
    // Engine
	
    /**
     * Returns the engine, containing the API methods.
     * <p>
     * @return The engine
     */
    public static OTGEngine getEngine()
    {
        return Engine;
    }
    
    /**
     * Sets the engine and calls its {@link OTGEngine#onStart()
     * onStart()} method.
     * <p>
     * @param engine The engine.
     */
    public static void setEngine(OTGEngine engine)
    {
        if (OTG.Engine != null)
        {
            throw new IllegalStateException("Engine is already set.");
        }

        OTG.Engine = engine;
        engine.onStart();
    }

    /**
     * Nulls out static references to free up memory. Should be called on
     * shutdown. Engine can be restarted after this.
     */
    public static void stopEngine()
    {
        Engine.onShutdown();
        Engine = null;
    }
    
    // Managers
    
    /**
     * Returns the biome managers. Register your own biome manager here.
     * <p>
     * @return The biome managers.
     */
    public static BiomeModeManager getBiomeModeManager()
    {
        return Engine.getBiomeModeManager();
    }

    public static BiomeResourcesManager getBiomeResourcesManager()
    {
        return Engine.getBiomeResourceManager();
    }

    public static CustomObjectResourcesManager getCustomObjectResourcesManager()
    {
        return Engine.getCustomObjectResourcesManager();
    }

    public static CustomObjectManager getCustomObjectManager()
    {
        return Engine.getCustomObjectManager();
    }
    
    // Plugin
    
    public static PluginConfig getPluginConfig()
    {
        return Engine.getPluginConfig();
    }    
    
    // Dimensions config
    
	/**
	 * A config for each dimension of the currently active world
	 */
    public static DimensionsConfig getDimensionsConfig()
    {    	
    	return Engine.getDimensionsConfig();
    }

    public static void setDimensionsConfig(DimensionsConfig dimensionsConfig)
    {
    	Engine.setDimensionsConfig(dimensionsConfig);
    }
    
    // Biomes
    
    // For bukkit plugin developers, do not remove. See: https://github.com/MCTCP/TerrainControl/wiki/Developer-page
    /*
	* Convenience method to quickly get the biome name at the given
	* coordinates. Will return null if the world isn't loaded by OTG
	* <p>
	* @param worldName The world name.
	* @param x         The block x in the world.
	* @param z         The block z in the world.
	* @return The biome name, or null if the world isn't managed by Terrain
    *         Control.
    */
	public static String getBiomeName(String worldName, int x, int z)
	{
	   LocalWorld world = getWorld(worldName);
       if (world == null)
       {
    	   // World isn't loaded by OTG
    	   return null;
       }       
       
       return world.getBiome(x, z).getName();
   	}

   	public static LocalBiome getBiomeByOTGId(int id)
   	{
	   ArrayList<LocalWorld> worlds = getAllWorlds();
	   if(worlds != null)
	   {
		   for(LocalWorld world : worlds)
		   {
			   LocalBiome biome = world.getBiomeByOTGIdOrNull(id);
			   if(biome != null)
			   {
				   return biome;
			   }
		   }
	   }
	   return null;
   	}
   	
    public static LocalBiome getBiome(String name, String worldName)
    {    	
        ArrayList<LocalWorld> worlds = getAllWorlds();
        if(worlds != null)
        {
	        for(LocalWorld world : worlds)
	        {
	        	if(world.getName().toLowerCase().equals(worldName.toLowerCase()))
	        	{
		        	LocalBiome biome = world.getBiomeByNameOrNull(name);
		        	if(biome != null)
		        	{
		        		return biome;
		        	}
	        	}
	        }
        }
        return null;
    }

    // Worlds
    
    public static LocalWorld getWorld(String name)
    {
        return Engine.getWorld(name);
    }

    public static LocalWorld getUnloadedWorld(String name)
    {
    	return Engine.getUnloadedWorld(name);
    }
    
    public static ArrayList<LocalWorld> getAllWorlds()
    {
        return Engine.getAllWorlds();
    }
    
    // Events
    
    /**
     * @see OTGEngine#registerEventHandler(EventHandler)
     */
    public static void registerEventHandler(EventHandler handler)
    {
        Engine.registerEventHandler(handler);
    }

    /**
     * @see OTGEngine#registerEventHandler(EventHandler,
     * EventPriority)
     */
    public static void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        Engine.registerEventHandler(handler, priority);
    }
    
    public static boolean fireReplaceBiomeBlocksEvent(int x, int z, ChunkBuffer chunkBuffer, LocalWorld localWorld)
    {
    	return Engine.fireReplaceBiomeBlocksEvent(x, z, chunkBuffer, localWorld);
    }
    
    /**
     * @see OTGEngine#fireCanCustomObjectSpawnEvent(CustomObject,
     * LocalWorld, int, int, int)
     */
    public static boolean fireCanCustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z)
    {
        return Engine.fireCanCustomObjectSpawnEvent(object, world, x, y, z);
    }

    /**
     * @see OTGEngine#firePopulationEndEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        Engine.firePopulationEndEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see OTGEngine#firePopulationStartEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        Engine.firePopulationStartEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see OTGEngine#fireResourceProcessEvent(Resource,
     * LocalWorld, Random, boolean, int, int)
     */
    public static boolean fireResourceProcessEvent(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        return Engine.fireResourceProcessEvent(resource, world, random, villageInChunk, chunkX, chunkZ);
    }    
    
    // Logging
    
    public static void log(LogMarker level, List<String> messages)
    {
    	Engine.getLogger().log(level, messages);
    }

    public static void log(LogMarker level, String message, Object... params)
    {
    	Engine.getLogger().log(level, message, params);
    }

    public static void printStackTrace(LogMarker level, Throwable e)
    {
        printStackTrace(level, e, Integer.MAX_VALUE);
    }

    public static void printStackTrace(LogMarker level, Throwable e, int maxDepth)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        Engine.getLogger().log(level, stringWriter.toString());
    }
    
    // Misc TODO: Clean up

    public static String correctOldBiomeConfigFolder(File settingsDir)
    {
        // Rename the old folder
        String biomeFolderName = WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME;
        File oldBiomeConfigs = new File(settingsDir, "BiomeConfigs");
        if (oldBiomeConfigs.exists())
        {
            if (!oldBiomeConfigs.renameTo(new File(settingsDir, biomeFolderName)))
            {
                OTG.log(LogMarker.WARN, "========================");
                OTG.log(LogMarker.WARN, "Found old `BiomeConfigs` folder, but it could not be renamed to `", biomeFolderName, "`!");
                OTG.log(LogMarker.WARN, "Please rename the folder manually.");
                OTG.log(LogMarker.WARN, "========================");
                biomeFolderName = "BiomeConfigs";
            }
        }
        return biomeFolderName;
    }
        	
    public static boolean IsInAreaBeingPopulated(int blockX, int blockZ, ChunkCoordinate chunkBeingPopulated)
    {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        return
			(
				chunkX == chunkBeingPopulated.getChunkX() ||
				chunkX == chunkBeingPopulated.getChunkX() + 1
			) && (
				chunkZ == chunkBeingPopulated.getChunkZ() ||
				chunkZ == chunkBeingPopulated.getChunkZ() + 1
			)
		;
    }
    
    public static void generateBO4Data(BO4Config config)
    {
        //write to disk
		String filePath = 
			config.getFile().getAbsolutePath().endsWith(".BO4") ? config.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo4") ? config.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".BO3") ? config.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo3") ? config.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			config.getFile().getAbsolutePath();

        File file = new File(filePath);
        if(!file.exists())
        {
            try {                
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				config.writeToStream(dos);
				byte[] compressedBytes = CompressionUtils.compress(bos.toByteArray());
				dos.close();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos2 = new DataOutputStream(fos);
				dos2.write(compressedBytes, 0, compressedBytes.length);
				dos2.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }        
}
