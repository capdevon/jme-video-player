package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.audio.LowPassFilter;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;
import com.jme3.water.WaterFilter;

/**
 *
 * @author capdevon
 */
public class PostWaterState extends BaseAppState implements ActionListener {

    private Node rootNode;
    private AssetManager assetManager;
    private InputManager inputManager;

    private final Vector3f lightDir = new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
    private WaterFilter water;
    private AudioNode audioWaves;
    private Node mainScene;
    private FilterPostProcessor fpp;

    @Override
    protected void initialize(Application app) {

        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.rootNode = ((SimpleApplication) app).getRootNode();
        Camera cam = app.getCamera();
        AppSettings settings = app.getContext().getSettings();

        mainScene = new Node("Main Scene");

        // 1. Create Terrain
        TerrainQuad terrain = createTerrain(new Vector3f(0, -30, 0), Quaternion.IDENTITY, mainScene);
        terrain.setLocalScale(new Vector3f(5, 5, 5));

        // 2. Create Lights
        DirectionalLight sun = new DirectionalLight();
        sun.setName("SunLight");
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone());
        mainScene.addLight(sun);

        AmbientLight al = new AmbientLight();
        al.setName("AmbientLight");
        al.setColor(new ColorRGBA(.1f, .1f, .1f, 1.0f));
        mainScene.addLight(al);

        // 3. Create Sky
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", EnvMapType.CubeMap);
        sky.setLocalScale(350);
        mainScene.attachChild(sky);

        // 4. Create Filters
        water = new WaterFilter(rootNode, lightDir);
        water.setWaterColor(new ColorRGBA().setAsSrgb(0.0078f, 0.3176f, 0.5f, 1.0f));
        water.setDeepWaterColor(new ColorRGBA().setAsSrgb(0.0039f, 0.00196f, 0.145f, 1.0f));
        water.setUnderWaterFogDistance(80);
        water.setWaterTransparency(0.12f);
        water.setFoamIntensity(0.4f);
        water.setFoamHardness(0.3f);
        water.setFoamExistence(new Vector3f(0.8f, 8f, 1f));
        water.setReflectionDisplace(50);
        water.setRefractionConstant(0.25f);
        water.setColorExtinction(new Vector3f(30, 50, 70));
        water.setCausticsIntensity(0.4f);
        water.setWaveScale(0.003f);
        water.setMaxAmplitude(2f);
        water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
        water.setRefractionStrength(0.2f);
        water.setWaterHeight(initialWaterHeight);

        BloomFilter bloom = new BloomFilter();
        bloom.setExposurePower(55);
        bloom.setBloomIntensity(1.0f);

        LightScatteringFilter lsf = new LightScatteringFilter(lightDir.mult(-300));
        lsf.setLightDensity(0.5f);

        DepthOfFieldFilter dof = new DepthOfFieldFilter();
        dof.setFocusDistance(0);
        dof.setFocusRange(100);

        fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(water);
        fpp.addFilter(bloom);
        fpp.addFilter(dof);
        fpp.addFilter(lsf);
        fpp.addFilter(new FXAAFilter());
        //fpp.addFilter(new TranslucentBucketFilter());

        int numSamples = settings.getSamples();
        if (numSamples > 0) {
            fpp.setNumSamples(numSamples);
        }

        // 5. Create Audio SFX
        audioWaves = new AudioNode(assetManager, "Sound/Environment/Ocean Waves.ogg", DataType.Buffer);
        audioWaves.setLooping(true);
        audioWaves.setReverbEnabled(true);
        audioWaves.setDryFilter(new LowPassFilter(1, 1));
        audioWaves.play();

        // 6. Configure Camera and Inputs
        configCamera(cam);
        setupKeys();
    }

    private void configCamera(Camera cam) {
        cam.setLocation(new Vector3f(-370.31592f, 182.04016f, 196.81192f));
        cam.setRotation(new Quaternion(0.015302252f, 0.9304095f, -0.039101653f, 0.3641086f));
        cam.setFrustumFar(4000);
    }

    private void setupKeys() {
        addMapping("foam1", new KeyTrigger(KeyInput.KEY_1));
        addMapping("foam2", new KeyTrigger(KeyInput.KEY_2));
        addMapping("foam3", new KeyTrigger(KeyInput.KEY_3));
        addMapping("upRM", new KeyTrigger(KeyInput.KEY_PGUP));
        addMapping("downRM", new KeyTrigger(KeyInput.KEY_PGDN));
    }

    private void addMapping(String bindingName, Trigger... triggers) {
        inputManager.addMapping(bindingName, triggers);
        inputManager.addListener(this, bindingName);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            if (name.equals("foam1")) {
                water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam.jpg"));
            }
            if (name.equals("foam2")) {
                water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
            }
            if (name.equals("foam3")) {
                water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam3.jpg"));
            }
            if (name.equals("upRM")) {
                water.setReflectionMapSize(Math.min(water.getReflectionMapSize() * 2, 4096));
                System.out.println("ReflectionMapSize: " + water.getReflectionMapSize());
            }
            if (name.equals("downRM")) {
                water.setReflectionMapSize(Math.max(water.getReflectionMapSize() / 2, 32));
                System.out.println("ReflectionMapSize: " + water.getReflectionMapSize());
            }
        }
    }

    private TerrainQuad createTerrain(Vector3f position, Quaternion rotation, Node parent) {

        Material matRock = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);
        matRock.setBoolean("WardIso", true);
        matRock.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("DiffuseMap", grass);
        matRock.setFloat("DiffuseMap_0_scale", 64);

        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("DiffuseMap_1", dirt);
        matRock.setFloat("DiffuseMap_1_scale", 16);

        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("DiffuseMap_2", rock);
        matRock.setFloat("DiffuseMap_2_scale", 128);

        Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
        normalMap0.setWrap(WrapMode.Repeat);

        Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
        normalMap1.setWrap(WrapMode.Repeat);

        Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
        normalMap2.setWrap(WrapMode.Repeat);

        matRock.setTexture("NormalMap", normalMap0);
        matRock.setTexture("NormalMap_1", normalMap1);
        matRock.setTexture("NormalMap_2", normalMap2);

        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
            heightmap.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TerrainQuad terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        terrain.setMaterial(matRock);
        terrain.setLocalTranslation(position);
        terrain.setLocalRotation(rotation);

        terrain.setShadowMode(ShadowMode.Receive);
        parent.attachChild(terrain);

        return terrain;
    }

    // This part is to emulate tides, slightly varying the height of the water plane
    private float time = 0.0f;
    private float waterHeight = 0.0f;
    private final float initialWaterHeight = 90f; // 0.8f;
    private boolean uw = false;

    @Override
    public void update(float tpf) {
        time += tpf;
        waterHeight = (float) Math.cos(((time * 0.6f) % FastMath.TWO_PI)) * 1.5f;
        water.setWaterHeight(initialWaterHeight + waterHeight);

        if (water.isUnderWater() && !uw) {
            uw = true;
            audioWaves.setReverbEnabled(false);
        }
        if (!water.isUnderWater() && uw) {
            uw = false;
            audioWaves.setReverbEnabled(true);
        }
    }

    @Override
    protected void cleanup(Application app) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onEnable() {
        rootNode.attachChild(mainScene);
        getApplication().getViewPort().addProcessor(fpp);
    }

    @Override
    protected void onDisable() {
        rootNode.detachChild(mainScene);
        getApplication().getViewPort().removeProcessor(fpp);
    }
}
