package internationalproject.medrawumeasure;

/**
 * Created by Arren on 4/23/2018.
 */

public abstract class MathLibrary {
    public static float calculateRico(float X1, float X2, float Y1, float Y2) {
        float Rico = 0;
        if (X1 < X2) {
            X1 = X1 - 30;
            X2 = X2 + 30;
            Rico = (Y2 - Y1) / (X2 - X1);
        } else if (X1 > X2) {
            X1 = X1 + 30;
            X2 = X2 - 30;
            Rico = (Y1 - Y2) / (X1 - X2);
        }
        return Rico;
    }

    public static float calculateOffSet(float X1, float Y1, float Rico) {
        float OffSet;
        OffSet = Y1 - X1 * Rico;
        return OffSet;
    }

    public static boolean IsInBetween(float Number1, float Number2, float X) {
        return (X > Number1 && X < Number2) || (X < Number1 && X > Number2);
    }

    public static boolean IsCloseBy(float X, float Y, float Radius) {
        return X > Y - Radius && X < Y + Radius;
    }

    public static float CalculateMiddle(float X1, float X2) {
        if (X1 < X2) {
            return X1 + Math.abs(X1 - X2) / 2;
        } else {
            return X2 + Math.abs(X2 - X1) / 2;
        }
    }
}
