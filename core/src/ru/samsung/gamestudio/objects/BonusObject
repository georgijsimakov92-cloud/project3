package simakov.licey97.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import simakov.licey97.GameSettings;

import java.util.Random;

public class BonusObject extends GameObject {

    public enum Type {
        LIFE,
        RAPID_FIRE,
        DOUBLE_SHOT,
        SHIELD
    }

    private final Type type;

    public BonusObject(
            int width,
            int height,
            String texturePath,
            World world,
            Type type
    ) {

        super(
                texturePath,
                new Random().nextInt(
                        GameSettings.SCREEN_WIDTH - width
                ) + width / 2,
                GameSettings.SCREEN_HEIGHT + height,
                width,
                height,
                GameSettings.TRASH_BIT,
                world
        );

        this.type = type;

        body.setLinearVelocity(
                new Vector2(0, -GameSettings.TRASH_VELOCITY)
        );
    }

    public Type getType() {
        return type;
    }

    public boolean isInFrame() {
        return getY() + height / 2 > 0;
    }
}
