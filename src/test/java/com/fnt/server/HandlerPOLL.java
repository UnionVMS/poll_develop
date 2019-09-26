package com.fnt.server;

import java.io.UnsupportedEncodingException;

import com.fnt.server.Response;

public class HandlerPOLL {

	private String arguments;

	public HandlerPOLL(String arguments) {
		this.arguments = arguments;
	}

	public boolean verify() {

		return true;
	}

	public Response execute() throws UnsupportedEncodingException {
		return new Response((arguments + " POLL").getBytes("utf-8"));
	}

}
