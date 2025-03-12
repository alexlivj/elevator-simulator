package simulator.elevator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.game.entity.passenger.PassengerState;
import simulator.elevator.game.manager.GameStateManager;
import simulator.elevator.game.scene.CastingDirection;
import simulator.elevator.game.scene.PortraitType;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.game.scene.SceneType;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.game.scene.line.AbstractLineTree;
import simulator.elevator.game.scene.line.Option;
import simulator.elevator.game.scene.line.OptionConsequence;
import simulator.elevator.game.scene.line.OptionLineTree;
import simulator.elevator.game.scene.line.StatementLineTree;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RandomUtility;
import simulator.elevator.util.RelativeCoordinate;
import simulator.elevator.util.TextureUtility;

public class Level {
    public final RelativeCoordinate WORLD_ORIGIN = new RelativeCoordinate(null, new Vector2(0,0));
    
    public final int GAME_LENGTH_SEC;
    public final List<RelativeCoordinate> FLOOR_SPAWNS;
    public final Pair<Integer,Integer> CAMERA_Y_BOUND_PIXEL;
    public final int CAMERA_Y_OFFSET_PIXEL;
    public final int ELEVATOR_DURABILITY_BUFFER_PIXEL;
    public final Pair<Integer,Integer> ELEVATOR_Y_BOUND;
    
    public final Pair<Integer,Integer> SLIDER_Y_BOUND_PIXEL;
    public final Vector2 SLIDER_POS;
    public final Vector2 BUTTON_POS;
    public final Vector2 NPC_PORTRAIT_POS;
    public final Vector2 NPC_TEXT_POS;
    public final Vector2 PLAYER_PORTRAIT_POS;
    public final Vector2 PLAYER_TEXT_POS;
    
    public final Texture FLOOR_TEXTURE;
    public final Texture STATIC_UI_TEXTURE;
    public final Texture OPTION_BOX_TEXTURE;
    public final Texture BUTTON_TEXTURE;
    public final Texture SLIDER_TEXTURE;
    public final Texture ELEVATOR_OPEN_TEXTURE;
    public final Texture ELEVATOR_CLOSED_TEXTURE;
    public final List<Texture> PASSENGER_TEXTURES;
    public final Map<PortraitType,Texture> PORTRAIT_TEXTURES;
    
    public final int ELEVATOR_SPEED_PIXEL_SEC;
    public final int ELEVATOR_UNSAFE_SPEED_PIXEL_SEC;
    public final float ELEVATOR_UNSAFE_DECAY_RATE_SEC;
    public final float ELEVATOR_HALT_DECAY_PER_PIXEL_SEC;
    public final float ELEVATOR_BOUND_HALT_DECAY_MOD;

    public final int ELEVATOR_FLOOR_BUFFER_PIXEL;
    public final int DOOR_X_PIXEL;
    public final int PASSENGER_WIDTH_PIXEL ;
    public final int WAIT_X_OFFSET_PIXEL;
    public final int RIDE_X_OFFSET_PIXEL;
    

    public final int MAX_PASSENGERS_WORLD;
    public final int MAX_PASSENGERS_FLOOR;
    public final int MAX_PASSENGERS_ELEVATOR;
    public final float SPAWN_OCCURRENCE_SEC;
    public final float SCENE_OCCURRENCE_SPAWN ;
    public final int MIN_SPEED_PIXEL_SEC;
    public final int MAX_SPEED_PIXEL_SEC;
    public final Color[] POSSIBLE_PASSENGER_COLORS;
    
    public final float HAPPINESS_DECAY_RATE_SEC;
    public final Map<PassengerState,Float> HAPPINESS_DECAY_MOD;
    public final float DOOR_SLAM_PENALTY;
    public final int MAX_TIP_CENTS;
    public final float BONUS_TIME;
    
    public final Map<SceneType,Map<CastingDirection,List<Scene>>> ALL_NORMAL_SCENES;
    public final List<StarRole> ALL_STAR_SCENES;
    public final int MAX_SCENES;
    
