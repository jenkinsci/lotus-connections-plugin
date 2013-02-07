package com.ibm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.lang.StringEscapeUtils;




public class Poster {
    
    /**
     * The constant NAME.
     */
    private static final String NAME = "POSTER";

    
    private Logger fLogger = Logger.getAnonymousLogger();
    private String fServer;
    private String fUser;
    private String fPasswd;
    private String fMessage;
    

    public void postStatus(String server, String user, String password, String message)
    {
        if ((server != null) 
            && (user != null) 
            && (password != null) 
            && (message != null))
        {
            fServer = server;
            fUser = user;
            fPasswd = password;
            fMessage = message;
            start();
        }
    }

    

    
    private void start()
    {
     // Initialize the Atom Client to update the status in Connections
        Abdera abdera = new Abdera();
        AbderaClient client = new AbderaClient(abdera);
        AbderaClient.registerTrustManager();
        try
        {
            client.addCredentials(fServer, null, null, new UsernamePasswordCredentials(fUser, fPasswd));
        }
        catch (URISyntaxException e)
        {
	    e.printStackTrace();
        }
        
        Parser parser = abdera.getParser();
        String urlText = fServer +"/profiles/atom/mv/thebuzz/entry/status.do?email=" + fUser;
        // Create the Atom entry with the new status message
        Entry status = abdera.newEntry();
        status.addCategory("http://www.ibm.com/xmlns/prod/sn/type", "entry", null);
        status.addCategory("http://www.ibm.com/xmlns/prod/sn/message-type", "status", null);
        fMessage = StringEscapeUtils.escapeHtml(fMessage);
        status.setContent(fMessage);
        
     // Send the new status message to Connections
        ClientResponse response = client.put(urlText, status);
        
        if (response.getType() == ResponseType.SUCCESS)
        {
        // yipee
        System.out.println("woo hoo");
        }
        else
        {
        // WTH?
        System.out.println("oh no!");
        }
    }
}
