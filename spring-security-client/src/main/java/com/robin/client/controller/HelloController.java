package com.robin.client.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class HelloController {

	//@GetMapping("/hello")
	@GetMapping("/api/hello")
	public String sayHello(Principal principal)
	{
		return "Hello "+ principal.getName() + "!!!! Welcome to the Security Spring Boot App";
	}
	
	@Autowired
	private WebClient webClient;
	
	@GetMapping("/api/users")
	public String[] users(@RegisteredOAuth2AuthorizedClient("api-client-authorization-code") OAuth2AuthorizedClient client)
	{
		return this.webClient
				.get()
				.uri("http://127.0.0.1/8090/api/users")
				.attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(client))
				.retrieve().bodyToMono(String[].class)
				.block();
	}
}
