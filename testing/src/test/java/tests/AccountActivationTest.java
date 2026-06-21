package tests;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.vega.configuration.LoadProperties;
import org.vega.pages.AdminPanelPage;
import org.vega.pages.HomePage;

public class AccountActivationTest extends TestBase {

    HomePage homePage;
    AdminPanelPage adminPanelPage;

    String name = faker.name().fullName();
    String email = faker.internet().emailAddress();
    String password = "Test1234";

    @Test(priority = 0)
    public void shouldRegisterSuccessfully() {
        homePage = new HomePage(webDriver);
        homePage.signUp(name, email, password, password, "User");
        assertIsEqual(homePage.toastMsg, "Registered successfully! Waiting for admin approval");
        softAssert.assertAll();
    }

    @Test(priority = 1)
    public void shouldFailRegistrationWithMismatchedPasswords() {
        homePage = new HomePage(webDriver);
        homePage.signUp(
            faker.name().fullName(),
            faker.internet().emailAddress(),
            "password123",
            "differentpassword",
            "User"
        );
        assertIsEqual(
            By.cssSelector("div.invalid-feedback"),
            "Passwords do not match"
        );
        softAssert.assertAll();
    }

    @Test(priority = 2)
    public void shouldFailRegistrationWithDuplicateEmail() {
        homePage = new HomePage(webDriver);
        homePage.signUp(
            faker.name().fullName(),
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            "password123",
            "password123",
            "User"
        );
        assertIsEqual(homePage.toastMsg, "user with this email already exists");
        softAssert.assertAll();
    }

    @Test(priority = 3)
    public void shouldFailLoginForUnapprovedUser() {
        homePage = new HomePage(webDriver);
        homePage.login(email, password);
        assertIsEqual(homePage.toastMsg, "Login failed. Please wait for admin approval.");
        softAssert.assertAll();
    }

    @Test(priority = 4)
    public void adminShouldSeeNewAccountInNewAccountsTab() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        homePage.openAdminPanel();
        adminPanelPage.click(By.xpath("//button[text()='New Accounts']"));
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");
        softAssert.assertTrue(
            webDriver.findElement(By.xpath("//button[contains(text(),'New Accounts')]")).isDisplayed(),
             "New Accounts tab should be visible"
        );
        softAssert.assertAll();
    }

    @Test(priority = 5)
    public void adminShouldActivateAccount() {
        try {
            webDriver.navigate().to(LoadProperties.env.getProperty("URL"));
            webDriver.findElement(By.linkText("Logout")).click();
        } catch (Exception e) {
            // not logged in, continue
        }
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        homePage.openAdminPanel();
        adminPanelPage.click(By.xpath("//button[text()='New Accounts']"));
        adminPanelPage.approveSignUpRequest();
        assertIsEqual(homePage.toastMsg, "Account activated successfully");
        softAssert.assertAll();
    }

    @Test(priority = 6)
    public void approvedUserShouldLoginSuccessfully() {
        try {
            webDriver.navigate().to(LoadProperties.env.getProperty("URL"));
            webDriver.findElement(By.linkText("Logout")).click();
        } catch (Exception e) {
            // not logged in, continue
        }
        homePage = new HomePage(webDriver);
        homePage.login(email, password);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean isLoggedIn = !webDriver.getCurrentUrl().contains("/login");
        softAssert.assertTrue(isLoggedIn, "User should be redirected away from login page after successful login");
        softAssert.assertAll();
    }
}
