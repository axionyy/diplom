package Interface;

import java.util.List;
import java.util.Map;

import Model.EatingRecord;
import Model.FoodItem;
import Model.Recipe;
import Model.User;
import Model.WeightRecord;
import ModelRequest.RecipeCreateRequest;
import ModelRequest.RecipeUpdateRequest;
import ModelRequest.UserLogin;
import ModelRequest.UserRegister;
import ModelRequest.UserUpdate;
import ModelRequest.VerifyPasswordRequest;
import ModelRequest.VerifyPasswordResponse;
import ModelRequest.WeightRecordRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @POST("users/{userId}/weight-history")
    Call<WeightRecord> createWeightRecord(
            @Path("userId") int userId,
            @Body WeightRecordRequest request
    );

    @GET("users/{userId}/weight-history")
    Call<List<WeightRecord>> getWeightHistory(@Path("userId") int userId);

    @DELETE("weight-records/{recordId}")
    Call<Void> deleteWeightRecord(@Path("recordId") int recordId);

    @POST("users/{userId}/recipes")
    Call<Void> createRecipe(@Path("userId") int userId, @Body RecipeCreateRequest request);

    @GET("users/{userId}/recipes")
    Call<List<Recipe>> getUserRecipes(@Path("userId") int userId);

    @PUT("recipes/{recipeId}")
    Call<Void> updateRecipe(@Path("recipeId") int recipeId, @Body RecipeUpdateRequest request);

    @DELETE("recipes/{recipeId}")
    Call<Void> deleteRecipe(@Path("recipeId") int recipeId);

    @POST("eating-records")
    Call<EatingRecord> createEatingRecord(@Body EatingRecord record);

    @GET("users/{userId}/eating-records")
    Call<List<EatingRecord>> getEatingRecords(@Path("userId") int userId,
                                              @Query("date") String date);

    @POST("food-items")
    Call<FoodItem> createFoodItem(@Body FoodItem foodItem);

    @GET("food-items/search")
    Call<List<FoodItem>> searchFoodItems(@Query("query") String query);

    @DELETE("eating-records/{recordId}")
    Call<Void> deleteEatingRecord(@Path("recordId") int recordId);

    @GET("check-login/{login}")
    Call<Map<String, Boolean>> checkLoginAvailability(@Path("login") String login);

    @GET("recipes")
    Call<List<Recipe>> getAllRecipes();

}