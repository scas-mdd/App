package controllers;

import java.util.List;

import javax.swing.text.html.FormSubmitEvent;

import org.apache.log4j.Logger;


import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation.ValidationResult;
import play.db.jpa.JPA;
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
			validation.equals("", customer.email).message("E-mail is already taken");
			
			if(validation.hasErrors()){
				play.Logger.info("E-mail is not valid");
				render("@register", customer, customer.email);
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
	public static void login(@Required String email, @Required String password){
		
		models.Customer customer = models.Customer.findByEmail(email);
		if(customer != null){
			if(BCrypt.checkpw(password, customer.password)){
				session.put("customer", customer);
				play.Logger.info("Customer with id "+customer.id + " has logged in.");
			}
			else{
				validation.required(password);
				validation.equals(password, BCrypt.checkpw(password, customer.password)).message("Password not valid");
				if(validation.hasErrors()){
					params.current();
					render("@loginPage", password);
				}
			}
		}
	}
	public static void logout(){
		session.clear();
	}
}
