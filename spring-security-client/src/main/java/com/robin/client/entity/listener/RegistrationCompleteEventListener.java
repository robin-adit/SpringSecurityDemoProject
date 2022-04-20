package com.robin.client.entity.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.robin.client.entity.User;
import com.robin.client.event.RegistrationCompleteEvent;
import com.robin.client.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent>{

	@Autowired
	private UserService userService;
	
	@Override
	public void onApplicationEvent(RegistrationCompleteEvent event) {
		// Create verification token for the user with Link
		User user = event.getUser();
		String token = UUID.randomUUID().toString();
		userService.saveVerificationTokenForUser(token,user);
		
		//Send Email to User
		String url = event.getApplicationUrl() + "/verifyRegistration?token=" + token;
		//Send verification  email here
		log.info("Click the link to verify your account:" + url);
	}

}
