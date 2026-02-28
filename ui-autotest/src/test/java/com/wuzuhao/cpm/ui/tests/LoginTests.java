package com.wuzuhao.cpm.ui.tests;

import com.wuzuhao.cpm.ui.core.BaseTest;
import com.wuzuhao.cpm.ui.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Epic("前端功能测试")
@Feature("登录模块")
public class LoginTests extends BaseTest {

    @Test(description = "正确账号密码登录成功")
    @Story("正常登录")
    @Severity(SeverityLevel.BLOCKER)
    @Description("输入正确用户名和密码后，应成功进入系统首页")
    public void testLoginSuccess() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl + "/login")
                .typeUsername("admin")
                .typePassword("123456789");

        loginPage.clickLogin();

        // 等待页面跳转
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "登录后未跳转到首页（/dashboard），当前URL: " + driver.getCurrentUrl());

        // 可选：检查是否有成功消息
        // String successMsg = loginPage.waitForSuccessMessage();
        // Assert.assertTrue(successMsg.contains("登录成功"), "应显示登录成功消息");
    }

    @Test(description = "错误密码登录失败")
    @Story("登录失败-密码错误")
    @Severity(SeverityLevel.CRITICAL)
    @Description("输入错误密码，应提示用户名或密码错误")
    public void testLoginWrongPassword() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl + "/login")
                .typeUsername("admin000")
                .typePassword("wrong");

        loginPage.clickLogin();

        // 使用专门的方法等待错误消息
        String msg = loginPage.waitForErrorMessage();

        // 验证没有跳转
        Assert.assertFalse(driver.getCurrentUrl().contains("/dashboard"),
                "错误密码不应跳转到首页");

        // 验证错误消息不为空
        Assert.assertFalse(msg.isEmpty(), "错误提示不应为空");

        // 打印消息内容用于调试
        System.out.println("错误消息内容: " + msg);

        // 根据实际后端返回验证具体错误信息
        // Assert.assertTrue(msg.contains("用户名或密码错误") ||
        //                   msg.contains("密码错误"),
        //                   "错误消息内容不正确，实际为: " + msg);
    }

    @Test(description = "必填项校验")
    @Story("登录失败-必填校验")
    @Severity(SeverityLevel.NORMAL)
    @Description("账号或密码为空时，应给出必填提示信息")
    public void testLoginRequiredValidation() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl + "/login");

        loginPage.clickLogin();

        // 等待表单校验错误消息
        WebDriverWait wait = new WebDriverWait(driver, 5);
        List<WebElement> errors = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".el-form-item__error"))
        );

        Assert.assertFalse(errors.isEmpty(), "应该出现至少一个必填校验错误提示");

        String allErrorText = errors.stream()
                .map(WebElement::getText)
                .collect(Collectors.joining(" "));

        Assert.assertTrue(allErrorText.contains("请输入"),
                "校验提示应包含“请输入”，实际为：" + allErrorText);
    }

    @Test(description = "用户名不存在登录失败")
    @Story("登录失败-用户不存在")
    @Severity(SeverityLevel.NORMAL)
    @Description("输入不存在的用户名，应提示用户不存在")
    public void testLoginUserNotExist() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl + "/login")
                .typeUsername("nonexistent_user")
                .typePassword("123456");

        loginPage.clickLogin();

        // 等待错误消息
        String msg = loginPage.waitForErrorMessage();

        // 验证
        Assert.assertFalse(driver.getCurrentUrl().contains("/dashboard"), "不应跳转到首页");
        Assert.assertFalse(msg.isEmpty(), "错误提示不应为空");

        System.out.println("错误消息内容: " + msg);
    }

    @Test(description = "连续多次登录失败测试")
    @Story("登录失败-连续失败")
    @Severity(SeverityLevel.MINOR)
    @Description("连续多次输入错误密码，验证错误消息每次都会出现")
    public void testMultipleLoginFailures() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl + "/login");

        String[] wrongPasswords = {"wrong1", "wrong2", "wrong3"};

        for (int i = 0; i < wrongPasswords.length; i++) {
            // 每次都需要重新输入用户名和密码
            // 先清空输入框再输入新值
            loginPage.clearUsername()
                    .typeUsername("admin")
                    .clearPassword()
                    .typePassword(wrongPasswords[i])
                    .clickLogin();

            // 等待错误消息
            String msg = loginPage.waitForErrorMessage();

            // 验证错误消息不为空
            Assert.assertFalse(msg.isEmpty(),
                    String.format("第%d次登录失败后应显示错误消息，但未获取到", i + 1));

            System.out.println(String.format("第%d次登录失败，错误消息: %s", i + 1, msg));

            // 等待消息消失后再进行下一次尝试
            boolean disappeared = loginPage.waitForMessageToDisappear();
            if (!disappeared) {
                System.out.println(String.format("警告: 第%d次登录的错误消息未在预期时间内消失", i + 1));
            }

            // 添加短暂等待，确保页面完全响应
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}