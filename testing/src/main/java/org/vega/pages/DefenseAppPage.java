package org.vega.pages;
 
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DefenseAppPage extends PageBase {

    public DefenseAppPage(WebDriver webDriver) {
        super(webDriver);
    }

    // Navbar links
    public By defenseDashboardNavLink = By.linkText("Defense Dashboard");
    public By downloadDefenseAppNavLink = By.linkText("Download Defense App");
 
    // Defense Admin Dashboard
    public By defenseAdminDashboardTitle = By.xpath("//h3[normalize-space()='Defense Admin Dashboard']");
 
    // Defense Client Dashboard
    public By defenseClientDashboardTitle = By.xpath("//h3[normalize-space()='Defense Client Dashboard']");

    // Buttons/dropdown 
    public By selectIfaceDropdown = By.cssSelector("[aria-label='Select iface']");
    public By selectIdsDropdown   = By.cssSelector("[aria-label='Select IDS']");
    public By startButton         = By.xpath("//button[normalize-space()='Start']");
    public By stopButton          = By.xpath("//button[normalize-space()='Stop']");
    public By uninstallButton     = By.xpath("//button[normalize-space()='Uninstall']");

    public boolean isButtonEnabled(By by) {
    webDriverWait.until(ExpectedConditions.presenceOfElementLocated(by));
    return webDriver.findElement(by).isEnabled();
    }   

    public void navigateToDefenseDashboard() {
    click(defenseDashboardNavLink);
    }
 
    public void selectIface(String ifaceValue) {
        select(selectIfaceDropdown, ifaceValue);
    }
 
    public void selectIds(String idsValue) {
        select(selectIdsDropdown, idsValue);
    }
 
    public void clickStart() {
        click(startButton);
    }
 
    public void clickStop() {
        click(stopButton);
    }
 
    public void clickUninstall() {
        click(uninstallButton);
    }
}