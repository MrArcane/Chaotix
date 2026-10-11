package me.arkallic.editor;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Starts the editor window.
 * Optional argument: the size of a new map, e.g. 64 for 64x64 (default 32).
 *   ./gradlew :editor:run --args="64"
 */
public class EditorLauncher {

    public static void main(String[] args) {
        int size = 32;
        if (args.length > 0) {
            try {
                size = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException e) {
                System.err.println("Map size must be a number, e.g. 32 or 64. Got: " + args[0]);
                return;
            }
        }
        size = Math.max(1, Math.min(64, size));

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Chaotix Map Editor");
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setWindowedMode(1280, 720);
        new Lwjgl3Application(new MapEditor(size), config);
    }
}
