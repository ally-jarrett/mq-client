package org.redhat.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerThread extends Thread {
	
	  private static final Logger LOG = LoggerFactory.getLogger(ConsumerThread.class);

	  int messageCount = 1000;
	  int receiveTimeOut = 3000;
	  int received = 0;
	  int transactions = 0;
	  String dest;
	  JMSService service;
	  boolean breakOnNull = false;
	  boolean running = false;
	  int sleep;
	  int transactionBatchSize;

	  public ConsumerThread(JMSService service, String dest)
	  {
	    this.dest = dest;
	    this.service = service;
	  }

	  public void run()
	  {
	    this.running = true;
	    MessageConsumer consumer = null;
	    try
	    {
	      consumer = this.service.createConsumer(this.dest);
	      while ((this.running) && (this.received < this.messageCount)) {
	        Message msg = consumer.receive(this.receiveTimeOut);
	        if (msg != null) {
	          LOG.info(new StringBuilder().append("Received ").append((msg instanceof TextMessage) ? ((TextMessage)msg).getText() : msg.getJMSMessageID()).toString());
	          this.received += 1;
	        } else {
	          if (this.breakOnNull)
	          {
	            break;
	          }
	        }
	        if ((this.transactionBatchSize > 0) && (this.received > 0) && (this.received % this.transactionBatchSize == 0)) {
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
	      if (consumer != null) {
	        try {
	          consumer.close();
	        } catch (JMSException e) {
	          e.printStackTrace();
	        }
	      }
	    }

	    LOG.info("Consumer thread finished");
	  }

	  public int getReceived() {
	    return this.received;
	  }

	  public void setMessageCount(int messageCount) {
	    this.messageCount = messageCount;
	  }

	  public void setBreakOnNull(boolean breakOnNull) {
	    this.breakOnNull = breakOnNull;
	  }

	  public void setReceiveTimeOut(int receiveTimeOut)
	  {
	    this.receiveTimeOut = receiveTimeOut;
	  }

	  public void setSleep(int sleep) {
	    this.sleep = sleep;
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
