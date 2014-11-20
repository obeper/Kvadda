package kvaddakopter.image_processing.algorithms;

import java.util.ArrayList;

import kvaddakopter.image_processing.data_types.ImageObject;
import kvaddakopter.image_processing.data_types.TargetObject;
import kvaddakopter.image_processing.utils.MatchesHelpFunctions;

import org.ejml.simple.SimpleMatrix;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

public class TemplateMatch  extends DetectionClass{


	ArrayList<ImageObject> mTemplateImageObjects;

	static double MagicVarianceScaleX = 2.0;
	static double MagicVarianceScaleY = 2.0;

	double mDetectionWidth = 1.0;
	double mDetectionHeight = 1.0;

	public TemplateMatch() {
		mTemplateImageObjects = new ArrayList<ImageObject>();
	}

	/**
	 * Read an image from disk. Compute keypoints <br>
	 * and descriptors. 
	 * (Maybe try to throw away keypoints background
	 * detected in the background )  
	 * 
	 * @param filePath filepath
	 */
	public void addNewTemplateImage(String filePath){

		Mat templateImage = Highgui.imread(filePath);
		ImageObject templateObject= new ImageObject(templateImage);
		mTemplateImageObjects.add(templateObject);
	}

	@Override
	public ArrayList<TargetObject> runMethod(ImageObject imageObject) {

		ArrayList<TargetObject> targetObjects = new ArrayList<TargetObject>();

		/* Start of with computing properties of the template image */
		for(ImageObject template: mTemplateImageObjects){
			if(!template.hasKeyPoints()){
				template.computeKeyPoints(FeatureDetector.SIFT);
				mIntermeditateResult = template.getImage().clone();
				estimateAndRemoveOutlier(template.getKeyPoints(),mIntermeditateResult,0.3,0.3);

				template.computeDescriptors(DescriptorExtractor.SIFT);
				return targetObjects;
			}
		}

		//Compute KP and descriptors for incoming image
		imageObject.computeKeyPoints(FeatureDetector.SIFT);
		imageObject.computeDescriptors(DescriptorExtractor.SIFT);

		for(ImageObject template: mTemplateImageObjects){
			int minumumRequiredMatches = 4;
			MatOfDMatch matches = template.findMatches(imageObject, minumumRequiredMatches);
			if(matches != null){

				//Retrieve inlier key points
				MatOfKeyPoint kpTemplateInlier = new MatOfKeyPoint();
				MatOfKeyPoint kpVideoStreamInlier = new MatOfKeyPoint();
				MatchesHelpFunctions.getInlierKeypoints(template.getKeyPoints(), imageObject.getKeyPoints(), kpTemplateInlier, kpVideoStreamInlier, matches);
				
				//Estimating center and size of object
				Rect rect = determineDetectionCenter(kpVideoStreamInlier,imageObject.getImage());
				mIntermeditateResult = imageObject.getImage().clone();
				//Adding detected object into list
//				TargetObject target = new TargetObject(rect, 0, null);
//				targetObjects.add(target);
				

			}
		}


		return targetObjects;
	}

	private Rect determineDetectionCenter(MatOfKeyPoint kp,Mat image){
		KeyPoint[] kpArray = kp.toArray();
		int numKeyPoints = kpArray.length;

		double imageWidth = image.cols();
		double imageHeight = image.rows();

		double x[] = new double[numKeyPoints];
		double y[] = new double[numKeyPoints];

		for (int i = 0; i < numKeyPoints; i++) {
			x[i] = kpArray[i].pt.x;
			y[i] = kpArray[i].pt.y;
		}
		double mean[] = new double[2];
		double var[] = new double[2];

		double discardDistanceX = mDetectionWidth*imageWidth;
		double discardDistanceY = mDetectionHeight*imageHeight;

		computeMeanAndVar(x, y,discardDistanceX, discardDistanceY ,mean,var);

		drawSome(mean,var,image,mDetectionWidth,mDetectionHeight);

		mDetectionWidth -= (mDetectionWidth  - var[0]*MagicVarianceScaleX);
		mDetectionHeight-= (mDetectionHeight - var[1]*MagicVarianceScaleY);

		// Pack into Rect
		double startCoord[] = new double[2];
		startCoord[0]= mean[0] - MagicVarianceScaleX*var[0];
		startCoord[1]= mean[1] - MagicVarianceScaleY*var[1];
		Point start = new Point(startCoord);
		double endCoord[] = new double[2];
		endCoord[0]= mean[0] + MagicVarianceScaleX*var[0];
		endCoord[1]= mean[1] + MagicVarianceScaleY*var[1];
		Point end = new Point(endCoord);
		Rect rect = new Rect(start, end);

		return rect;
	}
	/**
	 * Can be used when calibrating template object
	 * @param kp
	 * @param image
	 * @param cutoffX
	 * @param cutOffY
	 */
	private void estimateAndRemoveOutlier(MatOfKeyPoint kp,Mat image,double cutoffX,double cutOffY){

		KeyPoint[] kpArray = kp.toArray();
		int numKeyPoints = kpArray.length;

		double imageWidth = image.cols();
		double imageHeight = image.rows();

		double x[] = new double[numKeyPoints];
		double y[] = new double[numKeyPoints];

		for (int i = 0; i < numKeyPoints; i++) {
			x[i] = kpArray[i].pt.x;
			y[i] = kpArray[i].pt.y;
		}
		double mean[] = new double[2];
		double var[] = new double[2];

		double discardDistanceX = cutoffX*imageWidth;
		double discardDistanceY = cutOffY*imageHeight;

		int indices[] = computeMeanAndVar(x, y,discardDistanceX, discardDistanceY ,mean,var);

		KeyPoint[] kpArrayRefined = new KeyPoint[indices.length];
		for (int i = 0; i < kpArrayRefined.length; i++) {
			kpArrayRefined[i] = kpArray[indices[i]]; 
		}
		kp.fromArray(kpArrayRefined);

		//		drawSome(mean,var,image,discardDistanceX,discardDistanceY);
		//		Features2d.drawKeypoints(image, kp, image);


	}

