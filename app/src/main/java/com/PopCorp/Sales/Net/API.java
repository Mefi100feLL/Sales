package com.PopCorp.Sales.Net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.Path;

public interface API {

    /////////////////////////////// Profile
    @FormUrlEncoded
    @POST("/account/loginProcess/")
    void signIn(@Field("email") String email, @Field("password1") String password, @Field("rememberme") String remember, Callback<Response> cb);

    @FormUrlEncoded
    @POST("/account/register/")
    void signUp(@Field("username") String username, @Field("email") String email, @Field("password1") String password1, @Field("password2") String password2, Callback<Response> cb);

    @FormUrlEncoded
    @POST("/account/resetpassword/")
    void resetPassword(@Field("email") String email, Callback<Response> cb);


    /////////////////////////////// Notes
    @FormUrlEncoded
    @POST("/mynotes/addProcess?is_ajax=1")
    void addToNotes(@Field("product_id") String productId, @Field("user_id") String userId, Callback<Response> cb);

    @FormUrlEncoded
    @POST("/mynotes/delProcess?is_ajax=1")
    void delFromNotes(@Field("product_id") String productId, @Field("user_id") String userId, Callback<Response> cb);


    /////////////////////////////// Likes
    @FormUrlEncoded
    @POST("/product/ajaxupdatelikes?is_ajax=1")
    void updateLike(@Field("product_id") String productId, @Field("action") String action, Callback<Response> cb);


    /////////////////////////////// Comments
    @FormUrlEncoded
    @POST("/product/ajaxaddcomment?is_ajax=1")
    void addComment(@Field("city_id") String cityId, @Field("product_id") String productId, @Field("comment") String comment, @Field("username") String userName, Callback<Response> cb);

    @FormUrlEncoded
    @POST("/product/ajaxgetcomments?is_ajax=1")
    void getComments(@Field("product_id") String productId, Callback<Response> cb);


    //////////////////////////////// Sales
    @FormUrlEncoded
    @POST("{shop}?is_ajax=1")
    void getSales(@Path("shop") String shop, @Field("page") int page, Callback<Response> cb);
}