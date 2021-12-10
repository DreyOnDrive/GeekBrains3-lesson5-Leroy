import org.junit.jupiter.api.*;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LeroyMerlinTest {

    WebDriver driver;
    WebDriverWait webDriverWait;
    WebDriverWait webDriverWaitSmall;
    Actions actions;

    @BeforeAll
    static void registerDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupDriver() {
        driver = new ChromeDriver();
        webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        webDriverWaitSmall = new WebDriverWait(driver, Duration.ofSeconds(5));
        actions = new Actions(driver);
        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();

        driver.get("https://leroymerlin.ru/login");
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//iframe[@id='oauth-iframe']")));
        driver.switchTo().frame("oauth-iframe");
        driver.findElement(By.xpath("//input[@id=\"username\"]")).sendKeys("xowapi1@ineedsa.com");
        driver.findElement(By.xpath("//input[@id=\"password\"]")).sendKeys("Aa123456");
        driver.findElement(By.xpath("//input[@value=\"Войти\"]")).click();
    }

    @Test
    @DisplayName("Проверим добавление товара в корзину и расчёт цены")
    void addInBasket() throws InterruptedException {
        String priceText1;
        String priceText2;
        String nameText1;
        String nameText2;
        String itemPath;

        webDriverWait.until(ExpectedConditions.jsReturnsValue("return document.querySelector(\"a[href^=" +
                "'/catalogue/']\").textContent == 'Каталог'"));

        ((JavascriptExecutor)driver).executeScript("document.querySelector(\"a[href^='/catalogue/']\").click()");

        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[.='Декор']")));

        driver.findElement(By.xpath("//span[.='Декор']")).click();

//        ((JavascriptExecutor) driver).executeScript("javascript:window.scrollBy(0,550)");
//        Thread.sleep(1000);
//        driver.findElement(By.xpath("//span[.='Хранение']")).click();

//        ((JavascriptExecutor) driver).executeScript("javascript:window.scrollBy(0,1100)");
//        Thread.sleep(1000);
//        driver.findElement(By.xpath("//span[.='Плитка']")).click();

        //Специально выбраны локаторы которые не привязываются к содержимому а просто находят элемент по местоположению,
        // а наличие написанных человеком локатором сводится к минимуму на данной странице, было выделено два типа местоположения
        //хорошо бы сделать выполнения поиска этих элементов асинхронным, но пока это сложно, также  не учитывается что
        // появились партнёрские товары
        try {
            webDriverWaitSmall.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[@id=\"root\"]/main/div[2]/div[2]/div/section/div[4]/section/div[1]/div[1]")));
            itemPath = "//*[@id=\"root\"]/main/div[2]/div[2]/div/section/div[4]";
        } catch (TimeoutException e) {
            webDriverWaitSmall.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[@id=\"root\"]/main/div[2]/div[2]/div/section/div[3]/section/div[1]/div[1]")));
            itemPath = "//*[@id=\"root\"]/main/div[2]/div[2]/div/section/div[3]";
        }

        priceText1 = driver.findElement(By.xpath( itemPath + "/section/div[1]/div[1]/div[2]/div/div[1]/p[1]"))
                .getText().replaceAll("[^0-9/.]", "");
        nameText1 = driver.findElement(By.xpath( itemPath + "/section/div[1]/div[1]/div[1]/a/span/span")).getText();
        driver.findElement(By.xpath( itemPath + "/section/div[1]/div[1]/div[2]/button/span/span")).click();
        priceText2 = driver.findElement(By.xpath(itemPath + "/section/div[1]/div[2]/div[2]/div/div/p[1]"))
                .getText().replaceAll("[^0-9/.]", "");
        nameText2 = driver.findElement(By.xpath(itemPath + "/section/div[1]/div[2]/div[1]/a/span/span")).getText();
        driver.findElement(By.xpath(itemPath + "/section/div[1]/div[2]/div[2]/button")).click();
        driver.findElement(By.xpath("//a[@href=\"/basket/\"]")).click();
        webDriverWait.until(ExpectedConditions.jsReturnsValue("return document.querySelector(\"#root > uc-app >" +
                " uc-container > main > uc-basket\").shadowRoot.querySelector(\"section:nth-child(4) > div > h1\").textContent == \"Корзина\""));
        String namebasket1 = ((JavascriptExecutor) driver).executeScript("return document.querySelector(\"#root > uc-app >" +
                " uc-container > main > uc-basket\").shadowRoot.querySelector(\"section:nth-child(10) > section >" +
                " item-basket-card:nth-child(4)\").shadowRoot.querySelector(\"layout-basket-card > div > a\").textContent").toString();
        String namebasket2 = ((JavascriptExecutor) driver).executeScript("return document.querySelector(\"#root > uc-app >" +
                " uc-container > main > uc-basket\").shadowRoot.querySelector(\"section:nth-child(10) > section >" +
                " item-basket-card:nth-child(5)\").shadowRoot.querySelector(\"layout-basket-card > div > a\").textContent ").toString();
        String basketPriceText = ((JavascriptExecutor) driver).executeScript("return document.querySelector(\"#root >" +
                " uc-app > uc-container > main > uc-basket\").shadowRoot.querySelector(\"section:nth-child(10) > aside > div >" +
                " div > div.summary-information-block__total > span.summary-information-block__total-price\").textContent").toString();
        basketPriceText = basketPriceText.substring(0, basketPriceText.length()-2).replaceAll("[^0-9/.]", "");
        Double price1 = Double.parseDouble(priceText1);
        Double price2 = Double.parseDouble(priceText2);
        Double sumPrice = price1 + price2;
        Double basketPrice = Double.parseDouble(basketPriceText);
        Assertions.assertEquals(sumPrice, basketPrice);
        Assertions.assertEquals(nameText1, namebasket1);
        Assertions.assertEquals(nameText2, namebasket2);
    }

    @Test
    @DisplayName("Проверим, что без заполнения обязательных полей заказ невозможен")
    void notOrder() throws InterruptedException {
        webDriverWait.until(ExpectedConditions.jsReturnsValue("return document.querySelector(\"a[href^=" +
                "'/catalogue/']\").textContent == 'Каталог'"));
        ((JavascriptExecutor)driver).executeScript("document.querySelector(\"a[href^='/catalogue/']\").click()");
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[.='Освещение']")));
        driver.findElement(By.xpath("//span[.='Освещение']")).click();
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/main/div[2]" +
                "/div[2]/div/section/div[3]/section/div[1]/div[2]/div[2]/button/span/span")));
        driver.findElement(By.xpath("//*[@id=\"root\"]/main/div[2]/div[2]/div/section/div[3]/section/" +
                "div[1]/div[2]/div[2]/button/span/span")).click();
        driver.findElement(By.xpath("//a[@href=\"/basket/\"]")).click();
        webDriverWait.until(ExpectedConditions.jsReturnsValue("return document.querySelector(\"#root > uc-app >" +
                " uc-container > main > uc-basket\").shadowRoot.querySelector(\"section:nth-child(4) > div > h1\").textContent == \"Корзина\""));
        ((JavascriptExecutor) driver).executeScript("return document.querySelector(\"#root > uc-app > uc-container > main" +
                " > uc-basket\").shadowRoot.querySelector(\"section:nth-child(12) > aside > div > div > uc-button > button\").click()");
        webDriverWait.until(ExpectedConditions.jsReturnsValue("return document.querySelector(\"#root > uc-app > uc-container >" +
                " main > uc-checkout\").shadowRoot.querySelector(\"layout-checkout > h2\").textContent == 'Оформление заказа'"));
        ((JavascriptExecutor) driver).executeScript("document.querySelector(\"#root > uc-app > uc-container > main > uc-checkout\")" +
                ".shadowRoot.querySelector(\"layout-checkout > checkout-totals > uc-button > button\").click()");
        Assertions.assertTrue((Boolean) ((JavascriptExecutor) driver).executeScript("return document.querySelector(\"#root > uc-app >" +
                " uc-container > main > uc-checkout\").shadowRoot.querySelector(\"layout-checkout > checkout-totals > uc-button > button\").disabled"));

//      конечно тест кейс немного читерский, тут конечно хорошо бы проверить что при заполнении полей кнопка заказа доступна (как эквивалент возможности заказа)
//      но при заполнении полей через JS форма не реагирует, а через селениум так и не получилось достучаться до содержимого теневого корня
//
//      использовал подход описанный в присланной Вами статье на странице корзины, пытаюсь зайти в перый корень и получить  main
//      WebElement shadowRoot1 = driver.findElement(By.id("root"));
//      SearchContext shadowContext = shadowRoot1.getShadowRoot();
//      WebElement shadowContent = shadowContext.findElement(By.xpath("//main"));
    }

    @AfterEach
    void afterEach() {
        driver.quit();
    }
}