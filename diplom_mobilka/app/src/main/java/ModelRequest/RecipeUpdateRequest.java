package ModelRequest;

import com.google.gson.annotations.SerializedName;

public class RecipeUpdateRequest {
    @SerializedName("name")
    public String name;

    @SerializedName("callories")
    public float calories;

    @SerializedName("photo")
    public String photo;

    @SerializedName("components")
    public String components;

    @SerializedName("steps")
    public String steps;

    @SerializedName("squirrels")
    public float proteins;

    @SerializedName("fats")
    public float fats;

    @SerializedName("carbohydrates")
    public float carbs;

    public RecipeUpdateRequest(String name, float calories, String photo,
                               String components, String steps,
                               float proteins, float fats, float carbs) {
        this.name = name;
        this.calories = calories;
        this.photo = photo;
        this.components = components;
        this.steps = steps;
        this.proteins = proteins;
        this.fats = fats;
        this.carbs = carbs;
    }
}