package com.pg85.otg.terraingen.biome;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.terraingen.biome.layers.Layer;
import com.pg85.otg.terraingen.biome.layers.LayerFactory;

/**
 * This is the normal biome mode, which has all of Open Terrain Generator's features.
 */
public class BeforeGroupsBiomeGenerator extends LayeredBiomeGenerator
{
    public BeforeGroupsBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    protected Layer[] initLayers()
    {
        return LayerFactory.createBeforeGroups(world);
    }
}
