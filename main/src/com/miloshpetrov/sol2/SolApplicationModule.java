package com.miloshpetrov.sol2;

import com.badlogic.gdx.graphics.FPSLogger;
import com.miloshpetrov.sol2.files.FileManager;
import com.miloshpetrov.sol2.files.FileManagerImplementation;
import com.miloshpetrov.sol2.ui.SolInputManager;
import com.miloshpetrov.sol2.ui.UiDrawer;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
class SolApplicationModule {
    @Provides
    @Singleton
    FileManager provideFileManager() {
        return FileManagerImplementation.getInstance();
    }

    @Provides
    @Singleton
    FPSLogger fpsLogger() {
        return new FPSLogger();
    }

    @Provides
    @Singleton
    CommonDrawer provideCommonDrawer() {
        return new CommonDrawer(provideFileManager());
    }

    @Provides
    @Singleton
    TextureManager provideTextureManager() {
        return new TextureManager(provideFileManager());
    }

    @Provides
    @Singleton
    SolInputManager provideSolInputManager() {
        return new SolInputManager(provideTextureManager(), provideFileManager());
    }

    @Provides
    @Singleton
    UiDrawer provideUiDrawer() {
        return new UiDrawer(provideTextureManager(), provideCommonDrawer());
    }
}
