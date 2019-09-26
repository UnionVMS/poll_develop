package com.fnt.server;

import java.io.UnsupportedEncodingException;

import com.fnt.server.Response;

public class HandlerDNID {

	private String arguments;

	public HandlerDNID(String arguments) {
		this.arguments = arguments;

	}

	public boolean verify() {
		return true;
	}

	public Response execute() throws UnsupportedEncodingException {
		return new Response((arguments + " DNID").getBytes("utf-8"));
	}

}
