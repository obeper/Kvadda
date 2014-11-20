package kvaddakopter.interfaces;

import kvaddakopter.assignment_planer.MissionObject;
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
	 * 	Sets a mission object to the mainbus
	 */
	public void setMissionObject(MissionObject MO);
	
	/**
	 * Fetches the mission object from the mainbus
	 * @return
	 */
	public MissionObject getMissionObject();
	
}
