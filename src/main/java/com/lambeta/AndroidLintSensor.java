package com.lambeta;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import java.io.File;

public class AndroidLintSensor implements Sensor {
    private final static String LOG_PATH = "D:\\AndroidLintSensor.txt";

    public void describe(SensorDescriptor sensorDescriptor) {
        FileUtil.printLog(LOG_PATH, "describe SensorDescriptor ---------------------------------");

    }

    public void execute(SensorContext sensorContext) {
        FileUtil.printLog(LOG_PATH, "start execute SensorContext ---------------------------------");
        FileUtil.printLog("start run android lint analysis...");
        new AndroidLintProcessor().process(sensorContext);
        FileUtil.printLog("end run android lint analysis.");
        FileUtil.printLog(LOG_PATH, "end execute SensorContext ---------------------------------");
    }
}
