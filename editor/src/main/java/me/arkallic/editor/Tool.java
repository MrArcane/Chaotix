package me.arkallic.editor;

/** What a left-click does. (Right-click always erases.) */
enum Tool {
    /** Paint tiles with the selected tile type. */
    PAINT,
    /** Flood-fill a connected area of tiles with the selected tile type. */
    FILL,
    /** Set the height of tiles to the number in the Height box. */
    HEIGHT
}
