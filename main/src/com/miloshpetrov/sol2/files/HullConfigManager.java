package com.miloshpetrov.sol2.files;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.miloshpetrov.sol2.TextureManager;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.AbilityCommonConfigs;
import com.miloshpetrov.sol2.game.item.EngineItem;
import com.miloshpetrov.sol2.game.item.ItemManager;
import com.miloshpetrov.sol2.game.ship.AbilityConfig;
import com.miloshpetrov.sol2.game.ship.EmWave;
import com.miloshpetrov.sol2.game.ship.hulls.GunSlot;
import com.miloshpetrov.sol2.game.ship.hulls.HullConfig;
import com.miloshpetrov.sol2.game.ship.KnockBack;
import com.miloshpetrov.sol2.game.ship.ShipBuilder;
import com.miloshpetrov.sol2.game.ship.SloMo;
import com.miloshpetrov.sol2.game.ship.Teleport;
import com.miloshpetrov.sol2.game.ship.UnShield;
import com.miloshpetrov.sol2.game.sound.SoundManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Linus on 4-5-2015.
 */
public final class HullConfigManager {

    public HullConfigManager(ShipBuilder shipBuilder,
                             FileManager fileManager,
                             TextureManager textureManager,
                             ItemManager itemManager,
                             AbilityCommonConfigs abilityCommonConfigs,
                             SoundManager soundManager
    ) {
        this.shipBuilder = shipBuilder;
        this.fileManager = fileManager;
        this.textureManager = textureManager;
        this.soundManager = soundManager;
        this.itemManager = itemManager;
        this.abilityCommonConfigs = abilityCommonConfigs;

        nameToConfigMap = new HashMap<String, HullConfig>();
        configToNameMap = new HashMap<HullConfig, String>();
        readHullConfigs();
    }

    public HullConfig getConfig(String name) {
        return nameToConfigMap.get(name);
    }

    public String getName(HullConfig hull) {
        String result = configToNameMap.get(hull);
        return (result == null) ? "" : result;
    }

    private void readHullConfigs() {
        List<FileHandle> hullDirectories = getHullDirectories();

        for(FileHandle handle: hullDirectories) {
            HullConfig config = read(handle);
            String name = handle.nameWithoutExtension();
            nameToConfigMap.put(name, config);
            configToNameMap.put(config, name);
        }
    }

    private static Vector2 readVector2(JsonValue jsonValue, String name, Vector2 defaultValue) {
        String string = jsonValue.getString(name, null);
        return (string == null)
                ? defaultValue
                : SolMath.readV2(string);
    }

    private static EngineItem.Config readEngineConfig(ItemManager itemManager, JsonValue jsonValue, String name) {
        String string = jsonValue.getString(name, null);
        return itemManager.getEngineConfigs().get(string);
    }

    private static void validateEngineConfig(HullConfig.Data hull) {
        if (hull.engineConfig != null) {
            if (    // stations can't have engines
                    ( hull.type == HullConfig.Type.STATION ) ||
                            // the engine size must match the hull size
                            ( hull.engineConfig.big != (hull.type == HullConfig.Type.BIG) )
                    ) {
                throw new AssertionError("incompatible engine in hull " + hull.displayName);
            }
        }
    }

    private List<FileHandle> getHullDirectories() {
        List<FileHandle> subDirectories = new LinkedList<FileHandle>();

        for(FileHandle handle: fileManager.getHullsDirectory().list()) {
            if(handle.isDirectory()) {
                subDirectories.add(handle);
            }
        }

        return subDirectories;
    }

    private HullConfig read(FileHandle hullConfigDirectory) {
        final HullConfig.Data configData = new HullConfig.Data();

        final FileHandle propertiesFile = hullConfigDirectory.child(PROPERTIES_FILE_NAME);
        readProperties(propertiesFile, configData);

        String internalName = hullConfigDirectory.nameWithoutExtension();

        configData.internalName = internalName;
        configData.tex = textureManager.getTexture(hullConfigDirectory.child(TEXTURE_FILE_NAME));
        configData.icon = textureManager.getTexture(hullConfigDirectory.child(ICON_FILE_NAME));

        validateEngineConfig(configData);

        return new HullConfig(configData);
    }

