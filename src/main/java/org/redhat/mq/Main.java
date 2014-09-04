package org.redhat.mq;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Main {
	
	  static final String loggingLevelProperty = "org.ops4j.pax.logging.DefaultServiceLog.level";
	  String action;
	  String destination;
	  String brokerUrl = ActiveMQConnectionFactory.DEFAULT_BROKER_URL;
	  boolean persistent = true;
	  int count = 100;
	  int sleep = 0;
	  int size = 0;
	  String clientId;
	  String password;
	  String user;
	  int batchSize;

	  public static void main(String[] args)
	    throws Exception
	  {
	    if (System.getProperty("org.ops4j.pax.logging.DefaultServiceLog.level") == null) {
	      System.setProperty("org.ops4j.pax.logging.DefaultServiceLog.level", "INFO");
	    }

	    Main main = new Main();

	    LinkedList arg1 = new LinkedList(Arrays.asList(args));
	    main.action = shift(arg1);
	    while (!arg1.isEmpty()) {
	      try {
	        String arg = (String)arg1.removeFirst();
	        if ("--size".equals(arg)) {
	          main.size = Integer.parseInt(shift(arg1));
	        } else if ("--count".equals(arg)) {
	          main.count = Integer.parseInt(shift(arg1));
	        } else if ("--sleep".equals(arg)) {
	          main.sleep = Integer.parseInt(shift(arg1));
	        } else if ("--destination".equals(arg)) {
	          main.destination = shift(arg1);
	        } else if ("--brokerUrl".equals(arg)) {
	          main.brokerUrl = shift(arg1);
	        } else if ("--user".equals(arg)) {
	          main.user = shift(arg1);
	        } else if ("--password".equals(arg)) {
	          main.password = shift(arg1);
	        } else if ("--clientId".equals(arg)) {
	          main.clientId = shift(arg1);
	        } else if ("--batchSize".equals(arg)) {
	          main.batchSize = Integer.parseInt(shift(arg1));
	        } else if ("--persistent".equals(arg)) {
	          main.persistent = Boolean.valueOf(shift(arg1)).booleanValue();
	        } else {
	          System.err.println("Invalid usage: unknown option: " + arg);
	          displayHelpAndExit(1);
	        }
	      } catch (NumberFormatException e) {
	        System.err.println("Invalid usage: argument not a number");
	        displayHelpAndExit(1);
	      }
	    }

	    main.execute();
	    System.exit(0);
	  }

	  private void execute() {
	    initDestination();
	    System.out.println("Using destination: " + this.destination + ", on broker: " + this.brokerUrl);

	    ActiveMQService activeMQService = new ActiveMQService(this.user, this.password, this.brokerUrl);
	    activeMQService.setTransacted(this.batchSize > 0);
	    try
	    {
	      if ("producer".equals(this.action))
	      {
	        activeMQService.start();

	        ProducerThread producerThread = new ProducerThread(activeMQService, this.destination);
	        producerThread.setMessageCount(this.count);
	        producerThread.setMessageSize(this.size);
	        producerThread.setSleep(this.sleep);
	        producerThread.setPersistent(this.persistent);
	        producerThread.setTransactionBatchSize(this.batchSize);
	        producerThread.run();
	        System.out.println("Produced: " + producerThread.getSentCount());
	      }
	      else if ("consumer".equals(this.action))
	      {
	        activeMQService.setClientId(this.clientId);
	        activeMQService.start();

	        ConsumerThread consumerThread = new ConsumerThread(activeMQService, this.destination);
	        consumerThread.setMessageCount(this.count);
	        consumerThread.setSleep(this.sleep);
	        consumerThread.setTransactionBatchSize(this.batchSize);

	        System.out.println("Waiting for: " + this.count + " messages");
	        consumerThread.run();
	        System.out.println("Consumed: " + consumerThread.getReceived() + " messages");
	      }
	      else {
	        displayHelpAndExit(1);
	      }
	    }
	    catch (JMSException error) {
	      System.err.println("Execution failed with: " + error);
	      error.printStackTrace(System.err);
	      System.exit(2);
	    } finally {
	      activeMQService.stop();
	    }
	  }

	  private void initDestination() {
	    if (this.destination == null)
	      if (this.clientId != null)
	        this.destination = "topic://TEST";
	      else
	        this.destination = "queue://TEST";
	  }

	  private static String shift(LinkedList<String> argl)
	  {
	    if (argl.isEmpty()) {
	      System.out.println("Invalid usage: Missing argument");
	      displayHelpAndExit(1);
	    }
	    return (String)argl.removeFirst();
	  }

	  private static void displayHelpAndExit(int exitCode) {
	    System.out.println(" usage   : (producer|consumer) [OPTIONS]");
	    System.out.println(" options : [--destination (queue://..|topic://..) - ; default TEST");
	    System.out.println("           [--persistent  true|false] - use persistent or non persistent messages; default true");
	    System.out.println("           [--count       N] - number of messages to send or receive; default 100");
	    System.out.println("           [--size        N] - size in bytes of a BytesMessage; default 0, a simple TextMessage is used");
	    System.out.println("           [--sleep       N] - millisecond sleep period between sends or receives; default 0");
	    System.out.println("           [--batchSize   N] - use send and receive transaction batches of size N; default 0, no jms transactions");
	    System.out.println("           [--clientId   id] - use a durable topic consumer with the supplied id; default null, non durable consumer");
	    System.out.println("           [--brokerUrl URL] - connection factory url; default " + ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
	    System.out.println("           [--user      .. ] - connection user name");
	    System.out.println("           [--password  .. ] - connection password");

	    System.out.println("");

	    System.exit(exitCode);
	  }

}
