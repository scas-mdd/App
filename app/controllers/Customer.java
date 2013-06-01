package controllers;

import java.util.List;


import play.data.validation.Valid;
import play.db.jpa.JPA;

public class Customer extends Application{

	public static void saveCustomer(@Valid models.Customer customer, String verifyPassword){
		validation.required(verifyPassword);
		validation.equals(verifyPassword, customer.password).message("Your password does not match.");
		if(validation.hasErrors()){
			render("@register", customer, verifyPassword);
		}
		customer.save();
		session.put("customer", customer.email);
	    flash.success("Welcome, " + customer.firstName);
	    index();
	}
	
	public static void index() {
		List<Customer> customers =  JPA.em().createNativeQuery("select * from Customer", models.Customer.class).getResultList();
        render(customers);
		
	}
	public static void register() {
        render();
    }
	
	public static void logout(){
		session.clear();
	}
}
