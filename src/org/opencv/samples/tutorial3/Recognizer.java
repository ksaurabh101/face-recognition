package org.opencv.samples.tutorial3;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.io.File;
import java.io.FilenameFilter;
import com.googlecode.javacv.cpp.opencv_contrib.*;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

public class Recognizer {

	FaceRecognizer faceRecognizer;
	String mPath="";
	String imgname="";
	int count=0;
	private int mProb=999;

	public Recognizer(String path){
		 mPath=path;
		 faceRecognizer =  createLBPHFaceRecognizer(2,8,8,8,200);
		 
	}
	public void setmPath(String mPath) {
		this.mPath = mPath;
	}
	public void setImgName(String name)
	{
		imgname=name;
	}
public boolean train() {
        File root = new File(mPath);
        FilenameFilter jpgFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(imgname);
         }
        };
        File[] imageFiles = root.listFiles(jpgFilter);
        MatVector images = new MatVector(imageFiles.length);
        int[] labels = new int[imageFiles.length];
        int counter = 0;
        IplImage img=null;
        IplImage grayImg;
        for (File image : imageFiles) {
        	String p = image.getAbsolutePath();
        	img = cvLoadImage(p);
            grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
            cvCvtColor(img, grayImg, CV_BGR2GRAY);
            images.put(counter, grayImg);
            labels[counter] = counter+1;
            counter++;
        }	
        faceRecognizer.train(images, labels);
	return true;
	}
public void predict(IplImage img) {
	int n[] = new int[1];
	double p[] = new double[1];
	faceRecognizer.predict(img, n, p);
	if (n[0]!=-1)
         mProb=(int)p[0];                
	else
		mProb=-1;
}

public int getProb() {
	return mProb;
}
}
