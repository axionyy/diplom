package Interface;

import Model.User;
import ModelRequest.UserLogin;
import ModelRequest.UserRegister;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IUser {
    @POST("login")
    Call<User> loginUser(@Body UserLogin userLogin);

    @POST("register")
    Call<User> registerUser(@Body UserRegister userRegister);
}
