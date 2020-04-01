package com.pg85.otg.configuration.standard;

import com.pg85.otg.configuration.settingType.Setting;
import com.pg85.otg.configuration.settingType.Settings;
import com.pg85.otg.logging.LogLevels;

public class PluginStandardValues extends Settings
{
	// Files
	
    // Main Plugin Config
	
    public static final String PluginConfigFilename = "OTG.ini";
    
    // Folders
    
    public static final String BiomeConfigDirectoryName = "GlobalBiomes";
    public static final String BO_DirectoryName = "GlobalObjects";
    public static final String PresetsDirectoryName = "worlds";
    
    // Network
    
    public static final String ChannelName = "OpenTerrainGenerator";
    public static final int ProtocolVersion = 6;
    
    // Plugin Defaults
    
    public static final Setting<LogLevels> LogLevel = enumSetting("LogLevel", LogLevels.Standard);
    public static final String PLUGIN_NAME = "OpenTerrainGenerator";
    public static final String PLUGIN_NAME_LOWER_CASE = PLUGIN_NAME.toLowerCase();
    public static final String PLUGIN_NAME_SHORT = "OTG";           
    public static final Setting<Boolean> SPAWN_LOG = booleanSetting("SpawnLog", false);    
    public static final Setting<Boolean> DEVELOPER_MODE = booleanSetting("DeveloperMode", false);
    public static final Setting<Integer> PREGENERATOR_MAX_CHUNKS_PER_TICK = intSetting("PregeneratorMaxChunksPerTick", 2, 1, 5);
    
	/**
	 * The amount of different block ids that are supported. 4096 on Minecraft. 65535 with NotEnoughId's mod
	 */
	public static final int SUPPORTED_BLOCK_IDS = 65535;//4096; // TODO: Test if this creates lag
    
	public static final int WORLD_DEPTH = 0;
	public static final int WORLD_HEIGHT = 256;
	
	public static final int MAX_BORDER_BIOMES = 256;
	public static final int MAX_RIVER_BIOMES = 256;
	public static final int MAX_BIOMES_PER_WORLD = 1024;
}
