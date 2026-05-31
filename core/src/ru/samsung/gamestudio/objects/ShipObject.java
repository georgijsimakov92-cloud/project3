package simakov.licey97.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import simakov.licey97.GameSettings;

public class ShipObject extends GameObject {

    private long lastShotTime;

    private int livesLeft;

    private boolean shieldActive;
    private boolean doubleShotActive;

    private long shieldEndTime;
    private long doubleShotEndTime;
    private long rapidFireEndTime;

    public ShipObject(
            int x,
            int y,
            int width,
            int height,
            String texturePath,
            World world
    ) {
        super(
                texturePath,
                x,
                y,
                width,
                height,
                GameSettings.SHIP_BIT,
                world
        );

        body.setLinearDamping(10);

        livesLeft = 3;

        shieldActive = false;
        doubleShotActive = false;

        shieldEndTime = 0;
        doubleShotEndTime = 0;
        rapidFireEndTime = 0;
    }

    public int getLiveLeft() {
        return livesLeft;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public boolean isDoubleShotActive() {
        return doubleShotActive;
    }

    public boolean isRapidFireActive() {
        return TimeUtils.millis() < rapidFireEndTime;
    }

    public void applyLifeBonus() {
        livesLeft = Math.min(3, livesLeft + 1);
    }

    public void activateShield() {
        shieldActive = true;
        shieldEndTime = TimeUtils.millis() + 10000;
    }

    public void activateDoubleShot() {
        doubleShotActive = true;
        doubleShotEndTime = TimeUtils.millis() + 10000;
    }

    public void activateRapidFire() {
        rapidFireEndTime = TimeUtils.millis() + 10000;
    }

    public void updateBonuses() {

        long now = TimeUtils.millis();

        if (shieldActive && now > shieldEndTime) {
            shieldActive = false;
        }

        if (doubleShotActive && now > doubleShotEndTime) {
            doubleShotActive = false;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        updateBonuses();
        putInFrame();
        super.draw(batch);
    }

    public void move(Vector3 vector3) {

        body.applyForceToCenter(
                new Vector2(
                        (vector3.x - getX())
                                * GameSettings.SHIP_FORCE_RATIO,

                        (vector3.y - getY())
                                * GameSettings.SHIP_FORCE_RATIO
                ),
                true
        );
    }

    private void putInFrame() {

        if (getY() > (GameSettings.SCREEN_HEIGHT / 2f - height / 2f)) {
            setY((int)(GameSettings.SCREEN_HEIGHT / 2f - height / 2f));
        }

        if (getY() <= (height / 2f)) {
            setY(height / 2);
        }

        if (getX() < (-width / 2f)) {
            setX(GameSettings.SCREEN_WIDTH);
        }

        if (getX() > (GameSettings.SCREEN_WIDTH + width / 2f)) {
            setX(0);
        }
    }

    public boolean needToShoot() {

        int cooldown;

        if (isRapidFireActive()) {
            cooldown = 300;
        } else {
            cooldown = GameSettings.SHOOTING_COOL_DOWN;
        }

        if (TimeUtils.millis() - lastShotTime >= cooldown) {

            lastShotTime = TimeUtils.millis();
            return true;
        }

        return false;
    }

    @Override
    public void hit() {

        if (shieldActive) {
            return;
        }

        livesLeft--;
    }

    public boolean isAlive() {
        return livesLeft > 0;
    }
}
