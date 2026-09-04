package pages.ViewPage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class demoWebShop {
    private WebDriver driver;

    private Map<String, Object> elementMap = new ConcurrentHashMap<>();

    public demoWebShop(WebDriver driver) {
        this.driver = driver;

        // Initialize @FindBy elements FIRST
        PageFactory.initElements(driver, this);

        // Then populate the map (single concurrent map for both single and multiple elements)
        initializeSingleElements();
        initializeMultipleElements();
    }

    @FindBy(id = "small-searchterms")
    private WebElement searchBox;

    @FindBy(css = "input[value='Search']")
    private WebElement searchButton;

    @FindBy(css = "a.ico-login")
    private WebElement loginLink;

    @FindBy(xpath = "//div[@class='item-box']//span")
    private List<WebElement> productPrices;

    private void initializeSingleElements() {
        elementMap.put("searchBox", searchBox);
        elementMap.put("searchButton", searchButton);
        elementMap.put("loginLink", loginLink);
    }
    private void initializeMultipleElements() {
        elementMap.put("productPrices", productPrices);
    }
    public Map<String, Object> getElementMap() {
        return elementMap;
    }

}
