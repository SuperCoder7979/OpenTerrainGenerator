package com.pg85.otg.forge.world;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.biomes.ForgeBiomeRegistryManager;
import com.pg85.otg.forge.terraingen.OTGChunkGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.terraingen.ChunkBuffer;
import com.pg85.otg.terraingen.ObjectSpawner;
import com.pg85.otg.terraingen.biome.BiomeGenerator;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.TreeType;
import com.pg85.otg.customobjects.SpawnableObject;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.exception.BiomeNotFoundException;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

public class ForgeWorld extends LocalWorld
{
    public static final int STANDARD_WORLD_HEIGHT = 128; // TODO: Why is this 128, should be 255?
	
    private OTGChunkGenerator generator;
    private BiomeGenerator biomeGenerator;
    private ConfigProvider settings;
    
    // HURRAY FOR STATE!!!
    public HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();
    public HashMap<Biome, ForgeBiome> biomesMap = new HashMap<Biome, ForgeBiome>();
    
    public World world;
    private String name;
    private long seed;

    public ForgeWorld(String _name)
    {
		OTG.log(LogMarker.INFO, "Creating world \"" + _name + "\"");
        this.name = _name;
    }

    // Getters / setters
    
    public World getWorld()
    {
        return this.world;
    }
    
    public OTGChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }
    
    public void setBiomeGenerator(BiomeGenerator generator)
    {
        this.biomeGenerator = generator;
    }

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getWorldSettingsName()
	{
		return this.getWorld().getWorldInfo().getWorldName();
	}

	@Override
	public int getDimensionId()
	{
		return this.world.getDimension().getType().getId();
	}

	@Override
	public long getSeed()
	{
		return this.seed;
	}

	@Override
	public File getWorldSaveDir()
	{
		File worldDir;
		if(((ServerWorld)this.world).getSaveHandler() != null)
		{
			worldDir = ((ServerWorld)this.world).getSaveHandler().getWorldDirectory();
		} else {
			worldDir = new File(Minecraft.getInstance().gameDir + File.separator + "saves" + File.separator + this.world.getWorldInfo().getWorldName());
		}
		return worldDir;
	}

	@Override
	public ConfigProvider getConfigs()
	{
		return this.settings;
	}

	@Override
	public ObjectSpawner getObjectSpawner()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CustomStructureCache getStructureCache()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorldSession getWorldSession()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteWorldSessionData()
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public int getHeightCap()
	{
		return this.settings.getWorldConfig().worldHeightCap;
	}

	@Override
	public int getHeightScale()
	{
		return this.settings.getWorldConfig().worldHeightScale;
	}

	@Override
	public BiomeGenerator getBiomeGenerator()
	{
		return this.biomeGenerator;
	}

	// Configs
	// TODO: Use a configmanager object
	
    public void provideConfigs(ServerConfigProvider configs)
    {
        this.settings = configs;
    }
    
    /**
     * Call this method when the Minecraft world is loaded. Call this method
     * after {@link #provideConfigs(ServerConfigProvider)} has been called.
     * @param world The Minecraft world.
     */
    public void provideWorldInstance(ServerWorld world)
    {
        //ServerConfigProvider configs = (ServerConfigProvider) this.settings;
        //DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(world.getDimension().getType().getRegistryName().getPath());        

        this.world = world;
        this.seed = world.getWorldInfo().getSeed();
        //world.setSeaLevel(configs.getWorldConfig().waterLevelMax);
        //this.generator = new OTGChunkGenerator(this);
    }
	
	// Biomes
	
	@Override
	public LocalBiome createBiomeFor(BiomeConfig biomeConfig, int otgBiomeId, ConfigProvider configProvider, boolean isReload)
	{
    	ForgeBiome forgeBiome = ForgeBiomeRegistryManager.getOrCreateBiome(biomeConfig, otgBiomeId, this.getName(), configProvider);
        this.biomeNames.put(forgeBiome.getName(), forgeBiome);
        this.biomesMap.put(forgeBiome.biomeBase, forgeBiome);
        return forgeBiome;
	}

	@Override
	public ArrayList<LocalBiome> getAllBiomes()
	{
    	ArrayList<LocalBiome> biomes = new ArrayList<LocalBiome>();
		for(LocalBiome biome : this.settings.getBiomeArrayByOTGId())
		{
			if(biome != null)
			{
				biomes.add(biome);
			}
		}
    	return biomes;
	}

	public ForgeBiome getBiome(Biome biome)
	{
		return this.biomesMap.get(biome);
	}
	
	@Override
	public LocalBiome getBiome(int x, int z) throws BiomeNotFoundException
	{
		return getBiomeByOTGIdOrNull(this.biomeGenerator.getBiome(x, z));
	}

	@Override
	public LocalBiome getBiomeByOTGIdOrNull(int id)
	{
		return (ForgeBiome) this.settings.getBiomeByOTGIdOrNull(id);
	}

	@Override
	public LocalBiome getFirstBiomeOrNull()
	{
		return this.biomeNames.size() > 0 ? (LocalBiome) this.biomeNames.values().toArray()[0] : null;
	}

	@Override
	public LocalBiome getBiomeByNameOrNull(String name)
	{
		return this.biomeNames.get(name);
	}
	
	@Override
	public void prepareDefaultStructures(int chunkX, int chunkZ, boolean dry)
	{
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean placeDungeon(Random rand, int x, int y, int z)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean placeFossil(Random rand, ChunkCoordinate chunkCoord)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean placeDefaultStructures(Random rand, ChunkCoordinate chunkCoord)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SpawnableObject getMojangStructurePart(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean chunkHasDefaultStructure(Random rand, ChunkCoordinate chunk)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void placePopulationMobs(LocalBiome biome, Random random, ChunkCoordinate chunkCoord)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void spawnEntity(EntityFunction<?> entityData, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub		
	}

	@Override
	public void startPopulation(ChunkCoordinate chunkCoord)
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public void endPopulation()
	{
		// TODO Auto-generated method stub		
	}

	@Override
	public LocalMaterialData getMaterial(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBlockAboveLiquidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBlockAboveSolidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHighestBlockAboveYAt(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid,
			boolean ignoreSnow, boolean ignoreLeaves, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeightMapHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag,
			ChunkCoordinate chunkBeingPopulated)
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public LocalMaterialData[] getBlockColumnInUnloadedChunk(int x, int z)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void replaceBlocks(ChunkCoordinate chunkCoord)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isInsidePregeneratedRegion(ChunkCoordinate chunk)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ChunkCoordinate getSpawnChunk()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean generateModdedCaveGen(int x, int z, ChunkBuffer chunkBuffer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