	//	Features2d.drawMatches(
	//	template.getImage(),
	//	kpTemplateInlier,  
	//	imageObject.getImage(),
	//	kpVideoStreamInlier, 
	//	matches,
	//	mIntermeditateResult
	//	);
	/**
	 * Debug drawing function
	 * 
	 * @param mean
	 * @param var
	 * @param image
	 * @param discardDistanceX
	 * @param discardDistanceY
	 */
	private void drawSome(double[] mean,double [] var,Mat image,double discardDistanceX ,double discardDistanceY){
		double xMean = mean[0];
		double yMean = mean[1];
		double xVar = var[0];
		double yVar = var[1];
		double imageWidth = image.cols();
		double imageHeight = image.rows();
		double startCoord[] = new double[2];
		double scale = 2.0;
		if(xMean - scale*xVar > 0) startCoord[0]= xMean - scale*xVar;
		if(yMean - scale*yVar > 0) startCoord[1]= yMean - scale*yVar;
		Point start = new Point(startCoord);

		double endCoord[] = new double[2];
		endCoord[0] = imageWidth;
		endCoord[1] = imageHeight;
		if(xMean + scale*xVar < imageWidth) endCoord[0]= xMean + scale*xVar;
		if(yMean + scale*yVar < imageHeight) endCoord[1]= yMean + scale*yVar;
		Point end = new Point(endCoord);
		// draw enclosing rectangle (all same color, but you could use variable i to make them unique)
		Core.rectangle(image, start, end, new Scalar(255, 0, 0, 255), 3);


		startCoord = new double[2];
		scale = 2.0;
		if(xMean - discardDistanceX > 0) startCoord[0]= xMean - discardDistanceX;
		if(yMean - discardDistanceY > 0) startCoord[1]= yMean - discardDistanceY;
		start = new Point(startCoord);

		endCoord = new double[2];
		endCoord[0] = imageWidth;
		endCoord[1] = imageHeight;
		if(xMean + discardDistanceX < imageWidth) endCoord[0]= xMean + discardDistanceX;
		if(yMean + discardDistanceY< imageHeight) endCoord[1]= yMean + discardDistanceY;
		end = new Point(endCoord);
		//	Core.rectangle(image, start, end, new Scalar(255, 0, 255, 0), 3);


	}

	public int[] computeMeanAndVar(double[] x,double[] y,double discardDistanceX,double discardDistanceY,double meanOut[],double varOut[]){
		int numPoints = x.length;

		double xMeanRaw = 0.0;
		double yMeanRaw = 0.0;

		for (int i = 0; i < numPoints; i++) {
			xMeanRaw += x[i];
			yMeanRaw += y[i];
		}

		xMeanRaw /= (double)numPoints;
		yMeanRaw /= (double)numPoints;

		double xMean= 0.0;
		double yMean= 0.0;

		double inlierX = 0.0;
		double inlierY = 0.0;
		int[] indicesTemp = new int[numPoints];
		int ptr = 0;
		for (int i = 0; i < numPoints; i++) {
			double distanceX = Math.abs(x[i] - xMeanRaw);
			double distanceY = Math.abs(y[i] - yMeanRaw);
			if(distanceX < discardDistanceX && distanceY < discardDistanceY){	
				xMean += x[i];
				inlierX++;

				yMean += y[i];
				inlierY++;

				indicesTemp[ptr++] = i;
			}
		}
		int indices[] = new int[ptr];
		System.arraycopy(indicesTemp, 0, indices, 0, ptr);
		xMean /= inlierX;
		yMean /= inlierY;

		double xVar = 0.0;
		double yVar = 0.0;

		for (int i = 0; i < indices.length; i++) {
			xVar += Math.abs(x[indices[i]] - xMean);
			yVar += Math.abs(y[indices[i]] - yMean);
		}
		xVar /= (double)numPoints;
		yVar /= (double)numPoints;
		meanOut[0] = xMean;
		meanOut[1] = yMean;

		varOut[0] = xVar;
		varOut[1] = yVar;

		return indices;
	}
}

