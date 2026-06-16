package org.vega.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AdminPanelPage extends PageBase {
    public AdminPanelPage(WebDriver webDriver) {
        super(webDriver);
    }

    public By adminPanelTitle = By.cssSelector(".mb-4.f-bold.secondary-txt.text-uppercase");
    public By email = By.cssSelector("tbody tr:nth-child(1) td:nth-child(1)");
    public By name = By.cssSelector("tbody tr:nth-child(1) td:nth-child(2)");
    public By roleNewAccount = By.cssSelector("tbody tr:nth-child(1) td:nth-child(3) span:nth-child(1)");
    By approve = By.cssSelector("tbody tr:nth-child(1) td:nth-child(5) div:nth-child(1) button:nth-child(1)");
    By staffAccountsTab = By.cssSelector("div[class='col-md-9'] button:nth-child(2)");
    By btnUpdateRoleStaff = By.cssSelector("tbody tr:nth-child(1) td:nth-child(3) select:nth-child(1)");
    By allAccountsTab = By.xpath("//button[contains(text(), 'All Accounts')]"); // Navigate to all accounts tab in the admin panel

    public void approveSignUpRequest() {
        click(approve);
    }

    public void updateRole(String newRole) {
        click(staffAccountsTab);
        click(btnUpdateRoleStaff);
        select(btnUpdateRoleStaff, newRole);
    }
    // Update account role in the all accounts tab in the admin panel
    public void updateRoleForUserInAllAccounts(String email, String newRole) {
        click(allAccountsTab);
        By emailCell = By.xpath("//td[normalize-space()='" + email + "']");
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(emailCell));
        By roleSelect = By.xpath("//td[normalize-space()='" + email + "']/following-sibling::td[2]//select");
        select(roleSelect, newRole);
    }

}
