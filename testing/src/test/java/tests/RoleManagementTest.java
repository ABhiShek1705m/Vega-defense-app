package tests;

import org.testng.annotations.Test;
import org.vega.configuration.LoadProperties;
import org.vega.pages.AdminPanelPage;
import org.vega.pages.HomePage;

/**
 * Feature: Role management — role changes can only be performed by the Admin.
 *
 *   F1-S1 (good weather):  Admin opens admin panel, updates role in All Accounts tab, sees success toast.
 *   F1-S2 (bad weather):   Regular User cannot see the Admin panel nav link.
 *   F1-S3 (bad weather):   Non-admin account cannot see the Admin panel nav link.
 *
 * Feature: Role management — after the Admin changes a role, the user's access changes accordingly.
 *
 * Pre-condition: the USER_EMAIL and EMPLOYEE_EMAIL accounts must already exist in the DB
 * and be ACTIVE (approved by admin). Create them once via the sign-up form, then log in as
 * Admin to approve them from the New Accounts tab — the same flow shown in SignUpTest.
 *
 * Credentials are read from testing/.env so nothing is hardcoded here.
 * Each test freshly logs in and navigates to the admin panel to avoid SPA session carry-over bugs.
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
    // SETUP: reset both accounts to their canonical roles before the suite runs.
    // No toast assertions here — if the role is already correct, no onChange
    // fires and that is acceptable.
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

    // ─────────────────────────────────────────────────────────────────────────
    // F1-S1 (good weather): Admin can open the admin panel and update a role
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // F1-S2 (bad weather): Regular User role cannot see the Admin nav link
    // ─────────────────────────────────────────────────────────────────────────

    @Test(priority = 2)
    public void regularUserCannotSeeAdminPanel() {
        homePage = new HomePage(webDriver);
        homePage.login(userEmail, userPassword);
        assertElementNotPresent(homePage.adminNavLink);
        homePage.logout();
        softAssert.assertAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // F1-S3 (bad weather): Non-admin account cannot see the Admin nav link.
    // empEmail was changed to User role in priority 1; the test still validates
    // that any non-admin role is blocked from the admin panel.
    // ─────────────────────────────────────────────────────────────────────────

    @Test(priority = 3)
    public void nonAdminCannotSeeAdminPanel() {
        homePage = new HomePage(webDriver);
        homePage.login(empEmail, empPassword);
        assertElementNotPresent(homePage.adminNavLink);
        homePage.logout();
        softAssert.assertAll();
    }
    // ─────────────────────────────────────────────────────────────────────────
    // F2-S2 (good weather): After Admin sets role → Employee, user sees the
    //                        Resources tab but NOT the Admin tab
    // ─────────────────────────────────────────────────────────────────────────

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
