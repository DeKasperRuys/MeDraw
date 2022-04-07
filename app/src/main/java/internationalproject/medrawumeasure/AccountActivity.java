package internationalproject.medrawumeasure;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class AccountActivity extends AppCompatActivity {
    String Username;
    TextView TvFullname;
    TextView TvUsername;
    TextView TvAddress;
    TextView TvEmail;
    Button BtnEditAccount;
    Button BtnRemoveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Intent i = getIntent();
        Username = i.getStringExtra("Username");
        TvUsername = findViewById(R.id.TvUsername);
        TvUsername.setText(Username);
        TvAddress = findViewById(R.id.TvAddress);
        TvEmail = findViewById(R.id.TvEmail);
        TvFullname = findViewById(R.id.TvFullname);
        BtnEditAccount = findViewById(R.id.BtnEditAccount);
        BtnRemoveAccount = findViewById(R.id.BtnRemoveAccount);

        BtnEditAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent iEdit = new Intent(view.getContext(), EditAccount.class);
                iEdit.putExtra("Username", Username);
                startActivityForResult(iEdit, 0);
            }
        });
        BtnRemoveAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {


                Intent iRemove = new Intent(view.getContext(), EditAccount.class);
                startActivityForResult(iRemove, 0);

            }
        });

        getLoginData();
    }



    private void getLoginData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://" + getString(R.string.ip) + ":3000/users/get/details/"+Username;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(response, "onResponse: ");
                    response = response.replace("\"","");
                    String[] Details = response.split(",");
                    TvFullname.setText(Details[0]);
                    TvAddress.setText(Details[1]);
                    TvEmail.setText(Details[2]);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(error.toString(), "onErrorResponse: ");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);

}

public void RemoveAccount()
{
    RequestQueue queue = Volley.newRequestQueue(this);
    final String url = "http://" + getString(R.string.ip) + ":3000/users/delete/"+Username;
    StringRequest dr = new StringRequest(Request.Method.DELETE, url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    // response
                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);

                    builder.setCancelable(true);
                    builder.setTitle("Account was removed succesfully");
                    builder.setMessage("Account removed");

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent1 = new Intent(AccountActivity.this,LoginActivity.class);

                            startActivity(intent1);
                        }
                    });
                    builder.show();
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // error.
                    Log.d(error.toString(), "onErrorResponse: ");
                }
            }
    );
    queue.add(dr);

}







    }


