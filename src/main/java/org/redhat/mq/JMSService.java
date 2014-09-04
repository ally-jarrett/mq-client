package org.redhat.mq;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public abstract interface JMSService {
	
	  public abstract ConnectionFactory getConnectionFactory();

	  public abstract Connection getDefaultConnection();

	  public abstract Session getDefaultSession();

	  public abstract MessageProducer createProducer(String paramString)
	    throws JMSException;

	  public abstract MessageConsumer createConsumer(String paramString)
	    throws JMSException;

	  public abstract TextMessage createTextMessage(String paramString)
	    throws JMSException;

	  public abstract BytesMessage createBytesMessage(byte[] paramArrayOfByte)
	    throws JMSException;

	  public abstract void start()
	    throws JMSException;

	  public abstract void stop();

}
