package internationalproject.medrawumeasure;

import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.provider.Telephony;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText PtUsername;
    EditText PtPassword;
    CheckBox CbRememberMe;
    Button BtnLogin;
    public boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PtUsername = findViewById(R.id.PtUsername);
        PtPassword = findViewById(R.id.PtPassword);
        CbRememberMe = findViewById(R.id.CbRememberMe);
        Button register = findViewById(R.id.BtnRegister);
        BtnLogin = findViewById(R.id.BtnLogin);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent1 = new Intent(view.getContext(), RegisterActivity.class);
                startActivityForResult(myIntent1, 0);
            }
        });

        BtnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LoginVerification();
            }
        });
    }

    private void LoginVerification() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://" + getString(R.string.ip) + ":3000/users/get/login/" + PtUsername.getText();
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        Log.d("Response2", response.toString());
                        Log.d("Response1", ("{\"" + PtPassword.getText().toString() + "\":1}"));


                        if (Objects.equals(response.toString(), ("{\"" + PtPassword.getText().toString() + "\":1}"))) {
                            isLoggedIn = true;

                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                            builder.setCancelable(true);
                            builder.setTitle("Login Successful");
                            builder.setMessage("Welcome, " + PtUsername.getText() + " you are now logged in.");

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    intent.putExtra("Username", PtUsername.getText().toString());
                                    startActivity(intent);
                                }
                            });
                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                            builder.setCancelable(true);
                            builder.setTitle("Wrong credentials!");
                            builder.setMessage("Please verify your input for both the username and the password!");

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            builder.show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", String.valueOf(error));
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                        builder.setCancelable(true);
                        builder.setTitle("An error occured");
                        builder.setMessage("Database error: " + error);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        builder.show();
                    }
                }
        );
        // add it to the RequestQueue
        queue.add(getRequest);
    }
}

