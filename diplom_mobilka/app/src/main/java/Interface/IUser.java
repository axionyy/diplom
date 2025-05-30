package Interface;

import Model.User;
import ModelRequest.UserLogin;
import ModelRequest.UserRegister;
import ModelRequest.UserUpdate;
import ModelRequest.VerifyPasswordRequest;
import ModelRequest.VerifyPasswordResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface IUser {
    @POST("login")
    Call<User> loginUser(@Body UserLogin userLogin);

    @POST("register")
    Call<User> registerUser(@Body UserRegister userRegister);

    @GET("users/{userId}")
    Call<User> getUser(@Path("userId") int userId);

    @PUT("users/{userId}")
    Call<User> updateUser(@Path("userId") int userId, @Body UserUpdate userUpdate);

    @POST("users/verify-password")
    Call<VerifyPasswordResponse> verifyPassword(@Body VerifyPasswordRequest request);
}

