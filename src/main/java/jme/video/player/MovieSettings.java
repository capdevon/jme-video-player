package jme.video.player;

/**
 * 
 * @author capdevon
 */
public class MovieSettings {

    private String path;
    private int width;
    private int height;
    private float zoomingFactor;
    private boolean skippable;

    public MovieSettings(String path, int width, int height) {
        this(path, width, height, 1f, false);
    }

    public MovieSettings(String path, int width, int height, float zoomingFactor, boolean skippable) {
        this.path = path;
        this.width = width;
        this.height = height;
        this.zoomingFactor = zoomingFactor;
        this.skippable = skippable;
    }

    public String getPath() {
        return path;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getZoomingFactor() {
        return zoomingFactor;
    }

    public boolean isSkippable() {
        return skippable;
    }

}