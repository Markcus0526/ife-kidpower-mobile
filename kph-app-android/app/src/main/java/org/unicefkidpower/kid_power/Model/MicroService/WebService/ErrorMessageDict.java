package org.unicefkidpower.kid_power.Model.MicroService.WebService;

/**
 * Created by Ruifeng Shi on 3/18/2017.
 */

public class ErrorMessageDict {
	public static String serverResponse2UserFriendlyMessage(String response) {
		String result = response;

		switch (response.toLowerCase()) {
			case "that email address is already registered":
				result = "This email is already taken. If it's yours, please cancel and sign in instead.";
				break;
			case "username containing email or handle is required!":
				result = "Please submit a valid username or email.";
				break;
			case "that handle is already taken":
				result = "There's already an account with that name.";
				break;
		}

		return result;
	}
}
