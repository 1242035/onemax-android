package com.onemax.api;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;

import java.util.Map;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;

public class OnemaxBase 
{
	public static final HttpResponse get(String url, Map<String,String> params) throws IOException, GeneralSecurityException
	{
        GenericUrl getUrl = buildUrl(url ,params);
		OAuthParameters oauthParameters = getAuthParams( getUrl ,null );
	    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(oauthParameters);
	    HttpResponse response = requestFactory.buildGetRequest( getUrl ).execute();
	    
	    return response;
	}
	public static final HttpResponse post(String url, Map<String,String> params, File uploadFile) throws IOException, GeneralSecurityException
	{
        if( uploadFile != null ) {
            String fileName = uploadFile.getName();
            long size = uploadFile.length();
            params.put("name", fileName);
            params.put("size", String.valueOf(size) );
        }
        GenericUrl postUrl = buildUrl(url, params);
	    OAuthParameters oauthParameters = getAuthParams( postUrl , uploadFile);
	    System.out.println( postUrl.toString() );
	    HttpContent body = (params == null) ? null :  ( new UrlEncodedContent( params ) );

	    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(oauthParameters);
	    HttpResponse response = requestFactory.buildPostRequest(postUrl, body ).execute();
	    dump(response);
	    return response;
	}
	public static final OAuthHmacSigner getSigner()
	{
		 OAuthHmacSigner signer  = new OAuthHmacSigner();
		 signer.clientSharedSecret = Settings.API_SECRET;
		 return signer;
	}
    public static final String getSignature(OAuthHmacSigner signer, GenericUrl url, File file) throws GeneralSecurityException, IOException {
        StringBuilder needSignData = new StringBuilder();
        needSignData.append( url.build() );
        if( file != null ) {
            byte[] fileByte = getFileBinary(file);
            StringBuilder fileContent = new StringBuilder();
            for(byte byteContent: fileByte) {
                fileContent.append( getBits( byteContent ) );
            }
            needSignData.append( fileContent );
        }
        return signer.computeSignature( needSignData.toString() );

    }
	public static final OAuthParameters getAuthParams(GenericUrl url, File file) throws GeneralSecurityException, IOException {
		OAuthHmacSigner signer = getSigner();
		System.out.println("URL :: " + url.build()  );
		System.out.println("getSignature :: " + signer.computeSignature( null ) );
		System.out.println("getSignature :: " + signer.getSignatureMethod() );

		OAuthParameters oauthParameters = new OAuthParameters();

		oauthParameters.signer = signer;
		oauthParameters.signature = getSignature(signer, url ,file);
		oauthParameters.consumerKey = Settings.API_KEY;
		oauthParameters.computeNonce();
		oauthParameters.computeTimestamp();
		oauthParameters.version = Settings.API_VERSION;

		return oauthParameters;
	}
	/*public static final OAuthParameters getAuthParams(GenericUrl url, File file) throws GeneralSecurityException, IOException {
		OAuthHmacSigner signer = getSigner();
		System.out.println("URL :: " + url.build()  );
	    System.out.println("getSignature :: " + signer.computeSignature( null ) );
	    System.out.println("getSignature :: " + signer.getSignatureMethod() );
	  
		OAuthParameters oauthParameters = new OAuthParameters();
	    
	    oauthParameters.signer = signer;
        oauthParameters.signature = getSignature(signer, url ,file);
	    oauthParameters.consumerKey = Settings.API_KEY;    
	    oauthParameters.computeNonce();
	    oauthParameters.computeTimestamp();
	    oauthParameters.version = Settings.API_VERSION;
	    
	    return oauthParameters;
	}*/
	public static GenericUrl buildUrl(String baseUrl, Map<String, String> params){
        GenericUrl url = new GenericUrl( baseUrl );
        for(Map.Entry<String, String> entry: params.entrySet()) {
            url.put( entry.getKey() , entry.getValue() );
        }
        return url;
    }

    public static byte[] getFileBinary(File file) throws IOException {
        byte[] bytes = new byte[(int)file.length()];
        DataInputStream dataInputStream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(file)
                )
        );
        dataInputStream.readFully(bytes);
        dataInputStream.close();
        return bytes;
    }
    public static  String getBits(byte b)
    {
        String result = "";
        for(int i = 0; i < 8; i++) {
            result += (b & (1 << i)) == 0 ? "0" : "1";
        }
        return result;
    }
	public static void dump(Object o)
	{
	    Field[] fields = o.getClass().getDeclaredFields() ;
	    try
	    {
	        for(Field field : fields){
	            field.setAccessible(true);
	            Object value = field.get(o);
	            System.out.println(field.getName() + "=" + field.get(o));
	        }
	    }
	    catch(Exception e){
	        e.printStackTrace();
	    }
	}
}
