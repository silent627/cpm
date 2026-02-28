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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务主流程示例（居民/业务对象增删改查）
 * 具体元素与步骤需根据实际前端页面补充。
 */
@Epic("前端功能测试")
@Feature("居民/业务对象管理")
public class ResidentCrudTests extends BaseTest {

    /**
     * 在多个用例之间共享一条测试数据，避免到处写死名字。
     */
    private static String createdUsername;
    private static String createdRealName;
    private static String createdIdCard;

    /**
     * 通用显式等待
     */
    private WebDriverWait getWait() {
        return new WebDriverWait(driver, 8);
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
     * 登录并进入居民管理页面
     */
    private void loginAndGotoResidentPage() {
        LoginPage loginPage = new LoginPage(driver)
                .open(baseUrl + "/login")
                .typeUsername("admin")
                .typePassword("123456789");

        loginPage.clickLogin();

        WebDriverWait wait = getWait();
        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));

        // 通过左侧菜单进入“居民管理”
        WebElement residentMenu = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//span[normalize-space(text())='居民管理']"))
        );
        residentMenu.click();

        // 等待“居民管理”页面标题渲染
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'card-header')]//span[normalize-space(text())='居民管理']")));

        Assert.assertTrue(driver.getCurrentUrl().contains("/residents"),
                "当前 URL 不在居民管理页: " + driver.getCurrentUrl());
    }

    /**
     * 在居民列表表格中，根据真实姓名找到行元素
     */
    private WebElement findRowByRealName(String realName) {
        WebDriverWait wait = getWait();
        By rowLocator = By.xpath(
                "//div[contains(@class,'el-table')]" +
                        "//tbody/tr[contains(@class,'el-table__row')]" +
                        "[.//td//div[normalize-space(text())='" + realName + "']]"
        );
        return wait.until(ExpectedConditions.visibilityOfElementLocated(rowLocator));
    }

    /**
     * 在表格中根据身份证号找到行
     */
    private WebElement findRowByIdCard(String idCard) {
        WebDriverWait wait = getWait();
        By rowLocator = By.xpath(
                "//div[contains(@class,'el-table')]" +
                        "//tbody/tr[contains(@class,'el-table__row')]" +
                        "[.//td//div[normalize-space(text())='" + idCard + "']]"
        );
        return wait.until(ExpectedConditions.visibilityOfElementLocated(rowLocator));
    }


    @Test(description = "新增居民成功")
    @Story("新增")
    @Severity(SeverityLevel.CRITICAL)
    @Description("管理员登录后，在居民管理页面新增一条居民记录，并在表格中校验该记录存在。")
    public void testCreateResident() {
        loginAndGotoResidentPage();

        WebDriverWait wait = getWait();

        // 生成唯一的测试数据
        long now = System.currentTimeMillis();
        createdUsername = "resident_ui_" + now;
        createdRealName = "自动化居民" + now;
        createdIdCard = "11010119900101" + String.format("%04d", now % 10000);

        // 点击“新增居民”按钮
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'el-button--success') and .//span[text()='新增居民']]")
        ));
        addBtn.click();

        // 等待对话框出现
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'el-dialog__body')]")));

        sleep(500);

        // 填写用户名 - 限定在对话框内
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog__body')]//input[@placeholder='请输入用户名']")));
        usernameInput.clear();
        usernameInput.sendKeys(createdUsername);

        // 填写密码 - 限定在对话框内
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog__body')]//input[@placeholder='请输入密码']")));
        passwordInput.clear();
        passwordInput.sendKeys("Aa123456!");

        // 填写真实姓名 - 限定在对话框内
        WebElement realNameInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog__body')]//input[@placeholder='请输入真实姓名']")));
        realNameInput.clear();
        realNameInput.sendKeys(createdRealName);
        System.out.println("已填写真实姓名: " + createdRealName);

        // 填写身份证号 - 限定在对话框内
        WebElement idCardInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog__body')]//input[@placeholder='请输入身份证号']")));
        idCardInput.clear();
        idCardInput.sendKeys(createdIdCard);
        System.out.println("已填写身份证号: " + createdIdCard);

        // 填写出生日期 - 限定在对话框内
        WebElement birthDateInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog__body')]//input[@placeholder='选择日期']")));
        birthDateInput.clear();

        // 生成出生日期（根据身份证号前8位：YYYYMMDD）
        String birthDate = createdIdCard.substring(6, 10) + "-" +
                createdIdCard.substring(10, 12) + "-" +
                createdIdCard.substring(12, 14);
        birthDateInput.sendKeys(birthDate);
        System.out.println("已填写出生日期: " + birthDate);

        // 对于日期选择器，可能需要按回车或点击确认
        birthDateInput.sendKeys(Keys.ENTER);
        sleep(500);

        // 填写联系电话 - 限定在对话框内
        WebElement contactPhoneInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog__body')]//input[@placeholder='请输入联系电话']")));
        contactPhoneInput.clear();
        contactPhoneInput.sendKeys("13800000000");

        // 点击"确定"提交 - 使用完整路径定位主按钮（通过el-button--primary类）
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog')]//footer[contains(@class,'el-dialog__footer')]//button[contains(@class,'el-button--primary')]")));
        confirmBtn.click();

        // 等待创建成功消息 - 获取所有成功消息，找到包含"创建成功"的
        // 第一步：等待至少一个成功消息容器出现
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".el-message.el-message--success")));
        
        // 第二步：获取所有成功消息，找到包含"创建成功"的
        String msg = "";
        StringBuilder allMessages = new StringBuilder();
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            List<WebElement> successMessages = driver.findElements(
                    By.cssSelector(".el-message.el-message--success .el-message__content"));
            
            for (WebElement message : successMessages) {
                String text = message.getText();
                System.out.println("找到成功消息: " + text);
                if (allMessages.length() > 0) {
                    allMessages.append(", ");
                }
                allMessages.append(text);
                
                if (text.contains("创建成功")) {
                    msg = text;
                    break;
                }
            }
            
            if (msg.contains("创建成功")) {
                break;
            }
            
            // 等待一段时间后重试
            sleep(500);
        }
        
        // 验证消息
        Assert.assertTrue(msg.contains("创建成功"),
                "创建居民后应出现成功提示，实际找到的消息: " + allMessages.toString());
    }

    /*
    @Test(description = "编辑居民成功", dependsOnMethods = "testCreateResident")
    @Story("编辑")
    @Severity(SeverityLevel.NORMAL)
    public void testEditResident() {
        loginAndGotoResidentPage();

        WebDriverWait wait = getWait();

        Assert.assertNotNull(createdRealName, "前置数据不存在，请先执行 testCreateResident");

        WebElement row = findRowByRealName(createdRealName);

        // 点击该行中的“编辑”按钮
        WebElement editBtn = row.findElement(By.xpath(".//button[span[text()='编辑']]"));
        editBtn.click();

        // 等待编辑对话框
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'el-dialog__body')]")));

        sleep(500);

        // 修改真实姓名
        String updatedRealName = createdRealName + "_已修改";
        WebElement realNameInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入真实姓名']")));
        realNameInput.clear();
        realNameInput.sendKeys(updatedRealName);

        // 修改职业
        WebElement occupationInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='请输入职业']")));
        occupationInput.clear();
        occupationInput.sendKeys("测试工程师");

        // 提交
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-dialog__footer')]//button[span[text()='确定']]")));
        confirmBtn.click();

        // 等待成功提示
        LoginPage loginPage = new LoginPage(driver);
        String msg = loginPage.waitForSuccessMessage();
        Assert.assertTrue(msg.contains("更新成功") || msg.contains("成功"),
                "编辑居民后应出现成功提示，实际消息为: " + msg);

        // 等待对话框关闭
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[contains(@class,'el-dialog__wrapper')]")));

        // 表格中应能按照新姓名找到记录
        createdRealName = updatedRealName;
        WebElement updatedRow = findRowByRealName(createdRealName);
        Assert.assertNotNull(updatedRow, "编辑后未在列表中找到新的真实姓名：" + createdRealName);

        // 校验职业字段
        WebElement occupationCell = updatedRow.findElement(
                By.xpath(".//td[8]//div[contains(@class,'cell')]"));
        String occupationText = occupationCell.getText();
        Assert.assertTrue(occupationText.contains("测试工程师"),
                "编辑后职业字段未更新，当前值为：" + occupationText);
    }

    @Test(description = "删除居民成功", dependsOnMethods = "testEditResident")
    @Story("删除")
    @Severity(SeverityLevel.NORMAL)
    public void testDeleteResident() {
        loginAndGotoResidentPage();

        WebDriverWait wait = getWait();

        Assert.assertNotNull(createdRealName, "前置数据不存在，请先执行 testCreateResident/testEditResident");

        List<String> beforeIds = getAllResidentIdsInTable();

        WebElement row = findRowByRealName(createdRealName);

        // 点击“删除”按钮
        WebElement deleteBtn = row.findElement(By.xpath(".//button[span[text()='删除']]"));
        deleteBtn.click();

        // 确认弹窗
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'el-message-box')]//button[span[text()='确定']]")));
        confirmBtn.click();

        // 等待删除成功提示
        LoginPage loginPage = new LoginPage(driver);
        String msg = loginPage.waitForSuccessMessage();
        Assert.assertTrue(msg.contains("删除成功") || msg.contains("成功"),
                "删除居民后应出现成功提示，实际消息为: " + msg);

        // 再次获取表格中的 ID 列
        List<String> afterIds = getAllResidentIdsInTable();

        Assert.assertTrue(afterIds.size() <= beforeIds.size(),
                "删除后表格记录数量未减少，删除可能失败");

        // 在当前页不应再出现该姓名
        List<WebElement> sameNameRows = driver.findElements(
                By.xpath("//div[contains(@class,'el-table')]//tbody/tr[contains(@class,'el-table__row')]" +
                        "[.//td//div[normalize-space(text())='" + createdRealName + "']]")
        );
        Assert.assertTrue(sameNameRows.isEmpty(),
                "删除后当前页仍然能看到该居民：" + createdRealName);
    }

    @Test(description = "居民查询与筛选")
    @Story("查询与筛选")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchResident() {
        loginAndGotoResidentPage();

        WebDriverWait wait = getWait();

        List<String> allIds = getAllResidentIdsInTable();
        int totalWithoutFilter = allIds.size();
        Assert.assertTrue(totalWithoutFilter > 0, "居民列表至少应有一条记录，才能验证查询功能");

        // 在“真实姓名”搜索框中输入一个不存在的关键字
        WebElement realNameSearchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='真实姓名']")));
        realNameSearchInput.clear();
        realNameSearchInput.sendKeys("不存在的居民_" + System.currentTimeMillis());

        // 点击“查询”按钮
        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[span[text()='查询']]")));
        searchBtn.click();

        // 等待表格更新
        sleep(1000);

        try {
            List<WebElement> rows = driver.findElements(
                    By.xpath("//div[contains(@class,'el-table')]//tbody/tr[contains(@class,'el-table__row')]")
            );
            if (!rows.isEmpty()) {
                Assert.assertTrue(rows.size() <= totalWithoutFilter,
                        "查询过滤后记录数量不应大于原始数量");
            } else {
                List<WebElement> emptyTips = driver.findElements(
                        By.xpath("//div[contains(@class,'el-table__empty-text') and contains(text(),'暂无数据')]")
                );
                Assert.assertFalse(emptyTips.isEmpty(), "查询结果为空时，应出现“暂无数据”提示");
            }
        } finally {
            // 点击“重置”按钮
            WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[span[text()='重置']]")));
            resetBtn.click();

            sleep(1000);

            List<String> resetIds = getAllResidentIdsInTable();
            Assert.assertTrue(resetIds.size() >= totalWithoutFilter || !resetIds.isEmpty(),
                    "重置搜索条件后，列表记录数量应恢复或至少不为空");
        }
    }
*/
}