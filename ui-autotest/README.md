## CPM 前端自动化测试模块（Selenium + TestNG + Allure）

### 结构说明

- `pom.xml`：独立 Maven 模块，包含 Selenium、TestNG、Allure、WebDriverManager 依赖与 Surefire 配置。  
- `src/test/java/com/wuzuhao/cpm/ui/core/BaseTest.java`：WebDriver 初始化、关闭与失败截图（自动附加到 Allure）。  
- `src/test/java/com/wuzuhao/cpm/ui/pages/LoginPage.java`：登录页 Page Object，使用 Allure `@Step` 标记步骤。  
- `src/test/java/com/wuzuhao/cpm/ui/tests/LoginTests.java`：登录模块核心自动化用例。  
- `src/test/java/com/wuzuhao/cpm/ui/tests/ResidentCrudTests.java`：居民/业务对象增删改查流程用例骨架。  
- `src/test/resources/testng.xml`：TestNG 套件配置，包含 Smoke 和 Regression 两个测试集。  

### 使用方式

1. 在项目根目录执行：

   ```bash
   cd ui-autotest
   mvn clean test
   ```

2. 生成并查看 Allure 报告（需要本地安装 Allure 命令行工具）：

   ```bash
   mvn allure:serve
   ```

3. 按实际前端页面：
   - 修改 `BaseTest.baseUrl` 为真实测试环境地址。  
   - 调整 `LoginPage` 中元素定位器。  
   - 在 `ResidentCrudTests` 中补充具体页面操作与断言，实现完整业务流程自动化。  

