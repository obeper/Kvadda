package kvaddakopter.image_processing.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kvaddakopter.image_processing.data_types.ColorTemplate;

public class HSVSliders{
		static final int MIN_HUE = 0;
		static final int MAX_HUE = 179;
		
		static final int MIN_SAT = 0;
		static final int MAX_SAT = 255;
		
		static final int MIN_VAL = 0;
		static final int MAX_VAL = 400;
		
		Scene secondScene;
		Stage secondStage;
	public HSVSliders(){
		secondStage = new Stage();
	}
		
	public void setHSVChannels( final  ColorTemplate template, Stage primaryStage){
		Label secondLabel = new Label("HSV Channels");
		Label hueLowLabel = new Label("HUE LOW");
		Label hueHighLabel = new Label("HUE HIGH");
		Label satLowLabel = new Label("SAT LOW");
		Label satHighLabel = new Label("SAT HIGH");
		Label valLowLabel = new Label("VAL LOW");
		Label valHighLabel = new Label("VAL HIGH");
		
		Slider sliderHueLow = new Slider(MIN_HUE,MAX_HUE,30);
		sliderHueLow .setShowTickLabels(true);
		sliderHueLow .setShowTickMarks(true);
		sliderHueLow .setMajorTickUnit(50);
		sliderHueLow .setMinorTickCount(5);
		sliderHueLow .setBlockIncrement(10);
		sliderHueLow.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0,
							Number arg1, Number arg2) {
						synchronized(template){
						template.setHueLow(arg1.intValue());
						}
					}
					});
		
		Slider sliderHueHigh = new Slider(MIN_HUE,MAX_HUE,70);
		sliderHueHigh .setShowTickLabels(true);
		sliderHueHigh .setShowTickMarks(true);
		sliderHueHigh .setMajorTickUnit(50);
		sliderHueHigh .setMinorTickCount(5);
		sliderHueHigh .setBlockIncrement(10);
		sliderHueHigh.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0,
							Number arg1, Number arg2) {
						synchronized(template){
						template.setHueHigh(arg1.intValue());
						}
					}
					});
		
		Slider sliderSatLow = new Slider(MIN_SAT,MAX_SAT,30);
		sliderSatLow .setShowTickLabels(true);
		sliderSatLow .setShowTickMarks(true);
		sliderSatLow .setMajorTickUnit(50);
		sliderSatLow .setMinorTickCount(5);
		sliderSatLow .setBlockIncrement(10);
		sliderSatLow.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0,
							Number arg1, Number arg2) {
						synchronized(template){
						template.setSatLow(arg1.intValue());
						}
					}
					});
		
		Slider sliderSatHigh = new Slider(MIN_SAT,MAX_SAT,70);
		sliderSatHigh .setShowTickLabels(true);
		sliderSatHigh .setShowTickMarks(true);
		sliderSatHigh .setMajorTickUnit(50);
		sliderSatHigh .setMinorTickCount(5);
		sliderSatHigh .setBlockIncrement(10);
		sliderSatHigh.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0,
							Number arg1, Number arg2) {
						synchronized(template){
						template.setSatHigh(arg1.intValue());
						}
					}
					});
		
		Slider sliderValLow = new Slider(MIN_VAL,MAX_VAL,30);
		sliderValLow .setShowTickLabels(true);
		sliderValLow .setShowTickMarks(true);
		sliderValLow .setMajorTickUnit(50);
		sliderValLow .setMinorTickCount(5);
		sliderValLow .setBlockIncrement(10);
		sliderValLow.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0,
							Number arg1, Number arg2) {
						synchronized(template){
						template.setValLow(arg1.intValue());
						}
					}
					});
		
		Slider sliderValHigh = new Slider(MIN_VAL,MAX_VAL,70);
		sliderValHigh .setShowTickLabels(true);
		sliderValHigh .setShowTickMarks(true);
		sliderValHigh .setMajorTickUnit(50);
		sliderValHigh .setMinorTickCount(5);
		sliderValHigh .setBlockIncrement(10);
		sliderValHigh.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0,
							Number arg1, Number arg2) {
						synchronized(template){
						template.setValHigh(arg1.intValue());
						}
					}
					});
        
        StackPane secondaryLayout = new StackPane();
        secondaryLayout.setAlignment(Pos.TOP_CENTER);
        secondaryLayout.getChildren().add(secondLabel);
        
        //Positioning
        hueLowLabel.setTranslateY(20);
        secondaryLayout.getChildren().add(hueLowLabel);
        sliderHueLow.setTranslateY(35);
        sliderHueLow.setMaxWidth(180);
        secondaryLayout.getChildren().add(sliderHueLow);
        
        hueHighLabel.setTranslateY(65);
        secondaryLayout.getChildren().add(hueHighLabel);
        sliderHueHigh.setTranslateY(80);
        secondaryLayout.getChildren().add(sliderHueHigh);
        
        satLowLabel.setTranslateY(110);
        secondaryLayout.getChildren().add(satLowLabel);
        sliderSatLow.setTranslateY(125);
        secondaryLayout.getChildren().add(sliderSatLow);
        
        satHighLabel.setTranslateY(155);
        secondaryLayout.getChildren().add(satHighLabel);
        sliderSatHigh.setTranslateY(170);
        secondaryLayout.getChildren().add(sliderSatHigh);
        
        valLowLabel.setTranslateY(200);
        secondaryLayout.getChildren().add(valLowLabel);
        sliderValLow.setTranslateY(215);
        secondaryLayout.getChildren().add(sliderValLow);
        
        valHighLabel.setTranslateY(245);
        secondaryLayout.getChildren().add(valHighLabel);
        sliderValHigh.setTranslateY(265);
        secondaryLayout.getChildren().add(sliderValHigh);
        
         
        secondScene = new Scene(secondaryLayout, 200, 320);

        secondStage.setTitle("HSVCalibration");
        secondStage.setScene(secondScene);
         
        //Set position of second window, related to primary window.
        secondStage.setX(primaryStage.getX() - 250);
        secondStage.setY(primaryStage.getY() - 100);

        secondStage.show();
	}

}
