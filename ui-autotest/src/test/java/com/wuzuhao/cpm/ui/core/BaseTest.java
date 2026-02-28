package com.wuzuhao.cpm.ui.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import java.util.concurrent.TimeUnit;

/**
 * 基础测试类：负责 WebDriver 生命周期和通用操作。
 */
public abstract class BaseTest {

    protected WebDriver driver;

    // 前端开发环境地址（来自启动指南：http://localhost:3000）
    // 如需在生产网关下测试，可改为 http://localhost:8080
    protected String baseUrl = "http://localhost:3000";

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        // 如需固定使用本地 ChromeDriver，可在此处显式指定驱动路径
        // 注意：路径要与实际文件位置一致
        System.setProperty("webdriver.chrome.driver",
                "G:\\Graduation Project\\cpm\\ui-autotest\\drivers\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        // Selenium 3.x 使用 TimeUnit，而不是 Duration
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
        if (!result.isSuccess() && driver != null) {
            takeScreenshot();
        }
    }

    @Step("截图保存到 Allure 报告")
    @Attachment(value = "Failure Screenshot", type = "image/png")
    public byte[] takeScreenshot() {
        if (driver == null) {
            return new byte[0];
        }
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}

