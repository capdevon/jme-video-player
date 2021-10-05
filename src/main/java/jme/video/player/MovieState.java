package jme.video.player;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
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
import com.jme3.renderer.Camera;
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

    private static final String KEY_SKIP = "SKIP";
    private InputManager inputManager;

    private Camera cam;
    private Node guiNode;
    private Geometry screenGeom;

    private String movie;
    private TextureMovie textureMovie;
    private MediaPlayer mediaPlayer;

    /**
     * 
     * @param movie
     * @throws FileNotFoundException
     */
    public MovieState(String movie) throws FileNotFoundException {
        if (!Files.exists(Paths.get(movie))) {
            throw new FileNotFoundException("Movie file not found!");
        }
        this.movie = movie;
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            // Skip video
            if (KEY_SKIP.equals(name) && !isPressed) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
            }
        }
    };

    @Override
    protected void initialize(Application app) {

        this.guiNode = ((SimpleApplication) app).getGuiNode();
        this.inputManager = app.getInputManager();
        this.cam = app.getCamera();
        int width = cam.getWidth();
        int height = cam.getHeight();

        // Assign video skipping keys
        inputManager.addMapping(KEY_SKIP, new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, KEY_SKIP);

        // Start javafx
        PlatformImpl.startup(new Runnable() {
            @Override
            public void run() {}
        });

        URI videoUri = new File(movie).toURI();
        logger.log(Level.INFO, "Video URI: " + videoUri.getPath());

        Media media = new Media(videoUri.toASCIIString());
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

        // Draw video screen inside game
        screenGeom = new Geometry("Screen", new Quad(width, height));
        Material mat = new Material(app.getAssetManager(), "Shaders/MovieShader.j3md");
        mat.setTexture("ColorMap", textureMovie.getTexture());
        screenGeom.setMaterial(mat);

        // Stop video player when intro ends
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.stop();
            }
        });

        // Add video screen to game
        guiNode.attachChild(screenGeom);

        // Setup camera location to watch video intro
        cam.setLocation(new Vector3f(10, 10, 15));

        mediaPlayer.play();
    }

    @Override
    protected void cleanup(Application app) {
        // Clean our mapping
        inputManager.deleteMapping(KEY_SKIP);
        inputManager.removeListener(actionListener);

        // Dispose the video
        mediaPlayer.dispose();
        // Detach video screen
        guiNode.detachChild(screenGeom);
        // Reset camera location
        cam.setLocation(new Vector3f(0, 0, 0));
        // Stop javafx
        PlatformImpl.exit();
    }

    @Override
    protected void onEnable() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onDisable() {
        // TODO Auto-generated method stub
    }

    public boolean isStopped() {
        return mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED;
    }

    public boolean isDisposed() {
        return mediaPlayer.getStatus() == MediaPlayer.Status.DISPOSED;
    }

}
