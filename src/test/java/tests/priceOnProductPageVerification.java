package tests;

import core.base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ViewPage.demoWebShop;

import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class priceOnProductPageVerification extends BaseTest {

    @Test
    public void verifyProductsGreaterThan1000() {
        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get("https://demowebshop.tricentis.com/");

        List<WebElement> products = driver.findElements(
                By.cssSelector(".item-box")
        );

        Map<String, BigDecimal> productMap = new LinkedHashMap<>();
        //Map<String, Integer> productMap = new LinkedHashMap<>();

        for (WebElement product : products) {

            String productName = product.findElement(
                    By.cssSelector("h2.product-title")
            ).getText();

            String priceText = product.findElement(
                    By.cssSelector("span.actual-price")
            ).getText();

           // int price = (int) Double.parseDouble(priceText);
            BigDecimal price = new BigDecimal(priceText);
            productMap.put(productName, price);
        }

        System.out.println("All products:");
        System.out.println(productMap);

        System.out.println("Products having price greater than 1000:");

        productMap.entrySet()
                .stream()
                //.filter(entry -> entry.getValue() > 1000)
                .filter(entry -> entry.getValue().compareTo(new BigDecimal("1000")) > 0)
                .forEach(entry ->
                        System.out.println(
                                entry.getKey() + " --> " + entry.getValue()
                        )
                );
    }


    @Test
    public void priceOnProductPageVerification() throws InterruptedException {

        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get("https://demowebshop.tricentis.com/");

        Thread.sleep(2000);
        demoWebShop demoWebShopViewPage=new demoWebShop(driver);
        Map<String, Object> elements = demoWebShopViewPage.getElementMap();
        List<WebElement> prices =  (List<WebElement>) elements.get("productPrices");
        System.out.println(prices.size());
        for(WebElement price: prices){
            int priceValueInt = (int) Double.parseDouble(price.getText());
                if(priceValueInt > 1000) {
                    System.out.println(priceValueInt);
                }
        }
    }
}
