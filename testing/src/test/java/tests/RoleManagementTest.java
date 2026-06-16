package tests;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import org.vega.configuration.LoadProperties;
import org.vega.pages.AdminPanelPage;
import org.vega.pages.HomePage;
import java.time.Duration;

/*
  Feature: Role management — role changes can only be performed by the Admin.
 
    F1-S1 (good weather):  Admin opens admin panel, updates role in All Accounts tab, sees success toast.
    F1-S2 (bad weather):   Regular User cannot see the Admin panel nav link.
    F1-S3 (bad weather):   Non-admin account cannot see the Admin panel nav link.
 
  Feature: Role management — after the Admin changes a role, the user's access changes accordingly.

    F2-S1 (good weather):  After Admin sets role → User, user sees neither the Resources tab nor the Admin tab.
    F2-S2 (good weather):  After Admin sets role → Employee, user sees the Resources tab but NOT the Admin tab.
 
 Pre-condition: the USER_EMAIL and EMPLOYEE_EMAIL accounts must already exist in the DB
 and be ACTIVE (approved by admin). Create them once via the sign-up form, then log in as
 Admin to approve them from the New Accounts tab, similar to the flow in SignUpTest.
 
 Credentials are read from testing/.env so nothing is hardcoded here.
 Each test freshly logs in and navigates to the admin panel to avoid SPA session carry-over bugs.
 */

public class RoleManagementTest extends TestBase {

    HomePage homePage;
    AdminPanelPage adminPanelPage;

    private static final String ADMIN_EMAIL    = LoadProperties.env.getProperty("ADMIN_EMAIL");
    private static final String ADMIN_PASSWORD = LoadProperties.env.getProperty("ADMIN_PASSWORD");
    private static final String userEmail      = LoadProperties.env.getProperty("USER_EMAIL");
    private static final String userPassword   = LoadProperties.env.getProperty("USER_PASSWORD");
    private static final String empEmail       = LoadProperties.env.getProperty("EMPLOYEE_EMAIL");
    private static final String empPassword    = LoadProperties.env.getProperty("EMPLOYEE_PASSWORD");

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
        adminPanelPage.updateRoleForUserInAllAccounts(userEmail, "User");
        homePage.logout();
        softAssert.assertAll();
    }

    @Test(priority = 1)
    public void adminCanUpdateRoleInAdminPanel() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);

        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");

        // Change empEmail Employee → User (real role change; triggers onChange and shows toast).
        adminPanelPage.updateRoleForUserInAllAccounts(empEmail, "User");
        assertIsEqual(homePage.toastMsg, "Role updated successfully");
        assertElementNotPresent(homePage.toastMsg); // Toast should disappear after a few seconds.
        homePage.logout();
        softAssert.assertAll();
    }

    @Test(priority = 2)
    public void userCannotSeeAdminPanel() {
        homePage = new HomePage(webDriver);
        homePage.login(userEmail, userPassword);
        assertElementNotPresent(homePage.adminNavLink);
        homePage.logout();
        softAssert.assertAll();
    }

    @Test(priority = 3)
    public void employeeCannotSeeAdminPanel() {
        homePage = new HomePage(webDriver);
        // Checking for visibility of login button before clicking to avoid stale element exception.
        webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(homePage.loginBtn));
        
        homePage.login(empEmail, empPassword);
        assertElementNotPresent(homePage.adminNavLink);
        homePage.logout();
        softAssert.assertAll();
    }

    @Test(priority = 4)
    public void employeeRoleCanSeeResourcesButNotAdminTab() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);

        // Admin changes userEmail from User → Employee.
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");
        adminPanelPage.updateRoleForUserInAllAccounts(userEmail, "Employee");
        assertIsEqual(homePage.toastMsg, "Role updated successfully");
        homePage.logout();

        // userEmail (now Employee) should see Resources but not Admin.
        homePage.login(userEmail, userPassword);
        assertElementNotPresent(homePage.adminNavLink);
        assertIsEqual(homePage.resourcesNavLink, "Resources");
        homePage.logout();
        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // F2-S1 (good weather): After Admin sets role → User, user sees neither
    //                        the Resources tab nor the Admin tab
    // ─────────────────────────────────────────────────────────────────────────

    @Test(priority = 5)
    public void userRoleCannotSeeResourcesOrAdminTab() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        // Admin changes userEmail from Employee → User.
        
        // Checking for visibility of login button before clicking to avoid stale element exception.
        webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(homePage.loginBtn));
        
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        assertIsEqual(adminPanelPage.adminPanelTitle, "ADMIN PANEL");
        adminPanelPage.updateRoleForUserInAllAccounts(userEmail, "User");
        assertIsEqual(homePage.toastMsg, "Role updated successfully");
        homePage.logout();

        // userEmail (now User) should see neither Resources nor Admin.
        homePage.login(userEmail, userPassword);
        assertElementNotPresent(homePage.adminNavLink);
        assertElementNotPresent(homePage.resourcesNavLink);
        homePage.logout();
        softAssert.assertAll();
    }
}
