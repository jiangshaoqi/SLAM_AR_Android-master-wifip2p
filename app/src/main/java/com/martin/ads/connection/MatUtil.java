package com.martin.ads.connection;

import static android.opengl.Matrix.rotateM;

import android.opengl.Matrix;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.opencv.core.Mat;
import org.opencv.core.CvType;

import java.util.Base64;

public class MatUtil {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String matToJson(Mat mat){
        JsonObject obj = new JsonObject();

        if(mat.isContinuous()){
            int cols = mat.cols();
            int rows = mat.rows();
            int elemSize = (int) mat.elemSize();
            int type = mat.type();

            obj.addProperty("rows", rows);
            obj.addProperty("cols", cols);
            obj.addProperty("type", type);

            // We cannot set binary data to a json object, so:
            // Encoding data byte array to Base64.
            String dataString;

            if( type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
                int[] data = new int[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.getEncoder().encode(SerializationUtils.toByteArray(data)));
            }
            else if( type == CvType.CV_32F || type == CvType.CV_32FC2) {
                float[] data = new float[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.getEncoder().encode(SerializationUtils.toByteArray(data)));
            }
            else if( type == CvType.CV_64F || type == CvType.CV_64FC2) {
                double[] data = new double[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.getEncoder().encode(SerializationUtils.toByteArray(data)));
            }
            else if( type == CvType.CV_8U || type == CvType.CV_8UC4) {
                byte[] data = new byte[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.getEncoder().encode(data));
            }
            else {

                throw new UnsupportedOperationException("unknown type");
            }
            obj.addProperty("data", dataString);

            Gson gson = new Gson();
            String json = gson.toJson(obj);

            return json;
        } else {
            System.out.println("Mat not continuous.");
        }
        return "{}";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Mat matFromJson(String json){


        JsonParser parser = new JsonParser();
        JsonObject JsonObject = parser.parse(json).getAsJsonObject();

        int rows = JsonObject.get("rows").getAsInt();
        int cols = JsonObject.get("cols").getAsInt();
        int type = JsonObject.get("type").getAsInt();

        Mat mat = new Mat(rows, cols, type);

        String dataString = JsonObject.get("data").getAsString();
        if( type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
            int[] data = SerializationUtils.toIntArray(Base64.getDecoder().decode(dataString.getBytes()));
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_32F || type == CvType.CV_32FC2) {
            float[] data = SerializationUtils.toFloatArray(Base64.getDecoder().decode(dataString.getBytes()));
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_64F || type == CvType.CV_64FC2) {
            double[] data = SerializationUtils.toDoubleArray(Base64.getDecoder().decode(dataString.getBytes()));
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_8U || type == CvType.CV_8UC4) {
            byte[] data = Base64.getDecoder().decode(dataString.getBytes());
            mat.put(0, 0, data);
        }
        else {

            throw new UnsupportedOperationException("unknown type");
        }
        return mat;
    }

    // 4.1.2022 start
    // m1 * m2 = m3
    public static float[] matMul(float[] m1, float[] m2) {
        float[] m3 = new float[16];
        Matrix.multiplyMM(m3, 0, m1, 0, m2, 0);
        return m3;
    }

    public static float[] matInv(float[] m) {
        float[] mInv = new float[16];
        if (Matrix.invertM(mInv, 0, m, 0)) {
            return mInv;
        } else {
            return null;
        }
    }
    // 4.1.2022 end

    public static float[] matInv2(float[] m) {
        float[] mInv = new float[16];
        mInv[0] = m[0];
        mInv[1] = m[4];
        mInv[2] = m[8];
        mInv[3] = 0;
        mInv[4] = m[1];
        mInv[5] = m[5];
        mInv[6] = m[9];
        mInv[7] = 0;
        mInv[8] = m[2];
        mInv[9] = m[6];
        mInv[10] = m[10];
        mInv[11] = 0;
        mInv[12] = -1 * (m[0] * m[12] + m[1] * m[13] + m[2] * m[14]);
        mInv[13] = -1 * (m[4] * m[12] + m[5] * m[13] + m[6] * m[14]);
        mInv[14] = -1 * (m[8] * m[12] + m[9] * m[13] + m[10] * m[14]);
        mInv[15] = 1;

        return mInv;
    }

    public static float[] matRotate(float[] m) {
        float[] mRot = new float[16];
        rotateM(mRot, 0, m, 0, 0.0f, 0, 1, 0);
        return mRot;
    }
}
