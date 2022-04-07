package internationalproject.medrawumeasure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveActivity extends AppCompatActivity {

    String coordinatesString;
    ArrayList<EditText> angles;
    ArrayList<EditText> measurements;
    int pointcount;
    ArrayList<String> angleInputs;
    ArrayList<String> measurementsInputs;
    ArrayList<String> lineLengths;
    ArrayList<String> angleLenghts;
    String lineLengthsString;
    String angleLengthsString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        TextView CoordXTextView = findViewById(R.id.coordx_text_view);
        TextView CoordYTextView = findViewById(R.id.coordy_text_view);

        ArrayList<String> drawingCoordX;
        ArrayList<String> drawingCoordY;
        drawingCoordX = getIntent().getStringArrayListExtra("coordX");
        drawingCoordY = getIntent().getStringArrayListExtra("coordY");
        lineLengths = getIntent().getStringArrayListExtra("lineLenghts");
        angleLenghts = getIntent().getStringArrayListExtra("angleLenghts");
        String[] coordinates = new String[drawingCoordX.size() * 2];

        CoordXTextView.setText("X coordinates  of the points: " + drawingCoordX);
        CoordYTextView.setText("Y coordinates of the points: " + drawingCoordY);

        angleInputs = new ArrayList<>();
        measurementsInputs = new ArrayList<>();
        Intent mIntent = getIntent();
        pointcount = mIntent.getIntExtra("pointcount", 0);

        angles = new ArrayList<>();
        measurements = new ArrayList<>();
        measurements.add((EditText) findViewById(R.id.measurement1));
        measurements.add((EditText) findViewById(R.id.measurement2));
        measurements.add((EditText) findViewById(R.id.measurement3));
        measurements.add((EditText) findViewById(R.id.measurement4));
        measurements.add((EditText) findViewById(R.id.measurement5));
        measurements.add((EditText) findViewById(R.id.measurement6));
        measurements.add((EditText) findViewById(R.id.measurement7));
        measurements.add((EditText) findViewById(R.id.measurement8));
        measurements.add((EditText) findViewById(R.id.measurement9));
        measurements.add((EditText) findViewById(R.id.measurement10));
        angles.add((EditText) findViewById(R.id.Angle1));
        angles.add((EditText) findViewById(R.id.Angle2));
        angles.add((EditText) findViewById(R.id.Angle3));
        angles.add((EditText) findViewById(R.id.Angle4));
        angles.add((EditText) findViewById(R.id.Angle5));
        angles.add((EditText) findViewById(R.id.Angle6));
        angles.add((EditText) findViewById(R.id.Angle7));
        angles.add((EditText) findViewById(R.id.Angle8));
        angles.add((EditText) findViewById(R.id.Angle9));
        angles.add((EditText) findViewById(R.id.Angle10));

        drawAngleEditTexts();
        drawMeasurementEditTexts();

        int counter = 0;
        for (int i = 0; i < coordinates.length; i += 2) {
            coordinates[i] = drawingCoordX.get(counter);
            coordinates[i + 1] = drawingCoordY.get(counter);
            counter++;
        }

        coordinatesString = coordinates[0];
        for (int i = 1; i < coordinates.length; i++) {
            coordinatesString += ", " + coordinates[i];
        }
    }

    public void SaveToDatabase(View view) {
        String drawingName = "Untitled";
        EditText nameEditText = findViewById(R.id.name_edit_text);
        if (!nameEditText.getText().toString().equals(""))
            drawingName = nameEditText.getText().toString();

        if (lineLengths.size() > 0) {
            lineLengthsString = String.valueOf(measurements.get(0).getText());
            for (int i = 1; i < lineLengths.size(); i++) {
                lineLengthsString += ", " + measurements.get(i).getText();
            }
        }

        if (angleLenghts.size() > 0) {
            angleLengthsString = String.valueOf(angles.get(0).getText());
            for (int i = 1; i < angleLenghts.size(); i++) {
                angleLengthsString += ", " + angles.get(i).getText();
            }
        }

        RequestQueue queue = Volley.newRequestQueue(SaveActivity.this);
        String saveURL = "http://" + getString(R.string.ip) + ":3000/drawings/post";
        final String finalDrawingName = drawingName;
        StringRequest postRequest = new StringRequest(Request.Method.POST, saveURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("SaveActivity", "Database response: " + response);
                        saveMeasurementsToDatabase();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("SaveActivity", "Database error response: " + error);
                        Toast.makeText(SaveActivity.this, "Error! Couldn't update the database", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("drawingcoordinates", coordinatesString);
                params.put("name", finalDrawingName);

                return params;
            }
        };
        queue.add(postRequest);
    }

    private void saveMeasurementsToDatabase() {
        RequestQueue queue = Volley.newRequestQueue(SaveActivity.this);
        String saveURL = "http://" + getString(R.string.ip) + ":3000/drawingdatas/post/"; // + drawingID;
        StringRequest postRequest = new StringRequest(Request.Method.POST, saveURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Response
                        Log.d("SaveActivity", "Database response: " + response);
                        Toast.makeText(SaveActivity.this, "Database updated successfully", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(SaveActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("SaveActivity", "Database error response: " + error);
                        Toast.makeText(SaveActivity.this, "Error! Couldn't update the database", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("measurementdata", lineLengthsString);
                params.put("angledata", angleLengthsString);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void drawMeasurementEditTexts() {
        if (pointcount < 10) {
            for (int i = lineLengths.size(); i < 10; i++)
                measurements.get(i).setVisibility(View.GONE);
            for (int i = 0; i < lineLengths.size(); i++)
                measurements.get(i).setText(lineLengths.get(i));
        }
    }

    private void drawAngleEditTexts() {
        if (pointcount < 10) {
            for (int i = angleLenghts.size(); i < 10; i++)
                angles.get(i).setVisibility(View.GONE);
            for (int i = 0; i < angleLenghts.size(); i++)
                angles.get(i).setText(angleLenghts.get(i));
        }
    }
}