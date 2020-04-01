package com.pg85.otg.configuration.settingType;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.standard.DefaultMaterial;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.materials.MaterialHelper;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 *
 */
public class MaterialSetting extends Setting<LocalMaterialData>
{
    private final DefaultMaterial defaultValue;

    public MaterialSetting(String name, DefaultMaterial defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    public LocalMaterialData getDefaultValue()
    {
        return MaterialHelper.toLocalMaterialData(defaultValue);
    }

    @Override
    public LocalMaterialData read(String string) throws InvalidConfigException
    {
    	LocalMaterialData material = MaterialHelper.readMaterial(string);
        return material;
    }
}
