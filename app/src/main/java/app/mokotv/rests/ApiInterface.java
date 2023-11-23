package app.mokotv.rests;

import app.mokotv.Config;
import app.mokotv.callbacks.CallbackCategories;
import app.mokotv.callbacks.CallbackChannel;
import app.mokotv.callbacks.CallbackDetailCategory;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0, must-revalidate, no-cache";
    String AGENT = "Data-Agent: Moko TV";

    @Headers({CACHE, AGENT})
    @GET("api/get_posts/?api_key=" + Config.API_KEY)
    Call<CallbackChannel> getPostByPage(@Query("page") int page, @Query("count") int count);

    @Headers({CACHE, AGENT})
    @GET("api/get_category_index/?api_key=" + Config.API_KEY)
    Call<CallbackCategories> getAllCategories();

    @Headers({CACHE, AGENT})
    @GET("api/get_category_posts/?api_key=" + Config.API_KEY)
    Call<CallbackDetailCategory> getCategoryDetailsByPage(@Query("id") int id, @Query("page") int page, @Query("count") int count);

    @Headers({CACHE, AGENT})
    @GET("api/get_search_results/?api_key=" + Config.API_KEY)
    Call<CallbackChannel> getSearchPosts(@Query("search") String search, @Query("count") int count);
}