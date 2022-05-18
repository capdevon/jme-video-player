package mygame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import com.sun.javafx.application.PlatformImpl;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import jme.video.player.TextureMovie;

/**
 *
 * @author capdevon
 */
public class Test_BrickWall extends SimpleApplication implements ActionListener {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Test_BrickWall app = new Test_BrickWall();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        app.setSettings(settings);

        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    private BulletAppState physics;
    private Sphere bullet;
    private Material mat2;
    private final List<VideoData> lstVideoData = new ArrayList<>();

    @Override
    public void simpleInitApp() {

        PlatformImpl.startup(() -> {});

        physics = new BulletAppState();
        physics.setDebugEnabled(true);
        physics.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(physics);

        flyCam.setMoveSpeed(20f);
        cam.setLocation(new Vector3f(0, 4, 12));

        initFloor();
        initBullet();

        Texture2D texture = (Texture2D) assetManager.loadTexture("jme3-logo2.png");
        Node w1 = createImageWall(texture);
        w1.setLocalTranslation(0, 0, -2);
        addRigidBody(w1);

        File file = new File("src/main/resources/Videos/jme3_intro.mp4");
        Node w2 = createMovieWall(file);
        w2.setLocalTranslation(0, 0, 2);
        addRigidBody(w2);

        addLighting();
        registerInput();
    }

    @Override
    public void destroy() {
        super.destroy();
        lstVideoData.forEach(v -> v.clear());
        PlatformImpl.exit();
    }

