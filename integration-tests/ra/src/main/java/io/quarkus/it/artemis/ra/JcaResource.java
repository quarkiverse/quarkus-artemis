package io.quarkus.it.artemis.ra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.quarkus.narayana.jta.QuarkusTransaction;

@Path("/jca")
@ApplicationScoped
public class JcaResource {
    // add some rest methods here

    @Inject
    ConnectionFactory factory;

    @GET
    @Transactional
    public String hello(@QueryParam("name") @DefaultValue("JCA") String name) {
        try (JMSContext context = factory.createContext()) {
            Queue myQueue = context.createQueue("MyQueue");
            JMSProducer producer = context.createProducer();
            producer.send(myQueue, "Hello " + name);
            Gift gift = new Gift();
            gift.name = name;
            gift.persist();
            if (name.equals("rollback"))
                QuarkusTransaction.setRollbackOnly();
        }
        return "Hello " + name;
    }

    @POST
    @Transactional
    @Path("/sales")
    public void sendToSalesQueue(@FormParam("name") String name) throws Exception {
        try (JMSContext context = factory.createContext()) {
            JMSProducer producer = context.createProducer();
            TextMessage msg = context.createTextMessage(name);
            msg.setJMSReplyTo(context.createQueue("inventory"));
            producer.send(context.createQueue("sales"), msg);
        }
    }

    @DELETE
    @Path("/gifts")
    @Transactional
    public void deleteGifts() {
        Gift.deleteAll();
    }

    @GET
    @Path("/gifts/count")
    @Transactional
    public long countGifts() {
        return Gift.count();
    }

    @GET
    @Path("/transacted")
    @Transactional
    public boolean isTransacted() {
        try (JMSContext context = factory.createContext()) {
            return context.getTransacted();
        }
    }

    @GET
    @Path("/not-transacted")
    public boolean isNotTransacted() {
        try (JMSContext context = factory.createContext()) {
            return context.getTransacted();
        }
    }

}
