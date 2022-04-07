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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class EditAccount extends AppCompatActivity {

    EditText EtUsername;
    EditText EtFullName;
    EditText EtAddress;
    EditText EtEmail;
    EditText EtPassword;
    Button BtnChange;
    Button BtnCancel;
    String Username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        EtUsername = findViewById(R.id.EtUsername);
        EtFullName = findViewById(R.id.EtFullName);
        EtAddress = findViewById(R.id.EtAddress);
        EtEmail = findViewById(R.id.EtEmail);
        EtPassword = findViewById(R.id.EtPassword);
        BtnChange = findViewById(R.id.BtnChange);
        BtnCancel = findViewById(R.id.BtnCancel);
        Intent iEdit = getIntent();
        Username = iEdit.getStringExtra("Username");
        BtnChange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EditAccountDetails();
            }
        });
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent home = new Intent(EditAccount.this   , MainActivity.class);
                startActivity(home);
            }
        });
    }



    public void EditAccountDetails()
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://" + getString(R.string.ip) + ":3000/users/edit/"+Username;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditAccount.this);

                        builder.setCancelable(true);
                        builder.setTitle("Succesfully Changed details");
                        builder.setMessage("Details Have been changed succesfully");

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(EditAccount.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                        builder.show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("username", EtUsername.getText().toString());
                params.put("name", EtFullName.getText().toString());
                params.put("address", EtAddress.getText().toString());
                params.put("email", EtEmail.getText().toString());
                params.put("password", EtPassword.getText().toString());


                return params;
            }
        };
        queue.add(postRequest);

    }
}
