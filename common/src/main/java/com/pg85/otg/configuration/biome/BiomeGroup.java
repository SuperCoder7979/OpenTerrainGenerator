package com.pg85.otg.configuration.biome;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.standard.DefaultBiome;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.*;
import java.util.Map.Entry;

/**
 * Biomes are spawned in groups so that biomes of the same type are near each
 * other, and completely different biomes (desert vs taiga) don't spawn next
 * to each other.
 *
 * <p>This class represents such a biome group.
 */
public final class BiomeGroup extends ConfigFunction<WorldConfig>
{
    private int groupId;
    private String name;
    private int groupRarity;
    private int generationDepth = 0;
    private float avgTemp = 0;
    private Map<String, LocalBiome> biomes = new LinkedHashMap<String, LocalBiome>(32);

    /**
     * Variable used by the the ungrouped biome generator. This generator
     * needs to modify this variable. Directly after calling
     * {@link #processBiomeData(LocalWorld)} it has the value of
     * {@link #getGroupRarity()} plus the biome rarity values of each biome.
     */
    public int totalGroupRarity;

    /**
     * Loads the biome group using the provided settings.
     * @param config The world config.
     * @param args   The settings to be parsed.
     * @throws InvalidConfigException When the config is invalid.
     * @see #BiomeGroup(WorldConfig, String, int, int, List) Constructor to
     * properly initialize this biome group manually.
     */
    public BiomeGroup(WorldConfig config, List<String> args) throws InvalidConfigException
    {
        super(config);
        // Must have at least a GroupName and a Biome that belongs to it
        assureSize(4, args);
        this.name = args.get(0);
        this.generationDepth = readInt(args.get(1), 0, config.generationDepth);
        this.groupRarity = readInt(args.get(2), 1, Integer.MAX_VALUE);
        for (String biome : readBiomes(args, 3))
        {
            this.biomes.put(biome, null);
        }
    }

    /**
     * Creates a new <code>BiomeGroup</code>.
     * @param config    WorldConfig this biome group is part of.
     * @param groupName The name of this group.
     * @param size      Size value of this biome group.
     * @param rarity    Rarity value of this biome group.
     * @param biomes    List of names of the biomes that spawn in this group.
     */
    public BiomeGroup(WorldConfig config, String groupName, int size, int rarity, List<String> biomes)
    {
        super(config);
        this.name = groupName;
        this.generationDepth = size;
        this.groupRarity = rarity;
        for (String biome : biomes)
        {
            this.biomes.put(biome, null);
        }
    }

    /**
     * Does general post-initialization bookkeeping, like adding the
     * LocalBiome instances and initializing the average temperature and
     * group rarity.
     * @param world Used to look up biomes.
     */
    public void processBiomeData(LocalWorld world)
    {
        float totalTemp = 0;
        this.totalGroupRarity = 0;
        for (Iterator<Entry<String, LocalBiome>> it = this.biomes.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, LocalBiome> entry = it.next();
            String biomeName = entry.getKey();

            LocalBiome localBiome = world.getBiomeByNameOrNull(biomeName);
            entry.setValue(localBiome);
            if(localBiome == null)
            {
            	String breakpoint = "";
            	localBiome = world.getBiomeByNameOrNull(biomeName);
            }
            
            BiomeConfig biomeConfig = localBiome.getBiomeConfig();
            totalTemp += biomeConfig.biomeTemperature;
            this.totalGroupRarity += biomeConfig.biomeRarity;
        }
        this.avgTemp = totalTemp / this.biomes.size();
    }

    @Override
    public String toString()
    {
        return "BiomeGroup(" + name + ", " + generationDepth + ", " + groupRarity + ", " + StringHelper.join(biomes.keySet(), ", ") + ")";
    }

    /**
     * Reads all biomes from the start position until the end of the
     * list.
     * @param strings The input strings.
     * @param start   The position to start. The first element in the list
     *                has index 0, the last one size() - 1.
     * @return All biome names.
     * @throws InvalidConfigException If one of the elements in the list is
     *                                not a valid block id.
     */
    private List<String> readBiomes(List<String> strings, int start) throws InvalidConfigException
    {
        return new ArrayList<String>(strings.subList(start, strings.size()));
    }

