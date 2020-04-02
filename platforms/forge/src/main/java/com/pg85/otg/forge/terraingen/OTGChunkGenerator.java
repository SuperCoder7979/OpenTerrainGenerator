package com.pg85.otg.forge.terraingen;

import java.io.File;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import net.minecraft.entity.EntityClassification;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.ImprovedNoiseGenerator;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;

import java.util.Random;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.biomes.OTGBiomeProvider;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.terraingen.ChunkProviderOTG;
import com.pg85.otg.terraingen.biome.BiomeGenerator;
import com.pg85.otg.util.ChunkCoordinate;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class OTGChunkGenerator extends NoiseChunkGenerator<OTGGenSettings>
{
	// ChunkGenerator
	protected final IWorld world;
	protected final long seed;
	protected final OTGBiomeProvider biomeProvider;
	private ChunkProviderOTG generator;
	protected final OTGGenSettings settings;
	
	// NoiseChunkGenerator
	private static final float[] field_222561_h = Util.make(new float[13824], (p_222557_0_) ->
	{
		for(int i = 0; i < 24; ++i)
		{
			for(int j = 0; j < 24; ++j)
			{
				for(int k = 0; k < 24; ++k)
				{
					p_222557_0_[i * 24 * 24 + j * 24 + k] = (float)func_222554_b(j - 12, k - 12, i - 12);
				}
			}
		}
	});
	private static double func_222554_b(int p_222554_0_, int p_222554_1_, int p_222554_2_)
	{
		double d0 = (double)(p_222554_0_ * p_222554_0_ + p_222554_2_ * p_222554_2_);
		double d1 = (double)p_222554_1_ + 0.5D;
		double d2 = d1 * d1;
		double d3 = Math.pow(Math.E, -(d2 / 16.0D + d0 / 16.0D));
		double d4 = -d1 * MathHelper.fastInvSqrt(d2 / 2.0D + d0 / 2.0D) / 2.0D;
		return d4 * d3;
	}
	
	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	private final int verticalNoiseGranularity;
	private final int horizontalNoiseGranularity;
	private final int noiseSizeX;
	private final int noiseSizeY;
	private final int noiseSizeZ;
	protected final SharedSeedRandom randomSeed;
	private final OctavesNoiseGenerator field_222568_o;
	private final OctavesNoiseGenerator field_222569_p;
	private final OctavesNoiseGenerator field_222570_q;
	private final INoiseGenerator surfaceDepthNoise;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;
	
	// OverworldChunkGenerator
	private static final float[] field_222576_h = Util.make(new float[25], (p_222575_0_) ->
	{
		for(int i = -2; i <= 2; ++i)
		{
			for(int j = -2; j <= 2; ++j)
			{
				float f = 10.0F / MathHelper.sqrt((float)(i * i + j * j) + 0.2F);
				p_222575_0_[i + 2 + (j + 2) * 5] = f;
			}
		}
	});
	private final OctavesNoiseGenerator depthNoise;
	private final boolean field_222577_j;
	private final PhantomSpawner phantomSpawner = new PhantomSpawner();
	private final PatrolSpawner patrolSpawner = new PatrolSpawner();
	private final CatSpawner catSpawner = new CatSpawner();
	private final VillageSiege field_225495_n = new VillageSiege();
	
	public OTGChunkGenerator(IWorld worldIn, BiomeProvider provider, OTGGenSettings settingsIn)
	{
		super(worldIn, provider, 4, 8, 256, settingsIn, true);
		
		boolean usePerlin = true;
		this.world = worldIn;
		this.seed = worldIn.getSeed();
		this.biomeProvider = (OTGBiomeProvider)provider;
		this.settings = settingsIn;
		this.verticalNoiseGranularity = 8;
		this.horizontalNoiseGranularity = 4;
		this.defaultBlock = settingsIn.getDefaultBlock();
		this.defaultFluid = settingsIn.getDefaultFluid();
		this.noiseSizeX = 16 / this.horizontalNoiseGranularity;
		this.noiseSizeY = 256 / this.verticalNoiseGranularity;
		this.noiseSizeZ = 16 / this.horizontalNoiseGranularity;
		this.randomSeed = new SharedSeedRandom(this.seed);
		this.field_222568_o = new OctavesNoiseGenerator(this.randomSeed, 15, 0);
		this.field_222569_p = new OctavesNoiseGenerator(this.randomSeed, 15, 0);
		this.field_222570_q = new OctavesNoiseGenerator(this.randomSeed, 7, 0);
		this.surfaceDepthNoise = usePerlin ? new PerlinNoiseGenerator(this.randomSeed, 3, 0) : new OctavesNoiseGenerator(this.randomSeed, 3, 0);
		this.randomSeed.skip(2620);
		this.depthNoise = new OctavesNoiseGenerator(this.randomSeed, 15, 0);
		this.field_222577_j = worldIn.getWorldInfo().getGenerator() == WorldType.AMPLIFIED;
		
        // Create dirs for a new world if necessary (only needed for overworld, when creating a new OTG world)
        if(worldIn.getDimension().getType().getId() == 0)
        {
        	// Happens when the MP server is started
        	if(OTG.getDimensionsConfig() == null)
        	{
        		File worldDir;
        		if(((ServerWorld)worldIn).getSaveHandler() != null)
        		{
        			worldDir = ((ServerWorld)worldIn).getSaveHandler().getWorldDirectory();
        		} else {
        			worldDir = new File(Minecraft.getInstance().gameDir + File.separator + "saves" + File.separator + worldIn.getWorldInfo().getWorldName());
        		}
        		
        		// Check if a dimsconfig is saved for the world
        		DimensionsConfig savedConfig = DimensionsConfig.loadFromFile(worldDir);
        		if(savedConfig != null)
        		{
        			OTG.setDimensionsConfig(savedConfig);
        		} else {
            		// This is a new world, create a DimensionsConfig for it based on modpack config or worldconfig.
        			DimensionsConfig modPackConfig = DimensionsConfig.getModPackConfig(worldDir.getName());
        	        if(modPackConfig != null)
        	        {
        	        	DimensionsConfig dimsConfig = new DimensionsConfig(worldDir);
        	        	dimsConfig.Overworld = modPackConfig.Overworld;
        	        	dimsConfig.Dimensions = modPackConfig.Dimensions;
        	        	OTG.setDimensionsConfig(dimsConfig);
        	        	OTG.getDimensionsConfig().save();
        	        } else {
        	        	// Create dimensionsconfig from the preset's worldconfig, only works if worldname is the same as preset name (which is the case for OTG overworlds on MP servers)
        	        	WorldConfig worldConfig = WorldConfig.loadWorldConfigFromDisk(new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + worldDir.getName()));
        	        	if(worldConfig == null)
        	        	{
        	        		// The world dir / world config is missing, this can be either an error or an MP server being started and creating an OTG overworld, in which case default configs should be generated.
        	        		// Create a new world dir with default configs.
        					((ForgeEngine)OTG.getEngine()).getWorldLoader().createDefaultOTGWorld(worldDir.getName()); // For MP servers, world name == preset name.
        					OTG.getEngine().loadPresets();
        					worldConfig = WorldConfig.loadWorldConfigFromDisk(new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + worldDir.getName()));
        	        	}
        	        	
    	        		DimensionsConfig dimsConfig = new DimensionsConfig(worldDir);
    	        		dimsConfig.Overworld = new DimensionConfig(worldDir.getName(), worldConfig);
    	        		for(String dimToAdd : worldConfig.dimensions)
    	        		{
    	        			WorldConfig dimWorldConfig = WorldConfig.loadWorldConfigFromDisk(new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + dimToAdd));
    	        			if(dimWorldConfig != null)
    	        			{
    	        				dimsConfig.Dimensions.add(new DimensionConfig(dimToAdd, dimWorldConfig));
    	        			}
    	        		}
    	        		OTG.setDimensionsConfig(dimsConfig);
    	        		OTG.getDimensionsConfig().save();
        	        }
        		}
        	}
	        File worldDirectory = new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + OTG.getDimensionsConfig().Overworld.PresetName);
	
	        if (!worldDirectory.exists())
	        {
	            System.out.println("OpenTerrainGenerator: settings does not exist, creating defaults");

	            if (!worldDirectory.mkdirs())
	            {
	                System.out.println("OpenTerrainGenerator: cant create folder " + worldDirectory.getAbsolutePath());
	            }
	        }

	        File worldObjectsDir = new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + OTG.getDimensionsConfig().Overworld.PresetName + File.separator + WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME);
	        worldObjectsDir.mkdirs();

	        File worldBiomesDir = new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + OTG.getDimensionsConfig().Overworld.PresetName + File.separator + WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME);
	        worldBiomesDir.mkdirs();
	        
	        // For MP server
	        /*
	        if(!worldIn.getMinecraftServer().isSinglePlayer())
	        {
			    // TODO: Why does MC add \\.? Removing.. 
			    File worldSaveDir = new File(mcWorld.getSaveHandler().getWorldDirectory().getAbsolutePath().replace("\\.",  ""));			    
			    OTG.IsNewWorldBeingCreated = !new File(worldSaveDir, File.separator + "region").exists();
	        }
	        */
        }

    	// For MP server
        /*
        if(!mcWorld.getMinecraftServer().isSinglePlayer())
        {
	        WorldSettings worldSettings = new WorldSettings(mcWorld.getWorldInfo().getSeed(), mcWorld.getWorldInfo().getGameType(), mcWorld.getWorldInfo().isMapFeaturesEnabled(), mcWorld.getWorldInfo().isHardcoreModeEnabled(), OTGPlugin.OtgWorldType);
	        worldSettings.setGeneratorOptions("OpenTerrainGenerator");
	        mcWorld.getWorldInfo().setAllowCommands(mcWorld.getWorldInfo().areCommandsAllowed());
	        mcWorld.getWorldInfo().populateFromWorldSettings(worldSettings);
    	}
    	*/
        //

        // TODO: This allows only overworld, need OTGDimensionManager
        ForgeWorld world = ((ForgeEngine)OTG.getEngine()).getWorldLoader().getOrCreateForgeWorld((ServerWorld)worldIn, OTG.getDimensionsConfig().Overworld.PresetName);
        if (world == null) // TODO: When does this happen, if the world is not an OTG world?
        {
            throw new RuntimeException("This shouldn't happen, please contact team OTG about this crash.");
            //return super.getBiomeProvider(mcWorld);
        }
        ((OTGBiomeProvider) this.biomeProvider).provideWorldInstance(world);
        this.generator = new ChunkProviderOTG(world.getConfigs(), world);
        
        Class<? extends BiomeGenerator> biomeGenClass = world.getConfigs().getWorldConfig().biomeMode;
        BiomeGenerator biomeGenerator = OTG.getBiomeModeManager().createCached(biomeGenClass, world);
        world.setBiomeGenerator(biomeGenerator);
	}
	
	// Getters
	
	@Override
	public int getGroundHeight()
	{
		return this.world.getSeaLevel() + 1;
	}
	
	@Override
	public int getSeaLevel()
	{
		return 63;
	}
	
	@Override
	public OTGGenSettings getSettings()
	{
		return this.settings;
	}
		
	@Override
	public BiomeProvider getBiomeProvider()
	{
		return this.biomeProvider;
	}
	
	@Override
	public long getSeed()
	{
		return this.seed;
	}
	
	@Override
	public int getMaxHeight()
	{
		return 256;
	}
	
	@Override
	public int func_222550_i()
	{
		return this.noiseSizeY + 1;
	}
	
	@Override
	protected double func_222553_h()
	{
		return 0.0D;
	}
	
	// Categories in order processed during chunkgen
	
	// Structures
	
	@Override
	public void func_227058_a_(BiomeManager p_227058_1_, IChunk p_227058_2_, ChunkGenerator<?> p_227058_3_, TemplateManager p_227058_4_)
	{
		for(Structure<?> structure : Feature.STRUCTURES.values())
		{
			if (p_227058_3_.getBiomeProvider().hasStructure(structure))
			{
				StructureStart structurestart = p_227058_2_.getStructureStart(structure.getStructureName());
				int i = structurestart != null ? structurestart.func_227457_j_() : 0;
				SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
				ChunkPos chunkpos = p_227058_2_.getPos();
				StructureStart structurestart1 = StructureStart.DUMMY;
				Biome biome = p_227058_1_.func_226836_a_(new BlockPos(chunkpos.getXStart() + 9, 0, chunkpos.getZStart() + 9));
				if (structure.func_225558_a_(p_227058_1_, p_227058_3_, sharedseedrandom, chunkpos.x, chunkpos.z, biome))
				{
					StructureStart structurestart2 = structure.getStartFactory().create(structure, chunkpos.x, chunkpos.z, MutableBoundingBox.getNewBoundingBox(), i, p_227058_3_.getSeed());
					structurestart2.init(this, p_227058_4_, chunkpos.x, chunkpos.z, biome);
					structurestart1 = structurestart2.isValid() ? structurestart2 : StructureStart.DUMMY;
				}
				
				p_227058_2_.putStructureStart(structure.getStructureName(), structurestart1);
			}
		}
	}
	
	@Override
	public boolean hasStructure(Biome biomeIn, Structure<? extends IFeatureConfig> structureIn)
	{
		return biomeIn.hasStructure(structureIn);
	}
	
	@Override
	@Nullable
	public <C extends IFeatureConfig> C getStructureConfig(Biome biomeIn, Structure<C> structureIn)
	{
		return biomeIn.getStructureConfig(structureIn);
	}
	
	@Override
	public void generateStructureStarts(IWorld worldIn, IChunk chunkIn) // 4
	{
		//int i = 8;
		int j = chunkIn.getPos().x;
		int k = chunkIn.getPos().z;
		int l = j << 4;
		int i1 = k << 4;
		long l1;
		
		for(int j1 = j - 8; j1 <= j + 8; ++j1)
		{
			for(int k1 = k - 8; k1 <= k + 8; ++k1)
			{
				l1 = ChunkPos.asLong(j1, k1);			
				for(Entry<String, StructureStart> entry : worldIn.getChunk(j1, k1).getStructureStarts().entrySet())
				{
					StructureStart structurestart = entry.getValue();
					if (structurestart != StructureStart.DUMMY && structurestart.getBoundingBox().intersectsWith(l, i1, l + 15, i1 + 15))
					{
						chunkIn.addStructureReference(entry.getKey(), l1);
						DebugPacketSender.func_218804_a(worldIn, structurestart);
					}
				}
			}
		}
	}	
	
	@Override
	@Nullable
	public BlockPos findNearestStructure(World worldIn, String name, BlockPos pos, int radius, boolean p_211403_5_)
	{
		Structure<?> structure = Feature.STRUCTURES.get(name.toLowerCase(Locale.ROOT));
		return structure != null ? structure.findNearest(worldIn, this, pos, radius, p_211403_5_) : null;
	}
	
	@Override
	public int func_222532_b(int p_222532_1_, int p_222532_2_, Heightmap.Type p_222532_3_)
	{
		return this.func_222529_a(p_222532_1_, p_222532_2_, p_222532_3_);
	}
	
	@Override
	public int func_222531_c(int p_222531_1_, int p_222531_2_, Heightmap.Type p_222531_3_)
	{
		return this.func_222529_a(p_222531_1_, p_222531_2_, p_222531_3_) - 1;
	}
	
	// Biomes
	
	@Override
	public void generateBiomes(IChunk chunkIn)
	{
		// 1.12.2 fillBiomesArray?
		// Chunk internal array is filled with biomes instead of saved ids.
		ChunkPos chunkpos = chunkIn.getPos();
		((ChunkPrimer)chunkIn).func_225548_a_(new BiomeContainer(chunkpos, this.biomeProvider));
	}
	
	@Override
	protected Biome func_225552_a_(BiomeManager p_225552_1_, BlockPos p_225552_2_)
	{
		return p_225552_1_.func_226836_a_(p_225552_2_);
	}
	
	// Generate base terrain
	
	@Override
	public void makeBase(IWorld worldIn, IChunk chunkIn)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkIn.getPos().x, chunkIn.getPos().z);
		this.generator.generate(new ForgeChunkBuffer(chunkCoord, (ChunkPrimer)chunkIn));
		//vanillaMakeBase(worldIn, chunkIn);
	}
	
	private void vanillaMakeBase(IWorld worldIn, IChunk chunkIn)
	{
		int i = this.getSeaLevel();
		ObjectList<AbstractVillagePiece> objectlist = new ObjectArrayList<>(10);
		ObjectList<JigsawJunction> objectlist1 = new ObjectArrayList<>(32);
		ChunkPos chunkpos = chunkIn.getPos();
		int j = chunkpos.x;
		int k = chunkpos.z;
		int l = j << 4;
		int i1 = k << 4;

		for(Structure<?> structure : Feature.field_214488_aQ)
		{
			String s = structure.getStructureName();
			LongIterator longiterator = chunkIn.getStructureReferences(s).iterator();

			while(longiterator.hasNext())
			{
				long j1 = longiterator.nextLong();
				ChunkPos chunkpos1 = new ChunkPos(j1);
				IChunk ichunk = worldIn.getChunk(chunkpos1.x, chunkpos1.z);
				StructureStart structurestart = ichunk.getStructureStart(s);
				if (structurestart != null && structurestart.isValid())
				{
					for(StructurePiece structurepiece : structurestart.getComponents())
					{
						if (structurepiece.func_214810_a(chunkpos, 12) && structurepiece instanceof AbstractVillagePiece)
						{
							AbstractVillagePiece abstractvillagepiece = (AbstractVillagePiece)structurepiece;
							JigsawPattern.PlacementBehaviour jigsawpattern$placementbehaviour = abstractvillagepiece.func_214826_b().getPlacementBehaviour();
							if (jigsawpattern$placementbehaviour == JigsawPattern.PlacementBehaviour.RIGID)
							{
								objectlist.add(abstractvillagepiece);
							}

							for(JigsawJunction jigsawjunction : abstractvillagepiece.getJunctions())
							{
								int k1 = jigsawjunction.getSourceX();
								int l1 = jigsawjunction.getSourceZ();
								if (k1 > l - 12 && l1 > i1 - 12 && k1 < l + 15 + 12 && l1 < i1 + 15 + 12)
								{
									objectlist1.add(jigsawjunction);
								}
							}
						}
					}
				}
			}
		}

		double[][][] adouble = new double[2][this.noiseSizeZ + 1][this.noiseSizeY + 1];

		for(int j5 = 0; j5 < this.noiseSizeZ + 1; ++j5)
		{
			adouble[0][j5] = new double[this.noiseSizeY + 1];
			this.func_222548_a(adouble[0][j5], j * this.noiseSizeX, k * this.noiseSizeZ + j5);
			adouble[1][j5] = new double[this.noiseSizeY + 1];
		}

		ChunkPrimer chunkprimer = (ChunkPrimer)chunkIn;
		Heightmap heightmap = chunkprimer.func_217303_b(Heightmap.Type.OCEAN_FLOOR_WG);
		Heightmap heightmap1 = chunkprimer.func_217303_b(Heightmap.Type.WORLD_SURFACE_WG);
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		ObjectListIterator<AbstractVillagePiece> objectlistiterator = objectlist.iterator();
		ObjectListIterator<JigsawJunction> objectlistiterator1 = objectlist1.iterator();

		for(int k5 = 0; k5 < this.noiseSizeX; ++k5)
		{
			for(int l5 = 0; l5 < this.noiseSizeZ + 1; ++l5)
			{
				this.func_222548_a(adouble[1][l5], j * this.noiseSizeX + k5 + 1, k * this.noiseSizeZ + l5);
			}

			for(int i6 = 0; i6 < this.noiseSizeZ; ++i6)
			{
				ChunkSection chunksection = chunkprimer.func_217332_a(15);
				chunksection.lock();

				for(int j6 = this.noiseSizeY - 1; j6 >= 0; --j6)
				{
					double d16 = adouble[0][i6][j6];
					double d17 = adouble[0][i6 + 1][j6];
					double d18 = adouble[1][i6][j6];
					double d0 = adouble[1][i6 + 1][j6];
					double d1 = adouble[0][i6][j6 + 1];
					double d2 = adouble[0][i6 + 1][j6 + 1];
					double d3 = adouble[1][i6][j6 + 1];
					double d4 = adouble[1][i6 + 1][j6 + 1];

					for(int i2 = this.verticalNoiseGranularity - 1; i2 >= 0; --i2)
					{
						int j2 = j6 * this.verticalNoiseGranularity + i2;
						int k2 = j2 & 15;
						int l2 = j2 >> 4;
						if (chunksection.getYLocation() >> 4 != l2)
						{
							chunksection.unlock();
							chunksection = chunkprimer.func_217332_a(l2);
							chunksection.lock();
						}

						double d5 = (double)i2 / (double)this.verticalNoiseGranularity;
						double d6 = MathHelper.lerp(d5, d16, d1);
						double d7 = MathHelper.lerp(d5, d18, d3);
						double d8 = MathHelper.lerp(d5, d17, d2);
						double d9 = MathHelper.lerp(d5, d0, d4);

						for(int i3 = 0; i3 < this.horizontalNoiseGranularity; ++i3)
						{
							int j3 = l + k5 * this.horizontalNoiseGranularity + i3;
							int k3 = j3 & 15;
							double d10 = (double)i3 / (double)this.horizontalNoiseGranularity;
							double d11 = MathHelper.lerp(d10, d6, d7);
							double d12 = MathHelper.lerp(d10, d8, d9);

							for(int l3 = 0; l3 < this.horizontalNoiseGranularity; ++l3)
							{
								int i4 = i1 + i6 * this.horizontalNoiseGranularity + l3;
								int j4 = i4 & 15;
								double d13 = (double)l3 / (double)this.horizontalNoiseGranularity;
								double d14 = MathHelper.lerp(d13, d11, d12);
								double d15 = MathHelper.clamp(d14 / 200.0D, -1.0D, 1.0D);

								int k4;
								int l4;
								int i5;
								
								for(d15 = d15 / 2.0D - d15 * d15 * d15 / 24.0D; objectlistiterator.hasNext(); d15 += func_222556_a(k4, l4, i5) * 0.8D)
								{
									AbstractVillagePiece abstractvillagepiece1 = objectlistiterator.next();
									MutableBoundingBox mutableboundingbox = abstractvillagepiece1.getBoundingBox();
									k4 = Math.max(0, Math.max(mutableboundingbox.minX - j3, j3 - mutableboundingbox.maxX));
									l4 = j2 - (mutableboundingbox.minY + abstractvillagepiece1.getGroundLevelDelta());
									i5 = Math.max(0, Math.max(mutableboundingbox.minZ - i4, i4 - mutableboundingbox.maxZ));
								}

								objectlistiterator.back(objectlist.size());

								while(objectlistiterator1.hasNext())
								{
									JigsawJunction jigsawjunction1 = objectlistiterator1.next();
									int k6 = j3 - jigsawjunction1.getSourceX();
									k4 = j2 - jigsawjunction1.getSourceGroundY();
									l4 = i4 - jigsawjunction1.getSourceZ();
									d15 += func_222556_a(k6, k4, l4) * 0.4D;
								}

								objectlistiterator1.back(objectlist1.size());
								BlockState blockstate;
								if (d15 > 0.0D)
								{
									blockstate = this.defaultBlock;
								}
								else if (j2 < i)
								{
									blockstate = this.defaultFluid;
								} else {
									blockstate = AIR;
								}
								
								if (blockstate != AIR)
								{
									blockpos$mutable.setPos(j3, j2, i4);
									if (blockstate.getLightValue(chunkprimer, blockpos$mutable) != 0)
									{
										chunkprimer.addLightPosition(blockpos$mutable);
									}

									//OTG.log(LogMarker.INFO, "DERP: " + k3 + " " + k2 + " " + j4);
									//OTG.log(LogMarker.INFO, "DER2: " + k3 + " " + j2 + " " + j4);
									chunksection.setBlockState(k3, k2, j4, blockstate, false);
									heightmap.update(k3, j2, j4, blockstate);
									heightmap1.update(k3, j2, j4, blockstate);
								}
							}
						}
					}
				}
				chunksection.unlock();
			}
			double[][] adouble1 = adouble[0];
			adouble[0] = adouble[1];
			adouble[1] = adouble1;
		}
	}

	private static double func_222556_a(int p_222556_0_, int p_222556_1_, int p_222556_2_)
	{
		int i = p_222556_0_ + 12;
		int j = p_222556_1_ + 12;
		int k = p_222556_2_ + 12;
		if (i >= 0 && i < 24)
		{
			if (j >= 0 && j < 24)
			{
				return k >= 0 && k < 24 ? (double)field_222561_h[k * 24 * 24 + i * 24 + j] : 0.0D;
			} else {
				return 0.0D;
			}
		} else {
			return 0.0D;
		}
	}	
	
	// Noise

	// 1.12.2 ChunkProvider.generateTerrainNoise
	@Override
	protected void func_222548_a(double[] p_222548_1_, int p_222548_2_, int p_222548_3_)
	{
		this.func_222546_a(p_222548_1_, p_222548_2_, p_222548_3_, (double)684.412F, (double)684.412F, 8.555149841308594D, 4.277574920654297D, 3, -10);
	}
	
	@Override
	protected void func_222546_a(double[] p_222546_1_, int p_222546_2_, int p_222546_3_, double p_222546_4_, double p_222546_6_, double p_222546_8_, double p_222546_10_, int p_222546_12_, int p_222546_13_)
	{
		double[] adouble = this.func_222549_a(p_222546_2_, p_222546_3_);
		double d0 = adouble[0];
		double d1 = adouble[1];
		double d2 = this.func_222551_g();
		double d3 = this.func_222553_h();
	
		for(int i = 0; i < this.func_222550_i(); ++i)
		{
			double d4 = this.func_222552_a(p_222546_2_, i, p_222546_3_, p_222546_4_, p_222546_6_, p_222546_8_, p_222546_10_);
			d4 = d4 - this.func_222545_a(d0, d1, i);
			if ((double)i > d2)
			{
				d4 = MathHelper.clampedLerp(d4, (double)p_222546_13_, ((double)i - d2) / (double)p_222546_12_);
			}
			else if ((double)i < d3)
			{
				d4 = MathHelper.clampedLerp(d4, -30.0D, (d3 - (double)i) / (d3 - 1.0D));
			}
			p_222546_1_[i] = d4;
		}
	}
	
	@Override
	protected double[] func_222549_a(int p_222549_1_, int p_222549_2_)
	{
		double[] adouble = new double[2];
		float f = 0.0F;
		float f1 = 0.0F;
		float f2 = 0.0F;
		int j = this.getSeaLevel();
		float f3 = this.biomeProvider.func_225526_b_(p_222549_1_, j, p_222549_2_).getDepth();
		
		for(int k = -2; k <= 2; ++k)
		{
			for(int l = -2; l <= 2; ++l)
			{
				Biome biome = this.biomeProvider.func_225526_b_(p_222549_1_ + k, j, p_222549_2_ + l);
				float f4 = biome.getDepth();
				float f5 = biome.getScale();
				if (this.field_222577_j && f4 > 0.0F)
				{
					f4 = 1.0F + f4 * 2.0F;
					f5 = 1.0F + f5 * 4.0F;
				}
				
				float f6 = field_222576_h[k + 2 + (l + 2) * 5] / (f4 + 2.0F);
				if (biome.getDepth() > f3)
				{
					f6 /= 2.0F;
				}
				
				f += f5 * f6;
				f1 += f4 * f6;
				f2 += f6;
			}
		}
		
		f = f / f2;
		f1 = f1 / f2;
		f = f * 0.9F + 0.1F;
		f1 = (f1 * 4.0F - 1.0F) / 8.0F;
		adouble[0] = (double)f1 + this.func_222574_c(p_222549_1_, p_222549_2_);
		adouble[1] = (double)f;
		return adouble;
	}
	
	private double func_222574_c(int p_222574_1_, int p_222574_2_)
	{
		double d0 = this.depthNoise.func_215462_a((double)(p_222574_1_ * 200), 10.0D, (double)(p_222574_2_ * 200), 1.0D, 0.0D, true) * 65535.0D / 8000.0D;
		if (d0 < 0.0D)
		{
			d0 = -d0 * 0.3D;
		}
		
		d0 = d0 * 3.0D - 2.0D;
		if (d0 < 0.0D)
		{
			d0 = d0 / 28.0D;
		} else {
			if (d0 > 1.0D)
			{
				d0 = 1.0D;
			}
			d0 = d0 / 40.0D;
		}
		return d0;
	}
	
	@Override
	protected double func_222551_g()
	{
		return (double)(this.func_222550_i() - 4);
	}
	
	private double func_222552_a(int p_222552_1_, int p_222552_2_, int p_222552_3_, double p_222552_4_, double p_222552_6_, double p_222552_8_, double p_222552_10_)
	{
		double d0 = 0.0D;
		double d1 = 0.0D;
		double d2 = 0.0D;
		double d3 = 1.0D;
	
		for(int i = 0; i < 16; ++i)
		{
			double d4 = OctavesNoiseGenerator.maintainPrecision((double)p_222552_1_ * p_222552_4_ * d3);
			double d5 = OctavesNoiseGenerator.maintainPrecision((double)p_222552_2_ * p_222552_6_ * d3);
			double d6 = OctavesNoiseGenerator.maintainPrecision((double)p_222552_3_ * p_222552_4_ * d3);
			double d7 = p_222552_6_ * d3;
			ImprovedNoiseGenerator improvednoisegenerator = this.field_222568_o.getOctave(i);
			if (improvednoisegenerator != null)
			{
				d0 += improvednoisegenerator.func_215456_a(d4, d5, d6, d7, (double)p_222552_2_ * d7) / d3;
			}
	
			ImprovedNoiseGenerator improvednoisegenerator1 = this.field_222569_p.getOctave(i);
			if (improvednoisegenerator1 != null)
			{
				d1 += improvednoisegenerator1.func_215456_a(d4, d5, d6, d7, (double)p_222552_2_ * d7) / d3;
			}
	
			if (i < 8)
			{
				ImprovedNoiseGenerator improvednoisegenerator2 = this.field_222570_q.getOctave(i);
				if (improvednoisegenerator2 != null)
				{
					d2 += improvednoisegenerator2.func_215456_a(OctavesNoiseGenerator.maintainPrecision((double)p_222552_1_ * p_222552_8_ * d3), OctavesNoiseGenerator.maintainPrecision((double)p_222552_2_ * p_222552_10_ * d3), OctavesNoiseGenerator.maintainPrecision((double)p_222552_3_ * p_222552_8_ * d3), p_222552_10_ * d3, (double)p_222552_2_ * p_222552_10_ * d3) / d3;
				}
			}
			d3 /= 2.0D;
		}
	
		return MathHelper.clampedLerp(d0 / 512.0D, d1 / 512.0D, (d2 / 10.0D + 1.0D) / 2.0D);
	}
	
	@Override
	protected double func_222545_a(double p_222545_1_, double p_222545_3_, int p_222545_5_)
	{
		double d1 = ((double)p_222545_5_ - (8.5D + p_222545_1_ * 8.5D / 8.0D * 4.0D)) * 12.0D * 128.0D / 256.0D / p_222545_3_;
		if (d1 < 0.0D)
		{
			d1 *= 4.0D;
		}
		return d1;
	}
	
	@Override
	protected double[] func_222547_b(int p_222547_1_, int p_222547_2_)
	{
		double[] adouble = new double[this.noiseSizeY + 1];
		this.func_222548_a(adouble, p_222547_1_, p_222547_2_);
		return adouble;
	}

	@Override
	public int func_222529_a(int p_222529_1_, int p_222529_2_, Heightmap.Type p_222529_3_)
	{
		int i = Math.floorDiv(p_222529_1_, this.horizontalNoiseGranularity);
		int j = Math.floorDiv(p_222529_2_, this.horizontalNoiseGranularity);
		int k = Math.floorMod(p_222529_1_, this.horizontalNoiseGranularity);
		int l = Math.floorMod(p_222529_2_, this.horizontalNoiseGranularity);
		double d0 = (double)k / (double)this.horizontalNoiseGranularity;
		double d1 = (double)l / (double)this.horizontalNoiseGranularity;
		double[][] adouble = new double[][]{this.func_222547_b(i, j), this.func_222547_b(i, j + 1), this.func_222547_b(i + 1, j), this.func_222547_b(i + 1, j + 1)};
		int i1 = this.getSeaLevel();
	
		for(int j1 = this.noiseSizeY - 1; j1 >= 0; --j1)
		{
			double d2 = adouble[0][j1];
			double d3 = adouble[1][j1];
			double d4 = adouble[2][j1];
			double d5 = adouble[3][j1];
			double d6 = adouble[0][j1 + 1];
			double d7 = adouble[1][j1 + 1];
			double d8 = adouble[2][j1 + 1];
			double d9 = adouble[3][j1 + 1];
	
			for(int k1 = this.verticalNoiseGranularity - 1; k1 >= 0; --k1)
			{
				double d10 = (double)k1 / (double)this.verticalNoiseGranularity;
				double d11 = MathHelper.lerp3(d10, d0, d1, d2, d6, d4, d8, d3, d7, d5, d9);
				int l1 = j1 * this.verticalNoiseGranularity + k1;
				if (d11 > 0.0D || l1 < i1)
				{
					BlockState blockstate;
					if (d11 > 0.0D)
					{
						blockstate = this.defaultBlock;
					} else {
						blockstate = this.defaultFluid;
					}
					if (p_222529_3_.func_222684_d().test(blockstate))
					{
						return l1 + 1;
					}
				}
			}
		}
		return 0;
	}
	
	// Surface builder
	
	@Override
	public void func_225551_a_(WorldGenRegion p_225551_1_, IChunk p_225551_2_)
	{
		//TODO: Surface building is done in the noise generator itself currently. Maybe there's a way to gracefully decouple it to make it work like vanilla?

//		ChunkPos chunkpos = p_225551_2_.getPos();
//		int i = chunkpos.x;
//		int j = chunkpos.z;
//		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
//		sharedseedrandom.setBaseChunkSeed(i, j);
//		ChunkPos chunkpos1 = p_225551_2_.getPos();
//		int k = chunkpos1.getXStart();
//		int l = chunkpos1.getZStart();
//		//double d0 = 0.0625D;
//		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
//
//		for(int i1 = 0; i1 < 16; ++i1)
//		{
//			for(int j1 = 0; j1 < 16; ++j1)
//			{
//				int k1 = k + i1;
//				int l1 = l + j1;
//				int i2 = p_225551_2_.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, i1, j1) + 1;
//				double d1 = this.surfaceDepthNoise.func_215460_a((double)k1 * 0.0625D, (double)l1 * 0.0625D, 0.0625D, (double)i1 * 0.0625D) * 15.0D;
//				p_225551_1_.func_226691_t_(blockpos$mutable.setPos(k + i1, i2, l + j1)).buildSurface(sharedseedrandom, p_225551_2_, k1, l1, i2, d1, this.getSettings().getDefaultBlock(), this.getSettings().getDefaultFluid(), this.getSeaLevel(), this.world.getSeed());
//			}
//		}
//		this.makeBedrock(p_225551_2_, sharedseedrandom);
	}

	@Override
	protected void makeBedrock(IChunk chunkIn, Random rand)
	{
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		int i = chunkIn.getPos().getXStart();
		int j = chunkIn.getPos().getZStart();
		OTGGenSettings t = this.getSettings();
		int k = t.getBedrockFloorHeight();
		int l = t.getBedrockRoofHeight();

		for(BlockPos blockpos : BlockPos.getAllInBoxMutable(i, 0, j, i + 15, 0, j + 15))
		{
			if (l > 0)
			{
				for(int i1 = l; i1 >= l - 4; --i1)
				{
					if (i1 >= l - rand.nextInt(5))
					{
						chunkIn.setBlockState(blockpos$mutable.setPos(blockpos.getX(), i1, blockpos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
					}
				}
			}
			if (k < 256)
			{
				for(int j1 = k + 4; j1 >= k; --j1)
				{
					if (j1 <= k + rand.nextInt(5))
					{
						chunkIn.setBlockState(blockpos$mutable.setPos(blockpos.getX(), j1, blockpos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
					}
				}
			}
		}
	}
	
	// Carver / caves & ravines
	
	@Override
	public void func_225550_a_(BiomeManager p_225550_1_, IChunk p_225550_2_, GenerationStage.Carving p_225550_3_)
	{
		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		//int i = 8;
		ChunkPos chunkpos = p_225550_2_.getPos();
		int j = chunkpos.x;
		int k = chunkpos.z;
		Biome biome = this.func_225552_a_(p_225550_1_, chunkpos.asBlockPos());
		BitSet bitset = p_225550_2_.getCarvingMask(p_225550_3_);
		
		for(int l = j - 8; l <= j + 8; ++l)
		{
			for(int i1 = k - 8; i1 <= k + 8; ++i1)
			{
				List<ConfiguredCarver<?>> list = biome.getCarvers(p_225550_3_);
				ListIterator<ConfiguredCarver<?>> listiterator = list.listIterator();
		
				while(listiterator.hasNext())
				{
					int j1 = listiterator.nextIndex();
					ConfiguredCarver<?> configuredcarver = listiterator.next();
					sharedseedrandom.setLargeFeatureSeed(this.seed + (long)j1, l, i1);
					if (configuredcarver.shouldCarve(sharedseedrandom, l, i1))
					{
						configuredcarver.func_227207_a_(p_225550_2_, (p_227059_2_) ->
						{
							return this.func_225552_a_(p_225550_1_, p_227059_2_);
						}, sharedseedrandom, this.getSeaLevel(), l, i1, j, k, bitset);
					}
				}
			}
		}
	}

	// Population / decoration
	
	@Override
	public void decorate(WorldGenRegion region)
	{
		int i = region.getMainChunkX();
		int j = region.getMainChunkZ();
		int k = i * 16;
		int l = j * 16;
		BlockPos blockpos = new BlockPos(k, 0, l);
		Biome biome = this.func_225552_a_(region.func_225523_d_(), blockpos.add(8, 8, 8));
		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		long i1 = sharedseedrandom.setDecorationSeed(region.getSeed(), k, l);
		
		for(GenerationStage.Decoration generationstage$decoration : GenerationStage.Decoration.values())
		{
			try
			{
				biome.decorate(generationstage$decoration, this, region, i1, sharedseedrandom, blockpos);
			}
			catch (Exception exception)
			{
				CrashReport crashreport = CrashReport.makeCrashReport(exception, "Biome decoration");
				crashreport.makeCategory("Generation").addDetail("CenterX", i).addDetail("CenterZ", j).addDetail("Step", generationstage$decoration).addDetail("Seed", i1).addDetail("Biome", Registry.BIOME.getKey(biome));
				throw new ReportedException(crashreport);
			}
		}
	}	
	
	// Mobs
	
	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos)
	{
		if (Feature.SWAMP_HUT.func_202383_b(this.world, pos))
		{
			if (creatureType == EntityClassification.MONSTER)
			{
				return Feature.SWAMP_HUT.getSpawnList();
			}
			if (creatureType == EntityClassification.CREATURE)
			{
				return Feature.SWAMP_HUT.getCreatureSpawnList();
			}
		}
		else if (creatureType == EntityClassification.MONSTER)
		{
			if (Feature.PILLAGER_OUTPOST.isPositionInStructure(this.world, pos))
			{
					return Feature.PILLAGER_OUTPOST.getSpawnList();
			}
			if (Feature.OCEAN_MONUMENT.isPositionInStructure(this.world, pos))
			{
				return Feature.OCEAN_MONUMENT.getSpawnList();
			}
		}
		return this.world.func_226691_t_(pos).getSpawns(creatureType);
	}

	@Override
	public void spawnMobs(ServerWorld worldIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs)
	{
		this.phantomSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
		this.patrolSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
		this.catSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
		this.field_225495_n.func_225477_a(worldIn, spawnHostileMobs, spawnPeacefulMobs);
	}
	
	@Override
	public void spawnMobs(WorldGenRegion region)
	{
		int i = region.getMainChunkX();
		int j = region.getMainChunkZ();
		Biome biome = region.func_226691_t_((new ChunkPos(i, j)).asBlockPos());
		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		sharedseedrandom.setDecorationSeed(region.getSeed(), i << 4, j << 4);
		WorldEntitySpawner.performWorldGenSpawning(region, biome, i, j, sharedseedrandom);
	}
}
