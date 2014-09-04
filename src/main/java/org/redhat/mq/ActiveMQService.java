package org.redhat.mq;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQService implements JMSService {

	  private static final Logger LOG = LoggerFactory.getLogger(ActiveMQService.class);
	  Connection defaultConnection;
	  Session defaultSession;
	  boolean transacted = false;
	  int maxAttempts = 1;

	  boolean started = false;
	  private ActiveMQConnectionFactory connectionFactory;
	  private String clientId;

	  public ActiveMQService(String user, String password, String brokerUrl)
	  {
	    this(new ActiveMQConnectionFactory(user, password, brokerUrl));
	  }

	  public ActiveMQService(String brokerUrl) {
	    this(null, null, brokerUrl);
	  }

	  public ActiveMQService(ActiveMQConnectionFactory connectionFactory) {
	    this.connectionFactory = connectionFactory;
	  }

	  public ConnectionFactory getConnectionFactory() {
	    return this.connectionFactory;
	  }

	  public Connection getDefaultConnection() {
	    return this.defaultConnection;
	  }

	  public Session getDefaultSession() {
	    return this.defaultSession;
	  }

	  public MessageProducer createProducer(String destination) throws JMSException {
	    return this.defaultSession.createProducer(ActiveMQDestination.createDestination(destination, (byte)1));
	  }

	  public MessageConsumer createConsumer(String destination) throws JMSException {
	    if (this.clientId != null) {
	      return this.defaultSession.createDurableSubscriber((ActiveMQTopic)ActiveMQDestination.createDestination(destination, (byte)2), "fuseSub");
	    }
	    return this.defaultSession.createConsumer(ActiveMQDestination.createDestination(destination, (byte)1));
	  }

	  public TextMessage createTextMessage(String text) throws JMSException
	  {
	    return this.defaultSession.createTextMessage(text);
	  }

	  public BytesMessage createBytesMessage(byte[] payload) throws JMSException {
	    BytesMessage message = this.defaultSession.createBytesMessage();
	    message.writeBytes(payload);
	    return message;
	  }

	  public void start() throws JMSException {
	    int attempts = 0;
	    JMSException lastException = null;
	    while ((!this.started) && (attempts++ < this.maxAttempts))
	      try {
	        this.defaultConnection = this.connectionFactory.createConnection();
	        if (this.clientId != null) {
	          this.defaultConnection.setClientID(this.clientId);
	        }
	        this.defaultConnection.start();
	        this.defaultSession = this.defaultConnection.createSession(this.transacted, this.transacted ? 0 : 1);

	        this.started = true;
	      } catch (JMSException e) {
	        lastException = e;
	        LOG.warn("Could not start a connection", e);
	        try {
	          Thread.sleep(1000L);
	        }
	        catch (InterruptedException ignore) {
	        }
	      }
	    if (!this.started)
	      throw lastException;
	  }

	  public void stop()
	  {
	    if ((this.started) && 
	      (this.defaultConnection != null)) {
	      try {
	        LOG.info("Closed JMS connection");
	        this.defaultConnection.close();
	      } catch (JMSException ignored) {
	        LOG.info("Exception closing JMS exception", ignored);
	      }
	    }

	    this.started = false;
	  }

	  public int getMaxAttempts() {
	    return this.maxAttempts;
	  }

	  public void setMaxAttempts(int maxAttempts) {
	    this.maxAttempts = maxAttempts;
	  }

	  public void setClientId(String clientId) {
	    this.clientId = clientId;
	  }

	  public void setTransacted(boolean transacted) {
	    this.transacted = transacted;
	  }
	
}
