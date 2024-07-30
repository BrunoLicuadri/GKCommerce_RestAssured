package com.godknows.gkcommerce.controller;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.godknows.gkcommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class OrderControllerRA {
	
	private Long existingOrderId, nonExistingOrderId;
	private String adminUsername, adminPassword, adminToken;
	private String clientUsername, clientPassword, clientToken;
	private String invalidToken;
	
	
	@BeforeEach
	public void setUp() {
		baseURI = "http://localhost:8080";
		
		existingOrderId = 1L;
		nonExistingOrderId = 100L;
		
		adminUsername = "alex@gmail.com";
		adminPassword = "123456";
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		
		clientUsername = "maria@gmail.com";
		clientPassword = "123456";
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		
	}
	
	
	@Test
	public void findByIdShouldReturnOderDTOWhenIdExistsAndAdminLogged() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", existingOrderId)
		.then()
			.statusCode(200)
			.body("id", is(1))
			.body("moment", equalTo("2022-07-25T13:00:00Z"))
			.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
			.body("total", is(1431.0F));
	}
	
	@Test
	public void findByIdShouldReturnOrderDTOWhenIdExistsClientLoggedAndOwner() {
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", existingOrderId)
		.then()
			.statusCode(200)
			.body("id", is(1))
			.body("moment", equalTo("2022-07-25T13:00:00Z"))
			.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
			.body("total", is(1431.0F));
	}
	
	@Test
	public void findByIdShouldReturn403ForbiddenWhenIdExistsClientLoggedButNotOwner() {
		
		Long alexOrderId = 2L;
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", alexOrderId)
	.then()
		.statusCode(403)
		.body("error", equalTo("Access denied. Should be self or admin"));
	}
	
	@Test
	public void findByIdShouldReturn404NotFoundnWhenNonExistingIdAndAdminLogged() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + adminToken)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", nonExistingOrderId)
	.then()
		.statusCode(404)
		.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void findByIdShouldReturn404NotFoundnWhenNonExistingIdAndClientLogged() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", nonExistingOrderId)
	.then()
		.statusCode(404)
		.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void findByIdShouldReturn401UnauthorizeddWhenNotLoggedOrInvalidToken() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + invalidToken)
		.accept(ContentType.JSON)
	.when()
		.get("/orders/{id}", nonExistingOrderId)
	.then()
		.statusCode(401);
	}


}
