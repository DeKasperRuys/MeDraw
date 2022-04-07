package internationalproject.medrawumeasure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditDataActivity extends AppCompatActivity {

    EditDataAdapter measurementAdapter;
    EditDataAdapter angleAdapter;
    ListView editMeasurementsListView;
    ListView editAnglesListView;
    String measurementsString;
    String anglesString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);

        Button saveDataButton = findViewById(R.id.save_data_button);
        final String toEditID = getIntent().getStringExtra("toEditID");
        RequestQueue queue = Volley.newRequestQueue(this);  // this = context
        String url = "http://" + getString(R.string.ip) + ":3000/drawingdatas/get/" + toEditID; // IP address should be the local private IPv4 address of the host machine (if using a phone instead of emulator)

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Display response
                        Log.d("EditDataActivity", "Database response: " + response);
                        try {
                            String[] measurementData;
                            String[] angleData;

                            JSONObject jsonobject = response.getJSONObject(0);
                            measurementData = jsonobject.getString("measurementdata").split(", ");
                            angleData = jsonobject.getString("angledata").split(", ");

                            measurementAdapter = new EditDataAdapter(EditDataActivity.this, measurementData);
                            editMeasurementsListView = findViewById(R.id.edit_measurements_list_view);
                            editMeasurementsListView.setAdapter(measurementAdapter);

                            angleAdapter = new EditDataAdapter(EditDataActivity.this, angleData);
                            editAnglesListView = findViewById(R.id.edit_angles_list_view);
                            editAnglesListView.setAdapter(angleAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("EditDataActivity", "Database error response: " + error);
                    }
                }
        );
        queue.add(getRequest);

        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View measurementsListViewChild = editMeasurementsListView.getChildAt(0);
                EditText measurementsEditText = measurementsListViewChild.findViewById(R.id.edit_data_list_entry);
                measurementsString = measurementsEditText.getText().toString();

                for (int i = 1; i < measurementAdapter.getCount(); i++) {
                    measurementsListViewChild = getViewByPosition(i, editMeasurementsListView);
                    measurementsEditText = measurementsListViewChild.findViewById(R.id.edit_data_list_entry);
                    measurementsString += ", " + measurementsEditText.getText().toString();
                }

                View anglesListViewChild = editAnglesListView.getChildAt(0);
                EditText anglesEditText = anglesListViewChild.findViewById(R.id.edit_data_list_entry);
                anglesString = anglesEditText.getText().toString();

                for (int i = 1; i < angleAdapter.getCount(); i++) {
                    anglesListViewChild = getViewByPosition(i, editAnglesListView);
                    anglesEditText = anglesListViewChild.findViewById(R.id.edit_data_list_entry);
                    anglesString += ", " + anglesEditText.getText().toString();
                }

                RequestQueue queue = Volley.newRequestQueue(EditDataActivity.this);
                String saveURL = "http://" + getString(R.string.ip) + ":3000/drawingdatas/post/update/" + toEditID;
                StringRequest postRequest = new StringRequest(Request.Method.POST, saveURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("EditDataActivity", "Database response: " + response);
                                Toast.makeText(EditDataActivity.this, "Database updated successfully", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(EditDataActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("EditDataActivity", "Database error response: " + error);
                                Toast.makeText(EditDataActivity.this, "Error! Couldn't update the database", Toast.LENGTH_SHORT).show();
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("measurementdata", measurementsString);
                        params.put("angledata", anglesString);

                        return params;
                    }
                };
                queue.add(postRequest);
            }
        });
    }

    // Function from https://stackoverflow.com/questions/18463101/get-listview-children-that-are-not-in-view
    public View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition) {
            return listView.getAdapter().getView(position, listView.getChildAt(position), listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
