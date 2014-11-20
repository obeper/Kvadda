package kvaddakopter.gui;

import java.util.Date;

import javafx.application.Platform;
import kvaddakopter.gui.controllers.MainController;
import kvaddakopter.gui.interfaces.MainBusGUIInterface;
import kvaddakopter.utils.Clock;

public class GUIWorker implements Runnable{

	
	protected MainBusGUIInterface mainBuss;


	protected MainController mainController;

	
	protected long sampleTime = 1000;

	public GUIWorker(MainController main){
		this.mainController = main;
	}

	@Override
	public void run() {
		
		boolean killThread = false;
		Clock clock = new Clock();
		
		
		
		while(!killThread){
			// VERIFICATION CLOCK
			clock.tic();
			try {


                if( mainController != null){
                	
                	//RUN A MISSION
                	if(mainController.tabUtforController.shouldStart()){
   					 Platform.runLater(new Runnable() {
   						@Override
   						public void run() {
   							mainController.tabUtforController.drawQuadMarker();
   							mainController.tabUtforController.updateTimeLeft(sampleTime);
   						}
   					 });
                	}
                	
                }
                
                Thread.sleep(clock.stopAndGetSleepTime(1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

}