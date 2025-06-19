package Model;

public class Achievement {
    private String userId;
    private String id;
    private String title;
    private String description;
    private boolean unlocked;
    private int progress;
    private int target;
    private int imageResId;

    public Achievement(String id, String title, String description,
                       int target, int imageResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.target = target;
        this.imageResId = imageResId;
        this.unlocked = false;
        this.progress = 0;
    }

    public String getUserId() {
        return userId;
    }
    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public boolean isUnlocked() {
        return unlocked;
    }
    public int getProgress() {
        return progress;
    }
    public int getTarget() {
        return target;
    }
    public int getImageResId() {
        return imageResId;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }
    public void setTarget(int target) {
        this.target = target;
    }
    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
    public void unlock() {
        this.unlocked = true;
    }
    public void addProgress(int amount) {
        this.progress += amount;
        if (this.progress >= this.target) {
            unlock();
        }
    }
}