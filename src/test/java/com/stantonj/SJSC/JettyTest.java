package com.stantonj.SJSC;

import com.stantonj.SimpleJaxRSServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joey on 2/19/15.
 */
public class JettyTest {

    @Path("widgets")
    public static class ExampleResource {
        @GET
        @Path("List")
        @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public String getList() {return "List";}

        @GET
        @Path("Obj")
        @Produces(MediaType.APPLICATION_XML)
        public Object getObj() {

            Map<String, String> ret = new HashMap<String,String>();
            ret.put("asdf", "1");
            ret.put("qwerty", "3");
            return ret;
        }

        @GET @Path("bar")
        @Produces(MediaType.APPLICATION_JSON)
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

        SimpleJaxRSServletContainer container = new SimpleJaxRSServletContainer();
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