    /**
     * Gets the name of this biome group.
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Filters the biomes in this group, removing all biomes that have an
     * unrecognized name.
     * @param customBiomeNames Set of known custom biomes.
     */
    void filterBiomes(ArrayList<String> customBiomeNames, boolean logWarnings)
    {
        for (Iterator<String> it = this.biomes.keySet().iterator(); it.hasNext();)
        {
            String biomeName = it.next();
            if(biomeName != null && biomeName.trim().length() > 0)
            {
	            if (DefaultBiome.Contain(biomeName) || customBiomeNames.contains(biomeName))
	            {
	                continue;
	            }
	            // Invalid biome name, remove
	            if(logWarnings)
	            {
	            	OTG.log(LogMarker.WARN, "Invalid biome name {} in biome group {}", biomeName, this.name);
	            }
            }
            it.remove();
        }
    }

    /**
     * Gets whether this group contains the given biome. Biome name is case
     * sensitive.
     * @param name Name of the biome.
     * @return True if this group contains the given biome, false otherwise.
     */
    public boolean containsBiome(String name)
    {
        return this.biomes.containsKey(name);
    }

    /**
     * Sets the group id to the given value. Don't use this if the group is
     * already registered in a collection.
     * @param groupId The new group id.
     * @throws IllegalArgumentException If the group id is larger than
     * {@link BiomeGroupManager#MAX_BIOME_GROUP_COUNT}.
     */
    void setGroupId(int groupId)
    {
        if (groupId > BiomeGroupManager.MAX_BIOME_GROUP_COUNT)
        {
            throw new IllegalArgumentException("Tried to set group id to " + groupId
                    + ", max allowed is " + BiomeGroupManager.MAX_BIOME_GROUP_COUNT);
        }

        this.groupId = groupId;
    }

    /**
     * Gets the numerical id for this group. Group ids are sequential and
     * based on the order they are placed in the configuration files.
     * @return The numerical id.
     */
    public int getGroupId()
    {
        return this.groupId;
    }

    /**
     * Gets whether this group is considered cold. This is based on the
     * average temperatures of the biomes in the group.
     * @return True if the group is cold, false otherwise.
     */
    public boolean isColdGroup()
    {
        return this.avgTemp < WorldStandardValues.ICE_GROUP_MAX_TEMP;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<WorldConfig> other)
    {
        if (other instanceof BiomeGroup)
        {
            BiomeGroup group = (BiomeGroup) other;
            return group.name.equalsIgnoreCase(this.name);
        }
        return false;
    }

	HashMap<Integer, TreeMap<Integer, LocalBiome>> cachedDepthMapOrHigher = new HashMap<Integer, TreeMap<Integer, LocalBiome>>();
    public SortedMap<Integer, LocalBiome> getDepthMapOrHigher(int depth)
    {    	
    	TreeMap<Integer, LocalBiome> map = cachedDepthMapOrHigher.get(new Integer(depth));
    	if(map != null)
    	{
    		return map;
    	}
    	
        int cumulativeBiomeRarity = 0;
        map = new TreeMap<Integer, LocalBiome>();
        for (Entry<String, LocalBiome> biome : this.biomes.entrySet())
        {                                                           //>>	When depth given is negative, include all biomes in group
            if (biome.getValue().getBiomeConfig().biomeSize >= depth || depth < 0)
            {
                cumulativeBiomeRarity += biome.getValue().getBiomeConfig().biomeRarity;
                map.put(cumulativeBiomeRarity, biome.getValue());
            }
        }
        
        cachedDepthMapOrHigher.put(new Integer(depth), map);
        
        return map;
    }

    HashMap<Integer, TreeMap<Integer, LocalBiome>> cachedDepthMaps = new HashMap<Integer, TreeMap<Integer, LocalBiome>>();
    SortedMap<Integer, LocalBiome> getDepthMap(int depth)
    {
    	TreeMap<Integer, LocalBiome> map = cachedDepthMaps.get(new Integer(depth));
    	if(map != null)
    	{
    		return map;
    	}
    	
        int cumulativeBiomeRarity = 0;
        map = new TreeMap<Integer, LocalBiome>();
        for (Entry<String, LocalBiome> biome : this.biomes.entrySet())
        {                                                           //>>	When depth given is negative, include all biomes in group
            if (biome.getValue().getBiomeConfig().biomeSize == depth || depth < 0)
            {
                cumulativeBiomeRarity += biome.getValue().getBiomeConfig().biomeRarity;
                map.put(cumulativeBiomeRarity, biome.getValue());
            }
        }
        
        cachedDepthMaps.put(new Integer(depth), map);
        
        return map;
    }

    public int getGroupRarity()
    {
        return groupRarity;
    }

    public int getGenerationDepth()
    {
        return generationDepth;
    }

    /**
     * Gets whether this group has any biomes.
     * @return True if the group has no biomes and is thus empty, false
     * if the group has biomes.
     */
    boolean hasNoBiomes()
    {
        return biomes.isEmpty();
    }
}
