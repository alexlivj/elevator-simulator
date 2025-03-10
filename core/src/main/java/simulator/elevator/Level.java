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
import simulator.elevator.game.manager.PassengerCoordinator;
import simulator.elevator.game.scene.CastingDirection;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.game.scene.script.Option;
import simulator.elevator.game.scene.script.OptionLineTree;
import simulator.elevator.game.scene.script.PortraitType;
import simulator.elevator.game.scene.script.Scene;
import simulator.elevator.game.scene.script.SceneType;
import simulator.elevator.game.scene.script.StatementLineTree;
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
    public final Vector2 SLIDER_CENTER_PIXEL;
    public final Vector2 BUTTON_CENTER_PIXEL;
    
    public final Texture FLOOR_TEXTURE;
    public final Texture STATIC_UI_TEXTURE;
    public final Texture OPTION_BOX_TEXTURE;
    public final Texture BUTTON_TEXTURE;
    public final Texture SLIDER_TEXTURE;
    public final Texture ELEVATOR_OPEN_TEXTURE;
    public final Texture ELEVATOR_CLOSED_TEXTURE;
    public final List<Texture> PASSENGER_TEXTURES;
    
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
    public final float[] HAPPINESS_DECAY_MOD = new float[6];
    public final float DOOR_SLAM_PENALTY;
    public final int MAX_TIP_CENTS;
    
    public final Map<SceneType,Map<CastingDirection,List<Scene>>> ALL_NORMAL_SCENES;
    public final List<StarRole> ALL_STAR_SCENES;
    public final int MAX_SCENES;
    
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
        JSONArray sliderCenter = file.getJSONArray("slider_center_pixel");
        this.SLIDER_CENTER_PIXEL = new Vector2(sliderCenter.getFloat(0),sliderCenter.getFloat(1));
        JSONArray buttonCenter = file.getJSONArray("button_center_pixel");
        this.BUTTON_CENTER_PIXEL = new Vector2(buttonCenter.getFloat(0),buttonCenter.getFloat(1));

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
        
        this.MAX_PASSENGERS_WORLD = file.getInt("max_passengers_world");
        this.MAX_PASSENGERS_FLOOR = file.getInt("max_passengers_floor");
        this.MAX_PASSENGERS_ELEVATOR = file.getInt("max_passengers_elevator");
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
        
        this.HAPPINESS_DECAY_RATE_SEC = file.getInt("happiness_decay_rate_sec");
        JSONArray happinessDecayMod = file.getJSONArray("happiness_decay_mod");
        for (int i=0; i<this.HAPPINESS_DECAY_MOD.length; i++)
            this.HAPPINESS_DECAY_MOD[i] = happinessDecayMod.getFloat(i);
        this.DOOR_SLAM_PENALTY = file.getInt("door_slam_penalty");
        this.MAX_TIP_CENTS = file.getInt("max_tip_cents");
        
        this.MAX_SCENES = file.getInt("max_scenes");
        
        Map<String,CastingDirection> castingDirectory = new HashMap<String,CastingDirection>();
        JSONObject castings = file.getJSONObject("castings");
        for (String t : JSONObject.getNames(castings)) {
            JSONObject c = castings.getJSONObject(t);
            JSONArray speedBound = c.getJSONArray("speed_bound");
            JSONArray patienceBound = c.getJSONArray("patience_bound");
            JSONArray generosityBound = c.getJSONArray("generosity_bound");
            JSONArray happinessBound = c.getJSONArray("happiness_bound");

            class UnNuller {
                final Number defaultValue;
                UnNuller(Number def) {
                    this.defaultValue = def;
                }
                Number unNull(Object val) {
                    if (val == null || !(val instanceof Number))
                        return this.defaultValue;
                    else
                        return (Number)val;
                }
            }
            UnNuller unMin = new UnNuller(Integer.MIN_VALUE); //WARNING does this convert properly?
            UnNuller unMax = new UnNuller(Integer.MAX_VALUE);
            PassengerPersonality min = new PassengerPersonality(
                    (int)unMin.unNull(speedBound.get(0)),
                    unMin.unNull(patienceBound.get(0)).floatValue(),
                    unMin.unNull(generosityBound.get(0)).floatValue());
            PassengerPersonality max = new PassengerPersonality(
                    (int)unMax.unNull(speedBound.get(1)),
                    unMax.unNull(patienceBound.get(1)).floatValue(),
                    unMax.unNull(generosityBound.get(1)).floatValue());

            Pair<PassengerPersonality,PassengerPersonality> personalityBound =
                    new Pair<PassengerPersonality,PassengerPersonality>(min,max);
            Pair<Float,Float> hapinessBound =
                    new Pair<Float,Float>(
                            unMin.unNull(happinessBound.get(0)).floatValue(),
                            unMin.unNull(happinessBound.get(1)).floatValue());
            
            castingDirectory.put(text, new CastingDirection(personalityBound,hapinessBound));
        }
        
        this.ALL_NORMAL_SCENES = new HashMap<SceneType,Map<CastingDirection,List<Scene>>>();
        //TODO
        
        this.ALL_STAR_SCENES = new ArrayList<StarRole>();
        //TODO
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
