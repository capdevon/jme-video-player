package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

import jme.video.player.MovieSettings;
import jme.video.player.MovieState;

/**
 * 
 * @author capdevon
 */
public class Test_MovieState extends SimpleApplication {

    private MovieState movieState;

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_MovieState app = new Test_MovieState();

        // Setup window settings
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);

        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        //app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        try {
            String path = "src/main/resources/Videos/jme3_intro.mp4";
            MovieSettings movie = new MovieSettings(path, cam.getWidth(), cam.getHeight(), 0.8f, true);
            movieState = new MovieState(movie);
            stateManager.attach(movieState);

            // Prevent cam from moving while outside of game instance
            flyCam.setEnabled(false);
            inputManager.setCursorVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // After intro video has finished
        if (movieState.isStopped()) {
            stateManager.detach(movieState);

            flyCam.setMoveSpeed(50);
            flyCam.setEnabled(true);
            inputManager.setCursorVisible(false);

            stateManager.attach(new PostWaterState());
        }
    }

}
