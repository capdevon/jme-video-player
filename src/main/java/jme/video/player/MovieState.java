package jme.video.player;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.sun.javafx.application.PlatformImpl;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import jme.video.player.TextureMovie.LetterboxMode;

/**
 * 
 * @author capdevon
 */
public class MovieState extends BaseAppState {

    private static final Logger logger = Logger.getLogger(MovieState.class.getName());

    private static final String KEY_SKIP = "SKIP_VIDEO";
    private InputManager inputManager;
    private Node guiNode;
    private Geometry screen;
    private TextureMovie textureMovie;
    private MediaPlayer mediaPlayer;
    private MovieSettings movie;

    /**
     * 
     * @param movie
     * @throws FileNotFoundException
     */
    public MovieState(MovieSettings movie) throws FileNotFoundException {
        Path path = Paths.get(movie.getPath());
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Movie file not found!");
        }
        this.movie = movie;
    }

    @Override
    protected void initialize(Application app) {

        this.guiNode = ((SimpleApplication) app).getGuiNode();
        this.inputManager = app.getInputManager();

        // Start javafx
        PlatformImpl.startup(() -> {});

        startVideo(app);
    }

    private void startVideo(Application app) {

        int width = movie.getWidth();
        int height = movie.getHeight();
        float zoomingFactor = movie.getZoomingFactor();

        File f = new File(movie.getPath());
        Media media = new Media(f.toURI().toASCIIString());
        media.errorProperty().addListener(new ChangeListener<MediaException>() {

            @Override
            public void changed(final ObservableValue<? extends MediaException> observable,
                final MediaException oldValue, final MediaException newValue) {
                newValue.printStackTrace();
            }
        });

        mediaPlayer = new MediaPlayer(media);

        textureMovie = new TextureMovie(app, mediaPlayer, LetterboxMode.VALID_LETTERBOX);
        textureMovie.setLetterboxColor(ColorRGBA.Black);

        float quadWidth = width * zoomingFactor;
        float quadHeight = height * zoomingFactor;

        // Draw video screen inside game
        screen = new Geometry("Screen", new Quad(quadWidth, quadHeight));
        Material mat = new Material(app.getAssetManager(), "Shaders/MovieShader.j3md");
        mat.setTexture("ColorMap", textureMovie.getTexture());
        screen.setMaterial(mat);

        // Stop video player when intro ends
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.stop();
            }
        });

        // Setup screen location
        int offsetX = (int) ((quadWidth - width) / 2f);
        int offsetY = (int) ((quadHeight - height) / 2f);
        screen.setLocalTranslation(new Vector3f(-offsetX, -offsetY, 1));

        // Add video screen to game
        guiNode.attachChild(screen);

        // Play video
        mediaPlayer.play();
    }

    @Override
    protected void cleanup(Application app) {
        // Free all resources associated with player
        mediaPlayer.dispose();
        // Detach video screen
        guiNode.detachChild(screen);
        // Stop javafx
        PlatformImpl.exit();
    }

    @Override
    protected void onEnable() {
        // Assign video skipping keys
        inputManager.addMapping(KEY_SKIP, new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, KEY_SKIP);
    }

    @Override
    protected void onDisable() {
        // Clean our mapping
        inputManager.deleteMapping(KEY_SKIP);
        inputManager.removeListener(actionListener);
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (KEY_SKIP.equals(name) && !isPressed && movie.isSkippable()) {
                mediaPlayer.stop();
            }
        }
    };

    public boolean isStopped() {
        return mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED;
    }

    public boolean isDisposed() {
        return mediaPlayer.getStatus() == MediaPlayer.Status.DISPOSED;
    }

}
