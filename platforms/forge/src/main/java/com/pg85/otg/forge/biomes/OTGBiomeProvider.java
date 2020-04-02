package com.pg85.otg.forge.biomes;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pg85.otg.forge.world.ForgeWorld;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.feature.structure.Structure;

public class OTGBiomeProvider extends BiomeProvider
{
	private ForgeWorld world;
	
	private static final Set<Biome> field_226847_e_ = ImmutableSet.of(Biomes.OCEAN, Biomes.PLAINS, Biomes.DESERT, Biomes.MOUNTAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMP, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_MOUNTAINS, Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE, Biomes.BEACH, Biomes.DESERT_HILLS, Biomes.WOODED_HILLS, Biomes.TAIGA_HILLS, Biomes.MOUNTAIN_EDGE, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.JUNGLE_EDGE, Biomes.DEEP_OCEAN, Biomes.STONE_SHORE, Biomes.SNOWY_BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA_HILLS, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.WOODED_MOUNTAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.BADLANDS, Biomes.WOODED_BADLANDS_PLATEAU, Biomes.BADLANDS_PLATEAU, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.SUNFLOWER_PLAINS, Biomes.DESERT_LAKES, Biomes.GRAVELLY_MOUNTAINS, Biomes.FLOWER_FOREST, Biomes.TAIGA_MOUNTAINS, Biomes.SWAMP_HILLS, Biomes.ICE_SPIKES, Biomes.MODIFIED_JUNGLE, Biomes.MODIFIED_JUNGLE_EDGE, Biomes.TALL_BIRCH_FOREST, Biomes.TALL_BIRCH_HILLS, Biomes.DARK_FOREST_HILLS, Biomes.SNOWY_TAIGA_MOUNTAINS, Biomes.GIANT_SPRUCE_TAIGA, Biomes.GIANT_SPRUCE_TAIGA_HILLS, Biomes.MODIFIED_GRAVELLY_MOUNTAINS, Biomes.SHATTERED_SAVANNA, Biomes.SHATTERED_SAVANNA_PLATEAU, Biomes.ERODED_BADLANDS, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, Biomes.MODIFIED_BADLANDS_PLATEAU);
	public static final List<Biome> BIOMES_TO_SPAWN_IN = Lists.newArrayList(Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS);
	protected final Map<Structure<?>, Boolean> hasStructureCache = Maps.newHashMap();
	protected final Set<BlockState> topBlocksCache = Sets.newHashSet();
	protected final Set<Biome> field_226837_c_;
	
	public OTGBiomeProvider(OTGBiomeProviderSettings settingsProvider)
	{
		super(field_226847_e_);
		this.field_226837_c_ = field_226847_e_;
	}
	
	public void provideWorldInstance(ForgeWorld world)
	{
		this.world = world;
	}
	
	// Used during chunkgen to fetch biome for given pos.
	// For 1.12.2, this returned the saved id.
	@Override
	public Biome func_225526_b_(int p_225526_1_, int p_225526_2_, int p_225526_3_)
	{
		return ((ForgeBiome)this.world.getBiome(p_225526_1_, p_225526_3_)).biomeBase;
	}
	
	@Override
	public List<Biome> getBiomesToSpawnIn()
	{
		return BIOMES_TO_SPAWN_IN;
	}

	@Override
	public Set<Biome> func_225530_a_(int p_225530_1_, int p_225530_2_, int p_225530_3_, int p_225530_4_)
	{
		int i = p_225530_1_ - p_225530_4_ >> 2;
		int j = p_225530_2_ - p_225530_4_ >> 2;
		int k = p_225530_3_ - p_225530_4_ >> 2;
		int l = p_225530_1_ + p_225530_4_ >> 2;
		int i1 = p_225530_2_ + p_225530_4_ >> 2;
		int j1 = p_225530_3_ + p_225530_4_ >> 2;
		int k1 = l - i + 1;
		int l1 = i1 - j + 1;
		int i2 = j1 - k + 1;
		Set<Biome> set = Sets.newHashSet();

		for(int j2 = 0; j2 < i2; ++j2)
		{
			for(int k2 = 0; k2 < k1; ++k2)
			{
				for(int l2 = 0; l2 < l1; ++l2)
				{
					int i3 = i + k2;
					int j3 = j + l2;
					int k3 = k + j2;
					set.add(this.func_225526_b_(i3, j3, k3));
				}
			}
		}
		return set;
	}

	@Override
	@Nullable // Called by createspawnpos
	public BlockPos func_225531_a_(int p_225531_1_, int p_225531_2_, int p_225531_3_, int p_225531_4_, List<Biome> p_225531_5_, Random p_225531_6_)
	{
		int i = p_225531_1_ - p_225531_4_ >> 2;
		int j = p_225531_3_ - p_225531_4_ >> 2;
		int k = p_225531_1_ + p_225531_4_ >> 2;
		int l = p_225531_3_ + p_225531_4_ >> 2;
		int i1 = k - i + 1;
		int j1 = l - j + 1;
		int k1 = p_225531_2_ >> 2;
		BlockPos blockpos = null;
		int l1 = 0;

		for(int i2 = 0; i2 < j1; ++i2)
		{
			for(int j2 = 0; j2 < i1; ++j2)
			{
				int k2 = i + j2;
				int l2 = j + i2;
				if (p_225531_5_.contains(this.func_225526_b_(k2, k1, l2)))
				{
					if (blockpos == null || p_225531_6_.nextInt(l1 + 1) == 0)
					{
						blockpos = new BlockPos(k2 << 2, p_225531_2_, l2 << 2);
					}			
					++l1;
				}
			}
		}
		return blockpos;
	}

	@Override
	public float func_222365_c(int p_222365_1_, int p_222365_2_)
	{
		return 0.0F;
	}
	
	@Override
	public boolean hasStructure(Structure<?> structureIn)
	{
		return this.hasStructureCache.computeIfAbsent(structureIn, (p_226839_1_) ->
		{
			return this.field_226837_c_.stream().anyMatch((p_226838_1_) ->
			{
				return p_226838_1_.hasStructure(p_226839_1_);
			});
		});
	}

	@Override
	public Set<BlockState> getSurfaceBlocks()
	{
		if (this.topBlocksCache.isEmpty())
		{
			for(Biome biome : this.field_226837_c_)
			{
				this.topBlocksCache.add(biome.getSurfaceBuilderConfig().getTop());
			}
		}
		return this.topBlocksCache;
	}	
}
