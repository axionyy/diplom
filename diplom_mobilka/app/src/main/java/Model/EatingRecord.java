package Model;

import com.google.gson.annotations.SerializedName;

public class EatingRecord {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("food_id")
    private int foodId;

    @SerializedName("date")
    private String date;

    @SerializedName("meal_type")
    private String mealType;

    @SerializedName("quantity")
    private float quantity;

    @SerializedName("callories")
    private float callories;

    @SerializedName("squirrels")
    private float proteins; // squirrels в БД соответствует proteins

    @SerializedName("fats")
    private float fats;

    @SerializedName("carbohydrates")
    private float carbohydrates;

    @SerializedName("food_name")
    private String foodName;

    public EatingRecord(int userId, int foodId, String date, String mealType, float quantity) {
        this.userId = userId;
        this.foodId = foodId;
        this.date = date;
        this.mealType = mealType;
        this.quantity = quantity;
    }

    // Геттеры
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getFoodId() { return foodId; }
    public String getDate() { return date; }
    public String getMealType() { return mealType; }
    public float getQuantity() { return quantity; }
    public float getCallories() { return callories; }
    public float getProteins() { return proteins; }
    public float getFats() { return fats; }
    public float getCarbohydrates() { return carbohydrates; }
    public String getFoodName() { return foodName; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }
    public void setDate(String date) { this.date = date; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public void setQuantity(float quantity) { this.quantity = quantity; }
    public void setCallories(float callories) { this.callories = callories; }
    public void setProteins(float proteins) { this.proteins = proteins; }
    public void setFats(float fats) { this.fats = fats; }
    public void setCarbohydrates(float carbohydrates) { this.carbohydrates = carbohydrates; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    // Дополнительные методы для удобства
    public String getFormattedCalories() {
        return String.format("%.1f ккал", callories);
    }

    public String getFormattedQuantity() {
        return String.format("%.1f г", quantity);
    }

    public String getFormattedProteins() {
        return String.format("%.1f г", proteins);
    }

    public String getFormattedFats() {
        return String.format("%.1f г", fats);
    }

    public String getFormattedCarbs() {
        return String.format("%.1f г", carbohydrates);
    }

}