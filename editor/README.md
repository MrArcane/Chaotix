# Chaotix Map Editor

A separate desktop app for painting maps. It edits the game's own `me.arkallic.map.MapData`
and reads/writes the same JSON the game loads (`assets/maps/<name>.json`, format version 2),
using the same libGDX `Json` class as `GameMap`. So a file saved here loads in the game.

## Run

    ./gradlew :editor:run                     (new 32x32 map)
    ./gradlew :editor:run --args="64"         (new 64x64 map, max 64)

On Windows use `gradlew.bat`, or the `editor > Tasks > application > run` task in IntelliJ.
The working directory is `assets/`, so Save writes into `assets/maps/`, the folder the game loads from.

## Controls

- Tools: Paint (tiles), Fill (flood-fill tiles), Height (set heights)
- Left-click / drag applies. Right-click / drag erases (tile -> GRASS, height -> 0)
- Brush size 1x1 / 2x2 / 3x3 / 5x5 applies to Paint and Height
- Height box: the value the Height tool sets. `-` / `+` nudge it by 0.25.
  Height SETS rather than adds, so dragging slowly never piles terrain up.
- Show heights: shades the grid so you can see the terrain (brighter = higher)
- Under the grid: hover a tile to see its x, y, type and height
- Save / Load use the name in the box (Load `ashford` to edit Ashford); Clear resets tiles to GRASS and heights to 0

## The file

    { "version": 2, "name": "...", "width": 32, "height": 32,
      "tiles":   [ [ "GRASS", ... ],  ...   ],    // tiles[y][x]
      "heights": [ [ 0.0, ... ],      ...   ] }  // heights[y][x]

y = 0 is the SOUTH edge, so the first row in the file is the bottom row of the map.
Loading is strict: unknown tile names, wrong row lengths, extra fields or a newer
version are rejected with a message, rather than silently dropping data on re-save.

## Code map

    EditorLauncher  starts the window
    MapEditor       glue: owns the Stage, handles Save / Load / Clear
    EditorState     what is selected (tool, tile, brush, height) + the MapData being edited
    EditorUi        the sidebar (Scene2D widgets)
    GridView        draws the grid, turns mouse input into edits
    Painter         the edit operations (stamp, line, flood fill, heights) - pure logic
    MapOps          bounds-checked helpers over the game's public-field MapData
    MapFile         JSON in/out + validation (same Json + MapData as the game)
    Tool            PAINT, FILL or HEIGHT
