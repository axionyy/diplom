package Model;

import com.google.gson.annotations.SerializedName;

public class FoodItem {
    @SerializedName("id")
    private int id;

    @SerializedName("nameFood")
    private String nameFood;

    @SerializedName("callories")
    private float callories;

    @SerializedName("proteins")
    private float proteins;

    @SerializedName("fats")
    private float fats;

    @SerializedName("carbohydrates")
    private float carbohydrates;


    // Конструктор по умолчанию
    public FoodItem() {
    }
    // Существующий конструктор с параметрами
    public FoodItem(String nameFood, float callories, float proteins, float fats, float carbohydrates) {
        this.nameFood = nameFood;
        this.callories = callories;
        this.proteins = proteins;
        this.fats = fats;
        this.carbohydrates = carbohydrates;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public String getNameFood() { return nameFood; }
    public float getCallories() { return callories; }
    public float getProteins() { return proteins; }
    public float getFats() { return fats; }
    public float getCarbohydrates() { return carbohydrates; }

    public void setNameFood(String nameFood) { this.nameFood = nameFood; }
    public void setCallories(float callories) { this.callories = callories; }
    public void setProteins(float proteins) { this.proteins = proteins; }
    public void setFats(float fats) { this.fats = fats; }
    public void setCarbohydrates(float carbohydrates) { this.carbohydrates = carbohydrates; }
}