package org.opencv.samples.tutorial3;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class Tutorial3View extends JavaCameraView {

    private static final String TAG = "Sample::Tutorial3View";
    Bitmap picture;
    public Tutorial3View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }
    
    public void setCamFront()
    {
    	 disconnectCamera();
    	 setCameraIndex(org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT );
    	 connectCamera(getWidth(), getHeight());
    }
    public void setCamBack()
    {
    	 disconnectCamera();    	 
    	 setCameraIndex(org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK );
    	 connectCamera(getWidth(), getHeight());
    }

    public int numberCameras()
    {
     return	Camera.getNumberOfCameras();
    }
    
    public void takePicture() {
        Log.i(TAG, "Tacking picture");
        PictureCallback callback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "Saving a bitmap to file");
                picture = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        };

        mCamera.takePicture(null, null, callback);
    }
    public void resume()
    {
    	try {
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveImage(String pname)
    {
    	try
		{
			FileOutputStream out = new FileOutputStream(pname);
            picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    
}
