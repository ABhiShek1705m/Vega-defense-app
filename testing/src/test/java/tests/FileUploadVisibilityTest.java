package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.vega.configuration.LoadProperties;
import org.vega.pages.AdminPanelPage;
import org.vega.pages.HomePage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/*
  Feature: File Upload and Resources Visibility

  Scenario 1: Admin can upload a file from the admin panel Resources Upload section.
  Scenario 2: Admin and Employee can view uploaded resources in the Resources tab.

  Employee credentials are read from testing/.env (TEST_EMPLOYEE_EMAIL, TEST_EMPLOYEE_PASSWORD).
  These tests follow a TDD approach — file upload is currently broken, so tests will fail
  until the backend upload endpoint is fixed.
 */
public class FileUploadVisibilityTest extends TestBase {

    HomePage homePage;
    AdminPanelPage adminPanelPage;

    private static final String ADMIN_EMAIL    = LoadProperties.env.getProperty("ADMIN_EMAIL");
    private static final String ADMIN_PASSWORD = LoadProperties.env.getProperty("ADMIN_PASSWORD");
    private static final String empEmail       = LoadProperties.env.getProperty("TEST_EMPLOYEE_EMAIL");
    private static final String empPassword    = LoadProperties.env.getProperty("TEST_EMPLOYEE_PASSWORD");

    private static final By RESOURCE_CARD = By.cssSelector(".resource-card");

    private static String testFilePath;

    @BeforeClass(dependsOnMethods = "startDriver")
    public void setupTestFile() throws IOException {
        File testFile = File.createTempFile("vega-test-upload", ".txt");
        testFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Test file for Vega upload verification.");
        }
        testFilePath = testFile.getAbsolutePath();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 1: Admin can upload a file from the admin panel
    // ─────────────────────────────────────────────────────────────────────────

    @Test(priority = 0)
    public void adminCanUploadFileFromAdminPanel() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        // Login as admin and navigate to admin panel
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminPanelPage.adminPanelTitle));
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");

        // Click Resources Upload in the admin panel sidebar
        adminPanelPage.clickResourcesUpload();
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminPanelPage.selectFileLabel));

        // Upload a file by sending the file path to the hidden file input
        adminPanelPage.uploadFile(testFilePath);

        // Verify the success toast appears
        assertIsEqual(homePage.toastMsg, "File uploaded successfully");
        assertElementNotPresent(homePage.toastMsg);

        homePage.logout();
        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2a: Admin can see uploaded resources in the Resources tab
    // ─────────────────────────────────────────────────────────────────────────

    @Test(priority = 1, dependsOnMethods = "adminCanUploadFileFromAdminPanel")
    public void adminCanViewUploadedResources() {
        homePage = new HomePage(webDriver);
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(homePage.loginBtn));
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Navigate to Resources tab
        homePage.openResources();

        // Wait for resource cards to appear (uploaded file should be listed)
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESOURCE_CARD));
        List<WebElement> cards = webDriver.findElements(RESOURCE_CARD);
        softAssert.assertTrue(cards.size() > 0,
                "Expected at least one resource card visible for admin");

        homePage.logout();
        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2b: Employee can see uploaded resources in the Resources tab
    // ─────────────────────────────────────────────────────────────────────────

    @Test(priority = 2, dependsOnMethods = "adminCanUploadFileFromAdminPanel")
    public void employeeCanViewUploadedResources() {
        homePage = new HomePage(webDriver);
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(homePage.loginBtn));
        homePage.login(empEmail, empPassword);

        // Navigate to Resources tab
        homePage.openResources();

        // Wait for resource cards to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESOURCE_CARD));
        List<WebElement> cards = webDriver.findElements(RESOURCE_CARD);
        softAssert.assertTrue(cards.size() > 0,
                "Expected at least one resource card visible for employee");

        homePage.logout();
        softAssert.assertAll();
    }
}
