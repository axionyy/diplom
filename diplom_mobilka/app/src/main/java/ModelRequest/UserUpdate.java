package ModelRequest;

import com.google.gson.annotations.SerializedName;

public class UserUpdate {
    @SerializedName("login")
    private String login;

    @SerializedName("name")
    private String name;

    @SerializedName("surname")
    private String surname;

    @SerializedName("height")
    private Float height;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("password")
    private String password;

    @SerializedName("weight")
    private Float weight;

    public UserUpdate(String login, String name, String surname, Float height, String birthday, String password) {
        this.login = login;
        this.name = name;
        this.surname = surname;
        this.height = height;
        this.birthday = birthday;
        this.password = password;
    }

    // Новый конструктор с weight
    public UserUpdate(Float weight) {
        this.weight = weight;
    }

    // Геттеры
    public String getLogin() { return login; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public Float getHeight() { return height; }
    public String getBirthday() { return birthday; }
    public String getPassword() { return password; }
    public Float getWeight() { return weight; }
    public void setWeight(Float weight) { this.weight = weight; }
}