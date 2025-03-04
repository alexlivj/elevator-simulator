package simulator.elevator.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TextureUtility {
    
    public static Texture doubleTextureSize(String img) {
        Pixmap pm = new Pixmap(Gdx.files.internal(img));
        Pixmap pmdoubled = new Pixmap(pm.getWidth()*2, pm.getHeight()*2, pm.getFormat());
        pmdoubled.drawPixmap(pm,
                0, 0, pm.getWidth(), pm.getHeight(),
                0, 0, pmdoubled.getWidth(), pmdoubled.getHeight()
        );
        Texture texture = new Texture(pmdoubled);
        pm.dispose();
        pmdoubled.dispose();
        
        return texture;
    }
    
}
