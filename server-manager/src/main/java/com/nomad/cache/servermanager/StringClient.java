package com.nomad.cache.servermanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.ChildServer;

public class StringClient {
	private static Logger logger = LoggerFactory.getLogger(StringClient.class);

	public String getInfo(ChildServer server, String command ){
		Object[] p= {server.getHost(), server.getCommandPort(),command};
		logger.info("send command to {}:{} command: {}",p);
		Socket client=null;
		OutputStream output;
		String result="";
		
		BufferedReader in = null;		
		try {
			client = new Socket(server.getHost(), server.getCommandPort());
			output = client.getOutputStream();
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			command+="\n";
			output.write(command.getBytes("UTF-8"));
			output.flush();
			
			result=in.readLine();
			logger.info("answer ok: {}",result);
			client.close();
		} catch (UnknownHostException e) {
			logger.error("error:", e);
		} catch (IOException e) {
			logger.error("error:", e);
		}finally{
			if(client!=null){
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
