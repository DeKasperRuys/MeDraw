package internationalproject.medrawumeasure;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONArray;
import org.json.JSONException;

public class GalleryActivity extends AppCompatActivity {
    String URL;
    String selectedDrawingID;
    String selectedMeasurementID;
    boolean isDrawingSelected;
    TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        URL = "http://" + getString(R.string.ip) + ":3000/drawings/get"; // IP address should be the local private IPv4 address of the host machine (if using a phone instead of emulator)
        titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText(R.string.gallery_string);

        isDrawingSelected = false;
        loadDatabase();
    }

    private void loadDatabase() {
        RequestQueue queue = Volley.newRequestQueue(this);  // this = context

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response
                        Log.d("GalleryActivity", "Database response: " + response);
                        try {
                            if (URL.contains("drawingdatas")) {
                                if (response.length() > 0) {
                                    titleTextView.setText(R.string.required_data_string);
                                    String[] measurementIDs = new String[response.length()];
                                    String[] dataDescriptions = new String[response.length()];
                                    for (int i = 0; i < response.length(); i++) {
                                        measurementIDs[i] = response.getJSONObject(i).getString("id");
                                        dataDescriptions[i] = "Measurements: " + response.getJSONObject(i).getString("measurementdata") +
                                                "\nAngles: " + response.getJSONObject(i).getString("angledata");
                                    }
                                    setListAdapter(measurementIDs, dataDescriptions);
                                } else {
                                    Intent i = new Intent(GalleryActivity.this, MainActivity.class);
                                    i.putExtra("drawingID", selectedDrawingID);
                                    startActivity(i);
                                }
                            } else {
                                String[] drawingsIDs = new String[response.length()];
                                String[] drawingsNames = new String[response.length()];
                                for (int i = 0; i < response.length(); i++) {
                                    drawingsIDs[i] = response.getJSONObject(i).getString("id");
                                    drawingsNames[i] = response.getJSONObject(i).getString("name");
                                }
                                setListAdapter(drawingsIDs, drawingsNames);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("GalleryActivity", "Database error response: " + error);
                        Toast toast = Toast.makeText(getApplicationContext(), "Couldn't connect to the database.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
        );
        queue.add(getRequest);
    }

    private void setListAdapter(final String[] IDs, String[] names) {
        SwipeMenuListView galleryListView = findViewById(R.id.gallery_list_view);
        galleryListView.setAdapter(new GalleryAdapter(this, names));

        galleryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (isDrawingSelected) {
                    selectedMeasurementID = IDs[position];
                    Intent i = new Intent(GalleryActivity.this, MainActivity.class);
                    i.putExtra("drawingID", selectedDrawingID);
                    i.putExtra("measurementID", selectedMeasurementID);
                    startActivity(i);
                } else {
                    isDrawingSelected = true;
                    selectedDrawingID = IDs[position];
                    URL = "http://" + getString(R.string.ip) + ":3000/drawingdatas/getdrawing/" + selectedDrawingID;
                    loadDatabase();
                }
            }
        });

        // Library from https://github.com/baoyongzhang/SwipeMenuListView
        if (isDrawingSelected) {
            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    openItem.setBackground(new ColorDrawable(Color.rgb(0x68, 0x9d, 0xf2)));
                    openItem.setWidth(250);
                    openItem.setIcon(R.drawable.edit_icon);
                    menu.addMenuItem(openItem);
                }
            };

            galleryListView.setMenuCreator(creator);

            galleryListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    selectedMeasurementID = IDs[position];
                    Intent i = new Intent(GalleryActivity.this, EditDataActivity.class);
                    i.putExtra("toEditID", selectedMeasurementID);
                    startActivity(i);
                    return false;
                }
            });
        }
    }
}