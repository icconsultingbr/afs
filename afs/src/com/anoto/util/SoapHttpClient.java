package com.anoto.util;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

/**
 * 
 */
public class SoapHttpClient{

	public static String CallWebService(String url, String soapAction, String xml) {
		final DefaultHttpClient httpClient = new DefaultHttpClient();
		// request parameters
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 30000);
		HttpConnectionParams.setSoTimeout(params, 30000);
		// set parameter
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), true);

		// POST the envelope
		HttpPost httppost = new HttpPost(url);
		// add headers
		httppost.setHeader("soapaction", soapAction);
		httppost.setHeader("Content-Type", "text/xml; charset=utf-8");

		String responseString = "";
		try {

			// the entity holds the request
			HttpEntity entity = new StringEntity(xml);
			httppost.setEntity(entity);

			// Response handler
			ResponseHandler<String> rh = new ResponseHandler<String>() {
				// invoked when client receives response
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

					// get response entity
					HttpEntity entity = response.getEntity();

					// read the response as byte array
					StringBuffer out = new StringBuffer();
					byte[] b = EntityUtils.toByteArray(entity);

					// write the response byte array to a string buffer
					out.append(new String(b, 0, b.length));
					return out.toString();
				}
			};

			responseString = httpClient.execute(httppost, rh);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// close the connection
		httpClient.getConnectionManager().shutdown();
		return responseString;
	}
	
	
	public String inserirOuAtualizarDadosFichaDigital( String numeroFicha){
		
		String METHOD_NAME = "inserirOuAtualizarDadosFichaDigital";
		String NAMESPACE = "urn:mobilesamuonline";
		//OBS... A URL ser "treinamento", não tem problema, o servidor vai gravar na base única do AFS.
		String URL = "http://treinamento.samuonline.com.br/MobileService.asmx";
//		String URL = "http://localhost:48966/MobileService.asmx";
		
		String xmlBody = 
			"	      <urn:"+METHOD_NAME+"> "+
			"	         <urn:numeroFicha>"+numeroFicha+"</urn:numeroFicha> "+
			"	      </urn:"+METHOD_NAME+"> ";
			
		String xmlSoapMessage = getSoapMessage(xmlBody);
	   try {
			String xmlResult = SoapHttpClient.CallWebService(URL, NAMESPACE+"/"+METHOD_NAME, xmlSoapMessage);
			String resultado = XmlStringResultParser.parser(xmlResult, METHOD_NAME+"Response", METHOD_NAME+"Result");
			return resultado;

		} catch (Exception e) {
			return "Falha de conexão com o servidor" ;
		}
		
	}
	/**
	 * 
	 * @param xmlBody
	 * @return
	 */
	private String getSoapMessage(String xmlBody) {
		String soapXml = 
			"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:mobilesamuonline\"> "+
			"	  <soapenv:Header> " +
			"		   <urn:AuthSoapHeader> "+
		    "               <urn:strUserName>SAMU_ONLINE</urn:strUserName> "+
			"	            <urn:strPassword>!@#$%QWERT%$#@!</urn:strPassword> "+
			"	       </urn:AuthSoapHeader> "+
			"	  </soapenv:Header> "+
			"     <soapenv:Body> "+
					  xmlBody +
			"     </soapenv:Body> "+
			"</soapenv:Envelope>	 ";
		
		return soapXml;
	}
	
	public static void main(String[] args) {
		SoapHttpClient soap = new SoapHttpClient();
		String resultado = soap.inserirOuAtualizarDadosFichaDigital("AAA");
		System.out.println( resultado );
	}
}