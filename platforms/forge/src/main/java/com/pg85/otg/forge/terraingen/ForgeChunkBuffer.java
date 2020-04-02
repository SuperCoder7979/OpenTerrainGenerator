package com.pg85.otg.forge.terraingen;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.terraingen.ChunkBuffer;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;

/**
 * Implementation of {@link ChunkBuffer}. This implementation supports block
 * data, as well as extended ids. It uses a {@code Block[]} array to store
 * blocks internally, just like Minecraft does for chunk generation.
 *
 */
public class ForgeChunkBuffer implements ChunkBuffer
{
    private final ChunkCoordinate chunkCoord;
    private final ChunkPrimer chunkPrimer;
    private final Heightmap heightmap;
    private final Heightmap heightmap1;
    
    ForgeChunkBuffer(ChunkCoordinate chunkCoord, ChunkPrimer chunkIn)
    {
        this.chunkCoord = chunkCoord;
        this.chunkPrimer = chunkIn;
        this.heightmap = this.chunkPrimer.func_217303_b(Heightmap.Type.OCEAN_FLOOR_WG);
        this.heightmap1 = this.chunkPrimer.func_217303_b(Heightmap.Type.WORLD_SURFACE_WG);
    }

    public ChunkPrimer getChunkPrimer()
    {
    	return chunkPrimer;
    }
    
    @Override
    public ChunkCoordinate getChunkCoordinate()
    {
        return this.chunkCoord;
    }

    @Override
    public void setBlock(int internalX, int blockY, int internalZ, LocalMaterialData material)
    {	
		if(!material.isAir())
		{
			ChunkSection chunksection = this.chunkPrimer.func_217332_a(15);
			chunksection.lock();

			ForgeMaterialData forgeMaterial = (ForgeMaterialData)material;
			int l2 = blockY >> 4;
			if (chunksection.getYLocation() >> 4 != l2)
			{
				chunksection.unlock();
				chunksection = this.chunkPrimer.func_217332_a(l2);
				chunksection.lock();
			}
						
			// Get internal coordinates for block in chunk
	        int blockX = this.chunkCoord.getBlockX() + internalX;
	        int internalY = blockY & 0xF;
	        int blockZ = this.chunkCoord.getBlockZ() + internalZ;
			BlockPos pos = new BlockPos(blockX, blockY, blockZ);
			
			//OTG.log(LogMarker.INFO, "I " + internalX + " " + internalY + " " + internalZ + " P " + blockX + " " + blockY + " " + blockZ);
			
			if (forgeMaterial.internalBlock().getLightValue(this.chunkPrimer, pos) != 0)
			{
				this.chunkPrimer.addLightPosition(pos);
			}
			chunksection.setBlockState(internalX, internalY, internalZ, forgeMaterial.internalBlock(), false);
			
			heightmap.update(internalX, blockY, internalZ, forgeMaterial.internalBlock());
			heightmap1.update(internalX, blockY, internalZ, forgeMaterial.internalBlock());

			chunksection.unlock();
		}
    }

    @Override
    public LocalMaterialData getBlock(int blockX, int blockY, int blockZ)
    {
        BlockState blockState = this.chunkPrimer.getBlockState(new BlockPos(blockX, blockY, blockZ));
        return ForgeMaterialData.ofMinecraftBlockState(blockState);
    }

    /**
     * Creates a Minecraft chunk of the data of this chunk buffer.
     *
     * @param world
     *            The world the chunk will be in.
     * @return The chunk.
     */
    Chunk toChunk(World world)
    {
        return new Chunk(world, this.chunkPrimer);
    }
}
