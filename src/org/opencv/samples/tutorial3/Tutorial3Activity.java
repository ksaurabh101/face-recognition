package org.opencv.samples.tutorial3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_contrib.*;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Tutorial3Activity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";
    
    Recognizer recog;
    private Tutorial3View mOpenCvCameraView;
    private MenuItem               nBackCam;
    private MenuItem               mFrontCam;
  
    String path=" ";
    String img2path;
    
    Bitmap myBitmap,saveBitmap,bitMap;
	ImageView iv1,iv2;
	EditText name;
	Button go,save;
	int status,flag,imgbutton,imgview1,imgview2,clickflag;
	private Mat mRgba;
    private Mat mGray;
    
    private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
    private float mRelativeFaceSize   = 0.2f;
    private int  mAbsoluteFaceSize   = 0;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    
                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Tutorial3Activity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Tutorial3Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        
        name=(EditText) findViewById(R.id.editText1);
        go=(Button) findViewById(R.id.button5);
        save=(Button) findViewById(R.id.button4);
        iv1=(ImageView) findViewById(R.id.imageView1);
        iv2=(ImageView) findViewById(R.id.imageView2);
        
        name.setVisibility(View.INVISIBLE);
        go.setVisibility(View.INVISIBLE);
        iv1.setVisibility(View.INVISIBLE);
        iv2.setVisibility(View.INVISIBLE);
        
		recog=new Recognizer(path);
		clickflag=1;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    	mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
    	mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        int height = mGray.rows();
        if (Math.round(height * mRelativeFaceSize) > 0) {
            mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
        }
        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null){
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            {
        	Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
            }
        
        return mRgba;
    } 
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		if (mOpenCvCameraView.numberCameras()>1)
        {
        nBackCam = menu.add("FrontCamera");
        mFrontCam = menu.add("BackCamera");
        }
       
        return true;
	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Select Items " + item);
        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        if (item == nBackCam)
        {
        	mOpenCvCameraView.setCamFront();
        }
        else if (item==mFrontCam)
        {
        	mOpenCvCameraView.setCamBack();
        	
        }
       
        item.setChecked(true);
       
        return true;
    }
	
    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(clickflag==2)
    	{
        	Log.i(TAG,"onTouch event");
        	mOpenCvCameraView.resume();
        	clickflag=1;
        	return false;
    	}
        else{
        	Toast.makeText(this, "Already Resumed..!!", Toast.LENGTH_SHORT).show();
        	return false;
        }
    }
    public void click(View v)
    {
    	if(clickflag==1)
        {
    		mOpenCvCameraView.takePicture();
    		Toast.makeText(this, "Image Has Been Clicked", Toast.LENGTH_SHORT).show();
    		flag=1;
    		clickflag=2;
        }
    	else{
    		Toast.makeText(this, "First Resume Video By Touch..!!", Toast.LENGTH_SHORT).show();
    	}
    }
    public void show(View v)
    {
    	switch(flag)
		{
		case 1:
		{
			name.setVisibility(View.VISIBLE);
			go.setVisibility(View.VISIBLE);
			status=1;
			break;
		}
		case 2:
		{
			Toast.makeText(this, "Image Is Already Saved", Toast.LENGTH_SHORT).show();
			break;
		}
		default :
		{
			Toast.makeText(this, "First Click A Image", Toast.LENGTH_SHORT).show();
		}
		}
    }
    public void insert1(View v)
	{
    	imgbutton=1;
    	name.setVisibility(View.VISIBLE);
		go.setVisibility(View.VISIBLE);
		status=2;
	}
    public void insert2(View v)
	{
    	imgbutton=2;
    	name.setVisibility(View.VISIBLE);
		go.setVisibility(View.VISIBLE);
		status=3;
	}
    public void saveAndGetImage(View v)
	{
		
		String pname=name.getText().toString();
		if(pname.length()>0)
		{
			name.setText("");
			name.setVisibility(View.INVISIBLE);
			go.setVisibility(View.INVISIBLE);
			File imdir=null;
			char c=pname.charAt(0);
			int f=(int)c;
			String first=pname.substring(0,1);;
			if(f>=97 && f<=122 || f>=65 && f<=90)
			{
				File im_dir=getImageDirectory();
				imdir = new File(im_dir,first);
				if (!imdir.exists()) 
				{
					imdir.mkdir();
				}
				path=imdir.getAbsolutePath();
			switch(status)
			{
			case 1:
			{	
				File imfile=new File(imdir,pname);
				String filename=imfile.getPath()+".jpg";
				final String imgName=pname+".jpg";
				FilenameFilter jpgFilter = new FilenameFilter() {
		            @Override
		            public boolean accept(File dir, String name) {
		                return name.equalsIgnoreCase(imgName);
		         }
		        };
		        File[] imageFiles = imdir.listFiles(jpgFilter);
		        int size=imageFiles.length;
		        if(size>0)
		        {
		        	Toast.makeText(this, "An Image is Already Saved of this name..!!", Toast.LENGTH_SHORT).show();
		        	flag=1;
		        }
		        else{
		        	mOpenCvCameraView.saveImage(filename);
		        	Toast.makeText(this, "Image has been Saved", Toast.LENGTH_LONG).show();
		        	flag=2;
		        }
			}
			break;
			case 2:
			{	
				String s=pname+".jpg";
				insert(s,imdir);
				imgview1=1;
			}
			break;
			case 3:
			{
				String s=pname+".jpg";
				insert(s,imdir);
				imgview2=1;
			}
			break;
			default :
				break;
			}
			}
			else{
				Toast.makeText(this, "Please Enter A Valid Name", Toast.LENGTH_SHORT).show();
			}
			
		}
		else{
			Toast.makeText(this, "Enter Image Name Then Go !!", Toast.LENGTH_SHORT).show();
		}
	}
    public File getImageDirectory() {
		File root = Environment.getExternalStorageDirectory();
		File im_dir = new File(root, "facerec");
		if (!im_dir.exists()) 
		{
		im_dir.mkdir();
		}
		return im_dir;
	}
		public void insert(String s,File imdir)
		{
			File imfile=new File(imdir.getAbsolutePath()+"/"+s);
			String filename=imfile.getPath();
			if(imfile.exists())
			{
				try {
					if(imgbutton==1)
					{
						recog.setmPath(path);
						recog.setImgName(s);
						bitMap = BitmapFactory.decodeFile(filename);
			            iv1.setImageBitmap(bitMap);
			            iv1.setVisibility(View.VISIBLE);
			            imgbutton=0;
					}
					else if(imgbutton==2)
					{
						img2path=filename;
						bitMap = BitmapFactory.decodeFile(filename);
			            iv2.setImageBitmap(bitMap);
			            iv2.setVisibility(View.VISIBLE);
			            imgbutton=0;
					}
		            Toast.makeText(this, "Image has been Inserted", Toast.LENGTH_SHORT).show();
		        }  
		        catch (Exception e) {
		            e.printStackTrace();
		        }
			}
			else{
				Toast.makeText(this, "Sorry !! There is No Image Of This Name", Toast.LENGTH_SHORT).show();
			}
		}
		public void verify(View v)
		{
			if(imgview1==1 && imgview2==1)
			{
				recog.train();
				IplImage img;
				img=cvLoadImage(img2path);
				IplImage grayImg;
				grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);        
				cvCvtColor(img, grayImg, CV_BGR2GRAY);
				recog.predict(grayImg);
				int prob;
				prob=recog.getProb();
				if(prob<0)
					Toast.makeText(this,"Not Same", Toast.LENGTH_SHORT).show();
				else if(prob<50)
					Toast.makeText(this,"Same-Variance="+prob, Toast.LENGTH_SHORT).show();
				else if(prob<80)
					Toast.makeText(this,"Variance="+prob, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this,"Not Same-Variance="+prob, Toast.LENGTH_SHORT).show();
			}
			else{
				if(imgview1!=1)
				{
					Toast.makeText(this,"Image 1 Is Not Inserted..!!", Toast.LENGTH_SHORT).show();
				}
				else if(imgview2!=1)
				{
					Toast.makeText(this,"Image 2 Is Not Inserted..!!", Toast.LENGTH_SHORT).show();
				}
			}
		}
}
