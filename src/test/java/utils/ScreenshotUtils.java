package utils;

import base.DriverFactory;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;

public class ScreenshotUtils {
    public static File takePngToFile(AndroidDriver driver, File outFile) throws Exception {
        if (driver == null) driver = DriverFactory.getDriver();
        File tmp = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(tmp, outFile);
        return outFile;
    }
}
