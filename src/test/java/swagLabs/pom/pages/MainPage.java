package swagLabs.pom.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import swagLabs.pom.elements.ProductCard;

import java.util.ArrayList;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

public class MainPage {

    private final SelenideElement menuButton = $("#react-burger-menu-btn");
    private final SelenideElement basketButton = $("#shopping_cart_container");

    @Step("Открыть меню")
    public void openMenu() {
        menuButton.click();
    }

    @Step("Добавить в корзину товары {productNames}")
    public void addToBasket(ArrayList<String> productNames) {
        for (String prodName : productNames) {
            SelenideElement item = $$(".inventory_item").findBy(text(prodName));
            ProductCard product = new ProductCard(item);
            product.addToCart();
        }
    }

    @Step("Перейти в корзину")
    public void openBasket() {
        basketButton.click();
    }
}