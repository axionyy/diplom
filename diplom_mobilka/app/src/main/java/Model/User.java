package Model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("surname")
    public String surname;

    @SerializedName("height")
    public Float height;

    @SerializedName("weight")
    public Float weight;

    @SerializedName("gender")
    public Boolean gender;

    @SerializedName("birthday")
    public String birthday;

    @SerializedName("login")
    public String login;

    @SerializedName("password")
    public String password;
}