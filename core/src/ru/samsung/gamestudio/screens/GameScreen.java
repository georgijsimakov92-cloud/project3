package simakov.licey97.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import simakov.licey97.*;
import simakov.licey97.components.*;
import simakov.licey97.managers.ContactManager;
import simakov.licey97.managers.MemoryManager;
import simakov.licey97.objects.BulletObject;
import simakov.licey97.objects.ShipObject;
import simakov.licey97.objects.TrashObject;

import java.util.ArrayList;

public class GameScreen extends ScreenAdapter {

    MyGdxGame myGdxGame;
    GameSession gameSession;
    ShipObject shipObject;

    ArrayList<TrashObject> trashArray;
    ArrayList<BulletObject> bulletArray;

    ContactManager contactManager;

    MovingBackgroundView backgroundView;
    ImageView topBlackoutView;
    LiveView liveView;
    TextView scoreTextView;
    TextView levelTextView;
    ButtonView pauseButton;

    ImageView fullBlackoutView;
    TextView pauseTextView;
    ButtonView homeButton;
    ButtonView continueButton;

    TextView recordsTextView;
    RecordsListView recordsListView;
    ButtonView homeButton2;

    // ===== STATISTICS UI =====
    TextView statsTextView;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;

        gameSession = new GameSession();
        contactManager = new ContactManager(myGdxGame.world);