    private class NullHandler<T> {
        final T defaultValue;
        NullHandler(T def) {
            this.defaultValue = def;
        }
        @SuppressWarnings("unchecked")
        T handle(JSONArray jArray, Integer index) {
            if (jArray.isNull(index))
                return this.defaultValue;
            else
                return (T)jArray.get(index);
        }
    }
    
    public Level(String filename) throws IOException {
        String text = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
        JSONObject file = new JSONObject(text);
        
        this.GAME_LENGTH_SEC = file.getInt("game_length_sec");
        this.FLOOR_SPAWNS = new ArrayList<RelativeCoordinate>();
        JSONArray floorSpawns = file.getJSONArray("floor_world_spawns");
        for (Object spawn : floorSpawns) {
            JSONArray jsonVector = (JSONArray) spawn;
            Vector2 gdxVector = new Vector2(jsonVector.getFloat(0), jsonVector.getFloat(1));
            this.FLOOR_SPAWNS.add(new RelativeCoordinate(this.WORLD_ORIGIN, gdxVector));
        }
        JSONArray cameraYBound = file.getJSONArray("camera_y_bound_pixel");
        this.CAMERA_Y_BOUND_PIXEL = new Pair<Integer,Integer>(cameraYBound.getInt(0),cameraYBound.getInt(1));
        this.CAMERA_Y_OFFSET_PIXEL = file.getInt("camera_y_offset_pixel");
        this.ELEVATOR_DURABILITY_BUFFER_PIXEL = file.getInt("elevator_durability_buffer_pixel");
        int lowerElevatorY = this.CAMERA_Y_BOUND_PIXEL.first - this.ELEVATOR_DURABILITY_BUFFER_PIXEL;
        int upperElevatorY = this.CAMERA_Y_BOUND_PIXEL.second + this.ELEVATOR_DURABILITY_BUFFER_PIXEL;
        this.ELEVATOR_Y_BOUND = new Pair<Integer,Integer>(lowerElevatorY,upperElevatorY);

        JSONArray sliderYBound = file.getJSONArray("slider_y_bound_pixel");
        this.SLIDER_Y_BOUND_PIXEL = new Pair<Integer,Integer>(sliderYBound.getInt(0),sliderYBound.getInt(1));
        JSONArray sliderPos = file.getJSONArray("slider_pos");
        this.SLIDER_POS = new Vector2(sliderPos.getFloat(0),sliderPos.getFloat(1));
        JSONArray buttonPos = file.getJSONArray("button_pos");
        this.BUTTON_POS = new Vector2(buttonPos.getFloat(0),buttonPos.getFloat(1));
        JSONArray npcPortraitPos = file.getJSONArray("npc_portrait_pos");
        this.NPC_PORTRAIT_POS = new Vector2(npcPortraitPos.getFloat(0),npcPortraitPos.getFloat(1));
        JSONArray npcTextPos = file.getJSONArray("npc_text_pos");
        this.NPC_TEXT_POS = new Vector2(npcTextPos.getFloat(0),npcTextPos.getFloat(1));
        JSONArray playerPortraitPos = file.getJSONArray("player_portrait_pos");
        this.PLAYER_PORTRAIT_POS = new Vector2(playerPortraitPos.getFloat(0),playerPortraitPos.getFloat(1));
        JSONArray playerTextPos = file.getJSONArray("player_text_pos");
        this.PLAYER_TEXT_POS = new Vector2(playerTextPos.getFloat(0),playerTextPos.getFloat(1));

        this.FLOOR_TEXTURE = TextureUtility.doubleTextureSize(file.getString("floor_texture"));
        this.STATIC_UI_TEXTURE = TextureUtility.doubleTextureSize(file.getString("static_ui_texture"));
        this.OPTION_BOX_TEXTURE = TextureUtility.doubleTextureSize(file.getString("option_box_texture"));
        this.BUTTON_TEXTURE = TextureUtility.doubleTextureSize(file.getString("button_texture"));
        this.SLIDER_TEXTURE = TextureUtility.doubleTextureSize(file.getString("slider_texture"));
        this.ELEVATOR_OPEN_TEXTURE =
                TextureUtility.doubleTextureSize(file.getString("elevator_open_texture"));
        this.ELEVATOR_CLOSED_TEXTURE = 
                TextureUtility.doubleTextureSize(file.getString("elevator_closed_texture"));
        this.PASSENGER_TEXTURES = new ArrayList<Texture>();
        JSONArray passengerTextures = file.getJSONArray("passenger_textures");
        for (Object pTextures : passengerTextures)
            this.PASSENGER_TEXTURES.add(TextureUtility.doubleTextureSize((String) pTextures));
        this.PORTRAIT_TEXTURES = new HashMap<PortraitType,Texture>();
        JSONObject portraitTextures = file.getJSONObject("portrait_textures");
        for (String portraitKey : JSONObject.getNames(portraitTextures)) {
            Texture t = TextureUtility.doubleTextureSize(portraitTextures.getString(portraitKey));
            this.PORTRAIT_TEXTURES.put(PortraitType.getPortraitType(portraitKey), t);
        }
        
        this.ELEVATOR_SPEED_PIXEL_SEC = file.getInt("elevator_speed_pixel_sec");
        this.ELEVATOR_UNSAFE_SPEED_PIXEL_SEC = file.getInt("elevator_unsafe_speed_pixel_sec");
        this.ELEVATOR_UNSAFE_DECAY_RATE_SEC = file.getFloat("elevator_unsafe_decay_rate_sec");
        this.ELEVATOR_HALT_DECAY_PER_PIXEL_SEC = file.getFloat("elevator_halt_decay_per_pixel_sec");
        this.ELEVATOR_BOUND_HALT_DECAY_MOD = file.getFloat("elevator_bound_halt_decay_mod");
        
        this.ELEVATOR_FLOOR_BUFFER_PIXEL = file.getInt("elevator_floor_buffer_pixel");
        this.DOOR_X_PIXEL = file.getInt("door_x_pixel");
        this.PASSENGER_WIDTH_PIXEL = file.getInt("passenger_width_pixel");
        this.WAIT_X_OFFSET_PIXEL = file.getInt("wait_x_offset_pixel");
        this.RIDE_X_OFFSET_PIXEL = file.getInt("ride_x_offset_pixel");
        
        this.MAX_PASSENGERS_FLOOR = file.getInt("max_passengers_floor");
        this.MAX_PASSENGERS_ELEVATOR = file.getInt("max_passengers_elevator");
        int maxPossibleWorldPassengers =
                this.MAX_PASSENGERS_FLOOR*this.FLOOR_SPAWNS.size()+this.MAX_PASSENGERS_ELEVATOR;
        this.MAX_PASSENGERS_WORLD = Math.min(maxPossibleWorldPassengers,
                                             file.getInt("max_passengers_world"));
        this.SPAWN_OCCURRENCE_SEC = file.getFloat("spawn_occurrence_sec");
        this.SCENE_OCCURRENCE_SPAWN = file.getFloat("scene_occurrence_spawn");
        this.MIN_SPEED_PIXEL_SEC = file.getInt("min_speed_pixel_sec");
        this.MAX_SPEED_PIXEL_SEC = file.getInt("max_speed_pixel_sec");
        
        this.POSSIBLE_PASSENGER_COLORS = new Color[this.MAX_PASSENGERS_WORLD];
        JSONArray baseColor = file.getJSONArray("base_color");
        float h = baseColor.getFloat(0);
        float s = baseColor.getFloat(1);
        float v = baseColor.getFloat(2);
        int shift = RandomUtility.getRandomIntRange(0, MAX_PASSENGERS_WORLD-1);
        for (int i=0; i<MAX_PASSENGERS_WORLD; i++)
            this.POSSIBLE_PASSENGER_COLORS[(i+shift) % MAX_PASSENGERS_WORLD] =
                    hsvToRgb(h+((float)i)/((float)MAX_PASSENGERS_WORLD),s,v);
        
        this.HAPPINESS_DECAY_RATE_SEC = file.getFloat("happiness_decay_rate_sec");
        JSONObject happinessDecayMod = file.getJSONObject("happiness_decay_mod");
        this.HAPPINESS_DECAY_MOD = new HashMap<PassengerState,Float>();
        for (String stateKey : JSONObject.getNames(happinessDecayMod)) {
            float mod = happinessDecayMod.getFloat(stateKey);
            this.HAPPINESS_DECAY_MOD.put(PassengerState.getPassengerState(stateKey), mod);
        }
        this.DOOR_SLAM_PENALTY = file.getInt("door_slam_penalty");
        this.MAX_TIP_CENTS = file.getInt("max_tip_cents");
        this.BONUS_TIME = file.getInt("bonus_time");
        
        this.MAX_SCENES = file.getInt("max_scenes");
        
        Map<String,CastingDirection> castingDirectory = new HashMap<String,CastingDirection>();
        JSONObject castings = file.getJSONObject("castings");
        for (String castingKey : JSONObject.getNames(castings)) {
            JSONObject c = castings.getJSONObject(castingKey);
            JSONArray speedBound = c.getJSONArray("speed_bound");
            JSONArray patienceBound = c.getJSONArray("patience_bound");
            JSONArray generosityBound = c.getJSONArray("generosity_bound");
            JSONArray happinessBound = c.getJSONArray("happiness_bound");

            NullHandler<Number> nhMin = new NullHandler<Number>(Integer.MIN_VALUE);
            NullHandler<Number> nhMax = new NullHandler<Number>(Integer.MAX_VALUE);
            PassengerPersonality min = new PassengerPersonality(
                    (int)nhMin.handle(speedBound, 0),
                    nhMin.handle(patienceBound, 0).floatValue(),
                    nhMin.handle(generosityBound, 0).floatValue());
            PassengerPersonality max = new PassengerPersonality(
                    (int)nhMax.handle(speedBound, 1),
                    nhMax.handle(patienceBound, 1).floatValue(),
                    nhMax.handle(generosityBound, 1).floatValue());

            Pair<PassengerPersonality,PassengerPersonality> personalityBound =
                    new Pair<PassengerPersonality,PassengerPersonality>(min,max);
            Pair<Float,Float> hapinessBound =
                    new Pair<Float,Float>(
                            nhMin.handle(happinessBound, 0).floatValue(),
                            nhMin.handle(happinessBound, 1).floatValue());
            
            castingDirectory.put(castingKey, new CastingDirection(personalityBound,hapinessBound));
        }
        
        this.ALL_NORMAL_SCENES = new HashMap<SceneType,Map<CastingDirection,List<Scene>>>();
        JSONObject normalScenes = file.getJSONObject("normal_scenes");
        for (SceneType type : SceneType.values()) {
            String typeKey = type.getJSONKey();
            if (normalScenes.has(typeKey)) {
                JSONObject typedScenes = normalScenes.getJSONObject(typeKey);
                Map<CastingDirection,List<Scene>> castingSceneMap = new HashMap<CastingDirection,List<Scene>>();
                for (String castingKey : JSONObject.getNames(typedScenes)) {
                    JSONArray jsonSceneList = typedScenes.getJSONArray(castingKey);
                    List<Scene> sceneList = new ArrayList<Scene>();
                    for (Object jsonScene : jsonSceneList)
                        sceneList.add(parseScene((JSONObject)jsonScene));
                    castingSceneMap.put(castingDirectory.get(castingKey), sceneList);
                }
                this.ALL_NORMAL_SCENES.put(type,castingSceneMap);
            }
        }
        
        this.ALL_STAR_SCENES = new ArrayList<StarRole>();
        JSONObject starScenes = file.getJSONObject("star_scenes");
        for (String castingKey : JSONObject.getNames(starScenes)) {
            JSONObject castedRole = starScenes.getJSONObject(castingKey);
            Map<PassengerState,Scene> stateSceneMap = new HashMap<PassengerState,Scene>();
            for (PassengerState state : PassengerState.values()) {
                String stateKey = state.getJSONKey();
                if (castedRole.has(stateKey))
                    stateSceneMap.put(state, parseScene(castedRole.getJSONObject(stateKey)));
            }
            this.ALL_STAR_SCENES.add(new StarRole(stateSceneMap, castingDirectory.get(castingKey)));
        }
    }
    
