package com.godknows.gkcommerce.controller;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.godknows.gkcommerce.tests.TokenUtil;

import io.restassured.http.ContentType;


public class ProductControllersRA {
	
	private String clientUserName, adminUserName;
	private String clientPassword, adminPassword;
	private String clientToken, adminToken, invalidToken;
	
	private Long existingProductId, nonExistingProductId, dependentProductId;
	private String productName;
	
	private Map<String, Object> postProductInstance;
	
	private List<Map<String,Object>> categories;

	@BeforeEach
	public void setUp() {
		
		baseURI = "http://localhost:8080";
		productName = "Macbook";
		
		existingProductId = 25L;
		nonExistingProductId = 100L;
		dependentProductId = 3L;
		
		postProductInstance = new HashMap<>();
		postProductInstance.put("name", "Meu produto");
		postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
		postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		postProductInstance.put("price", 50.0F);
		
		categories = new ArrayList<>();
		
		Map<String, Object> category2 = new HashMap<>();
		category2.put("id", 2);
		Map<String, Object> category3 = new HashMap<>();
		category3.put("id", 3);
		
		categories.add(category2);
		categories.add(category3);
		
		postProductInstance.put("categories", categories);
		
		clientUserName = "maria@gmail.com";
		clientPassword = "123456";
		clientToken = TokenUtil.obtainAccessToken(clientUserName, clientPassword);
		
		adminUserName = "alex@gmail.com";
		adminPassword = "123456";
		adminToken = TokenUtil.obtainAccessToken(adminUserName, adminPassword);
		
		invalidToken = adminToken + "xpto";
		
	}
	
	@Test
	public void findByIdShouldREturnProductDTOWhenIdExists() {
		
		existingProductId = 2L;
		
		given()
			.get("/products/{id}", existingProductId)
		.then()
			.statusCode(200)
			.body("id", is(2))
			.body("name", equalTo("Smart TV"))
			.body("price", is(2190.0F))
			.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
			.body("categories.id", hasItems(2,3))
			.body("categories.name", hasItems("Eletrônicos", "Computadores"));
	}
	
	@Test
	public void findAllShouldReturnProductsWhenNameNotGiven() {
		
		given()
			.get("/products?page=0")
		.then()
			.statusCode(200)
			.body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
	}
	
	@Test
	public void findAllShouldReturnProductWhenGivenName() {
		
		given()
			.get("/products?name={productName}", productName)
		.then()
			.statusCode(200)
			.body("content.id[0]", is(3))
			.body("content.name[0]", equalTo("Macbook Pro"))
			.body("content.price[0]", is(1250.0F))
			.body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
	}
	
	@Test
	public void findAllShouldReturnProductsPricierThan2000() {
		
		given()
			.get("/products?size=25")
		.then()
			.statusCode(200)
			.body("content.findAll{ it.price > 2000 }.name", hasItems("Smart TV", "PC Gamer Weed", "PC Gamer Hera"));
	}
	
	@Test
	public void insertShouldReturnProductCreatedWhenAdminLogged() {
		
		JSONObject newProduct = new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(201)
			.body("name", equalTo("Meu produto"))
			.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
			.body("price", is(50.0F))
			.body("categories.id", hasItems(2, 3));
	}
	
	@Test
	public void insertShouldReturn422WhenProductNameInvalidAndAdminLogged() {
		postProductInstance.put("name", "ab");
		
		JSONObject invalidProductName = new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + adminToken)
			.body(invalidProductName)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));
	}
	
	@Test
	public void insertShouldRetunr422WhenProductDescritionIsInvalidAndAdminLogged() {
		
		postProductInstance.put("description", "123456789");
		
		JSONObject invalidProductDescription = new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + adminToken)
			.body(invalidProductDescription)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
	}
	
	@Test
	public void insertShouldReturn422WhenProductPriceIsNegativeAndAdminLogged() {
		
		postProductInstance.put("price", -5.0);
		
		JSONObject productPriceNegative = new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + adminToken)
			.body(productPriceNegative)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}
	
	@Test
	public void insertShouldReturn422WhenProductPriceIsZeroAndAdminLogged() {
		
		postProductInstance.put("price", 0.0);
		
		JSONObject productPriceZero = new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + adminToken)
			.body(productPriceZero)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}
	
	@Test
	public void insertShouldReturn422WhenProductCategoriesIsEmptyAndAdminLogged() {
		
		categories.clear();
		postProductInstance.put("categories", categories);
		// we also could have just done:  postProductInstance.put("categories", null);
		
		JSONObject productSinCategory = new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + adminToken)
			.body(productSinCategory)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
	}
	
	@Test
	public void insertShouldReturn403WhenClientLogged() {
		
		JSONObject newProduct= new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + clientToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void insertShouldReturn401WhenInvalidToken() {
		
		JSONObject newProduct= new JSONObject(postProductInstance);
		
		given()
			.header("content-type", "application/json")
			.header("authorization", "Bearer " + invalidToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(401);
	}

	@Test
	public void deleteShouldReturnNoContentWhenIdExisttsAndAdminLogged() {
		
		given()
			.header("Authorization", "Bearer " + adminToken)
		.when()
			.delete("products/{id}", existingProductId)
		.then()
			.statusCode(204);
	}
	
	@Test
	public void deleteShouldReturn404WhenNonExistingProductIdAndAdminLogged() {
		
		given()
			.header("Authorization", "Bearer " + adminToken)
		.when()
			.delete("/products/{id}", nonExistingProductId)
		.then()
			.statusCode(404)
			.body("error", equalTo("Recurso não encontrado"))
			.body("status", equalTo(404));
	}
	
	@Test
	public void deleteShouldReturn400WhenDependentIdAndAdminLogged() {
		
		given()
			.header("Authorization", "Bearer " + adminToken)
		.when()
			.delete("/products/{id}", dependentProductId)
		.then()
			.statusCode(400);
	}
	
	@Test
	public void deleteShouldReturn403WhenExistingProductIdButClientLogged() {
		
		given()
			.header("Authorization", "Bearer " + clientToken)
		.when()
			.delete("/products/{id}", existingProductId)
		.then()
			.statusCode(403);
	}
	
	@Test
	public void deleteShouldReturn401WhenExistingProductIdButNotLogged() {
		
		given()
			.header("Authorization", "Bearer " + invalidToken)
		.when()
			.delete("/products/{id}", existingProductId)
		.then()
			.statusCode(401);
	}
	
	
}
