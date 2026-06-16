package tests;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import org.vega.configuration.LoadProperties;
import org.vega.pages.AdminPanelPage;
import org.vega.pages.HomePage;
import java.time.Duration;


/*
  Feature 1: Role management — role changes can only be performed by the Admin.
 
    F1:  Admin opens admin panel, updates role in All Accounts tab, sees success toast.
 
  Feature 2: Role management — after the Admin changes a role, the user's access changes accordingly.

   F2: Admin changes empEmail from Employee → User. empEmail should see Resources disappear and Admin tab remain hidden.
   F2: Admin changes empEmail from User → Employee. empEmail should see Resources appear but Admin tab remain hidden.
 
   Employee credentials are read from testing/.env.

   Employee account used for testing:

    Name: Paul Aguilar – CEO
    Email: paguilar@vega.com
    Password: X#abcRT
 */

public class RoleManagementTest extends TestBase {

    HomePage homePage;
    AdminPanelPage adminPanelPage;

    private static final String ADMIN_EMAIL    = LoadProperties.env.getProperty("ADMIN_EMAIL");
    private static final String ADMIN_PASSWORD = LoadProperties.env.getProperty("ADMIN_PASSWORD");
    private static final String empEmail       = LoadProperties.env.getProperty("TEST_EMPLOYEE_EMAIL");
    private static final String empPassword    = LoadProperties.env.getProperty("TEST_EMPLOYEE_PASSWORD");

    // ─────────────────────────────────────────────────────────────────────────
    // SETUP: reset both accounts to their correct roles before the tests runs.
    // No toast assertions here — if the role is already correct, no "roles changed successfully" toast will
    // fire and that is acceptable.
    // ─────────────────────────────────────────────────────────────────────────

    @Test(priority = 0)
    public void setupAccountRoles() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        adminPanelPage.updateRoleForUserInAllAccounts(empEmail, "Employee");
        homePage.logout();
        softAssert.assertAll();
    }

    // F1
    @Test(priority = 1)
    public void adminCanUpdateRoleInAdminPanel() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);

        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");

        // Change Employee → User triggers onChange and shows toast.
        adminPanelPage.updateRoleForUserInAllAccounts(empEmail, "User");
        assertIsEqual(homePage.toastMsg, "Role updated successfully");
        assertElementNotPresent(homePage.toastMsg); // Toast should disappear after a few seconds.
        homePage.logout();
        softAssert.assertAll();
    }

    // F2
    @Test(priority = 2)
    public void employeeRoleCanSeeResourcesButNotAdminTab() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);

        // Admin changes empEmail from User → Employee.
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");
        adminPanelPage.updateRoleForUserInAllAccounts(empEmail, "Employee");
        assertIsEqual(homePage.toastMsg, "Role updated successfully");
        homePage.logout();

        // empEmail (now Employee) should see Resources but not Admin.
        homePage.login(empEmail, empPassword);
        assertElementNotPresent(homePage.adminNavLink);
        assertIsEqual(homePage.resourcesNavLink, "Resources");
        homePage.logout();
        softAssert.assertAll();
    }

    // F2
    @Test(priority = 3)
    public void userRoleCannotSeeResourcesOrAdminTab() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        // Checking for visibility of login button before clicking to avoid stale element exception.
        webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(homePage.loginBtn));
        
        // Admin changes empEmail from Employee → User.
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");
        adminPanelPage.updateRoleForUserInAllAccounts(empEmail, "User");
        assertIsEqual(homePage.toastMsg, "Role updated successfully");
        homePage.logout();

        // empEmail (now User) should see neither Resources nor Admin.
        homePage.login(empEmail, empPassword);
        assertElementNotPresent(homePage.adminNavLink);
        assertElementNotPresent(homePage.resourcesNavLink);
        homePage.logout();
        softAssert.assertAll();
    }
}