    private void parseGunSlotList(JsonValue containerNode, HullConfig.Data configData) {
        Vector2 builderOrigin = shipBuilder.getOrigin(configData.internalName);

        for(JsonValue gunSlotNode: containerNode) {
            Vector2 position = readVector2(gunSlotNode, "position", null);
            position.sub(builderOrigin)
                    .scl(configData.size);

            boolean isUnderneathHull = gunSlotNode.getBoolean("isUnderneathHull", false);
            boolean allowsRotation = gunSlotNode.getBoolean("allowsRotation", true);

            configData.gunSlots.add(new GunSlot(position, isUnderneathHull, allowsRotation));
        }
    }

    private void readProperties(FileHandle propertiesFile, HullConfig.Data configData) {
        JsonReader jsonReader = new JsonReader();
        JsonValue jsonNode = jsonReader.parse(propertiesFile);

        configData.size = jsonNode.getFloat("size");
        configData.maxLife = jsonNode.getInt("maxLife");

        configData.e1Pos = readVector2(jsonNode, "e1Pos", new Vector2());
        configData.e2Pos = readVector2(jsonNode, "e2Pos", new Vector2());

        configData.lightSrcPoss = SolMath.readV2List(jsonNode, "lightSrcPoss");
        configData.hasBase = jsonNode.getBoolean("hasBase", false);
        configData.forceBeaconPoss = SolMath.readV2List(jsonNode, "forceBeaconPoss");
        configData.doorPoss = SolMath.readV2List(jsonNode, "doorPoss");
        configData.type = HullConfig.Type.forName(jsonNode.getString("type"));
        configData.durability = (configData.type == HullConfig.Type.BIG) ? 3 : .25f;
        configData.engineConfig = readEngineConfig(itemManager, jsonNode, "engine");
        configData.ability = loadAbility(jsonNode, itemManager, abilityCommonConfigs);

        configData.displayName = jsonNode.getString("displayName", "---");
        configData.price = jsonNode.getInt("price", 0);
        configData.hirePrice = jsonNode.getFloat("hirePrice", 0);

        process(configData);

        parseGunSlotList(jsonNode.get("gunSlots"), configData);
    }

    private AbilityConfig loadAbility(
            JsonValue hullNode,
            ItemManager itemManager,
            AbilityCommonConfigs abilityCommonConfigs
    ) {
        JsonValue abNode = hullNode.get("ability");
        if (abNode == null) return null;
        String type = abNode.getString("type");
        if ("sloMo".equals(type)) return SloMo.Config.load(abNode, itemManager, abilityCommonConfigs.sloMo);
        if ("teleport".equals(type)) return Teleport.Config.load(abNode, itemManager, abilityCommonConfigs.teleport);
        if ("knockBack".equals(type)) return KnockBack.Config.load(abNode, itemManager, abilityCommonConfigs.knockBack);
        if ("emWave".equals(type)) return EmWave.Config.load(abNode, itemManager, abilityCommonConfigs.emWave);
        if ("unShield".equals(type)) return UnShield.Config.load(abNode, itemManager, abilityCommonConfigs.unShield);
        return null;
    }

    // Seems to offsets all positions by the shipbuilder origin
    // Todo: Find out what this function does and provide a better name.
    private void process(HullConfig.Data configData) {
        Vector2 builderOrigin = shipBuilder.getOrigin(configData.internalName);

        configData.origin.set(builderOrigin)
                         .scl(configData.size);

        configData.e1Pos.sub(builderOrigin)
                        .scl(configData.size);

        configData.e2Pos.sub(builderOrigin)
                        .scl(configData.size);

        for (Vector2 position : configData.lightSrcPoss) {
            position.sub(builderOrigin)
                    .scl(configData.size);
        }

        for (Vector2 position : configData.forceBeaconPoss) {
            position.sub(builderOrigin)
                    .scl(configData.size);
        }

        for (Vector2 position : configData.doorPoss) {
            position.sub(builderOrigin)
                    .scl(configData.size);
        }
    }

    private final ShipBuilder shipBuilder;
    private final FileManager fileManager;
    private final TextureManager textureManager;
    private final ItemManager itemManager;
    private final SoundManager soundManager;
    private final AbilityCommonConfigs abilityCommonConfigs;

    private final Map<String,HullConfig> nameToConfigMap;
    private final Map<HullConfig, String> configToNameMap;

    public static final String PROPERTIES_FILE_NAME = "properties.json";
    public static final String TEXTURE_FILE_NAME = "texture.png";
    public static final String ICON_FILE_NAME = "icon.png";
}
