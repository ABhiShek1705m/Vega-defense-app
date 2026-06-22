package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.vega.configuration.LoadProperties;
import org.vega.pages.DefenseAppPage;
import org.vega.pages.HomePage;
import java.time.Duration;

/**
 * Defense App Monitoring – Automated Functional Tests
 *
 * User Stories covered:
 *  - Users can download the defense app after logging in.
 *  - Users can only see the Defense Client Dashboard after logging in.
 *  - Users can select the network interface that needs to be monitored.
 *  - Users can select the IDS mode (True/False) for protection.
 *  - Users can start, stop, and uninstall the defense app.
 *  - Admin can see both Defense Admin Dashboard and Defense Client Dashboard.
 *
 * NOTE: Tests that interact with the dashboard require the vega defense
 * app to be running in the VM and registered to the test user's account.
 * Tests marked with the "requiresVM" group can be skipped when the VM is not
 * available by excluding that group in testng.xml.
 */
public class DefenseAppTest extends TestBase {

    private SoftAssert sa;
    private WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        sa = new SoftAssert();
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
        // Navigate to base URL before each test so state is clean
        webDriver.navigate().to(LoadProperties.env.getProperty("URL"));
    }

    @AfterMethod
    public void logout() {
        try {
            webDriver.navigate().to(LoadProperties.env.getProperty("URL"));
            HomePage homePage = new HomePage(webDriver);
            homePage.logout();
        } catch (Exception e) {
            // Already logged out or on wrong page, ignore
        }
    }

    // ----------------------------------------------------------------
    // 1. Defense Dashboard visibility – unauthenticated user
    // ----------------------------------------------------------------

    @Test(priority = 0)
    public void shouldNotSeeDefenseDashboardWhenNotLoggedIn() {
        webDriver.navigate().to(LoadProperties.env.getProperty("URL") + "/DefenseAppDashboard");

        boolean isRedirected = new org.openqa.selenium.support.ui.WebDriverWait(webDriver, java.time.Duration.ofSeconds(5))
            .until(driver -> driver.getCurrentUrl().contains("/Login"));

        sa.assertTrue(
            isRedirected,
            "Unauthenticated user should be redirected to /Login"
        );
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 2. Admin can see BOTH dashboards
    // ----------------------------------------------------------------

    /**
     * Admin logs in and navigates to the Defense Dashboard.
     * Both "Defense Admin Dashboard" and "Defense Client Dashboard" headings
     * must be visible.
     */

    @Test(priority = 1)
    public void adminShouldSeeBothDefenseDashboards() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        assertIsEqual(defenseAppPage.defenseAdminDashboardTitle,  "Defense Admin Dashboard");
        assertIsEqual(defenseAppPage.defenseClientDashboardTitle, "Defense Client Dashboard");
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 3. Download Defense App link is accessible after login
    // ----------------------------------------------------------------

    /**
     * After logging in the "Download Defense App" nav link must be
     * present and clickable.
     */
    @Test(priority = 2)
    public void shouldSeeDownloadDefenseAppLinkAfterLogin() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );

        assertIsEqual(defenseAppPage.downloadDefenseAppNavLink, "Download Defense App");
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 4. Regular user can only see Defense Client Dashboard (not Admin)
    // ----------------------------------------------------------------

    /**
     * A regular (non-admin) user who logs in should see only the
     * Defense Client Dashboard heading, not the Defense Admin Dashboard heading.
     */
    @Test(priority = 3)
    public void regularUserShouldOnlySeeClientDashboard() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("USER_EMAIL"),
            LoadProperties.env.getProperty("USER_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        assertIsEqual(defenseAppPage.defenseClientDashboardTitle, "Defense Client Dashboard");

        sa.assertEquals(
            webDriver.findElements(
                By.xpath("//h3[normalize-space()='Defense Admin Dashboard']")
            ).size(),
            0,
            "Regular user should not see Defense Admin Dashboard"
        );
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 5. Select network interface (requires VM)
    // ----------------------------------------------------------------

    /**
     * After logging in as admin and navigating to the dashboard,
     * the user should be able to select a network interface from the dropdown.
     * Requires VM + registered defense app.
     */
    @Test(priority = 4, groups = {"requiresVM"})
    public void shouldSelectNetworkInterface() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        defenseAppPage.selectIface(LoadProperties.env.getProperty("DEFENSE_IFACE"));

        var ifaceSelect = webDriver.findElement(defenseAppPage.selectIfaceDropdown);
        sa.assertEquals(
            ifaceSelect.getAttribute("value"),
            LoadProperties.env.getProperty("DEFENSE_IFACE"),
            "Network interface dropdown should reflect the selected value"
        );
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 6. Select IDS mode (requires VM)
    // ----------------------------------------------------------------

    /**
     * After logging in, the user should be able to select an IDS mode.
     * Requires VM + registered defense app.
     */
    @Test(priority = 5, groups = {"requiresVM"})
    public void shouldSelectIDSMode() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        defenseAppPage.selectIds(LoadProperties.env.getProperty("DEFENSE_IDS_MODE"));

        var idsSelect = webDriver.findElement(defenseAppPage.selectIdsDropdown);
        sa.assertEquals(
            idsSelect.getAttribute("value"),
            LoadProperties.env.getProperty("DEFENSE_IDS_MODE"),
            "IDS dropdown should reflect the selected value"
        );
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 7. Start button disabled without iface and IDS (requires VM
    //     because buttons only render when an app is registered)
    // ----------------------------------------------------------------

    /**
     * Clicking Start without selecting iface or IDS should keep the
     * Start button disabled.
     */
    @Test(priority = 6, groups = {"requiresVM"})
    public void startButtonShouldBeDisabledWithoutIfaceAndIDS() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        sa.assertFalse(
        defenseAppPage.isButtonEnabled(defenseAppPage.startButton),
        "Start button should be disabled when no iface or IDS is selected"
        );
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 8. Stop button disabled when app not started (requires VM)
    // ----------------------------------------------------------------

    /**
     * The Stop button should be disabled when the defense app has
     * not been started yet.
     */
    @Test(priority = 7, groups = {"requiresVM"})
    public void stopButtonShouldBeDisabledWhenAppNotStarted() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        sa.assertFalse(
        defenseAppPage.isButtonEnabled(defenseAppPage.stopButton),
        "Stop button should be disabled when the defense app has not been started"
        );
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 9. Start defense app (requires VM)
    // ----------------------------------------------------------------

    /**
     * With iface and IDS selected, clicking Start should show a
     * success toast.
     */
    @Test(priority = 8, groups = {"requiresVM"})
    public void shouldStartDefenseAppSuccessfully() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        defenseAppPage.selectIface(LoadProperties.env.getProperty("DEFENSE_IFACE"));
        defenseAppPage.selectIds(LoadProperties.env.getProperty("DEFENSE_IDS_MODE"));
        defenseAppPage.clickStart();

        assertIsEqual(homePage.toastMsg, "Defense app started successfully");
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 10. Stop defense app (requires VM)
    // ----------------------------------------------------------------

    /**
     * After starting the defense app, clicking Stop should show a
     * success toast.
     */
    @Test(priority = 9, groups = {"requiresVM"}, dependsOnMethods = {"shouldStartDefenseAppSuccessfully"})
    public void shouldStopDefenseAppSuccessfully() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();

        defenseAppPage.selectIface(LoadProperties.env.getProperty("DEFENSE_IFACE"));
        defenseAppPage.selectIds(LoadProperties.env.getProperty("DEFENSE_IDS_MODE"));
        defenseAppPage.clickStart();

        assertIsEqual(homePage.toastMsg, "Defense app started successfully");
        defenseAppPage.clickStop();

        assertIsEqual(homePage.toastMsg, "Defense app stopped successfully");
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 11. Uninstall defense app (requires VM)
    // ----------------------------------------------------------------

    /**
     * Clicking Uninstall should show a success toast.
     */
    @Test(priority = 10, groups = {"requiresVM"})
    public void shouldUninstallDefenseAppSuccessfully() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();
        defenseAppPage.clickUninstall();

        assertIsEqual(homePage.toastMsg, "Defense app uninstalled successfully");
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 12. Admin sees "no app registered" alert (run after uninstall)
    // ----------------------------------------------------------------

    /**
     * After uninstalling, the Admin Dashboard should show
     * "No defense application is registered yet."
     */
    @Test(priority = 11)
    public void adminShouldSeeNoAppRegisteredAlertInAdminDashboard() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("ADMIN_EMAIL"),
            LoadProperties.env.getProperty("ADMIN_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();
        webDriver.navigate().refresh(); 
        boolean isAlertPresent = wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.tagName("body"), "No defense application is registered yet."
        ));

        sa.assertTrue(isAlertPresent, "Admin Dashboard should show 'No defense application is registered yet.' alert");
        sa.assertAll();
    }

    // ----------------------------------------------------------------
    // 13. User sees "no app registered" alert (run after uninstall)
    // ----------------------------------------------------------------

    /**
     * After uninstalling, the Client Dashboard should show
     * "No defense app is registered for this account."
     */
    @Test(priority = 12)
    public void userShouldSeeNoAppRegisteredAlertInClientDashboard() {
        HomePage homePage = new HomePage(webDriver);
        DefenseAppPage defenseAppPage = new DefenseAppPage(webDriver);

        homePage.login(
            LoadProperties.env.getProperty("USER_EMAIL"),
            LoadProperties.env.getProperty("USER_PASSWORD")
        );
        defenseAppPage.navigateToDefenseDashboard();
        webDriver.navigate().refresh();
        boolean isAlertPresent = wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.tagName("body"), "No defense app is registered for this account."
        ));

        sa.assertTrue(isAlertPresent, "Client Dashboard should show 'No defense app is registered for this account.' alert");
        sa.assertAll();
    }

}