package controllers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import javax.swing.text.html.FormSubmitEvent;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;


import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation.ValidationResult;
import play.db.jpa.JPA;
import play.libs.Mail;
import util.encryption.BCrypt;

public class Customer extends Application{
	
	public static void saveCustomer(@Valid models.Customer customer, String verifyPassword){
		play.Logger.debug("Saving customer");
		boolean validEmail = true;
		List<String> emails = JPA.em().createNativeQuery("select email from Customer").getResultList();
		for(String e : emails){
			if(e.equals(customer.email)){
				validEmail = false;
				play.Logger.info("Mail already exists",validEmail);
			}
		}
		if(!validEmail){
			validation.required(customer.email);
			//validation.equals("", customer.email).message("E-mail is already taken");
			validation.addError("customer.email","This e-mail address is already registered. If this is your account, reset the password.");
			if(validation.hasErrors()){
				play.Logger.info("E-mail is not valid");
				render("@register", customer, validation.error("customer.email"));
			}
		}
		
		validation.required(verifyPassword);
		validation.equals(verifyPassword, customer.password).message("Your password does not match.");
		if(validation.hasErrors()){
			render("@register", customer, verifyPassword);
		}
		
		if(validEmail){
			play.Logger.info("Valid e-mail");
			String hash = BCrypt.hashpw(verifyPassword, BCrypt.gensalt());
			customer.password = hash;
			customer.save();
			play.Logger.info("Customer saved");	
			//session.put("customer", customer.email);
		    flash.success("Welcome, " + customer.firstName);
		    index();
		}
	}
	
	public static void index() {
		List<Customer> customers =  JPA.em().createNativeQuery("select * from Customer", models.Customer.class).getResultList();
        render(customers);
		
	}
	public static void register() {
        render();
    }
	
	public static void loginPage(){
		render();
	}
	
	public static void resetPassword(){
		render();
	}
	public static void login(@Required String email, @Required String password){
		validation.required(email);
		if(validation.hasErrors()){
			render("@loginPage", email);
		}
		models.Customer customer = null;
		try{
			customer = models.Customer.findByEmail(email);
		}catch(Exception e){
			validation.required(email);
			validation.addError("email", "This e-mail does not exists in our database.");
			if(validation.hasErrors()){
				render("@loginPage",validation.error("email"));
			}
		}
		if(customer != null){
			if(BCrypt.checkpw(password, customer.password)){
				session.put("customer", customer);
				play.Logger.info("Customer with id "+customer.id + " has logged in.");
				flash.success("Welcome "+customer.firstName);
				index();
			}
			else{
				validation.required(password);
				validation.equals(password, BCrypt.checkpw(password, customer.password)).message("Password not valid");
				if(validation.hasErrors()){
					render("@loginPage", password);
				}
			}
		}
	}
	
	public static void lostPassword(@Required String email) throws EmailException{
		validation.required(email);
		if(validation.hasErrors()){
			render("@resetPassword", email);
		}
		SecureRandom random = new SecureRandom();
		String newPass = new BigInteger(32,random).toString(32);
		models.Customer customer = null;
		try{
			customer = models.Customer.findByEmail(email);
		}catch(Exception e){
			validation.required(email);
			validation.addError("email", "This e-mail does not exists in our database.");
			if(validation.hasErrors()){
				render("@resetPassword",validation.error("email"));
			}
		}
		
		SimpleEmail e_mail = new SimpleEmail();
		e_mail.setFrom("handler@drinkstation.com");
		e_mail.addTo(email);
		e_mail.setSubject("Password change request");
		e_mail.setMsg("Dear "+customer.firstName + "\n\n  You or someone else requested to change the password on your account at DrinkStation."
				+"\n  The new password is "+newPass+".\n " +
				"\nPlease change your password as soon as possible. "+" \nBest regards, \n  Team DrinkStation");
		Mail.send(e_mail);
		customer.password = BCrypt.hashpw(newPass, BCrypt.gensalt());
		customer.save();
		play.Logger.info("Password reseted" );
		
		flash.success("Password reseted. You will receive an e-mail with your password shortly.");
	}
	public static void logout(){
		session.clear();
	}
}
