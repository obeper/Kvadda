package kvaddakopter.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.image.Image;
import kvaddakopter.assignment_planer.MissionObject;
import kvaddakopter.image_processing.data_types.TargetObject;
import kvaddakopter.maps.GPSCoordinate;

public interface MainBusGUIInterface {
	
	
	/**
	 * Current speed as a double 
	 * eg. 5 km/h
	 * @return
	 */
	public double getCurrentSpeed();
	
	
	/**
	 * Quads current gps position.
	 * @return
	 */
	public GPSCoordinate getCurrentQuadPosition();
	
	
	/**
	 * Wifi-link working?
	 * @return
	 */
	public boolean wifiFixOk();
	
	/**
	 * GPS working?
	 */
	public boolean gpsFixOk();
	
	/**
	 * Starts AssignmentPlaner
	 */
	public void setAssignmentPlanerOn(boolean state);
	
	/**
	 * Checks the status of the AssignmentPlaner
	 * @return
	 */
	public boolean isAssignmentPlanerOn();
	
	
	/**
	 * 	Sets a mission object to the mainbus
	 */
	public void setMissionObject(MissionObject MO);
	
	/**
	 * Fetches the mission object from the mainbus
	 * @return
	 */
	public MissionObject getMissionObject();
	
	/**
	 * Fetches the image on mainbus
	 * @return
	 */
	public Image getImage();
	
	/**
	 * Sets emergency stop to newBool
	 * @param newBool
	 */
	public void setEmergencyStop(boolean newBool);


	/**
	 * Gets current speed
	 * @return speed
	 */
	public float getSpeed();


	/**
	 * Get current battery level
	 * @return battery level
	 */
	public float getBattery();
	
	/**
	 * Get list of targets detected by image processing
	 * @return targetList
	 */
	public HashMap<String,GPSCoordinate> getTargets();
	
}
