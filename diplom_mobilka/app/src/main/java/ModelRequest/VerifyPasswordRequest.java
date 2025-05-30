package ModelRequest;

import com.google.gson.annotations.SerializedName;

public class VerifyPasswordRequest {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("password")
    private String password;

    public VerifyPasswordRequest(int userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}