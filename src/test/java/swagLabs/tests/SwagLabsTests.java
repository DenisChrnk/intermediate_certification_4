package swagLabs.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import swagLabs.ext.CartPageResolver;
import swagLabs.ext.CheckoutPageResolver;
import swagLabs.ext.LoginPageResolver;
import swagLabs.ext.MainPageResolver;
import swagLabs.pom.pages.CartPage;
import swagLabs.pom.pages.CheckoutPage;
import swagLabs.pom.pages.LoginPage;
import swagLabs.pom.pages.MainPage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static io.qameta.allure.Allure.step;

@ExtendWith({LoginPageResolver.class, MainPageResolver.class, CartPageResolver.class, CheckoutPageResolver.class})

public class SwagLabsTests {
    static String standardUserLogin;
    static String lockedUserLogin;
    static String glitchUserLogin;
    static String usersPassword;

    static Properties properties;

    @BeforeAll
    public static void globalSetUp() throws IOException {

        String appConfigPath = "src/test/resources/env.properties";

        properties = new Properties();
        properties.load(new FileInputStream(appConfigPath));

        standardUserLogin = properties.getProperty("standard_user_login");
        lockedUserLogin = properties.getProperty("locked_user_login");
        glitchUserLogin = properties.getProperty("glitch_user_login");
        usersPassword = properties.getProperty("users_password");

        Configuration.baseUrl = properties.getProperty("baseURI");
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
        Configuration.timeout = 10000L;
    }

    private static Stream<String> getLogins() {
        return Stream.of(standardUserLogin, glitchUserLogin);
    }

    @Test
    @DisplayName("Проверка авторизации с валидным логином и паролем")
    public void validAuth(LoginPage loginPage, MainPage mainPage) {
        loginPage.openShop();
        loginPage.auth(standardUserLogin, usersPassword);
        mainPage.openMenu();
        step("Проверить, что есть кнопака \"Logout\"", () -> $("#logout_sidebar_link").shouldBe(text("Logout")));
    }

    @Test
    @DisplayName("Проверка невозможности авторизоваться заблокированным пользователем")
    public void invalidAuth(LoginPage loginPage) {
        String textToBe = "Epic sadface: Sorry, this user has been locked out.";

        loginPage.openShop();
        loginPage.auth(lockedUserLogin, usersPassword);
        step("Проверить, что на странице отображен текст " + textToBe, () -> $("h3").shouldHave(text(textToBe)));
    }

    @ParameterizedTest
    @MethodSource("getLogins")
    @DisplayName("Проверка полного цикла заказа")
    public void fullOrderCycle(String logins, LoginPage loginPage, MainPage mainPage, CartPage cartPage, CheckoutPage checkoutPage) {
        String textToBe = "Thank you for your order!";

        loginPage.openShop();
        loginPage.auth(logins, usersPassword);
        mainPage.addToBasket(new ArrayList<>(List.of("Sauce Labs Backpack", "Sauce Labs Bolt T-Shirt", "Sauce Labs Onesie")));
        mainPage.openBasket();
        step("Проверить, что в корзине добавлено 3 товара", () -> $$(".cart_item").shouldHave(size(3)));
        cartPage.clickCheckoutButton();
        checkoutPage.fillingForm("Bob", "Bob", "345636");
        checkoutPage.clickContinue();
        step("Проверить, что сумма заказа равна $58.29", () -> $(".summary_total_label").shouldHave(text("Total: $58.29")));
        checkoutPage.clickFinish();
        step("Проверить, что на странице отображен текст " + textToBe, () -> $(".complete-header").shouldHave(text(textToBe)));
    }
}