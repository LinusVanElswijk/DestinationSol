package com.miloshpetrov.sol2.desktop;

import com.miloshpetrov.sol2.TextureManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by Linus on 7-5-2015.
 */
@Module
public class SolApplicationModule {

    @Provides @Singleton
    TextureManager provideTextureManager() {
        return new TextureManager();
    }
}