    private Scene parseScene(JSONObject jObj) {
        AbstractLineTree scriptTree = parseLineTree(jObj.getJSONObject("script"), "start");
        StatementLineTree ejectTree = (StatementLineTree)parseLineTree(jObj, "eject");
        
        return new Scene(this, scriptTree, ejectTree);
    }
    
    private AbstractLineTree parseLineTree(JSONObject jObj, String lineName) {
        if (!jObj.has(lineName) || jObj.isNull(lineName))
            return null;

        JSONObject lineObj = jObj.getJSONObject(lineName);
        
        if (lineObj == JSONObject.NULL) {
            return null;
        } else {
            PortraitType portrait = PortraitType.getPortraitType(lineObj.getString("portrait"));
            if (lineObj.has("options")) {
                // OptionLineTree
                JSONArray jsonOptions = lineObj.getJSONArray("options");
                List<Option> options = new ArrayList<Option>();
                for (Object option : jsonOptions) {
                    JSONObject jOption = (JSONObject) option;
                    String line = jOption.getString("line");
                    OptionConsequence consequence = null;
                    if (!jOption.isNull("consequence")) {
                        JSONObject jsonConsequence = jOption.getJSONObject("consequence");
                        if (jsonConsequence != null) {
                            String consequenceType = jsonConsequence.getString("type");
                            String consequenceAttr = jsonConsequence.getString("attribute");
                            Float consequenceValue = jsonConsequence.getFloat("value");
                            switch (consequenceType) {
                            case "mod":
                                switch (consequenceAttr) {
                                case "happiness":
                                    consequence = passenger -> passenger.modHappiness(consequenceValue);
                                    break;
                                case "money":
                                    consequence = _p -> GameStateManager.getInstance().getElevator().giveTip(Math.round(consequenceValue));
                                    break;
                                }
                                break;
                            }
                        }
                    }
                    AbstractLineTree nextLine = null;
                    if (!jOption.isNull("next"))
                        nextLine = parseLineTree(jObj, jOption.getString("next"));
                    options.add(new Option(line, consequence, nextLine));
                }
                
                return new OptionLineTree(portrait, options);
            } else {
                // StatementLineTree
                //String sfxFname = lineObj.getString("sfx"); // not used right now
                String line = lineObj.getString("line");
                AbstractLineTree nextLine = null;
                if (!lineObj.isNull("next"))
                    nextLine = parseLineTree(jObj, lineObj.getString("next"));
                return new StatementLineTree(portrait, null, line, nextLine);
            }
        }
    }
    
    private static Color hsvToRgb (float h, float s, float v) {
        float r = 1f;
        float g = 1f;
        float b = 1f;
        
        int i = (int) Math.floor(h*6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        
        switch (i % 6) {
        case 0:
            r = v;
            g = t;
            b = p;
            break;
        case 1:
            r = q;
            g = v;
            b = p;
            break;
        case 2:
            r = p;
            g = v;
            b = t;
            break;
        case 3:
            r = p;
            g = q;
            b = v;
            break;
        case 4:
            r = t;
            g = p;
            b = v;
            break;
        case 5:
            r = v;
            g = p; 
            b = q;
            break;
        }
        
        return new Color(r, g, b, 1f);
    }
}
