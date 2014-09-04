MQ-CLIENT
=========

Recompile of AMQ mq-client to contain all necessary dependencies and run as a standalone client on the cli

Build and run the target/mq-client-jar-with-dependencies.jar --help 

_**java -jar mq-client-jar-with-dependencies.jar --help**_
     
           Using destination: queue://TEST, on broker: failover://tcp://localhost:61616
           usage   : (producer|consumer) [OPTIONS]
           options : [--destination (queue://..|topic://..) - ; default TEST
           [--persistent  true|false] - use persistent or non persistent messages; default true
           [--count       N] - number of messages to send or receive; default 100
           [--size        N] - size in bytes of a BytesMessage; default 0, a simple TextMessage is used
           [--sleep       N] - millisecond sleep period between sends or receives; default 0
           [--batchSize   N] - use send and receive transaction batches of size N; default 0, no jms transactions
           [--clientId   id] - use a durable topic consumer with the supplied id; default null, non durable consumer
           [--brokerUrl URL] - connection factory url; default failover://tcp://localhost:61616
           [--user      .. ] - connection user name
           [--password  .. ] - connection password
           

example usage: 

     java -jar mq-client-jar-with-dependencies.jar producer --destination queue://myQueue --count 100 --sleep 100 --brokerUrl failover://tcp://localhost:61616 --user ${username} --password ${password}

