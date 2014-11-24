package kvaddakopter.image_processing.algorithms;

import java.util.ArrayList;

import kvaddakopter.image_processing.data_types.FormTemplate;
import kvaddakopter.image_processing.data_types.ImageObject;
import kvaddakopter.image_processing.data_types.TargetObject;
import kvaddakopter.image_processing.utils.MatchesHelpFunctions;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

public class TemplateMatch  extends DetectionClass{


	ArrayList<FormTemplate> mTemplates;
	Mat mBoxPoints;

	public TemplateMatch() {
		
	}
	
	public void setTemplates(ArrayList<FormTemplate> templates){
		mTemplates = templates;
	}

	@Override
	public ArrayList<TargetObject> runMethod(ImageObject imageObject) {

		ArrayList<TargetObject> targetObjects = new ArrayList<TargetObject>();

		/* Start of with computing properties of the template image */
		for(FormTemplate template: mTemplates){
			if(!template.getImageObject().hasKeyPoints()){
				template.getImageObject().computeKeyPoints(FeatureDetector.SIFT);
				mIntermeditateResult = template.getImageObject().getImage().clone();
				//estimateAndRemoveOutlier(template.getKeyPoints(),mIntermeditateResult,0.12,0.35);

				template.getImageObject().computeDescriptors(DescriptorExtractor.SIFT);
				return targetObjects;
			}
		}

		//Compute KP and descriptors for incoming image
		imageObject.computeKeyPoints(FeatureDetector.SIFT);
		imageObject.computeDescriptors(DescriptorExtractor.SIFT);

		for(FormTemplate template: mTemplates){
			int minumumRequiredMatches = 4;
			MatOfDMatch matches = template.getImageObject().findMatches(imageObject, minumumRequiredMatches);
			if(matches != null){

				// Save copy of key points
				MatOfKeyPoint keyPointsTemplate = new MatOfKeyPoint();
				MatOfKeyPoint keyPointsVideo = new MatOfKeyPoint();
				Mat homo = MatchesHelpFunctions.findHomography_EXT(matches, template.getImageObject().getKeyPoints(), imageObject.getKeyPoints(),keyPointsTemplate,keyPointsVideo);

				if(homo != null){
					Mat transformBoxPoints = new Mat();
					Core.perspectiveTransform(mBoxPoints, transformBoxPoints,homo);
					mIntermeditateResult = imageObject.getImage().clone();
					for (int i = 0; i < 4; i++) {
						Point start = new Point(transformBoxPoints.get(i, 0));
						Point end = new Point(transformBoxPoints.get((i+1) % 4, 0));
						
						Core.line(mIntermeditateResult, start, end, new Scalar(255,0,0,255), 3);
					}

					mIntermeditateResult = new Mat();
					Features2d.drawMatches(
							template.getImageObject().getImage(),
							keyPointsTemplate,  
							imageObject.getImage(),
							keyPointsVideo, 
							matches,
							mIntermeditateResult
							);
				}

			}
		}


		return targetObjects;
	}

	
	/**
	 * Can be used when calibrating template object
	 * @param kp
	 * @param image
	 * @param cutoffX
	 * @param cutOffY
	 */
	private void calibrateCurrentImage(FormTemplate template){
		Mat image = template.getImageObject().getImage().clone();
		KeyPoint[] kpArray = template.getImageObject().getKeyPoints().toArray();
		
		
		int numKeyPoints = kpArray.length;
		double imageWidth = image.cols();
		double imageHeight = image.rows();

		double x[] = new double[numKeyPoints];
		double y[] = new double[numKeyPoints];
		
		double[] boxCenter = template.getScaledBoxCenter(imageWidth, imageHeight);
		double[] boxSize   = template.getScaledBoxSize(imageWidth, imageHeight);
		
		boxSize[0] /=2.0;
		boxSize[1] /=2.0;
		
		for (int i = 0; i < numKeyPoints; i++) {
			x[i] = kpArray[i].pt.x;
			y[i] = kpArray[i].pt.y;
		}
		
		int[] indicesTemp = new int[numKeyPoints];
		int ptr = 0;
		for (int i = 0; i < numKeyPoints; i++) {
			double distanceX = Math.abs(x[i] - boxCenter[0]);
			double distanceY = Math.abs(y[i] - boxCenter[1]);
			if(distanceX < boxSize[0] && distanceY < boxSize[1]){	
				indicesTemp[ptr++] = i;
			}
		}
		int indices[] = new int[ptr];
		System.arraycopy(indicesTemp, 0, indices, 0, ptr);
		KeyPoint[] kpArrayRefined = new KeyPoint[indices.length];
		
		for (int i = 0; i < kpArrayRefined.length; i++) {
			kpArrayRefined[i] = kpArray[indices[i]]; 
		}
		
		template.getImageObject().getKeyPoints().fromArray(kpArrayRefined);

		drawSome(boxCenter,boxSize,image);
		Features2d.drawKeypoints(image, template.getImageObject().getKeyPoints(), image);
		mIntermeditateResult = image;

	}

	/**
	 * Debug drawing function
	 * 
	 * @param mean
	 * @param var
	 * @param image
	 * @param discardDistanceX
	 * @param discardDistanceY
	 */
	private void drawSome(double[] mean,double [] var,Mat image){
		double xMean = mean[0];
		double yMean = mean[1];
		double xVar = var[0];
		double yVar = var[1];
		double imageWidth = image.cols();
		double imageHeight = image.rows();
		double startCoord[] = new double[2];
		if(xMean - xVar > 0) startCoord[0]= xMean - xVar;
		if(yMean - yVar > 0) startCoord[1]= yMean - yVar;
		Point start = new Point(startCoord);

		double endCoord[] = new double[2];
		endCoord[0] = imageWidth;
		endCoord[1] = imageHeight;
		if(xMean + xVar < imageWidth) endCoord[0]= xMean +  xVar;
		if(yMean + yVar < imageHeight) endCoord[1]= yMean + yVar;
		Point end = new Point(endCoord);
		Core.rectangle(image, start, end, new Scalar(255, 0, 0, 255), 3);
		
		double p0[] = new double[]{xMean - xVar,yMean - yVar};
		double p1[] = new double[]{xMean + xVar,yMean - yVar};
		double p2[] = new double[]{xMean + xVar,yMean + yVar};
		double p3[] = new double[]{xMean - xVar,yMean + yVar};

		mBoxPoints = new Mat(4,1,CvType.CV_64FC2); 
		mBoxPoints.put(0, 0, p0);
		mBoxPoints.put(1, 0, p1);
		mBoxPoints.put(2, 0, p2);
		mBoxPoints.put(3, 0, p3);

	}



	public void calibrateTemplate(FormTemplate formTemplate) {
		if(formTemplate != null){
			/* Start of with computing properties of the template image */
			formTemplate.getImageObject().computeKeyPoints(FeatureDetector.SIFT);
			calibrateCurrentImage(formTemplate);
			formTemplate.getImageObject().computeDescriptors(DescriptorExtractor.SIFT);
			formTemplate.setCalibrated(true);
		}else{
			mIntermeditateResult = new Mat();
		}
	}

}

/*
 * 
 * 	public int[] computeMeanAndVar(double[] x,double[] y,double discardDistanceX,double discardDistanceY,double meanOut[],double varOut[]){
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

		//computeMeanAndVar(x, y,discardDistanceX, discardDistanceY ,mean,var);

		drawSome(mean,var,image);

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
 */

