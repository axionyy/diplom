package Interface;

import Model.User;
import ModelRequest.UserLogin;
import ModelRequest.UserRegister;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IUser {
    @POST("login")
    Call<User> loginUser(@Body UserLogin userLogin);

    @POST("register")
    Call<User> registerUser(@Body UserRegister userRegister);

    @GET("users/{userId}")
    Call<User> getUser(@Path("userId") int userId);
}
