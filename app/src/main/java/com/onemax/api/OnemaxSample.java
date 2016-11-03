package com.onemax.api;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
public class OnemaxSample 
{	
	private static HttpResponse postVoucher(String voucherId) throws IOException, GeneralSecurityException
	{
		//
		String url = Settings.BASE_URL + "/vouchers/"+voucherId;
		Map<String, String> params = new HashMap<>(2);
        HttpResponse response = OnemaxBase.post(url, params, null);
        return response;
	}
	
	private static HttpResponse getVoucher() throws IOException, GeneralSecurityException
	{
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("country_code", "vn");

		String url = Settings.BASE_URL + "/states";
		HttpResponse response = OnemaxBase.get(url,params);
		return response;
	}

	public static void main(String[] args) 
	{
		try
		{
			HttpResponse response = postVoucher("b646454fb2572511654253ae");

			System.out.println(response.parseAsString());
			return;
		} 
		catch (IOException e) {
			System.err.println(e.getMessage());
		} 
		catch (Throwable t) {
			t.printStackTrace();
		}
		System.exit(1);
	}
}
