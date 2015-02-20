package com.stantonj.SJSC;

import com.stantonj.SimpleJaxRSServletContainer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by Joey on 2/19/15.
 */
public class test1 {

    @Path("widgets")
    public static class ExampleResource {
        @GET @Path("List")
        String getList() {return "List";}

        @GET @Path("bar")
        String getWidget(@PathParam("foo") String id) {return id;}
    }

    public static void main(String[] args){
        SimpleJaxRSServletContainer container = new SimpleJaxRSServletContainer();
        container.AddServlet(new ExampleResource());

    }

}