        trashArray = new ArrayList<>();
        bulletArray = new ArrayList<>();

        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2,
                150,
                GameSettings.SHIP_WIDTH,
                GameSettings.SHIP_HEIGHT,
                GameResources.SHIP_IMG_PATH,
                myGdxGame.world
        );

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);

        topBlackoutView = new ImageView(0, 1180, GameResources.BLACKOUT_TOP_IMG_PATH);

        liveView = new LiveView(305, 1215);

        scoreTextView = new TextView(myGdxGame.commonWhiteFont, 50, 1215);

        levelTextView = new TextView(myGdxGame.commonWhiteFont, 470, 1215, "Level: 1");

        pauseButton = new ButtonView(
                605, 1200,
                46, 54,
                GameResources.PAUSE_IMG_PATH
        );

        fullBlackoutView = new ImageView(0, 0, GameResources.BLACKOUT_FULL_IMG_PATH);

        pauseTextView = new TextView(myGdxGame.largeWhiteFont, 282, 842, "Pause");

        homeButton = new ButtonView(
                138, 695,
                200, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Home"
        );

        continueButton = new ButtonView(
                393, 695,
                200, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Continue"
        );

        recordsListView = new RecordsListView(myGdxGame.commonWhiteFont, 690);

        recordsTextView = new TextView(myGdxGame.largeWhiteFont, 206, 842, "Last records");

        homeButton2 = new ButtonView(
                280, 365,
                160, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Home"
        );

        statsTextView = new TextView(myGdxGame.commonWhiteFont, 180, 760, "");
    }

    @Override
    public void show() {
        restartGame();
    }

    @Override
    public void render(float delta) {

        handleInput();

        if (gameSession.state == GameState.PLAYING) {

            // ===== SPAWN =====
            if (gameSession.shouldSpawnTrash()) {

                float velocity =
                        GameSettings.TRASH_VELOCITY +
                                gameSession.getDifficultyLevel() * 2f;

                TrashObject trashObject = new TrashObject(
                        GameSettings.TRASH_WIDTH,
                        GameSettings.TRASH_HEIGHT,
                        GameResources.TRASH_IMG_PATH,
                        myGdxGame.world,
                        velocity
                );

                trashArray.add(trashObject);
            }

            // ===== SHOOT =====
            if (shipObject.needToShoot()) {

                BulletObject bullet = new BulletObject(
                        shipObject.getX(),
                        shipObject.getY() + shipObject.height / 2,
                        GameSettings.BULLET_WIDTH,
                        GameSettings.BULLET_HEIGHT,
                        GameResources.BULLET_IMG_PATH,
                        myGdxGame.world
                );

                bulletArray.add(bullet);
                gameSession.registerShot();

                if (myGdxGame.audioManager.isSoundOn)
                    myGdxGame.audioManager.shootSound.play();
            }

            // ===== GAME OVER =====
            if (!shipObject.isAlive()) {
                gameSession.endGame();

                recordsListView.setRecords(MemoryManager.loadRecordsTable());

                statsTextView.setText(
                        "Time: " + gameSession.getSurvivalTime() / 1000 + "s\n" +
                                "Accuracy: " + String.format("%.1f", gameSession.getAccuracy()) + "%\n" +
                                "Destroyed: " + gameSession.getHits()
                );
            }

            updateTrash();
            updateBullets();

            backgroundView.move();

            gameSession.updateScore();

            scoreTextView.setText("Score: " + gameSession.getScore());

            levelTextView.setText("Level: " + gameSession.getDifficultyLevel());

            liveView.setLeftLives(shipObject.getLiveLeft());

            myGdxGame.stepWorld();
        }

        draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            myGdxGame.touch = myGdxGame.camera.unproject(
                    new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)
            );

            switch (gameSession.state) {

                case PLAYING:
                    if (pauseButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        gameSession.pauseGame();
                    }
                    shipObject.move(myGdxGame.touch);
                    break;

                case PAUSED:
                    if (continueButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        gameSession.resumeGame();
                    }
                    if (homeButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        myGdxGame.setScreen(myGdxGame.menuScreen);
                    }
                    break;

                case ENDED:
                    if (homeButton2.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        myGdxGame.setScreen(myGdxGame.menuScreen);
                    }
                    break;
            }
        }
    }

    private void draw() {

        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);
        ScreenUtils.clear(Color.CLEAR);

        myGdxGame.batch.begin();

        backgroundView.draw(myGdxGame.batch);

        for (TrashObject t : trashArray) t.draw(myGdxGame.batch);
        shipObject.draw(myGdxGame.batch);
        for (BulletObject b : bulletArray) b.draw(myGdxGame.batch);

        topBlackoutView.draw(myGdxGame.batch);
        scoreTextView.draw(myGdxGame.batch);
        levelTextView.draw(myGdxGame.batch);
        liveView.draw(myGdxGame.batch);
        pauseButton.draw(myGdxGame.batch);

        if (gameSession.state == GameState.PAUSED) {

            fullBlackoutView.draw(myGdxGame.batch);
            pauseTextView.draw(myGdxGame.batch);
            homeButton.draw(myGdxGame.batch);
            continueButton.draw(myGdxGame.batch);

        } else if (gameSession.state == GameState.ENDED) {

            fullBlackoutView.draw(myGdxGame.batch);
            recordsTextView.draw(myGdxGame.batch);
            recordsListView.draw(myGdxGame.batch);
            statsTextView.draw(myGdxGame.batch);
            homeButton2.draw(myGdxGame.batch);
        }

        myGdxGame.batch.end();
    }

    private void updateTrash() {

        for (int i = 0; i < trashArray.size(); i++) {

            TrashObject t = trashArray.get(i);

            boolean destroy = !t.isAlive() || !t.isInFrame();

            if (!t.isAlive()) {
                gameSession.destructionRegistration();

                gameSession.registerHit();

                if (myGdxGame.audioManager.isSoundOn)
                    myGdxGame.audioManager.explosionSound.play(0.2f);
            }

            if (destroy) {
                myGdxGame.world.destroyBody(t.body);
                trashArray.remove(i--);
            }
        }
    }

    private void updateBullets() {

        for (int i = 0; i < bulletArray.size(); i++) {

            BulletObject b = bulletArray.get(i);

            if (b.hasToBeDestroyed()) {
                myGdxGame.world.destroyBody(b.body);
                bulletArray.remove(i--);
            }
        }
    }

    private void restartGame() {

        for (TrashObject t : trashArray) {
            myGdxGame.world.destroyBody(t.body);
        }

        trashArray.clear();

        if (shipObject != null) {
            myGdxGame.world.destroyBody(shipObject.body);
        }

        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2,
                150,
                GameSettings.SHIP_WIDTH,
                GameSettings.SHIP_HEIGHT,
                GameResources.SHIP_IMG_PATH,
                myGdxGame.world
        );

        bulletArray.clear();
        gameSession.startGame();
    }
}
