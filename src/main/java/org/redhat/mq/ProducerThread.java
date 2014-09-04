package org.redhat.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerThread extends Thread {
	
	  private static final Logger LOG = LoggerFactory.getLogger(ProducerThread.class);

	  int messageCount = 1000;
	  String dest;
	  protected JMSService service;
	  int sleep = 0;
	  int sentCount = 0;
	  int transactions = 0;
	  boolean persistent = true;
	  int messageSize = 0;
	  byte[] payload = null;
	  int transactionBatchSize;
	  boolean running = false;

	  public ProducerThread(JMSService service, String dest) {
	    this.dest = dest;
	    this.service = service;
	  }

	  public void run() {
	    MessageProducer producer = null;
	    try {
	      producer = this.service.createProducer(this.dest);
	      producer.setDeliveryMode(this.persistent ? 2 : 1);
	      initPayLoad();
	      this.running = true;
	      for (this.sentCount = 0; (this.sentCount < this.messageCount) && 
	        (this.running); this.sentCount += 1)
	      {
	        Message message = createMessage(this.sentCount);
	        producer.send(message);
	        LOG.info(new StringBuilder().append("Sent: ").append((message instanceof TextMessage) ? ((TextMessage)message).getText() : message.getJMSMessageID()).toString());

	        if ((this.transactionBatchSize > 0) && (this.sentCount > 0) && (this.sentCount % this.transactionBatchSize == 0)) {
	          LOG.info(new StringBuilder().append("Committing transaction: ").append(this.transactions++).toString());
	          this.service.getDefaultSession().commit();
	        }

	        if (this.sleep > 0)
	          Thread.sleep(this.sleep);
	      }
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    } finally {
	      if (producer != null) {
	        try {
	          producer.close();
	        } catch (JMSException e) {
	          e.printStackTrace();
	        }
	      }
	    }
	    LOG.info("Producer thread finished");
	  }

	  private void initPayLoad() {
	    if (this.messageSize > 0) {
	      this.payload = new byte[this.messageSize];
	      for (int i = 0; i < this.payload.length; i++)
	        this.payload[i] = 46;
	    }
	  }

	  protected Message createMessage(int i) throws Exception
	  {
	    if (this.payload != null) {
	      return this.service.createBytesMessage(this.payload);
	    }
	    return this.service.createTextMessage(new StringBuilder().append("test message: ").append(i).toString());
	  }

	  public void setMessageCount(int messageCount)
	  {
	    this.messageCount = messageCount;
	  }

	  public void setSleep(int sleep) {
	    this.sleep = sleep;
	  }

	  public int getMessageCount() {
	    return this.messageCount;
	  }

	  public int getSentCount() {
	    return this.sentCount;
	  }

	  public void setPersistent(boolean persistent) {
	    this.persistent = persistent;
	  }

	  public void setMessageSize(int size) {
	    this.messageSize = size;
	  }

	  public void setTransactionBatchSize(int transactionBatchSize) {
	    this.transactionBatchSize = transactionBatchSize;
	  }

	  public boolean isRunning() {
	    return this.running;
	  }

	  public void setRunning(boolean running) {
	    this.running = running;
	  }

}
