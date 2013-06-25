package org.zanata.page.groups;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class VersionGroupsPage extends AbstractPage
{
   public static final int GROUP_NAME_COLUMN = 0;

   @FindBy(id = "groupForm:groupTable")
   private WebElement groupTable;

   public VersionGroupsPage(WebDriver driver)
   {
      super(driver);
   }

   public List<String> getGroupNames()
   {
      By by = By.id("groupForm:groupTable");
      return WebElementUtil.getColumnContents(getDriver(), by, GROUP_NAME_COLUMN);
   }

   public CreateVersionGroupPage createNewGroup()
   {
      WebElement createLink = getDriver().findElement(By.linkText("Create New Group"));
      createLink.click();
      return new CreateVersionGroupPage(getDriver());
   }

   public VersionGroupPage goToGroup(String groupName)
   {
      WebElement groupLink = groupTable.findElement(By.linkText(groupName));
      groupLink.click();
      return new VersionGroupPage(getDriver());
   }
}