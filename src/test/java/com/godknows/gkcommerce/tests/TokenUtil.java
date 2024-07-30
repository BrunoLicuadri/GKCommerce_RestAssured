package com.godknows.gkcommerce.tests;

import static io.restassured.RestAssured.given;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TokenUtil {
	
	public static String obtainAccessToken(String userName, String userPassword) {
		
		Response response = authRequest(userName, userPassword);
		JsonPath jsonBody = response.jsonPath();
		
		return jsonBody.getString("access_token");
	}

	private static Response authRequest(String userName, String userPassword) {

		return given()
				.auth()
				.preemptive()
				.basic("myclientid", "myclientsecret")
			.contentType("application/x-www-form-urlencoded")
				.formParam("grant_type", "password")
				.formParam("username", userName)
				.formParam("password", userPassword)
			.when()
			.post("/oauth2/token");
	}

}
