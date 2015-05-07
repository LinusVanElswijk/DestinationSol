package com.miloshpetrov.sol2;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.miloshpetrov.sol2.common.SolColor;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.*;
import com.miloshpetrov.sol2.menu.MenuScreens;
import com.miloshpetrov.sol2.ui.*;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;

public class SolApplication implements ApplicationListener {

    @Singleton
    @Component(modules = SolApplicationModule.class )
    public interface ApplicationComponent {
        void inject(SolApplication application);
    }

    @Inject TextureManager textureManager;
    @Inject SolInputManager inputManager;
    @Inject CommonDrawer commonDrawer;
    @Inject FPSLogger myFpsLogger;
    //@Inject UiDrawer myUiDrawer; //TODO: For some mysterious reason, this crashes the application on the first update

    private UiDrawer myUiDrawer;


    private ApplicationComponent component;

  private MenuScreens myMenuScreens;
  private SolLayouts myLayouts;
  private boolean myReallyMobile;
  private GameOptions myOptions;

  private String myFatalErrorMsg;
  private String myFatalErrorTrace;

  private float myAccum = 0;
  private SolGame myGame;

  public SolApplication() { }

  @Override
  public void create() {
      myReallyMobile = Gdx.app.getType() == Application.ApplicationType.Android || Gdx.app.getType() == Application.ApplicationType.iOS;
      if (myReallyMobile) DebugOptions.read(null);
      myOptions = new GameOptions(isMobile(), null);

    component = DaggerSolApplication_ApplicationComponent.builder().build();
    component.inject(this);

    myUiDrawer = new UiDrawer(textureManager, commonDrawer);
    myLayouts = new SolLayouts(myUiDrawer.r);
    myMenuScreens = new MenuScreens(myLayouts, textureManager, isMobile(), myUiDrawer.r);

    inputManager.setScreen(this, myMenuScreens.main);
  }

  @Override
  public void resize(int i, int i1) {

  }

  public void render() {
    myAccum += Gdx.graphics.getDeltaTime();
    while (myAccum > Const.REAL_TIME_STEP) {
      safeUpdate();
      myAccum -= Const.REAL_TIME_STEP;

    }
    draw();
  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  private void safeUpdate() {
    if (myFatalErrorMsg != null) return;
    try {
      update();
    } catch (Throwable t) {
      t.printStackTrace();
      myFatalErrorMsg = "A fatal error occurred:\n" + t.getMessage();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      myFatalErrorTrace = sw.toString();
    }
  }

  private void update() {
    DebugCollector.update();
    if (DebugOptions.SHOW_FPS) {
      DebugCollector.debug("Fps", Gdx.graphics.getFramesPerSecond());
      myFpsLogger.log();
    }
    inputManager.update(this);
    if (myGame != null) {
      myGame.update();
    }

    SolMath.checkVectorsTaken(null);
  }

  private void draw() {
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
    commonDrawer.begin();
    if (myGame != null) {
      myGame.draw();
    }
    myUiDrawer.updateMtx();
    inputManager.draw(myUiDrawer, this);
    if (myGame != null) {
      myGame.drawDebugUi(myUiDrawer);
    }
    if (myFatalErrorMsg != null) {
      myUiDrawer.draw(myUiDrawer.whiteTex, myUiDrawer.r, .5f, 0, 0, 0, .25f, 0, SolColor.UI_BG);
      myUiDrawer.drawString(myFatalErrorMsg, myUiDrawer.r / 2, .5f, FontSize.MENU, true, SolColor.W);
      myUiDrawer.drawString(myFatalErrorTrace, .2f * myUiDrawer.r, .6f, FontSize.DEBUG, false, SolColor.W);
    }
    DebugCollector.draw(myUiDrawer);
    if (myGame == null) {
      myUiDrawer.drawString("version: " + Const.VERSION, 0.01f, .98f, FontSize.DEBUG, false, SolColor.W);
    }
    commonDrawer.end();
  }

  public void loadNewGame(boolean tut, boolean usePrevShip) {
    if (myGame != null) throw new AssertionError("Starting a new game with unfinished current one");
    inputManager.setScreen(this, myMenuScreens.loading);
    myMenuScreens.loading.setMode(tut, usePrevShip);
  }

  public void startNewGame(boolean tut, boolean usePrevShip) {
    myGame = new SolGame(this, usePrevShip, textureManager, tut, commonDrawer);
    inputManager.setScreen(this, myGame.getScreens().mainScreen);
  }

  public SolInputManager getInputMan() {
    return inputManager;
  }

  public MenuScreens getMenuScreens() {
    return myMenuScreens;
  }

  public void dispose() {
    commonDrawer.dispose();
    if (myGame != null) myGame.onGameEnd();
    textureManager.dispose();
    inputManager.dispose();
  }

  public SolGame getGame() {
    return myGame;
  }

  public SolLayouts getLayouts() {
    return myLayouts;
  }

  public void finishGame() {
    myGame.onGameEnd();
    myGame = null;
    inputManager.setScreen(this, myMenuScreens.main);
  }

  public TextureManager getTexMan() {
    return textureManager;
  }

  public boolean isMobile() {
    return DebugOptions.EMULATE_MOBILE || myReallyMobile;
  }

  public GameOptions getOptions() {
    return myOptions;
  }

  public void paused() {
    if (myGame != null) myGame.saveShip();
  }
}
