package dk.noxitech.awards.models;

public class AwardProgress {
    private boolean isUnlocked;
    private int timeLeft;

    public AwardProgress() {
        this.isUnlocked = false;
        this.timeLeft = 0;
    }

    public AwardProgress(boolean isUnlocked, int timeLeft) {
        this.isUnlocked = isUnlocked;
        this.timeLeft = timeLeft;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public void updateTimeLeft(int currentPlaytime, int requiredTime) {
        this.timeLeft = Math.max(0, requiredTime - currentPlaytime);
    }
}