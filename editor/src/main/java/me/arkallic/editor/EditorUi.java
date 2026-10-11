package me.arkallic.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import me.arkallic.map.tile.TileType;

/**
 * The left-hand panel: tool, brush size, tile palette, and the file buttons.
 * <p>
 * It builds Scene2D widgets (libGDX's built-in UI toolkit) using the same skin
 * the game already ships in assets/ui/uiskin.json. When you click something it
 * either writes to EditorState (tool/brush/tile) or calls the Listener
 * (clear/save/load), so it never edits the map itself.
 */
final class EditorUi implements Disposable {

    /** What the UI asks the rest of the editor to do. MapEditor implements this. */
    interface Listener {
        void onClear();

        void onSave(String name);

        void onLoad(String name);
    }

    static final float WIDTH = 340f;

    private static final int[] BRUSH_SIZES = {1, 2, 3, 5};
    private static final Color SELECTED = new Color(0.19f, 0.66f, 0.96f, 1f);
    private static final Color HEADING = new Color(0.60f, 0.65f, 0.70f, 1f);

    private final Skin skin;
    private final Texture whiteTexture;
    private final TextureRegionDrawable white;
    private final TextButton.TextButtonStyle toggleStyle;
    private final Table sidebar = new Table();
    private final TextField nameField;
    private final Label status;
    private final Label hover;

    EditorUi(Stage stage, final EditorState state, final Listener listener) {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // A 1x1 white pixel. Tinted, it gives us any flat colour we want (swatches, panel background).
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        whiteTexture = new Texture(pixel);
        pixel.dispose();
        white = new TextureRegionDrawable(new TextureRegion(whiteTexture));

        // The skin's default button has no "checked" look, so make a copy that has one.
        toggleStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        toggleStyle.checked = skin.newDrawable("button-normal", SELECTED);
        toggleStyle.checkedOver = toggleStyle.checked;

        sidebar.setBackground(white.tint(new Color(0.11f, 0.12f, 0.15f, 1f)));
        sidebar.top().left().pad(10);

        // ---- Tool ----
        addHeading("TOOL (left-click)");
        ButtonGroup<TextButton> tools = new ButtonGroup<>();
        Table toolRow = new Table();
        toolRow.add(toggle("Paint", tools, () -> state.tool = Tool.PAINT)).width(100).height(28).padRight(6);
        toolRow.add(toggle("Fill", tools, () -> state.tool = Tool.FILL)).width(100).height(28);
        sidebar.add(toolRow).left().row();

        // ---- Brush ----
        addHeading("BRUSH SIZE");
        ButtonGroup<TextButton> brushes = new ButtonGroup<>();
        Table brushRow = new Table();
        for (final int size : BRUSH_SIZES) {
            brushRow.add(toggle(size + "x" + size, brushes, () -> state.brush = size)).width(60).height(28).padRight(6);
        }
        sidebar.add(brushRow).left().row();

        // ---- Palette: one swatch + one toggle button per TileType, two per row ----
        addHeading("TILE");
        ButtonGroup<TextButton> palette = new ButtonGroup<>();
        Table paletteTable = new Table();
        TileType[] types = TileType.values();
        for (int i = 0; i < types.length; i++) {
            final TileType type = types[i];
            paletteTable.add(new Image(white.tint(type.color()))).size(18).padRight(4);
            paletteTable.add(toggle(type.name(), palette, () -> state.tile = type)).width(126).height(24).padBottom(2).padRight(i % 2 == 0 ? 10 : 0);
            if (i % 2 == 1) {
                paletteTable.row();
            }
        }
        sidebar.add(paletteTable).left().row();

        // ---- Files ----
        addHeading("MAP FILE  (assets/maps/<name>.map)");
        nameField = new TextField("my_map", skin);
        nameField.setMaxLength(32);
        nameField.setTextFieldFilter((field, c) -> Character.isLetterOrDigit(c) || c == '_' || c == '-');
        sidebar.add(nameField).width(WIDTH - 20).height(28).left().row();

        Table fileRow = new Table();
        fileRow.add(action("Save", () -> listener.onSave(nameField.getText()))).width(100).height(28).padRight(6);
        fileRow.add(action("Load", () -> listener.onLoad(nameField.getText()))).width(100).height(28).padRight(6);
        fileRow.add(action("Clear", listener::onClear)).width(100).height(28);
        sidebar.add(fileRow).left().padTop(6).row();

        // ---- Messages ----
        status = new Label("Ready.", skin);
        status.setWrap(true);
        sidebar.add(status).width(WIDTH - 20).left().padTop(8).row();

        hover = new Label("", skin);
        hover.setColor(HEADING);
        sidebar.add(hover).left().padTop(4).row();

        Label help = new Label("Left-click: paint   Right-click: erase", skin);
        help.setColor(HEADING);
        sidebar.add(help).left().padTop(4).row();

        // Show the initial selection (must match what EditorState starts with).
        tools.setChecked("Paint");
        brushes.setChecked("1x1");
        palette.setChecked(state.tile.name());

        stage.addActor(sidebar);
    }

    /** Called on window resize: the sidebar is always full height, WIDTH wide, pinned to the left. */
    void layout(float stageHeight) {
        sidebar.setBounds(0, 0, WIDTH, stageHeight);
        sidebar.invalidate();
    }

    void setStatus(String text) {
        status.setText(text);
    }

    void setHover(String text) {
        hover.setText(text);
    }

    // ------------------------------------------------------------------
    // Small widget helpers
    // ------------------------------------------------------------------

    private void addHeading(String text) {
        Label label = new Label(text, skin);
        label.setColor(HEADING);
        sidebar.add(label).left().padTop(10).padBottom(3).row();
    }

    /** A button that stays pressed. Buttons sharing a group act like radio buttons. */
    private TextButton toggle(String text, ButtonGroup<TextButton> group, final Runnable onSelected) {
        final TextButton button = new TextButton(text, toggleStyle);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (button.isChecked()) {
                    onSelected.run();
                }
            }
        });
        group.add(button);
        return button;
    }

    /** A normal click-once button. */
    private TextButton action(String text, final Runnable onClick) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        return button;
    }

    @Override
    public void dispose() {
        skin.dispose();
        whiteTexture.dispose();
    }
}
