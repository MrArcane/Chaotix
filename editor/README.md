# Chaotix Map Editor

A separate desktop app for painting maps. It depends on `:core` (TileType, MapData,
MapFormat) and nothing else from the game; the game never depends on it.

## Run

    ./gradlew :editor:run                     (new 32x32 map)
    ./gradlew :editor:run --args="64"         (new 64x64 map)

On Windows use `gradlew.bat`, or the `editor > Tasks > application > run` task in IntelliJ.

The working directory is `assets/`, so Save writes to `assets/maps/<name>.map`
and Load reads from there - the same folder the game loads from.

## Controls

- Left-click / drag: paint with the selected tile (Paint tool) or fill the area (Fill tool)
- Right-click / drag: erase (resets tiles to GRASS)
- Brush size 1x1 / 2x2 / 3x3 / 5x5 applies to the Paint tool
- Save / Load use the name in the box; Clear resets every tile to GRASS

## Code map

    EditorLauncher  starts the window
    MapEditor       glue: owns the Stage, handles Save / Load / Clear
    EditorState     what is selected (tile, tool, brush) + the MapData being edited
    EditorUi        the sidebar (Scene2D widgets)
    GridView        draws the grid, turns mouse input into edits
    Painter         the edit operations (stamp, line, flood fill) - pure logic, no libGDX
    Tool            PAINT or FILL
