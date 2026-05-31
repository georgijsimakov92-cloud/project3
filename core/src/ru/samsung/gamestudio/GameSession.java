package simakov.licey97;

import com.badlogic.gdx.utils.TimeUtils;
import simakov.licey97.managers.MemoryManager;

import java.util.ArrayList;

public class GameSession {

    public GameState state;

    private int score;
    private int destructedTrashNumber;

    private long sessionStartTime;
    private long pauseStartTime;
    private long survivalTime;

    private long nextTrashSpawnTime;

    private int shotsFired;
    private int hits;

    private int difficultyLevel;

    public void startGame() {
        state = GameState.PLAYING;

        score = 0;
        destructedTrashNumber = 0;

        shotsFired = 0;
        hits = 0;

        difficultyLevel = 1;

        sessionStartTime = TimeUtils.millis();

        nextTrashSpawnTime = sessionStartTime +
                GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN;
    }

    public void pauseGame() {
        state = GameState.PAUSED;
        pauseStartTime = TimeUtils.millis();
    }

    public void resumeGame() {
        state = GameState.PLAYING;
        sessionStartTime += TimeUtils.millis() - pauseStartTime;
    }

    public void endGame() {
        updateScore();

        survivalTime = TimeUtils.millis() - sessionStartTime;

        state = GameState.ENDED;

        ArrayList<Integer> recordsTable = MemoryManager.loadRecordsTable();
        if (recordsTable == null) recordsTable = new ArrayList<>();

        int idx = 0;
        for (; idx < recordsTable.size(); idx++) {
            if (recordsTable.get(idx) < getScore()) break;
        }

        recordsTable.add(idx, getScore());
        MemoryManager.saveTableOfRecords(recordsTable);
    }

    // ===== STATISTICS =====

    public void registerShot() {
        shotsFired++;
    }

    public void registerHit() {
        hits++;
    }

    public float getAccuracy() {
        if (shotsFired == 0) return 0;
        return (float) hits / shotsFired * 100f;
    }

    public long getSurvivalTime() {
        if (state == GameState.ENDED) return survivalTime;
        return TimeUtils.millis() - sessionStartTime;
    }

    public int getHits() {
        return hits;
    }

    public int getShotsFired() {
        return shotsFired;
    }

    // ===== SCORE =====

    public void destructionRegistration() {
        destructedTrashNumber++;
    }

    public void updateScore() {
        score =
                (int) (TimeUtils.millis() - sessionStartTime) / 100
                        + destructedTrashNumber * 100;
    }

    public int getScore() {
        return score;
    }

    // ===== DIFFICULTY =====

    public int getDifficultyLevel() {
        difficultyLevel = 1 + (int) ((TimeUtils.millis() - sessionStartTime) / 15000);
        return difficultyLevel;
    }

    // ===== SPAWN =====

    public boolean shouldSpawnTrash() {
        if (nextTrashSpawnTime <= TimeUtils.millis()) {
            nextTrashSpawnTime = TimeUtils.millis()
                    + GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN;
            return true;
        }
        return false;
    }
}
