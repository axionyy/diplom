package ModelRequest;

import java.util.List;

public class UserRegister {
    private String login;
    private String password;
    private String name;
    private String surname;
    private Float height;
    private Float weight;
    private Boolean gender;
    private String birthday;


    public UserRegister(String login, String password, String name, String surname,
                        Float height, Float weight, Boolean gender, String birthday) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.birthday = birthday;

    }

    // Геттеры и сеттеры
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

}
