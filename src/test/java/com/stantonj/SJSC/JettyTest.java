package com.stantonj.SJSC;

import com.stantonj.SimpleJerseyServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by Joey on 2/19/15.
 */
public class JettyTest {

    @Path("widgets")
    public static class ExampleResource {
        @GET
        @Path("List")
        public String getList() {return "List";}

        @GET @Path("bar")
        public String getWidget(@PathParam("foo") String id) {return id;}
    }

    @Path("widgets")
    public static class ExampleResource2 {
        @GET @Path("List2")
        public String getList() {return "List2";}

        @GET @Path("bar2")
        public String getWidget(@PathParam("foo2") String id) {return id;}
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        SimpleJerseyServletContainer container = new SimpleJerseyServletContainer();
        container.AddServlet(ExampleResource.class);
        container.AddServlet(new ExampleResource2());

        context.addServlet(new ServletHolder(container), "/*");

        Server server = new Server(8080);
        server.setHandler(context);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
