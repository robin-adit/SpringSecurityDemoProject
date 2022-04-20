package com.robin.client.controller;

import org.springframework.web.bind.annotation.RestController;

import com.robin.client.entity.User;
import com.robin.client.entity.VerificationToken;
import com.robin.client.event.RegistrationCompleteEvent;
import com.robin.client.model.PasswordModel;
import com.robin.client.model.UserModel;
import com.robin.client.repository.VerificationTokenRepository;
import com.robin.client.service.UserService;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@Slf4j
public class RegistrationController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@PostMapping("/register")
	public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request)
	{
		User user = userService.registerUser(userModel);
		publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
		return "Success";
	}
	
	@GetMapping("/verifyRegistration")
	public String verifyRegistration(@RequestParam("token") String token)
	{
		String result = userService.validateVerificationToken(token);
		if (result.equalsIgnoreCase("valid"))
			return "User Verified Successfully";
		else
			return "Bad User";
	}
	
	@GetMapping("/resendVerifyToken")
	public String reSendVerificationToken(@RequestParam("token") String oldToken,
											HttpServletRequest request)
	{
		VerificationToken verificationToken =
				userService.generateNewVerificationToken(oldToken);
		
		User user = verificationToken.getUser();
		resendVerificationTokenMail(user, verificationToken, applicationUrl(request));
		
		return "Verification Link Sent";
	}
	
	@PostMapping("/resetPassword")
	public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request)
	{
		User user = userService.findUserByEmail(passwordModel.getEmail());
		String url= "";
		if(user != null)
			{
				String token = UUID.randomUUID().toString();
				userService.createPasswordResetTokenForUser(user,token);
				url = passwordResetTokenMail(user, applicationUrl(request),token);
			}
		
		return url;
	}
	
	@PostMapping("/savePassword")
	public String savePassword(@RequestParam("token") String token, 
			@RequestBody PasswordModel passwordModel)
	{
		String result = userService.validatePasswordResetToken(token);
		
		if(!result.equalsIgnoreCase("valid"))
		return "Invalid Token";
		 
		Optional<User> user = userService.getUserByPasswordResetToken(token);
		if(user.isPresent())
		{
			userService.changePassword(user.get(),passwordModel.getNewPassword());
			return "Password Reset Successful";
		}
		else
			return "Invalid Token";
	}
	
	@PostMapping("/changePassword")
	public String changePassword(@RequestBody PasswordModel passwordModel)
	{
		User user = userService.findUserByEmail(passwordModel.getEmail());
		if(!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword()))
		{
			return "Invalid Old Password";
		}
		
		//Save New Password
		userService.changePassword(user, passwordModel.getNewPassword());
		
		return "Password Changed Successfully";
	}

	private String applicationUrl(HttpServletRequest request) {
		String url = "http://" + request.getServerName() +
					":" + request.getServerPort() +
					request.getContextPath();
		return url;
	}
	
	private void resendVerificationTokenMail(User user, VerificationToken verificationToken,String applicationUrl)
	{
		String url = applicationUrl + "/verifyRegistration?token=" + verificationToken.getToken();
		//Send verification  email here
		log.info("Click the link to verify your account:" + url);

	}
	
	private String passwordResetTokenMail(User user, String applicationUrl, String token)
	{
		String url = applicationUrl + "/savePassword?token=" + token;
		//Send verification  email here
		log.info("Click the link to reset your password:" + url);
		
		return url;
	}
}
