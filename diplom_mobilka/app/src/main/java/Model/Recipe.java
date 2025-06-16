package Model;

import com.google.gson.annotations.SerializedName;

public class Recipe {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("photo")
    public String photo;

    @SerializedName("callories")
    public float calories;

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

    @SerializedName("dateCreate")
    public String dateCreate;

}