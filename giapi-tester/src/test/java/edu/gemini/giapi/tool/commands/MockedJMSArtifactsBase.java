package edu.gemini.giapi.tool.commands;

import edu.gemini.jms.api.JmsProvider;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MockedJMSArtifactsBase {
    protected ConnectionFactory connectionFactory;
    protected Session session;
    protected JmsProvider provider;
    protected MessageProducer producer;
    protected MessageConsumer consumer;
    protected MapMessage mapMessage;

    public void createMockedObjects() throws JMSException {
        provider = Mockito.mock(JmsProvider.class);
        mockSessionProducerAndConsumer();
        when(provider.getConnectionFactory()).thenReturn(connectionFactory);
    }

    protected void mockSessionProducerAndConsumer() throws JMSException {
        session = mockSessionCreation();

        producer = Mockito.mock(MessageProducer.class);
        when(session.createProducer(Matchers.<Destination>anyObject())).thenReturn(producer);
        consumer = Mockito.mock(MessageConsumer.class);
        when(session.createConsumer(Matchers.<Destination>anyObject(), anyString())).thenReturn(consumer);

        Queue queue = mock(Queue.class);
        when(session.createQueue(anyString())).thenReturn(queue);
        System.out.println(queue);

        Topic topic = mock(Topic.class);
        when(session.createTopic(anyString())).thenReturn(topic);

        mapMessage = Mockito.mock(MapMessage.class);
        when(session.createMapMessage()).thenReturn(mapMessage);
    }

    private Session mockSessionCreation() throws JMSException {
        Session session = Mockito.mock(Session.class);
        // Mock connection factory
        connectionFactory = Mockito.mock(ConnectionFactory.class);

        // Mock connection
        Connection connection = Mockito.mock(Connection.class);
        when(connectionFactory.createConnection()).thenReturn(connection);

        // Mock session
        when(connection.createSession(Matchers.anyBoolean(), Matchers.anyInt())).thenReturn(session);
        return session;
    }
}
