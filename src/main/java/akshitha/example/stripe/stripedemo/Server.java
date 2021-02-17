package akshitha.example.stripe.stripedemo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;


@RestController
public class Server {
	private static Gson gson = new Gson();
	@ResponseBody
	@RequestMapping(value = "/create-checkout-session" , method = RequestMethod.POST, produces = { "application/json"})
	public  String createCheckoutSession() {
		  // This is your real test secret API key.
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";

	     SessionCreateParams params =
	    	        SessionCreateParams.builder()
	    	          .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
	    	          .setMode(SessionCreateParams.Mode.PAYMENT)
	    	          .setCustomerEmail("customer@example.com")
	    	          .setSuccessUrl("http://localhost:8080/success.html")
	    	          .setCancelUrl("http://localhost:8080/cancel.html")
	    	          .addLineItem(
	    	          SessionCreateParams.LineItem.builder()
	    	            .setQuantity(1L)
	    	            .setPriceData(
	    	              SessionCreateParams.LineItem.PriceData.builder()
	    	                .setCurrency("usd")
	    	                .setUnitAmount(2000L)
	    	                .setProductData(
	    	                  SessionCreateParams.LineItem.PriceData.ProductData.builder()
	    	                    .setName("T-shirt")
	    	                    .build())
	    	                .build())
	    	            .build())
	    	          .build();

	    	      Session session = null;
				try {
					session = Session.create(params);
				} catch (StripeException e) {
					e.printStackTrace();
				}

	    	      Map<String, String> responseData = new HashMap();
	    	      responseData.put("id", session.getId());
	    	      
	    	      return gson.toJson(responseData);
	    
	    
	    
	    
	}
	
	@ResponseBody
	@RequestMapping(value = "/create-checkout-session-save-card" , method = RequestMethod.POST, produces = { "application/json"})
	public  String createNSaveCheckoutSession() {
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";

	    
	    
		 SessionCreateParams params =
	    	        SessionCreateParams.builder()
	    	          .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
	    	          .setMode(SessionCreateParams.Mode.SETUP)
	    	          .setCustomer(getCustomer().getId())
	    	          .setSuccessUrl("http://localhost:8080/success.html?session_id={CHECKOUT_SESSION_ID}")
	    	          .setCancelUrl("http://localhost:8080/cancel.html")

	       	          .build();

	    	      Session session = null;
				try {
					session = Session.create(params);
				} catch (StripeException e) {
					e.printStackTrace();
				}

	    	      Map<String, String> responseData = new HashMap();
	    	      responseData.put("id", session.getId());
	    	      
	    	      return gson.toJson(responseData);
	    
	    
	}

	private Customer getCustomer() {
		CustomerCreateParams params =
				  CustomerCreateParams.builder()
				  .setEmail("abcd@ef.com")
				  .setName("Ann corn")
				    .build();

		Customer customer = null;
				try {
					customer = Customer.create(params);
				} catch (StripeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		return customer;
	}
	
	public void retrieveSessionObject() {
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";

	    try {
			Session session =
					  Session.retrieve(
					    "cs_test_tzGDgl0pxxuJo75CZPv2Y6VkFIaqO50ngkO55OAOeZ1jP3ydDd3NmjS5"
					  );
			SetupIntent intent = SetupIntent.retrieve(session.getSetupIntent());

		} catch (StripeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void chargeFromSavedCard() {
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";

		PaymentIntentCreateParams params =
				  PaymentIntentCreateParams.builder()
				    .setCurrency("usd")
				    .setAmount(10l)
				    .setPaymentMethod("{{PAYMENT_METHOD_ID}}")
				    .setCustomer("{{CUSTOMER_ID}}")
				    .setConfirm(true)
				    .setOffSession(true)
				    .build();
				try {
				  PaymentIntent.create(params);
				} catch (CardException err) {
				  // Error code will be authentication_required if authentication is needed
				  System.out.println("Error code is : " + err.getCode());
				  String paymentIntentId = err.getStripeError().getPaymentIntent().getId();
				  PaymentIntent paymentIntent = null;
				try {
					paymentIntent = PaymentIntent.retrieve(paymentIntentId);
				} catch (StripeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  System.out.println(paymentIntent.getId());
				} catch (StripeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
}
