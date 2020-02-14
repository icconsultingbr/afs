/* Copyright (c) 2008 Anoto AB. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
* Neither the name of Anoto AB nor the names of contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
* This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. ANOTO AB AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL ANOTO AB OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF ANOTO AB HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. */

package httplibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.anoto.wppgm.WppgmCommons;


public class HttpPoster {

    private String url;
    private PostMethod postMethod;
    private HttpClient client;
    private ArrayList<Part> partList;
    private int statusCode;

    public HttpPoster(String url) {
        this.url = url;
        client = new HttpClient();
        partList = new ArrayList<Part>();
    }

    public InputStream post() throws IOException,HttpException {
    	InputStream responseStream = null;
    	boolean condition = true;
    	
    	while (condition) {
	        postMethod = new PostMethod(this.url);
	        
	        //Add the paramers
	        Part[] parts = (Part[])partList.toArray(new Part[0]);
	        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
	        
	        //Execute
	        client.getParams().setSoTimeout(Integer.parseInt(WppgmCommons.getProperty("connection_timeout")));
	        statusCode = client.executeMethod(postMethod);
	        
	        if (statusCode == HttpStatus.SC_OK) {
	        	//Get the response as a stream
	        	responseStream = postMethod.getResponseBodyAsStream();
	        	condition = false;
	        } else {
	        	closeConnection();
	        }
    	}
                
        return responseStream;
    }

    public void addParameter(String name, String value) {
        StringPart part = new StringPart(name,value);
        partList.add(part);
    }

        public void addFile(String name, File file) throws FileNotFoundException {
        FilePart part = new FilePart(name,file);
        partList.add(part);
    }
    
    public void closeConnection()
    {
    	 postMethod.releaseConnection();
    }

    public int getStatusCode() {
        return statusCode;
    }

}