package demo.events;

public interface Event extends KafkaMessage {

    /**
     * Event type to differentiate events within the same Kafka topic. Kafka Consumer is expected to only consume events
     * of the given type.
     */
    String getType();

    /**
     * Event creation epoch.
     */
    Long getTimestamp();

    String getNewsArticleTopic();

    default String key() {
        return getNewsArticleTopic();
    }
}