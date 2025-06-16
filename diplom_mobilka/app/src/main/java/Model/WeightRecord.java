package Model;

import com.google.gson.annotations.SerializedName;

public class WeightRecord {
    @SerializedName("id")
    private int id;

    @SerializedName("date")
    private String date;

    @SerializedName("weight")
    private float weight;

    // Конструктор без ID (для новых записей)
    public WeightRecord(String date, float weight) {
        this.date = date;
        this.weight = weight;
    }

    // Конструктор с ID (для записей с сервера)
    public WeightRecord(int id, String date, float weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    // Геттеры
    public int getId() { return id; }
    public String getDate() { return date; }
    public float getWeight() { return weight; }
}