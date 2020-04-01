package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class aids in the task of finding the blocks at the top or bottom of a collection of blocks
 */
class ObjectExtrusionHelper
{
    /**
     * The Y coordinate of the appropriate level to be extruding blocks from
     */
    private int blockExtrusionY;

    /**
     * The style to use for extruding; Currently either BottomDown or TopUp
     */
    private BO3Settings.ExtrudeMode extrudeMode;

    /**
     * These materials are the set of materials that are allow to be extruded through; That is, as soon as we find a
     * block in the world that is not in this list, we will stop extruding the BO3
     */
    private MaterialSet extrudeThroughBlocks;

    /**
     * These blocks are the blocks that are found to be at the location dictated by the extrudeMode, and will be
     * extruded until hitting a material not listed in extrudeThroughBlocks
     */
    private ArrayList<BO3BlockFunction> blocksToExtrude = new ArrayList<BO3BlockFunction>();

    /**
     * Constructor
     *
     * @param extrudeMode          The style of extrusion to perform
     * @param extrudeThroughBlocks The types of materials to allow extrusion to act upon
     */
    ObjectExtrusionHelper(BO3Settings.ExtrudeMode extrudeMode, MaterialSet extrudeThroughBlocks)
    {
        this.extrudeMode = extrudeMode;
        this.extrudeThroughBlocks = extrudeThroughBlocks;
        blockExtrusionY = extrudeMode.getStartingHeight();
    }

    /**
     * Determines if the block is one we wish to add to the list of blocks to be extruded. If it is, it will be added
     * otherwise, nothing happens. Any blocks added to the list that are on a level not optimal to the current level
     * will be purged to create the optimal list of blocks to extrude
     *
     * @param block The block to add.
     */
    void addBlock(BO3BlockFunction block)
    {
        if (extrudeMode != BO3Settings.ExtrudeMode.None)
        {
            if (extrudeMode == BO3Settings.ExtrudeMode.BottomDown && block.y < blockExtrusionY)
            {
                blocksToExtrude.clear();
                blockExtrusionY = block.y;
            }
            else if (extrudeMode == BO3Settings.ExtrudeMode.TopUp && block.y > blockExtrusionY)
            {
                blocksToExtrude.clear();
                blockExtrusionY = block.y;
            }
            if (block.y == blockExtrusionY)
            {
                blocksToExtrude.add(block);
            }
        }
    }

    /**
     * This method takes the blocks that have been added to this and extrudes them individually until a block outside
     * of the extrudeThroughBlocks has been hit
     *
     * @param world  The LocalWorld to extrude block in
     * @param random The random generator to use to spawning
     * @param x      The BO3 base X spawn location
     * @param y      The BO3 base Y spawn location
     * @param z      The BO3 base Z spawn location
     */
    void extrude(LocalWorld world, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        for (BO3BlockFunction block : blocksToExtrude)
        {
            if (extrudeMode == BO3Settings.ExtrudeMode.BottomDown)
            {
                for (int yi = y + block.y - 1;
                     yi > extrudeMode.getEndingHeight() && extrudeThroughBlocks.contains(world.getMaterial(x + block.x, yi, z + block.z, chunkBeingPopulated));
                     --yi)
                {
                	world.setBlock(x + block.x, yi, z + block.z, block.material.parseForWorld(world), block.metaDataTag, chunkBeingPopulated);
                }
            }
            else if (extrudeMode == BO3Settings.ExtrudeMode.TopUp)
            {
                for (int yi = y + block.y + 1;
                     yi < extrudeMode.getEndingHeight() && extrudeThroughBlocks.contains(world.getMaterial(x + block.x, yi, z + block.z, chunkBeingPopulated));
                     ++yi)
                {
                	world.setBlock(x + block.x, yi, z + block.z, block.material.parseForWorld(world), block.metaDataTag, chunkBeingPopulated);
                }
            }
        }
    }
}
