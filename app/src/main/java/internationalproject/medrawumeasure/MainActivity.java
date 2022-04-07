package internationalproject.medrawumeasure;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ConnectFragment.OnItemSelectedListener {
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    Button removeButton;
    public static Button BtnAccount;

    ArrayList<Float> coordX = new ArrayList<>();
    ArrayList<Float> coordY = new ArrayList<>();
    ArrayList<Integer> lineLengths = new ArrayList<>();
    ArrayList<Integer> angleLengths = new ArrayList<>();

    int pointCount = 0;
    int pointCounter = 0;
    int removePointRadius = 50;
    int movePointIndex;
    int moveTimer = 0;
    boolean blockDrawPoint = false;
    boolean removePoint = false;
    boolean movePoint = false;
    Paint textPaint = new Paint();
    Paint transparentGray = new Paint();
    Paint transparentWhite = new Paint();
    boolean InputPad = false;
    int lineIndex;
    DigitInput digitInput;
    BluetoothService btService;
    String connectedDeviceName;
    String measurement;
    String Username;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btService = new BluetoothService(this, mHandler);

        removeButton = findViewById(R.id.remove_button);
        BtnAccount = findViewById(R.id.BtnAccount);

        BtnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getIntent();
                Username = i.getStringExtra("Username");

                Intent myIntent = new Intent(view.getContext(), AccountActivity.class);
                myIntent.putExtra("Username", Username);
                startActivityForResult(myIntent, 0);
            }
        });

        final Button bluetooth = findViewById(R.id.bluetooth_button);
        bluetooth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                final FragmentManager manager = getSupportFragmentManager();
                ConnectFragment fragment = ConnectFragment.newInstance();
                fragment.show(manager, "Bluetooth");
            }
        });

        imageView = this.findViewById(R.id.canvas_image_view);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getY() > 0 && event.getX() < canvas.getWidth()) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            if (!InputPad) {
                                // MOVING POINTS
                                moveTimer = 0;
                                for (int i = 0; i < pointCount; i++) {
                                    if (pointCollision(event.getX(), event.getY(), i)) {
                                        movePoint = true;
                                        movePointIndex = i;
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (!InputPad) {
                                moveTimer++;
                                if (movePoint && moveTimer > 20 && pointCount > 1) {
                                    coordX.set(movePointIndex, event.getX());
                                    coordY.set(movePointIndex, event.getY());
                                    redrawLines();
                                }
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                            boolean CloseInputPad = false;
                            if (InputPad) {
                                digitInput.CalcOutput(event.getX(), event.getY());
                                if (digitInput.output == 13) {
                                    //Bluetooth Button
                                    measure();
                                } else if (digitInput.Exit) {
                                    CloseInputPad = true;
                                    redrawLines();
                                    if (digitInput.OutputNumber > 0) {

                                        if (digitInput.Type == 1) {
                                            CalcTextPosition(lineIndex, digitInput.OutputNumber, digitInput.textSpacing, digitInput.Type);
                                            lineLengths.set(lineIndex, digitInput.OutputNumber);
                                        } else if (digitInput.Type == 2) {
                                            CalcTextPosition(digitInput.PointIndex, digitInput.OutputNumber, digitInput.textSpacing, digitInput.Type);
                                            angleLengths.set(digitInput.PointIndex, digitInput.OutputNumber);
                                        }
                                        digitInput = null;
                                        blockDrawPoint = true;
                                    }
                                } else if (digitInput.output == 11) {
                                    InputPad = false;
                                    digitInput = null;
                                    redrawLines();
                                } else if (digitInput.output == 12 || digitInput.BluetoothOutput) {
                                    redrawLines();
                                    digitInput.DrawInput(transparentGray, transparentWhite);
                                    digitInput.BluetoothOutput = false;
                                    digitInput.OutputNumber = 0;
                                } else if (digitInput.output != 10) {
                                    digitInput.DrawText(textPaint);
                                }
                            }
                            //POINT PRESS
                            if (!InputPad && !removePoint && moveTimer < 20) {
                                for (int i = 1; i < pointCount - 1; i++) {
                                    if (pointCollision(event.getX(), event.getY(), i)) {
                                        digitInput = new DigitInput(0, 0, canvas, textPaint, 2);
                                        InputPad = true;
                                        digitInput.PointIndex = i;
                                        redrawLines();
                                        digitInput.DrawInput(transparentGray, transparentWhite);
                                    }
                                }
                            }
                            //Calculate Collision with Line
                            if (!InputPad && moveTimer < 20) {
                                for (int i = 0; i < coordX.size() - 1; i++) {
                                    if (MathLibrary.IsInBetween(coordX.get(i), coordX.get(i + 1), event.getX()) && MathLibrary.IsInBetween(coordY.get(i), coordY.get(i + 1), event.getY()) && !InputPad) {
                                        float Rico = MathLibrary.calculateRico(coordX.get(i), coordX.get(i + 1), coordY.get(i), coordY.get(i + 1));
                                        float OffSet = MathLibrary.calculateOffSet(coordX.get(i), coordY.get(i), Rico);
                                        float pointY = event.getY();
                                        float TargetY = event.getX() * Rico + OffSet;
                                        if (MathLibrary.IsCloseBy(pointY, TargetY, 55)) {
                                            digitInput = new DigitInput(0, 0, canvas, textPaint, 1);
                                            InputPad = true;
                                            redrawLines();
                                            digitInput.DrawInput(transparentGray, transparentWhite);
                                            lineIndex = i;
                                        }
                                    }
                                }

                            }
                            if (removePoint) {
                                for (int i = 0; i < pointCount; i++) {
                                    if ((event.getY() > coordY.get(i) - removePointRadius && event.getY() < coordY.get(i) + removePointRadius)
                                            && (event.getX() > coordX.get(i) - removePointRadius && event.getX() < coordX.get(i) + removePointRadius)) {
                                        removePoint(i);
                                    }
                                }
                            }
                            //PLACE POINT
                            if (!removePoint && !movePoint && !InputPad && !blockDrawPoint) {
                                coordX.add(pointCount, event.getX());
                                coordY.add(pointCount, event.getY());
                                canvas.drawCircle(coordX.get(pointCount), coordY.get(pointCount), 15, paint);
                                pointCount++;
                                if (pointCount > 1) {
                                    canvas.drawLine(coordX.get(pointCount - 2), coordY.get(pointCount - 2), coordX.get(pointCount - 1), coordY.get(pointCount - 1), paint);
                                    lineLengths.add(0);

                                }
                                angleLengths.add(0);
                                pointCounter = pointCount;
                                break;
                            }

                            if (blockDrawPoint) {
                                blockDrawPoint = false;
                            }
                            movePoint = false;
                            if (CloseInputPad) {
                                InputPad = false;
                            }
                        case MotionEvent.ACTION_CANCEL:
                            break;

                        default:
                            break;
                    }
                }
                imageView.invalidate();
                return true;
            }
        });
    }

    // This creates the canvas and sets the paint when the window has focus (right after the onCreate).
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
            imageView.setImageBitmap(bitmap);
            canvas = new Canvas(bitmap);
            paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(12);
            textPaint.setColor(Color.BLACK);
            textPaint.setStrokeWidth(50);
            textPaint.setTextSize(50);
            transparentGray.setARGB(235, 192, 192, 192);
            transparentWhite.setARGB(235, 255, 255, 255);
            redrawLines();
            if (getIntent().getStringExtra("drawingID") != null) {
                getDrawingFromDatabase();
            }
            blockDrawPoint = false;
            InputPad = false;
        }
    }

    private boolean pointCollision(float X, float Y, int i) {
        return (Y > coordY.get(i) - removePointRadius && Y < coordY.get(i) + removePointRadius)
                && (X > coordX.get(i) - removePointRadius && X < coordX.get(i) + removePointRadius);
    }

    private void redrawLines() {
        try {
            if (pointCount > 0) {
                canvas.drawColor(Color.WHITE);
                paint.setColor(Color.BLACK);
                for (int i = 0; i < pointCount; i++) {
                    canvas.drawCircle(coordX.get(i), coordY.get(i), 15, paint);
                    if (i > 0)
                        canvas.drawLine(coordX.get(i - 1), coordY.get(i - 1), coordX.get(i), coordY.get(i), paint);
                }
                imageView.invalidate();
            }
            if (pointCount > 1 && !lineLengths.isEmpty()) {
                for (int i = 0; i < pointCount - 1; i++) {
                    boolean AboveTen = true;
                    int DigitCount = 0;
                    int Temp = lineLengths.get(i);
                    while (AboveTen) {
                        if (Temp / 10 < 1) {
                            AboveTen = false;
                        } else {
                            Temp = Temp / 10;
                            DigitCount++;
                        }
                    }
                    if (lineLengths.get(i) > 0) {
                        CalcTextPosition(i, lineLengths.get(i), DigitCount, 1);
                    }
                }
            }

            if (pointCount > 2 && !angleLengths.isEmpty()) {
                for (int i = 0; i < pointCount - 1; i++) {
                    boolean AboveTen = true;
                    int DigitCount = 0;
                    int Temp = angleLengths.get(i);
                    while (AboveTen) {
                        if (Temp / 10 < 1) {
                            AboveTen = false;
                        } else {
                            Temp = Temp / 10;
                            DigitCount++;
                        }
                    }
                    if (angleLengths.get(i) > 0) {
                        CalcTextPosition(i, angleLengths.get(i), DigitCount, 2);
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void getDrawingFromDatabase() {
        String idFromGallery = getIntent().getStringExtra("drawingID");
        RequestQueue queue = Volley.newRequestQueue(this);  // this = context
        final String url = "http://" + getString(R.string.ip) + ":3000/drawings/get/" + idFromGallery; // IP address should be the local private IPv4 address of the host machine (if using a phone instead of emulator)

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Display response
                        Log.d("MainActivity", "Database response: " + response);
                        try {
                            String[] drawingCoords;

                            JSONObject jsonobject = response.getJSONObject(0);
                            drawingCoords = jsonobject.getString("drawingcoordinates").split(", ");

                            ArrayList<Float> drawingCoordX = new ArrayList<>();
                            ArrayList<Float> drawingCoordY = new ArrayList<>();
                            int counter = 0;
                            int multiplier = canvas.getWidth() / 300;
                            Log.d("multiplier", String.valueOf(multiplier));
                            for (int i = 0; i < drawingCoords.length; i += 2) {
                                drawingCoordX.add(counter, Float.parseFloat(drawingCoords[i]) * multiplier);
                                drawingCoordY.add(counter, Float.parseFloat(drawingCoords[i + 1]) * multiplier);
                                counter++;
                            }
                            coordX = drawingCoordX;
                            coordY = drawingCoordY;
                            pointCount = drawingCoordX.size();
                            if (getIntent().getStringExtra("measurementID") != null) {
                                getDataFromDatabase();
                            } else
                                redrawLines();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity", "Database error response: " + error);
                    }
                }
        );
        queue.add(getRequest);
    }

    private void getDataFromDatabase() {
        String idFromGallery = getIntent().getStringExtra("measurementID");
        RequestQueue queue = Volley.newRequestQueue(this);  // this = context
        final String url = "http://" + getString(R.string.ip) + ":3000/drawingdatas/get/" + idFromGallery; // IP address should be the local private IPv4 address of the host machine (if using a phone instead of emulator)

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Display response
                        Log.d("MainActivity", "Database response: " + response);
                        try {
                            String[] measurementData;
                            String[] angleData;

                            JSONObject jsonobject = response.getJSONObject(0);
                            measurementData = jsonobject.getString("measurementdata").split(", ");
                            angleData = jsonobject.getString("angledata").split(", ");

                            try {
                                for (String aMeasurementData : measurementData)
                                    lineLengths.add(Integer.valueOf(aMeasurementData));
                                angleLengths.add(0);
                                for (String anAngleData : angleData)
                                    angleLengths.add(Integer.valueOf(anAngleData));
                                angleLengths.add(0);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            redrawLines();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity", "Database error response: " + error);
                    }
                }
        );
        queue.add(getRequest);
    }

    public void ClearCanvas(View v) {
        canvas.drawColor(Color.WHITE);
        pointCount = 0;
        coordX.clear();
        coordY.clear();
        lineLengths.clear();
        angleLengths.clear();
        lineIndex = 0;
        imageView.invalidate();
        blockDrawPoint = false;
        InputPad = false;
    }

    public void UndoLine(View v) {
        if (pointCount > 1) {
            pointCount--;
            redrawLines();
        }
    }

    public void RedoLine(View v) {
        if (pointCount < pointCounter) {
            pointCount++;
            redrawLines();
        }
    }

    public void RemovePoints(View v) {
        removePoint = !removePoint;
        if (removePoint)
            removeButton.setTextColor(Color.RED);
        else
            removeButton.setTextColor(Color.BLACK);
    }

    private void removePoint(int index) {
        pointCount--;
        for (int i = index; i < pointCount; i++) {
            coordX.set(i, coordX.get(i + 1));
            coordY.set(i, coordY.get(i + 1));
        }
        redrawLines();
    }

    public void CalcTextPosition(int index, int output, int TextSpacing, int Type) {
        float middleX = 0;
        float middleY = 0;
        int Ymodifier = 0;
        int Xmodifier = 0;
        String symbol = "mm";
        if (Type == 1) {
            middleX = MathLibrary.CalculateMiddle(coordX.get(index), coordX.get(index + 1));
            middleY = MathLibrary.CalculateMiddle(coordY.get(index), coordY.get(index + 1));

            if ((coordX.get(index) < coordX.get(lineIndex + 1) && coordY.get(index) > coordY.get(index + 1)) ||
                    (coordX.get(index + 1) < coordX.get(index) && coordY.get(index + 1) > coordY.get(index))) {
                Ymodifier = -35;
                Xmodifier = (int) (-85 - TextSpacing*1.1);
            } else {
                Ymodifier = -35;
                Xmodifier = 50;
            }
            symbol = "mm";
        }
        if (Type == 2) {
            middleX = coordX.get(index);
            middleY = coordY.get(index);
            if (coordY.get(index) < coordY.get(lineIndex + 1)) {
                Xmodifier = 0;
                Ymodifier = -65;
            } else {
                Xmodifier = 0;
                Ymodifier = 65;
            }
            symbol = "°";
        }
        canvas.drawText(String.valueOf(output) + symbol, middleX + Xmodifier, middleY + Ymodifier, textPaint);
    }

    public void GoToGallery(View view) {
        Intent i = new Intent(view.getContext(), GalleryActivity.class);
        startActivity(i);
    }

    public void GoToSave(View view) {
        if (!coordX.isEmpty()) {
            int multiplier = canvas.getWidth() / 300;
            ArrayList<String> coordXString = new ArrayList<>(coordX.size());
            ArrayList<String> coordYString = new ArrayList<>(coordY.size());
            ArrayList<String> lineLenghtsString = new ArrayList<>(lineLengths.size());
            ArrayList<String> angleLenghtsString = new ArrayList<>(angleLengths.size());
            for (int i = 0; i < pointCount; i++) {
                coordXString.add(String.valueOf(Math.round(coordX.get(i) / multiplier)));
                coordYString.add(String.valueOf(Math.round(coordY.get(i) / multiplier)));
            }
            for (int i = 0; i < (pointCount - 1); i++) {
                lineLenghtsString.add(String.valueOf(lineLengths.get(i)));
            }
            for (int i = 1; i < (pointCount - 1); i++) {
                angleLenghtsString.add(String.valueOf(angleLengths.get(i)));
            }
            Intent i = new Intent(view.getContext(), SaveActivity.class);
            i.putStringArrayListExtra("coordX", coordXString);
            i.putStringArrayListExtra("coordY", coordYString);
            i.putStringArrayListExtra("lineLenghts", lineLenghtsString);
            i.putStringArrayListExtra("angleLenghts", angleLenghtsString);
            i.putExtra("pointcount", pointCount);
            startActivity(i);
        } else {
            Toast.makeText(this, "There's no drawing to save", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;


                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    //String readMessage = new String(readBuf, 0, msg.arg1);

                    measurement = String.valueOf(btService.extractData(readBuf));

                    // Set the measurement on the digit input
                    if (Integer.parseInt(measurement) > 0) {
                        redrawLines();
                        digitInput.DrawInput(transparentGray, transparentWhite);
                        canvas.drawText(measurement, 90, 155, textPaint);
                        digitInput.OutputNumber = Integer.parseInt(measurement);
                    }

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (connectedDeviceName != null) {
                        Toast.makeText(MainActivity.this, "Connected to "
                                + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case Constants.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    public void onComplete(BluetoothDevice device) {
        connectedDeviceName = device.getName();

        btService.connect(device);
    }

    public void measure() {
        btService.write(btService.hexToBytes("C04000EE"));
    }
}
