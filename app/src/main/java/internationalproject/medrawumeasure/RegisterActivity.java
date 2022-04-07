package internationalproject.medrawumeasure;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText PtFullName;
    EditText PtUsername;
    EditText PtEmail;
    EditText PtAddress;
    EditText PtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        PtFullName = findViewById(R.id.PtFullName);
        PtUsername = findViewById(R.id.PtUsername);
        PtEmail = findViewById(R.id.PtEmail);
        PtAddress = findViewById(R.id.PtAddress);
        PtPassword = findViewById(R.id.PtPassword);


        Button BtnRegister = findViewById(R.id.BtnRegister);
        BtnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                putAccountDetailsToDatabase();
            }
        });
        Button BackLogin = findViewById(R.id.BtnBackLogin);
        BackLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent5 = new Intent(view.getContext(), LoginActivity.class);
                startActivityForResult(myIntent5, 0);
            }
        });
        Button BackHome = findViewById(R.id.BtnBackMain);
        BackHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent6 = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent6, 0);
            }
        });
    }

    private void putAccountDetailsToDatabase() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://" + getString(R.string.ip) + ":3000/users/post/"; //edit ip
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                        builder.setCancelable(true);
                        builder.setTitle("Succesfully registered");
                        builder.setMessage("Welcome, " + PtUsername.getText() + " you are now reistered");

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                        builder.show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", PtFullName.getText().toString());
                params.put("username", PtUsername.getText().toString());
                params.put("password", PtPassword.getText().toString());
                params.put("email", PtEmail.getText().toString());
                params.put("address", PtAddress.getText().toString());

                return params;
            }
        };
        queue.add(postRequest);
    }
}
