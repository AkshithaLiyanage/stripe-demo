package akshitha.example.stripe.stripedemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	
	/**
	1. PAY WITHOUT SAVING CARD
	**/
	@ResponseBody
	@RequestMapping(value = "/create-checkout-session" , method = RequestMethod.POST, produces = { "application/json"})
	public  String createCheckoutSession() {
		  // This is your real test secret API key.
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";
/*
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
	    	          
	    	          .build();*/
	     
	     
	     
	     Map<String, Object> params = new HashMap<String, Object>();

	     ArrayList<String> paymentMethodTypes = new ArrayList<String>();
	     paymentMethodTypes.add("card");
	     params.put("payment_method_types", paymentMethodTypes);

	     ArrayList<HashMap<String, Object>> lineItems = new ArrayList<HashMap<String, Object>>();
	     HashMap<String, Object> lineItem = new HashMap<String, Object>();
	     lineItem.put("name", "Kavholm rental");
	     lineItem.put("amount", 5000);
	     lineItem.put("currency", "usd");
	     lineItem.put("quantity", 1);
	     lineItems.add(lineItem);
	     params.put("line_items", lineItems);

	     HashMap<String, Object> paymentIntentData = new HashMap<String, Object>();
	     paymentIntentData.put("application_fee_amount", 1000);
	     HashMap<String, Object> transferData = new HashMap<String, Object>();
	     transferData.put("destination", "acct_1IMAqJPdVMbtVzuo");
	     paymentIntentData.put("transfer_data", transferData);
	     params.put("payment_intent_data", paymentIntentData);

	     params.put("success_url", "http://localhost:8080/success.html");
	     params.put("cancel_url", "http://localhost:8080/cancel.html");

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
	
	/**
	2. SAVE CARD
	**/
	
	@ResponseBody
	@RequestMapping(value = "/create-checkout-session-save-card" , method = RequestMethod.POST, produces = { "application/json"})
	public  String createNSaveCheckoutSession() {
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";

	    
	    
		 SessionCreateParams params =
	    	        SessionCreateParams.builder()
	    	          .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
	    	          .setMode(SessionCreateParams.Mode.SETUP)
	    	          .setCustomer("cus_Iy2jgzUKJKEkHW")// CREATE THE CUSTOMER FOR THE FIRST TIME if customer id alredy not in db
	    	          .setSuccessUrl("http://localhost:8080/saved-success.html?session_id={CHECKOUT_SESSION_ID}")
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
	
	
	/* 2.1
	 * RETRIEVE SESSION ID FROM SAVE CARD STEP AND CHARGE THAT CUSTOMER (from RETURN URL)
	 */
	
	@GetMapping("/saved-success.html")
	public void chargeCustomer(@RequestParam("session_id") String session_id) {
		System.out.println("session_id : "+session_id);
		
		// IF SUCCESS getting session id got charging step othervise riderect to page - card saving failed
		
		// save session id with relevant customer seperately
		
		chargeFromSavedCard(retrieveIntent(getsessionObj(session_id).getSetupIntent()));
		
		//if chargefromsave card fail redirect fail other vise redirect to success
		

	}


	/**
	  3. charge customer later sessions without entering card details
	 **/
	
	@GetMapping("/charge-customer")
	public void chargeCustomerWithCustomerSetupIntent(@RequestParam("setupIntent") String setupIntent) {
		chargeFromSavedCard(retrieveIntent(setupIntent));
	}
	
	
/*
 * utility methods
 */
	
	private Session getsessionObj(String session_id) {
		Session session = null;
		try {
			session = Session.retrieve(session_id );
		} catch (StripeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}

	public SetupIntent retrieveIntent(String setupIntent) {
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";
	    SetupIntent intent = null;
	    try {
			
			intent = SetupIntent.retrieve(setupIntent);
			
		} catch (StripeException e) {
			e.printStackTrace();
		}
	    return intent;
	}
	
	public void chargeFromSavedCard(SetupIntent setupIntent) {
	    Stripe.apiKey = "sk_test_51IDkRqBNaYCeNlA4jUNeiS3qSxjTPkn65FcencgM3919vmPIFzWlYXXhqNOGDVqavNnrzPK0GGk67pytjPFS2wF000s4RXZo7b";

	    for(String types : setupIntent.getPaymentMethodTypes()) {
	    	System.out.println("method types"+ types);
	    }
	    System.out.println("setupIntent.getPaymentMethod() : " + setupIntent.getPaymentMethod());
	    

	    ArrayList paymentMethodTypes = new ArrayList();
	    paymentMethodTypes.add("card");

	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("payment_method_types", paymentMethodTypes);
	    params.put("amount", 6000);
	    params.put("currency", "usd");
	    params.put("application_fee_amount", 2000);
	    Map<String, Object> transferDataParams = new HashMap<String, Object>();
	    transferDataParams.put("destination", "acct_1IMAqJPdVMbtVzuo");
	    params.put("transfer_data", transferDataParams);

	    
	    
		PaymentIntentCreateParams param =
				  PaymentIntentCreateParams.builder()
				    //.setCurrency("usd")
				    //.setAmount(200l)
				    .setPaymentMethod(setupIntent.getPaymentMethod())
				    .setCustomer(setupIntent.getCustomer())
				    .setConfirm(true)
				    .setOffSession(true)
				    .putAllExtraParam(params)
				    .build();
				try {
				  PaymentIntent.create(param);
				  System.out.println("CARD CHARGED SUCCEFULLY !!!");
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
	
	/*
	 * CREATE THE CUSTOMER FOR THE FIRST TIME
	 */

	private Customer createCustomer() {
		CustomerCreateParams params =
				  CustomerCreateParams.builder()
				  .setEmail("alexf@ef.com")
				  .setName("Alex Ferraro")
				    .build();

		Customer customer = null;
				try {
					customer = Customer.create(params);
				} catch (StripeException e) {
					e.printStackTrace();
				}
		return customer;
	}
	
	
	
}
