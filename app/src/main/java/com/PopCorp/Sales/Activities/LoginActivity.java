package com.PopCorp.Sales.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.PopCorp.Sales.Data.User;
import com.PopCorp.Sales.Net.NetHelper;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_LOGIN = "LOGIN";

    private CookieManager cookieManager;

    private EditText email;
    private EditText password;

    private EditText nameReg;
    private EditText emailReg;
    private EditText passwordReg;
    private EditText passwordRegRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        email = (EditText) findViewById(R.id.activity_login_email);
        password = (EditText) findViewById(R.id.activity_login_password);
        final Button signIn = (Button) findViewById(R.id.activity_login_sign_in);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    return;
                }
                signIn(email.getText().toString(), password.getText().toString());
            }
        });

        nameReg = (EditText) findViewById(R.id.activity_login_reg_name);
        emailReg = (EditText) findViewById(R.id.activity_login_reg_email);
        passwordReg = (EditText) findViewById(R.id.activity_login_reg_password);
        passwordRegRepeat = (EditText) findViewById(R.id.activity_login_reg_password_repeat);
        Button signUp = (Button) findViewById(R.id.activity_login_sign_up);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailReg.getText().toString().isEmpty() || nameReg.getText().toString().isEmpty() ||
                        passwordReg.getText().toString().isEmpty() || passwordRegRepeat.getText().toString().isEmpty()) {
                    return;
                }
                if (!passwordReg.getText().toString().equals(passwordRegRepeat.getText().toString())){
                    return;
                }
                ((SalesApplication) getApplication()).getService().signUp(nameReg.getText().toString(), emailReg.getText().toString(), passwordReg.getText().toString(),
                        passwordRegRepeat.getText().toString(), new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                if (registrationSuccess(response)){
                                    signIn(emailReg.getText().toString(), passwordReg.getText().toString());
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private boolean registrationSuccess(Response response) {
        String body = NetHelper.stringFromResponse(response);
        return false;
    }

    private void signIn(final String email, String password){
        ((SalesApplication) getApplication()).getService().signIn(email, password, SD.REMEMBER_ME, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                boolean findedCookie = false;
                List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
                for (HttpCookie cookie : cookies){
                    if (cookie.getName().startsWith(SD.HOST_NAME)){
                        User newUser = new User(NetHelper.stringFromResponse(response), email, cookie);
                        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sPref.edit();
                        editor.putString(SD.PREFS_CURRENT_USER, newUser.getId()).apply();
                        newUser.putInDB(((SalesApplication) getApplication()).getDB());
                        findedCookie = true;
                    }
                }
                if (!findedCookie){
                    Toast.makeText(LoginActivity.this, R.string.error_not_availible_email_or_password, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
