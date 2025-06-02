package ModelRequest;

import com.google.gson.annotations.SerializedName;

public class WeightRecordRequest {
    @SerializedName("date")
    private String date;

    @SerializedName("weight")
    private float weight;

    public WeightRecordRequest(String date, float weight) {
        this.date = date;
        this.weight = weight;
    }

    // Геттеры
    public String getDate() { return date; }
    public float getWeight() { return weight; }
}