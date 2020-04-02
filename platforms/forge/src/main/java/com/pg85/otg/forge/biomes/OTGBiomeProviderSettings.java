package com.pg85.otg.forge.biomes;

import com.pg85.otg.forge.terraingen.OTGGenSettings;

import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.IBiomeProviderSettings;
import net.minecraft.world.storage.WorldInfo;

public class OTGBiomeProviderSettings implements IBiomeProviderSettings
{
	private final long field_226848_a_;
	private final WorldType field_226849_b_;
	private OTGGenSettings generatorSettings = new OTGGenSettings();
	
	public OTGBiomeProviderSettings(WorldInfo p_i225751_1_)
	{
		this.field_226848_a_ = p_i225751_1_.getSeed();
		this.field_226849_b_ = p_i225751_1_.getGenerator();
	}
	
	public OTGBiomeProviderSettings setGeneratorSettings(OTGGenSettings p_205441_1_)
	{
		this.generatorSettings = p_205441_1_;
		return this;
	}
	
	public long func_226850_a_()
	{
		return this.field_226848_a_;
	}
	
	public WorldType func_226851_b_()
	{
		return this.field_226849_b_;
	}
	
	public OTGGenSettings getGeneratorSettings()
	{
		return this.generatorSettings;
	}
}
