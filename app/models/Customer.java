package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.*;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;

@Entity
public class Customer extends GenericModel{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	@Required
	public String firstName;
	@Required
	public String lastName;
	@Required
	public String street;
	@Required
	public String city;
	@Required
	public String postalCode;
	@Required
	public String country;
	@Required
	@MaxSize(value=255, message = "email.maxsize")
    @Email
    public String email;
	
	@Required
	@Password
	public String password;
	public Customer(String firstName, String lastName, String street,
			String city, String postalCode, String country, String email,
			String password) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.street = street;
		this.city = city;
		this.postalCode = postalCode;
		this.country = country;
		this.email = email;
		this.password = password;
	}
	
	
	@Override
	public String toString() {
		return "Customer [firstName=" + firstName
				+ ", lastName=" + lastName + ", street=" + street + ", city="
				+ city + ", postalCode=" + postalCode + ", country=" + country
				+ ", email=" + email + ", password=" + password + "]";
	}

	public static models.Customer findByEmail(String email2) {
		// TODO Auto-generated method stub
		return (Customer) JPA.em().createNativeQuery("select * from customer where email='"+email2+"'",Customer.class).getSingleResult();
	}
	
}
