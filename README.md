# dlq-bug-reproducer

This is a reproducer project for a bug introduced in Quarkus 3.7 that affects Reactive Messaging consumers that
connect to a Kafka broker using SASL authentication and a Dead Letter Queue (DLQ) as failure strategy.

# Structure

This project has 2 submodules. Both contain exactly the same code and configuration properties.
The only difference between the two is the version of Quarkus:

- `quarkus-3.6` uses Quarkus 3.6.9. This was made to show that the example works in this version.
- `quarkus-3.7` uses Quarkus 3.7.2. This is the actual reproducer.

# Requirements

- Docker
- JDK 17+

# How to run

1. To start a Kafka broker locally, run this command:
   ```shell
   docker compose up -d
   ```
   The bootstrap server will be `localhost:9092`. In addition, Kafka UI will be available on http://localhost:8081
2. Run a submodule with one of the following commands:
   ```shell
     ./mvnw compile quarkus:dev --pl quarkus-3.6
   ```
   ```shell
     ./mvnw compile quarkus:dev --pl quarkus-3.7
   ```

No other configuration should be needed.

# How to reproduce

The following steps can be followed on both submodules. In `quarkus-3.6` everything works fine,
in `quarkus-3.7` the application won't be able to connect to the DLQ.

1. Run the submodule and wait for the application to be ready (you should see the `test-quarkus-bug` topic in Kafka UI
   after a few seconds).
2. Open Kafka UI and send a message with the following payload to the `test-quarkus-bug` topic:
   ```json
   { "shouldAck" : true }
   ```
3. The application should successfully ack the message (both in `quarkus-3.6` and `quarkus-3.7`).
4. Now send the following payload to the same topic:
   ```json
   { "shouldAck" : false }
   ```
5. Depending on the submodule, this is what should happen:
    1. On `quarkus-3.6`, the message is nacked and sent to the DLQ. You should see the `test-quarkus-bug-dlq` topic
       containing the nacked message in Kafka UI.
    2. On `quarkus-3.7`, nack fails and no message is sent to the DLQ. After about 60 seconds, the following error
       message will be printed on the console:
       ```
       Topic test-quarkus-bug-dlq not present in metadata after 60000 ms.
       ```

## Expected outcome

On `quarkus-3.6`, the message is nacked and sent to the DLQ correctly.

On Kafka UI the `test-quarkus-bug-dlq` topic is present and contains the nacked message.

## Actual outcome

On `quarkus-3.7`, nacking the message fails after 60 seconds of timeout.

No new message can be seen in `test-quarkus-bug-dlq`.

The following error message is printed to the console:

```
Topic test-quarkus-bug-dlq not present in metadata after 60000 ms.
```

In addition, from the moment the application starts, the following warning is repeatedly printed to the console:

```
WARN  [org.apa.kaf.cli.NetworkClient] (kafka-producer-network-thread | kafka-dead-letter-topic-producer-kafka-consumer-test-consumer) [Producer clientId=kafka-dead-letter-topic-producer-kafka-consumer-test-consumer] Bootstrap broker localhost:9092 (id: -1 rack: null) disconnected
```
