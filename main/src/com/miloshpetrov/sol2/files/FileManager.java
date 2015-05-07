package com.miloshpetrov.sol2.files;

import com.badlogic.gdx.files.FileHandle;

/**
 * Created by Linus on 7-5-2015.
 */
public interface FileManager {
    /**
     * Returns a file handle to the assets directory.
     * @return A file handle to the assets directory.
     */
    FileHandle getAssetsDirectory();

    /**
     * Returns a file handle to the config directory.
     * @return A file handle to the config directory.
     */
    FileHandle getConfigDirectory();

    /**
     * Returns a file handle to the hulls directory.
     * @return A file handle to the hulls directory.
     */
    FileHandle getHullsDirectory();

    /**
     * Returns a file handle to the items directory.
     * @return A file handle to the items directory.
     */
    FileHandle getItemsDirectory();

    /**
     * Returns a file handle to the fonts directory.
     * @return A file handle to the fonts directory.
     */
    FileHandle getFontsDirectory();

    /**
     * Returns a file handle to the sounds directory.
     * @return A file handle to the sounds directory.
     */
    FileHandle getSoundsDirectory();

    /**
     * Returns a file handle to the images directory.
     * @return A file handle to the images directory.
     */
    FileHandle getImagesDirectory();

    /**
     * Returns a handle to a static file.
     * Dynamic files are files which are written or updated by the application.
     * @param filePath The path to the file, relative to the dynamic file directory.
     * @return A file handle to the file.
     */
    FileHandle getDynamicFile(String filePath);

    /**
     * Returns a handle to a static file.
     * Static files are files which are not written to by the application.
     * @param filePath The path to the file, relative to the static file directory.
     * @return A file handle to the file.
     */
    FileHandle getStaticFile(String filePath);

    /**
     * Returns a handle to a static file or dynamic file.
     * Static files are files which are not written to by the application.
     * @param filePath The path to the file, relative to the static/dynamic file directory.
     * @param fileLocation Whether the file resides in the static or dynamic file directory.
     * @return A file handle to the file.
     */
    FileHandle getFile(String filePath, FileLocation fileLocation);

    /**
     * Enum for the storage locations of files.
     */
    public static enum FileLocation {
        // Static files are files which are not written to by the application.
        STATIC_FILES,

        // Dynamic files are files which are written or updated by the application.
        DYNAMIC_FILES
    }
}
