package internationalproject.medrawumeasure;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Arren on 4/17/2018.
 */

public class DigitInput {
    private float X;
    private float Y;
    public int output;
    public boolean Exit = false;
    public int Type;
    private Canvas canvas;
    public int textSpacing = 0;
    public int OutputNumber = 0;
    public int PointIndex = 0;
    public boolean BluetoothOutput = false;
    private Paint textPaint;
    private  Paint BluetoothButton = new Paint();

    DigitInput(float x, float y, Canvas canvas, Paint textPaint, int Type) {
        this.Type = Type;
        X = x;
        Y = y;
        this.canvas = canvas;
        this.textPaint = textPaint;
        BluetoothButton.setARGB(235,100,100,255);
    }

    public void DrawInput(Paint BackGround, Paint Buttons) {
        canvas.drawRect(X + 45, Y + 45, X + 855, Y + 665, BackGround);
        canvas.drawRect(X + 90, Y + 90, X + 710, Y + 165, Buttons);

        canvas.drawRect(X + 90, Y + 200, X + 210, Y + 320, Buttons);
        canvas.drawRect(X + 90, Y + 350, X + 210, Y + 470, Buttons);
        canvas.drawRect(X + 90, Y + 500, X + 210, Y + 620, Buttons);
        canvas.drawText("1", X + 130, Y + 280, textPaint);
        canvas.drawText("4", X + 130, Y + 430, textPaint);
        canvas.drawText("7", X + 130, Y + 580, textPaint);

        canvas.drawRect(X + 240, Y + 200, X + 360, Y + 320, Buttons);
        canvas.drawRect(X + 240, Y + 350, X + 360, Y + 470, Buttons);
        canvas.drawRect(X + 240, Y + 500, X + 360, Y + 620, Buttons);
        canvas.drawText("2", X + 280, Y + 280, textPaint);
        canvas.drawText("5", X + 280, Y + 430, textPaint);
        canvas.drawText("8", X + 280, Y + 580, textPaint);

        canvas.drawRect(X + 390, Y + 200, X + 510, Y + 320, Buttons);
        canvas.drawRect(X + 390, Y + 350, X + 510, Y + 470, Buttons);
        canvas.drawRect(X + 390, Y + 500, X + 510, Y + 620, Buttons);
        canvas.drawText("3", X + 430, Y + 280, textPaint);
        canvas.drawText("6", X + 430, Y + 430, textPaint);
        canvas.drawText("9", X + 430, Y + 580, textPaint);

        canvas.drawRect(X + 540, Y + 200, X + 660, Y + 320, Buttons);
        canvas.drawRect(X + 540, Y + 350, X + 660, Y + 470, Buttons);
        canvas.drawRect(X + 540, Y + 500, X + 660, Y + 620, Buttons);
        canvas.drawText("0", X + 580, Y + 280, textPaint);
        canvas.drawText("C", X + 580, Y + 430, textPaint);
        canvas.drawText("Save", X + 548, Y + 580, textPaint);

        canvas.drawRect(X + 690, Y + 200, X + 810, Y + 620, BluetoothButton);
    }

    public void DrawText(Paint textPaint) {
        OutputNumber = OutputNumber * 10 + output;
        canvas.drawText(String.valueOf(output), 90 + textSpacing, 155, textPaint);
        textSpacing = textSpacing + 28;
    }

    public void CalcOutput(float XPos, float YPos) {
        output = 10;
        if (XPos > 90 && XPos < 660 && YPos > 200 && YPos < 620) {
            output = 10;
            if (XPos > 90 && XPos < 210) {
                output = 1;
            } else if (XPos > 240 && XPos < 360) {
                output = 2;
            } else if (XPos > 390 && XPos < 510) {
                output = 3;
            }
            if (YPos > 350 && YPos < 470 && output < 10) {
                if (output == 10) {
                    output = 0;
                }
                output = output + 3;
            } else if (YPos > 500 && YPos < 620 && output < 10) {
                if (output == 10) {
                    output = 0;
                }
                output = output + 6;
            }
            if (XPos > 540 && XPos < 660) {
                if (YPos > 200 && YPos < 320 && OutputNumber > 0) {
                    output = 0;
                } else if (YPos > 350 && YPos < 470) {
                    output = 12;
                    OutputNumber = 0;
                    textSpacing = 0;
                } else if (YPos > 500 && YPos < 620) {
                    Exit = true;
                }
            }
        } else if (XPos > 690 && XPos < 810 && YPos < 620 && YPos > 200) {
            BluetoothOutput = true;
            output = 13;
            OutputNumber = 0;
            textSpacing = 0;
        } else if (XPos > 1420) {
            output = 11;
        }
    }
}
