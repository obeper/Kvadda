package kvaddakopter.image_processing.programs;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import kvaddakopter.Mainbus.Mainbus;
import kvaddakopter.image_processing.algorithms.DetectionClass;
import kvaddakopter.image_processing.algorithms.Tracking;
import kvaddakopter.image_processing.decoder.DecoderListener;
import kvaddakopter.image_processing.decoder.FFMpegDecoder;
import kvaddakopter.image_processing.utils.ImageConversion;
import kvaddakopter.image_processing.utils.KeyBoardHandler;
import kvaddakopter.image_processing.utils.KeyBoardListener;

import org.opencv.core.Mat;

import com.xuggle.xuggler.demos.VideoImage;


public class ProgramClass implements Runnable,DecoderListener,KeyBoardListener {

	//Create image queue, which is a list that is holding the most recent
	//images
	protected static int ImageQueueSize = 4;
	protected  ArrayList<BufferedImage> mImageQueue  = new ArrayList<BufferedImage>();;

	ImageProcessingMainProgram mDetetctionListener;
	
	//Algorithm
	protected DetectionClass mCurrentMethod;
	protected List<DetectionClass> mDetectionMethodList;
	protected Tracking mTracker;

	//Decoder
	//TODO set shared decoder
	protected FFMpegDecoder mDecoder;
	private boolean toBeReconnected = false;
	//Window
	private static VideoImage mScreen = null;

	//Sleep time / FPS
	private long mSleepTime = 20;
	
	//private volatile Container container;
	protected Mainbus mMainbus;
    protected int mThreadId;
    
	
	// KeyBoard handler
	KeyBoardHandler mKeyBoardHandler = null;

	public ProgramClass(int threadid, Mainbus mainbus) {
		mMainbus = mainbus;
	    mThreadId = threadid;
		init();
	}
	
	/** 
	 *Init function of a program class. <br> 
	 * This function is implicitly called be constructor of the ProgramClass <br>
	 *Example how this function can  be implemented in subclasses: <br>
	 *<code>
	 *<ul>
	 *protected void init(){
	 *<ul>
	//Create and initialize decoder<br>
		mDecoder = new FFMpegDecoder();<br>
		mDecoder.initialize(FFMpegDecoder.STREAM_ADDR_BIPBOP);<br>
	<br>
		// Listen to decoder events<br>
		mDecoder.setDecoderListener(this);<br>
	<br>
		//Start stream on a separate thread<br>
		mDecoder.startStream();<br>
	<br>
		//Open window <br>
		openVideoWindow();<br>
	 *</ul>
	 *}
	 *</ul>
	 *</code> 
	 */
	public void init(){
		System.err.println("ProgramClass: 'init()' not implemented");
		System.exit(0);
	}

	/** 
	 *Update function of a program class. This function is called when there is a fresh new image.  <br>
	 *<br>
	 *Example how this function can  be implemented in subclasses: <br>
	 *<code>
	 *<ul>
	 *protected void update(){
	 *<ul>
	 *Mat currentImage 		  = getNextFrame(); <br>
	 *ImageObject imageObject  = new ImageObject(currentImage); <br>
	 *ArrayList<TargetObjects> = mCurrentMethod.start(imageObject); <br>
	 *</ul>
	 *}
	 *</ul>
	 *</code> 
	 */
	protected  void update(){
		System.err.println("ProgramClass: 'update()' not implemented");
		System.exit(0);
	};

	
	public DetectionClass getCurrentMethod(){
		return mCurrentMethod;
	}
	public void run()  {
		
		//Start program
		while(true){
			if(!isImageQueueEmpty()){
				update();
			}
			try {
				Thread.sleep(mSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(toBeReconnected){
				System.err.println("FFMPEG decoder: Connection to decoder lost. Trying to reconnect...");
				mDecoder.stopStream();
				mDecoder.reinitialize();
				mDecoder.startStream();
				System.err.println("FFMPEG decoder: Reconnected sucessfully");
				toBeReconnected = false;
				
			}
		}
	}

	protected void setSleepTime(long t){
		mSleepTime = t;
	}

	protected void openVideoWindow(){
		mScreen = new VideoImage();
	}

	protected static void updateJavaWindow(BufferedImage javaImage)
	{
		if(mScreen != null){
			mScreen.setImage(javaImage);
		}
	}

	protected boolean isImageQueueEmpty(){
		synchronized (mImageQueue) {
			return mImageQueue.isEmpty();
		}
	}

	protected Mat getNextFrame(){
		Mat matImage = null;
		synchronized (mImageQueue) {
			if(mImageQueue.size() > 0){

				//LIFO queue
				BufferedImage img = mImageQueue.get(0);

				//Remove image from 
				mImageQueue.remove(0);

				//Conversion from BufferedImage to Mat
				matImage = ImageConversion.img2Mat(img);
			}
		}
		return matImage;
	}

	// Decoder Events
	@Override
	public boolean onFrameRecieved(BufferedImage image) {

		synchronized (mImageQueue) {
			// If buffer is full, remove oldest image to make space 
			// for the new incoming image 
			if(mImageQueue.size() >= ImageQueueSize)
				mImageQueue.remove(0);

			mImageQueue.add(image);

			return mImageQueue.size() >= ImageQueueSize;
		}
	}

	@Override
	public void onConnectionLost(boolean manualStop) {
		// If decoder was shutdown by unknown reasons. Automatically
		// reconnect.
			toBeReconnected = !manualStop;
	}
	@Override
	public void onKeyBoardInput(String inputString) {};

}
