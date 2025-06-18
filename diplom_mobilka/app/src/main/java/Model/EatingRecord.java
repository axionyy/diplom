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

    public EatingRecord(int userId, int foodId, String date, String mealType, float quantity) {
        this.userId = userId;
        this.foodId = foodId;
        this.date = date;
        this.mealType = mealType;
        this.quantity = quantity;
    }

    // Геттеры

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public int getFoodId() { return foodId; }
    public String getDate() { return date; }
    public String getMealType() { return mealType; }
    public float getQuantity() { return quantity; }

    // Сеттер для даты
    public void setDate(String date) { this.date = date; }
}