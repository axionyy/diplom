package ModelRequest;

import com.google.gson.annotations.SerializedName;

public class VerifyPasswordResponse {
    @SerializedName("is_valid")
    private boolean isValid;

    public boolean isValid() {
        return isValid;
    }
}