package com.managemc.spigot.util;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class HttpRequestMaker {

  public HttpResponse<String> get(String url) throws UnirestException {
    return Unirest.get(url).asString();
  }
}
