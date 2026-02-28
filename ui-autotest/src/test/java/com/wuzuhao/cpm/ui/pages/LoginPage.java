package com.wuzuhao.cpm.ui.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录页 Page Object
 * 根据实际前端页面元素进行调整定位器。
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait shortWait; // 用于快速检查
    private final JavascriptExecutor js;

    // 根据实际 Login.vue 结构：通过 placeholder 和按钮文本定位
    private final By usernameInput = By.xpath("//input[@placeholder='用户名']");
    private final By passwordInput = By.xpath("//input[@placeholder='密码']");
    private final By loginButton = By.cssSelector("button.el-button--primary");

    // Element Plus 全局错误提示：ElMessage.error() 会在页面顶部显示消息框
    // DOM 结构：<div class="el-message el-message--error"><div class="el-message__content">错误文本</div></div>
    private final By errorMessage = By.cssSelector(".el-message.el-message--error .el-message__content");

    // 更通用的ElMessage定位器（包含所有类型）
    private final By anyMessage = By.cssSelector(".el-message .el-message__content");

    // ElMessage容器定位器
    private final By messageContainer = By.className("el-message");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 8);
        this.shortWait = new WebDriverWait(driver, 2);
        this.js = (JavascriptExecutor) driver;
    }

    @Step("打开登录页：{url}")
    public LoginPage open(String url) {
        driver.get(url);
        // 等待用户名输入框渲染完成
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        return this;
    }

    @Step("输入用户名：{username}")
    public LoginPage typeUsername(String username) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(usernameInput));
        element.clear();
        element.sendKeys(username);
        return this;
    }

    @Step("清空用户名")
    public LoginPage clearUsername() {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(usernameInput));
        element.clear();
        js.executeScript("arguments[0].value = '';", element);
        return this;
    }

    @Step("输入密码")
    public LoginPage typePassword(String password) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(passwordInput));
        element.clear();
        element.sendKeys(password);
        return this;
    }

    @Step("清空密码")
    public LoginPage clearPassword() {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(passwordInput));  // ✅ 修复：使用passwordInput
        element.clear();
        js.executeScript("arguments[0].value = '';", element);
        return this;
    }

    @Step("清空所有输入框")
    public LoginPage clearAllInputs() {
        clearUsername();
        clearPassword();
        return this;
    }

    @Step("点击登录按钮")
    public void clickLogin() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        btn.click();

        // 点击后短暂等待，让ElMessage有足够时间出现
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取错误提示文本 - 使用显式等待确保元素存在DOM中
     */
    @Step("获取错误提示文本")
    public String getErrorMessage() {
        try {
            // 方法1: 先等待消息容器出现
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".el-message.el-message--error")));

            // 然后获取内容元素
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(errorMessage));
            String text = msg.getText();

            // 添加日志
            System.out.println("捕获到错误消息: " + text);

            return text;
        } catch (TimeoutException e) {
            System.out.println("未找到错误消息元素");
            return "";
        }
    }

    /**
     * 获取任意ElMessage提示文本 - 使用更稳健的等待策略
     */
    @Step("获取任意ElMessage提示文本")
    public String getAnyMessage() {
        try {
            // 先等待任何ElMessage容器出现
            wait.until(ExpectedConditions.presenceOfElementLocated(messageContainer));

            // 等待内容元素出现
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(anyMessage));
            String text = msg.getText();

            System.out.println("捕获到消息: " + text);

            return text;
        } catch (TimeoutException e) {
            System.out.println("未找到任何消息元素");
            return "";
        }
    }

    /**
     * 等待特定类型的ElMessage出现并返回其文本
     */
    @Step("等待并获取{messageType}类型的消息")
    public String waitForMessageByType(String messageType) {
        try {
            // 构建消息类型的选择器
            String typeSelector = ".el-message.el-message--" + messageType;

            // 等待消息容器出现
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(typeSelector)));

            // 获取内容
            By messageContent = By.cssSelector(typeSelector + " .el-message__content");
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(messageContent));

            return msg.getText();
        } catch (TimeoutException e) {
            return "";
        }
    }

    /**
     * 等待错误消息出现（专门用于错误类型）
     */
    @Step("等待错误消息出现")
    public String waitForErrorMessage() {
        return waitForMessageByType("error");
    }

    /**
     * 等待成功消息出现
     */
    @Step("等待成功消息出现")
    public String waitForSuccessMessage() {
        return waitForMessageByType("success");
    }

    /**
     * 等待警告消息出现
     */
    @Step("等待警告消息出现")
    public String waitForWarningMessage() {
        return waitForMessageByType("warning");
    }    /**
     * 等待信息消息出现
     */
    @Step("等待信息消息出现")
    public String waitForInfoMessage() {
        return waitForMessageByType("info");
    }

    /**
     * 等待ElMessage消失
     */
    @Step("等待ElMessage消失")
    public boolean waitForMessageToDisappear() {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(messageContainer));
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * 检查ElMessage是否包含特定文本
     */
    @Step("检查ElMessage是否包含文本: {expectedText}")
    public boolean isMessageContainsText(String expectedText) {
        try {
            // 先等待消息出现
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(anyMessage));
            String actualText = msg.getText();

            System.out.println("消息文本: '" + actualText + "', 期望包含: '" + expectedText + "'");

            return actualText.contains(expectedText);
        } catch (TimeoutException e) {
            System.out.println("未找到消息元素，无法检查文本");
            return false;
        }
    }

    /**
     * 获取所有当前显示的ElMessage
     */
    @Step("获取所有当前显示的ElMessage")
    public List<String> getAllMessages() {
        try {
            // 等待至少一个消息出现
            wait.until(ExpectedConditions.presenceOfElementLocated(anyMessage));

            // 获取所有消息内容
            List<WebElement> messages = driver.findElements(anyMessage);

            return messages.stream()
                    .map(WebElement::getText)
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.toList());

        } catch (TimeoutException e) {
            return Collections.emptyList();
        }
    }

    /**
     * 带重试机制的消息获取（最多尝试3次）
     */
    @Step("带重试机制获取消息")
    public String getMessageWithRetry(int maxRetries) {
        String message = "";

        for (int i = 0; i < maxRetries; i++) {
            try {
                // 每次尝试等待2秒
                WebDriverWait retryWait = new WebDriverWait(driver, 2);
                WebElement msg = retryWait.until(ExpectedConditions.presenceOfElementLocated(anyMessage));
                message = msg.getText();

                if (!message.isEmpty()) {
                    System.out.println("第" + (i + 1) + "次尝试成功，获取到消息: " + message);
                    return message;
                }
            } catch (TimeoutException e) {
                System.out.println("第" + (i + 1) + "次尝试未获取到消息");
            }

            // 最后一次尝试后不再等待
            if (i < maxRetries - 1) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return message;
    }
}
