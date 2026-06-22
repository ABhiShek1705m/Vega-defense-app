package tests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.vega.configuration.LoadProperties;
import org.vega.pages.AdminPanelPage;
import org.vega.pages.HomePage;


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

    @BeforeClass
    public void setupAccountRoles() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        homePage.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        homePage.openAdminPanel();
        adminPanelPage.updateRoleForUserInAllAccounts(empEmail, "Employee");
        homePage.logout();
    }

    // F1
    @Test(priority = 0)
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
    @Test(priority = 1)
    public void userRoleCannotSeeResourcesOrAdminTab() {
        homePage = new HomePage(webDriver);
        adminPanelPage = new AdminPanelPage(webDriver);
        // empEmail (now User) should see neither Resources nor Admin.
        homePage.login(empEmail, empPassword);
        assertElementNotPresent(homePage.adminPanelModule);
        assertElementNotPresent(homePage.resourcesNavLink);
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
        assertElementNotPresent(homePage.adminPanelModule);
        assertIsEqual(homePage.resourcesNavLink, "Resources");
        homePage.logout();
        softAssert.assertAll();
    }

}