    private void addLighting() {
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", SkyFactory.EnvMapType.CubeMap);
        sky.setShadowMode(RenderQueue.ShadowMode.Off);
        rootNode.attachChild(sky);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.8f));
        rootNode.addLight(al);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 4096, 3);
        dlsf.setLight(sun);
        dlsf.setShadowIntensity(0.4f);
        dlsf.setShadowZExtend(256);

        FXAAFilter fxaa = new FXAAFilter();

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        fpp.addFilter(fxaa);
        viewPort.addProcessor(fpp);
    }

    private Node createMovieWall(File file) {
        final Media media = new Media(file.toURI().toString());
        media.errorProperty().addListener((observable, oldValue, newValue) -> newValue.printStackTrace());

        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        mediaPlayer.setCycleCount(Integer.MAX_VALUE);

        TextureMovie textureMovie = new TextureMovie(this, mediaPlayer, TextureMovie.LetterboxMode.VALID_SQUARE);
        textureMovie.setLetterboxColor(ColorRGBA.Black);

        lstVideoData.add(new VideoData(file.getName(), textureMovie, mediaPlayer));

        return createImageWall(textureMovie.getTexture());
    }

    private Node createImageWall(Texture2D texture) {

        Material colorMaterial = new Material(assetManager, Materials.LIGHTING);
        colorMaterial.setBoolean("UseMaterialColors", true);
        colorMaterial.setColor("Diffuse", ColorRGBA.randomColor());

        Material texMaterial = new Material(assetManager, Materials.LIGHTING);
        texMaterial.setTexture("DiffuseMap", texture);

        int pieces = 8;
        Vector3f wallSize = new Vector3f(8.5f, 5, .5f);
        System.out.println("Pieces: " + pieces + " WallSize: " + wallSize);

        Node wall = new Node("Wall");

        // building a brick wall
        for (int i = 0; i < pieces; ++i) {
            for (int j = 0; j < pieces; ++j) {

                float offset = 1.0f / pieces;
                Vector2f texOffset;
                Vector2f texSize;

                boolean leftSide = (i == 0 && j % 2 == 0);
                boolean rightSide = (i == (pieces - 1) && ((i + j) % 2) == 1);

                if (leftSide || rightSide) {
                    texOffset = new Vector2f(offset * i, offset * j);
                    texSize = new Vector2f(1.0f / pieces, 1.0f / pieces);

                } else if ((i + j) % 2 == 0) {
                    // odd part - don't add
                    continue;

                } else {
                    texOffset = new Vector2f(offset * i, offset * j);
                    texSize = new Vector2f(2.0f / pieces, 1.0f / pieces);
                }

                Vector3f brickPosition = new Vector3f((texOffset.x - wallSize.z) * wallSize.x, texOffset.y * wallSize.y, 0);
                Vector3f brickSize = new Vector3f(texSize.x * wallSize.x, texSize.y * wallSize.y, wallSize.z);

                Node brick = createBrick(colorMaterial, texMaterial, brickPosition, brickSize, texOffset, texSize);
                brick.setName(String.format("Brick_%d_%d", i, j));
                wall.attachChild(brick);
                System.out.println(brick + " " + brick.getWorldTranslation());
            }
        }

        wall.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(wall);

        return wall;
    }

    private Node createBrick(Material colorMaterial, Material texMaterial, Vector3f position, Vector3f size,
            Vector2f texOffset, Vector2f texSize) {

        Node brick = new Node("Brick");
        brick.setUserData("halfExtents", new Vector3f(size.x / 2, size.y / 2, size.z));
        Vector2f[] texCoords;

        // 1. Front
        Quad frontQuad = new Quad(size.x, size.y);
        texCoords = new Vector2f[]{
            new Vector2f(texOffset.x, texOffset.y),
            new Vector2f(texOffset.x + texSize.x, texOffset.y),
            new Vector2f(texOffset.x + texSize.x, texOffset.y + texSize.y),
            new Vector2f(texOffset.x, texOffset.y + texSize.y)
        };
        frontQuad.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        Geometry front = new Geometry("front", frontQuad);
        front.setLocalTranslation(-size.x / 2, -size.y / 2, size.z);
        front.setMaterial(texMaterial);
        brick.attachChild(front);

        // 2. Back
        Quad backQuad = new Quad(size.x, size.y);
        texCoords = new Vector2f[]{
            new Vector2f(texOffset.x + texSize.x, texOffset.y),
            new Vector2f(texOffset.x, texOffset.y),
            new Vector2f(texOffset.x, texOffset.y + texSize.y),
            new Vector2f(texOffset.x + texSize.x, texOffset.y + texSize.y)
        };
        backQuad.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        Geometry back = new Geometry("back", backQuad);
        back.setLocalTranslation(size.x / 2, -size.y / 2, -size.z);
        back.setLocalScale(new Vector3f(-1, 1, 1));
        back.setMaterial(texMaterial);
        brick.attachChild(back);

        // 3. Side (right, left)
        Quad side = new Quad(size.y, size.z * 2);

        Geometry right = new Geometry("right", side);
        right.setLocalTranslation(size.x / 2, -size.y / 2, -size.z);
        right.rotate(0, FastMath.HALF_PI, FastMath.HALF_PI);
        right.setMaterial(colorMaterial);
        brick.attachChild(right);

        Geometry left = new Geometry("left", side);
        left.setLocalTranslation(-size.x / 2, -size.y / 2, size.z);
        left.rotate(0, -FastMath.HALF_PI, FastMath.HALF_PI);
        left.setMaterial(colorMaterial);
        brick.attachChild(left);

        // 4. Side (top, bottom)
        side = new Quad(size.x, size.z * 2);

        Geometry top = new Geometry("top", side);
        top.setLocalTranslation(-size.x / 2, size.y / 2, size.z);
        top.rotate(-FastMath.HALF_PI, 0, 0);
        top.setMaterial(colorMaterial);
        brick.attachChild(top);

        Geometry bottom = new Geometry("bottom", side);
        bottom.setLocalTranslation(-size.x / 2, -size.y / 2, -size.z);
        bottom.rotate(FastMath.HALF_PI, 0, 0);
        bottom.setMaterial(colorMaterial);
        brick.attachChild(bottom);

        brick.setLocalTranslation(
                position.x + size.x / 2,
                position.y + size.y / 2,
                position.z);

//        BoxCollisionShape collShape = new BoxCollisionShape(new Vector3f(size.x / 2, size.y / 2, size.z));
//        RigidBodyControl rbc = new RigidBodyControl(collShape, 1f);
//        brick.addControl(rbc);
//        physics.getPhysicsSpace().add(rbc);
                
        return brick;
    }

    private void addRigidBody(Node wall) {
        List<Node> bricks = wall.descendantMatches(Node.class, "^.*Brick.*$");
        for (Node brick : bricks) {
            BoxCollisionShape collShape = new BoxCollisionShape(brick.getUserData("halfExtents"));
            RigidBodyControl rbc = new RigidBodyControl(collShape, 1.5f);
            rbc.setFriction(0.6f);
            brick.addControl(rbc);
            physics.getPhysicsSpace().add(rbc);
        }
    }

    private void initFloor() {

        Vector3f extents = new Vector3f(40, 0.2f, 40);
        Box box = new Box(extents.x, extents.y, extents.z);
        Geometry floor = new Geometry("floor.geo", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", new ColorRGBA(0f, .5f, 0f, 1f));
        floor.setMaterial(mat);
        floor.setLocalTranslation(0, -0.5f, 0);

        Node node = new Node("Floor");
        node.attachChild(floor);

        BoxCollisionShape collShape = new BoxCollisionShape(extents);
        RigidBodyControl rbc = new RigidBodyControl(collShape, PhysicsBody.massForStatic);
        floor.addControl(rbc);
        rootNode.attachChild(node);
        physics.getPhysicsSpace().add(rbc);
        
        node.setShadowMode(RenderQueue.ShadowMode.Receive);
    }

    private void initBullet() {
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);

        mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        mat2.setTexture("ColorMap", tex2);
    }

    private void shootBall() {

        Geometry geom = new Geometry("bullet", bullet);
        geom.setMaterial(mat2);
        geom.setShadowMode(ShadowMode.CastAndReceive);
        geom.setLocalTranslation(cam.getLocation());

        float force = 25f;
        SphereCollisionShape collShape = new SphereCollisionShape(0.4f);
        RigidBodyControl rbc = new RigidBodyControl(collShape, 1);
        rbc.setLinearVelocity(cam.getDirection().mult(force));
        geom.addControl(rbc);
        rootNode.attachChild(geom);
        physics.getPhysicsSpace().add(rbc);
    }

    private void registerInput() {
        addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        addMapping("TogglePhysicsDebug", new KeyTrigger(KeyInput.KEY_0));
    }

    private void addMapping(String bindingName, Trigger... triggers) {
        inputManager.addMapping(bindingName, triggers);
        inputManager.addListener(this, bindingName);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Shoot") && isPressed) {
            shootBall();

        } else if (name.equals("TogglePhysicsDebug") && isPressed) {
            boolean debug = physics.isDebugEnabled();
            physics.setDebugEnabled(!debug);
        }
    }

    private class VideoData {

        private String name;
        private TextureMovie textureMovie;
        private MediaPlayer mediaPlayer;

        public VideoData(String name, TextureMovie textureMovie, MediaPlayer mediaPlayer) {
            this.name = name;
            this.textureMovie = textureMovie;
            this.mediaPlayer = mediaPlayer;
        }

        public void clear() {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            System.out.println("Free all resources associated with media player... " + name);
        }

    }

}
