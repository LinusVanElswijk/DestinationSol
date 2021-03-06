package com.miloshpetrov.sol2.game.projectile;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.miloshpetrov.sol2.*;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.files.FileManager;
import com.miloshpetrov.sol2.game.DmgType;
import com.miloshpetrov.sol2.game.GameColors;
import com.miloshpetrov.sol2.game.particle.EffectConfig;
import com.miloshpetrov.sol2.game.particle.EffectTypes;
import com.miloshpetrov.sol2.game.sound.SolSound;
import com.miloshpetrov.sol2.game.sound.SoundManager;

import java.util.HashMap;
import java.util.Map;

public class ProjectileConfigs {

  private final Map<String, ProjectileConfig> myConfigs;

  public ProjectileConfigs(TextureManager textureManager, SoundManager soundManager, EffectTypes effectTypes, GameColors cols) {
    myConfigs = new HashMap<String, ProjectileConfig>();
    JsonReader r = new JsonReader();
    FileHandle configFile = FileManager.getInstance().getConfigDirectory().child("projectiles.json");
    JsonValue parsed = r.parse(configFile);
    for (JsonValue sh : parsed) {
      String texName = "smallGameObjs/projectiles/" + sh.getString("texName");
      TextureAtlas.AtlasRegion tex = textureManager.getTex(texName, configFile);
      float texSz = sh.getFloat("texSz");
      float spdLen = sh.getFloat("spdLen");
      float physSize = sh.getFloat("physSize", 0);
      boolean stretch = sh.getBoolean("stretch", false);
      DmgType dmgType = DmgType.forName(sh.getString("dmgType"));
      String collisionSoundPath = sh.getString("collisionSound", "");
      SolSound collisionSound = collisionSoundPath.isEmpty() ? null : soundManager.getSound(collisionSoundPath, configFile);
      float lightSz = sh.getFloat("lightSz", 0);
      EffectConfig trailEffect = EffectConfig.load(sh.get("trailEffect"), effectTypes, textureManager, configFile, cols);
      EffectConfig bodyEffect = EffectConfig.load(sh.get("bodyEffect"), effectTypes, textureManager, configFile, cols);
      EffectConfig collisionEffect = EffectConfig.load(sh.get("collisionEffect"), effectTypes, textureManager, configFile, cols);
      EffectConfig collisionEffectBg = EffectConfig.load(sh.get("collisionEffectBg"), effectTypes, textureManager, configFile, cols);
      float guideRotSpd = sh.getFloat("guideRotSpd", 0);
      boolean zeroAbsSpd = sh.getBoolean("zeroAbsSpd", false);
      Vector2 origin = SolMath.readV2(sh.getString("texOrig", "0 0"));
      float acc = sh.getFloat("acceleration", 0);
      String workSoundDir = sh.getString("workSound", "");
      SolSound workSound = workSoundDir.isEmpty() ? null : soundManager.getLoopedSound(workSoundDir, configFile);
      boolean bodyless = sh.getBoolean("massless", false);
      float density = sh.getFloat("density", -1);
      float dmg = sh.getFloat("dmg");
      float emTime = sh.getFloat("emTime", 0);
      ProjectileConfig c = new ProjectileConfig(tex, texSz, spdLen, stretch, physSize, dmgType, collisionSound,
        lightSz, trailEffect, bodyEffect, collisionEffect, collisionEffectBg, zeroAbsSpd, origin, acc, workSound, bodyless, density, guideRotSpd, dmg, emTime);
      myConfigs.put(sh.name, c);
    }
  }

  public ProjectileConfig find(String name) {
    return myConfigs.get(name);
  }
}
