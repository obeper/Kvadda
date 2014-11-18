package kvaddakopter.control_module;
import kvaddakopter.control_module.modules.*;
import kvaddakopter.control_module.signals.*;


/*Initialize
* 1: Read sensor data and set origo @ intilong init lat
* 2: Initialize kalmanfilter
* 3:
*/
		

/*Sensor fusion loop
* 1: For every sample:
* 
* 	i: Read latest sensor data
* 	ii: Transform data into fixed coordinate system
* 	iii: Estimate positions with kalmanfilter (time update) in X and Y direction.
* 	iv: Transform states into quad velocities.

* 2: For every 1/sampletime sample
* 	i: Read new gps measurement.
* 	ii: Transform into fixed coordinate system
* 	iii: Estimate positions with kalmanfilter (measurement update) in X and Y direction.
* 	iv: Transform states into quad velocities.
*/







public class Sensorfusionmodule {
	public double 				 sampletime		= 1;
	public double 				 time;
	public SensorData 			 sdata			= new SensorData();
	public Kalmanfilter			 skalmanx 		= new Kalmanfilter(sampletime,1,1,0,0);
	public Kalmanfilter			 skalmany 		= new Kalmanfilter(sampletime,1,1,0,0);
	public RefinedSensorData 	 rsdata  		= new RefinedSensorData();

	
	public void start(){

//		//First Read
		sdata.setGPSposition(new double[]{58.400991,15.595099});	// New GPS measurement
		sdata.setydot(0);											// New velocity read (ydot)
		sdata.setxdot(0);											// New velocity read (xdot)
		sdata.setYaw(0);											// New Yaw read
		sdata.setHeight(4);											// New Height measurement
		
		sdata.setinitial();											// Fix local coordinate system XY
		sdata.GPS2XY();												// Transformation GPS to XY coordinates
		sdata.xydot2XYdot();										// Transformation velocities to XY(dot)

		
		//Set initials
		rsdata.setXstates(skalmanx.timeupdate(sdata.getXdot())); 	// Kalmanfilter in X direction
		rsdata.setYstates(skalmany.timeupdate(sdata.getYdot())); 	// Kalmanfilter in Y direction
		rsdata.setYaw(sdata.getYaw());								// Set Yaw
		rsdata.setHeight(sdata.getHeight());						// Set Height
		rsdata.XYdot2Vel();											//Transform Xdot,Ydot to velocities
			
		//Sensor fusion loop ------------------------------------------			
				while(true)
				{
				time = System.currentTimeMillis();	
				//For every sample  -_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
			    //Statements
				sdata.setydot(0.2);									//read sdata
				sdata.setxdot(0.1);									//read sdata
				sdata.xydot2XYdot();								//transformation
				
				//Update states from kalmanfilter	
				rsdata.setXstates(skalmanx.timeupdate(sdata.getXdot())); //Kalmanfilter in X direction
				rsdata.setYstates(skalmany.timeupdate(sdata.getYdot())); //Kalmanfilter in Y direction
				rsdata.XYdot2Vel();										 //Transform Xdot,Ydot to velocities
				// -_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
				
				if(true)
				{
				//For every new GPS measurement-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-	
				sdata.setGPSposition(new double[]{58.401122,15.595410});	// New GPS measurement
				sdata.GPS2XY();												// Transformation GPS to XY coordinates
				
				rsdata.setXstates(skalmanx.measurementupdate(sdata.getX()));// Measurement update in kalmanfilter
				rsdata.setYstates(skalmany.measurementupdate(sdata.getY()));// Measurement update in kalmanfilter
				rsdata.XYdot2Vel();											//Transform Xdot,Ydot to velocities
				// -_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
				}
				time = sampletime - (System.currentTimeMillis()-time);
				if(time>0){
				
				try {
					Thread.sleep((long) time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
				}
				
				
				
				
 // Test
/*				//First Read
				sdata.setGPSposition(new double[]{58.401122,15.595410});	// New GPS measurement
				sdata.setydot(1);											// New velocity read (ydot)
				sdata.setxdot(0);											// New velocity read (xdot)
				sdata.setYaw(0);											// New Yaw read
				sdata.setHeight(4);											// New Height measurement
				
				sdata.setinitial();											// Fix local coordinate system XY
				sdata.GPS2XY();												// Transformation GPS to XY coordinates
				sdata.xydot2XYdot();										// Transformation velocities to XY(dot)
				sdata.print();
				
				//Refined
				rsdata.setXstates(skalmanx.timeupdate(sdata.getXdot())); 	// Kalmanfilter in X direction
				rsdata.setYstates(skalmany.timeupdate(sdata.getYdot())); 	// Kalmanfilter in Y direction				
				rsdata.setYaw(sdata.getYaw());								// Set Yaw
				rsdata.setHeight(sdata.getHeight());						// Set Height
				rsdata.XYdot2Vel();											//Transform Xdot,Ydot to velocities
				rsdata.print();
				
				
				//Refined
				rsdata.setXstates(skalmanx.timeupdate(sdata.getXdot())); 	// Kalmanfilter in X direction
				rsdata.setYstates(skalmany.timeupdate(sdata.getYdot())); 	// Kalmanfilter in Y direction				
				rsdata.setYaw(sdata.getYaw());								// Set Yaw
				rsdata.setHeight(sdata.getHeight());						// Set Height
				rsdata.XYdot2Vel();											//Transform Xdot,Ydot to velocities
				rsdata.print();
		
				rsdata.setXstates(skalmanx.measurementupdate(0));			// Measurement update in kalmanfilter
				rsdata.setYstates(skalmany.measurementupdate(0.5));			// Measurement update in kalmanfilter
				rsdata.XYdot2Vel();
				rsdata.print();
				
				rsdata.setXstates(skalmanx.timeupdate(sdata.getXdot())); 	// Kalmanfilter in X direction
				rsdata.setYstates(skalmany.timeupdate(sdata.getYdot())); 	// Kalmanfilter in Y direction				
				rsdata.setYaw(sdata.getYaw());								// Set Yaw
				rsdata.setHeight(sdata.getHeight());						// Set Height
				rsdata.XYdot2Vel();											//Transform Xdot,Ydot to velocities
				rsdata.print();*/
				
	}
	
}
