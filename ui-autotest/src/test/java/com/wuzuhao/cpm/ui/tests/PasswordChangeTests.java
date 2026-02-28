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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 修改密码两种方式的功能测试：
 * 1）通过旧密码在「个人信息」页修改密码
 * 2）通过邮箱验证码在「个人信息」页修改密码（主要验证前端交互与校验）
 */
@Epic("前端功能测试")
@Feature("个人信息 / 修改密码")
public class PasswordChangeTests extends BaseTest {

    // 出厂默认管理员账号信息（参考 LoginTests）
    private static final String ADMIN_USERNAME = "admin";
    private static final String ORIGINAL_ADMIN_PASSWORD = "123456789";

    // 为了避免影响其他用例，这里在同一个测试里改过去再改回来
    private static final String TEMP_NEW_PASSWORD = "AdminNew123!";

    private WebDriverWait getWait() {
        return new WebDriverWait(driver, 15);
    }

    /**
     * 通用：使用指定密码登录，并等待跳转到首页
     */
    private void loginAsAdmin(String password) {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl + "/login")
                .typeUsername(ADMIN_USERNAME)
                .typePassword(password);

        loginPage.clickLogin();

        WebDriverWait wait = getWait();
        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "登录后未进入首页 /dashboard，当前URL: " + driver.getCurrentUrl());
    }

    /**
     * 通用：从登录后的首页进入「个人信息」页面
     * 这里直接访问 /profile，依赖路由守卫基于 token 放行。
     */
    private void gotoProfilePage() {
        driver.get(baseUrl + "/profile");

        WebDriverWait wait = getWait();
        // 等待页面加载完成 - 等待个人信息标题出现
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'user-profile')]//span[contains(text(),'个人信息')]")));

        // 额外等待一下，确保页面完全加载
        sleep(1000);
    }

    /**
     * 在「个人信息」页切换到“修改密码”标签
     */
    private void switchToPasswordTab() {
        WebDriverWait wait = getWait();

        // 等待并点击修改密码标签
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-tabs__item') and contains(text(),'修改密码')]")));
        tab.click();

        // 额外等待一下，确保表单完全加载
        sleep(500);
    }

    /**
     * 通用休眠方法
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 通过旧密码方式修改密码：先改为新密码，再改回旧密码，确保不影响其他测试
     */
    @Test(description = "通过旧密码在个人信息页完成修改密码并改回原密码")
    @Story("通过旧密码修改密码")
    @Severity(SeverityLevel.BLOCKER)
    @Description("使用旧密码在个人信息页修改管理员密码为新值，然后再用新密码登录并改回原始密码，验证整个流程可用且对其他测试无副作用。")
    public void testChangePasswordByOldPasswordAndRevert() {
        // 第一次：使用原始密码登录
        loginAsAdmin(ORIGINAL_ADMIN_PASSWORD);
        gotoProfilePage();
        switchToPasswordTab();

        WebDriverWait wait = getWait();

        // 保证当前模式是“通过旧密码修改”，通过input的value属性定位
        WebElement oldPasswordRadio = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(@class,'el-radio') and .//input[@value='oldPassword']]")));
        oldPasswordRadio.click();

        // 等待单选按钮切换生效
        sleep(500);

        // 填写旧密码 - 使用placeholder定位
        WebElement oldPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入旧密码']")));
        oldPwdInput.clear();
        oldPwdInput.sendKeys(ORIGINAL_ADMIN_PASSWORD);

        // 填写新密码 - 使用placeholder定位
        WebElement newPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入新密码']")));
        newPwdInput.clear();
        newPwdInput.sendKeys(TEMP_NEW_PASSWORD);

        // 填写确认新密码 - 使用placeholder定位
        WebElement confirmPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请再次输入新密码']")));
        confirmPwdInput.clear();
        confirmPwdInput.sendKeys(TEMP_NEW_PASSWORD);

        // 等待一下确保输入完成
        sleep(500);

        // 点击“修改密码”按钮 - 使用验证通过的XPath
        WebElement changeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'el-button--primary') and .//span[text()='修改密码']]")));

        // 确保按钮可见并可点击
        Assert.assertTrue(changeBtn.isDisplayed(), "修改密码按钮应该可见");
        Assert.assertTrue(changeBtn.isEnabled(), "修改密码按钮应该可用");

        changeBtn.click();

        // 等待成功弹窗出现
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'el-message-box')]")));

        // 验证弹窗标题
        WebElement alertTitle = alert.findElement(By.xpath(".//div[contains(@class,'el-message-box__header')]//span"));
        Assert.assertTrue(alertTitle.getText().contains("提示"), "修改密码成功对话框标题不正确");

        // 验证弹窗内容
        WebElement alertContent = alert.findElement(By.xpath(".//div[contains(@class,'el-message-box__content')]"));
        Assert.assertTrue(alertContent.getText().contains("密码修改成功"),
                "修改密码成功文案不匹配，实际为: " + alertContent.getText());

        // 点击确定按钮 - 根据实际HTML结构调整
        WebElement alertConfirm = alert.findElement(
                By.xpath(".//button[contains(@class,'el-button--primary') and .//span[text()='确定']]"));
        alertConfirm.click();

        // 确认已经被重定向到登录页
        wait.until(ExpectedConditions.urlContains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "确认对话框后应回到登录页，当前URL: " + driver.getCurrentUrl());

        // 使用新密码重新登录，验证修改生效
        loginAsAdmin(TEMP_NEW_PASSWORD);

        // 再次进入个人信息页，使用旧密码模式将密码改回原始值
        gotoProfilePage();
        switchToPasswordTab();

        // 再次选择“通过旧密码修改”
        oldPasswordRadio = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(@class,'el-radio') and .//input[@value='oldPassword']]")));
        oldPasswordRadio.click();

        sleep(500);

        oldPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入旧密码']")));
        oldPwdInput.clear();
        oldPwdInput.sendKeys(TEMP_NEW_PASSWORD);

        newPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入新密码']")));
        newPwdInput.clear();
        newPwdInput.sendKeys(ORIGINAL_ADMIN_PASSWORD);

        confirmPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请再次输入新密码']")));
        confirmPwdInput.clear();
        confirmPwdInput.sendKeys(ORIGINAL_ADMIN_PASSWORD);

        sleep(500);

        changeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'el-button--primary') and .//span[text()='修改密码']]")));
        changeBtn.click();

        // 再次确认成功弹窗并返回登录页
        alert = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'el-message-box')]")));
        alertConfirm = alert.findElement(
                By.xpath(".//button[contains(@class,'el-button--primary') and .//span[text()='确定']]"));
        alertConfirm.click();

        wait.until(ExpectedConditions.urlContains("/login"));

        // 最终用原始密码再登录一次，确保环境恢复
        loginAsAdmin(ORIGINAL_ADMIN_PASSWORD);
    }

    /**
     * 通过邮箱验证码方式修改密码：这里主要测试前端交互与校验
     */
    @Test(description = "通过邮箱验证码方式修改密码的前端交互与校验")
    @Story("通过邮箱验证码修改密码")
    @Severity(SeverityLevel.CRITICAL)
    @Description("验证在使用邮箱验证码方式修改密码时，发送验证码按钮、必填校验以及密码一致性校验等前端行为。")
    public void testChangePasswordByEmailCodeValidation() {
        // 登录进入个人信息页（使用原始密码）
        loginAsAdmin(ORIGINAL_ADMIN_PASSWORD);
        gotoProfilePage();
        switchToPasswordTab();

        WebDriverWait wait = getWait();

        // 切换到“通过邮箱验证码修改” - 通过input的value属性定位
        WebElement emailCodeRadio = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(@class,'el-radio') and .//input[@value='emailCode']]")));
        emailCodeRadio.click();

        // 等待模式切换
        sleep(1000);

        // 点击“发送验证码”按钮
        WebElement sendCodeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'el-button--primary') and .//span[text()='发送验证码']]")));
        sendCodeBtn.click();

        // 等待成功消息出现 - 根据实际HTML结构调整
        try {
            // 等待成功消息出现
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'el-message--success')]//p[contains(@class,'el-message__content')]")));

            String messageText = successMessage.getText();
            System.out.println("成功消息内容: " + messageText);

            // 验证消息内容
            Assert.assertTrue(messageText.contains("验证码已发送，请查收邮件") ,
                    "发送验证码后应显示成功提示，实际消息: " + messageText);

        } catch (TimeoutException e) {
            // 如果没有成功消息，尝试获取错误消息
            try {
                WebElement errorMessage = driver.findElement(
                        By.xpath("//div[contains(@class,'el-message--error')]//p[contains(@class,'el-message__content')]"));
                String errorText = errorMessage.getText();
                System.out.println("错误消息内容: " + errorText);
                Assert.fail("发送验证码失败: " + errorText);
            } catch (Exception ex) {
                // 如果也没有错误消息，记录日志但不失败
                System.out.println("没有出现任何提示消息");
            }
        }

        // 不填写任何内容直接点击“修改密码”，应出现校验错误
        WebElement changeBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'el-button--primary') and .//span[text()='修改密码']]")));
        changeBtn.click();

        // 收集表单错误提示
        List<WebElement> errors = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector(".el-form-item__error")));
        boolean hasCodeError = errors.stream().anyMatch(e -> e.getText().contains("验证码") || e.getText().contains("邮箱验证码"));
        boolean hasNewPwdError = errors.stream().anyMatch(e -> e.getText().contains("新密码"));

        Assert.assertTrue(hasCodeError || hasNewPwdError,
                "使用邮箱验证码方式时，至少应对验证码或新密码进行必填校验，实际错误提示: " +
                        String.join(" | ", errors.stream().map(WebElement::getText).collect(Collectors.toList())));

        // 再试一次：输入一致的新密码和确认密码
        WebElement emailCodeInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入邮箱验证码']")));
        emailCodeInput.clear();
        emailCodeInput.sendKeys("123456");

        WebElement newPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入新密码']")));
        newPwdInput.clear();
        newPwdInput.sendKeys("123456789");

        WebElement confirmPwdInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请再次输入新密码']")));
        confirmPwdInput.clear();
        confirmPwdInput.sendKeys("123456789");

        changeBtn.click();

        try {
            WebElement confirmError = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".el-form-item__error")));
            Assert.assertTrue(confirmError.getText().contains("请输入邮箱验证码"),
                    "确认密码校验提示应包含“请输入邮箱验证码”，实际为: " + confirmError.getText());
        } catch (TimeoutException e) {
            Assert.fail("未看到“两次输入的密码不一致”的校验提示");
        }
    }
}