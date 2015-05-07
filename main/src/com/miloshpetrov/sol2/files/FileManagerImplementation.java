package com.miloshpetrov.sol2.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.miloshpetrov.sol2.game.DebugOptions;

/**
 * Singleton class that can provide file handles to various directories.
 */
public final class FileManagerImplementation implements FileManager {

    @Override
    public FileHandle getAssetsDirectory() {
        return getFile(ASSETS_DIR, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getConfigDirectory() {
        return getFile(CONFIG_DIR, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getHullsDirectory() {
        return getFile(HULLS_CONFIG_DIR, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getItemsDirectory() {
        return getFile(ITEMS_CONFIG_DIR, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getFontsDirectory() {
        return getFile(FONTS_DIR, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getSoundsDirectory() {
        return getFile(SOUNDS_DIR, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getImagesDirectory() {
        return getFile(IMAGES_DIR, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getDynamicFile(String filePath) {
        return getFile(filePath, FileLocation.DYNAMIC_FILES);
    }

    @Override
    public FileHandle getStaticFile(String filePath) {
        return getFile(filePath, FileLocation.STATIC_FILES);
    }

    @Override
    public FileHandle getFile(String filePath, FileLocation fileLocation) {
        if(DebugOptions.DEV_ROOT_PATH != null) {
            return Gdx.files.absolute(DebugOptions.DEV_ROOT_PATH + filePath);
        }

        switch (fileLocation) {
            case STATIC_FILES:
                return Gdx.files.internal(filePath);
            case DYNAMIC_FILES:
                return Gdx.files.local(filePath);
            default:
                throw new UnsupportedOperationException(String.format(UNEXPECTED_FILE_LOCATION_TYPE, fileLocation));
        }
    }

    /**
     * Returns the singleton instance of this class.
     * @return The instance.
     */
    public static FileManager getInstance() {
        if(instance == null) {
            instance = new FileManagerImplementation();
        }

        return instance;
    }

    private FileManagerImplementation() {

    }

    private final static String ASSETS_DIR = "res/";
    private final static String FONTS_DIR = ASSETS_DIR + "fonts/";
    private final static String SOUNDS_DIR = ASSETS_DIR + "sounds/";
    private final static String CONFIG_DIR = ASSETS_DIR + "configs/";
    private final static String IMAGES_DIR = ASSETS_DIR + "imgs/";
    private final static String HULLS_CONFIG_DIR = CONFIG_DIR + "hulls/";
    private final static String ITEMS_CONFIG_DIR = CONFIG_DIR + "items/";
    private final static String UNEXPECTED_FILE_LOCATION_TYPE = "Unexpected file location type: %s.";

    private static FileManager instance = null;
}
