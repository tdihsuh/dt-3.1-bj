package com.hansight.kunlun.collector.mq.kafka;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.OffsetMetadataAndError;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

import java.nio.ByteBuffer;
import java.util.*;

public class SimpleExample {
    private static TopicAndPartition topicAndPartition;
    private static String clientId;
    private static String groupId;

    public static void main(String args[]) {

        SimpleExample example = new SimpleExample();
        long maxReads = 100;
        String topic = "CLbzfD0zTJe7ftreMHpmBg";
        int partition = 1;
        List<String> seeds = new ArrayList<>();
        seeds.add("slave01.dev");
        int port = 9092;
        try {
            topicAndPartition = new TopicAndPartition(topic, partition);
            clientId = "Client_" + topicAndPartition.topic() + "_" + topicAndPartition.partition();
            example.run(maxReads, seeds, port);
        } catch (Exception e) {
            System.out.println("Oops:" + e);
            e.printStackTrace();
        }
    }

    private List<String> m_replicaBrokers = new ArrayList<String>();

    public SimpleExample() {
        m_replicaBrokers = new ArrayList<String>();
    }

    public void run(long maxReads, List<String> brokers, int port) throws Exception {
        // find the meta data about the topic and partition we are interested in
        //
        PartitionMetadata metadata = findLeader(brokers, port);
        if (metadata == null) {
            System.out.println("Can't find metadata for Topic and Partition. Exiting");
            return;
        }
        if (metadata.leader() == null) {
            System.out.println("Can't find Leader for Topic and Partition. Exiting");
            return;
        }
        String leadBroker = metadata.leader().host();


        SimpleConsumer consumer = new SimpleConsumer(leadBroker, port, 100000, 64 * 1024, clientId);

        //kafka.api.OffsetRequest.EarliestTime LatestTime()
        long readOffset = getLastOffset(consumer, kafka.api.OffsetRequest.EarliestTime(), clientId);

        int numErrors = 0;
        while (maxReads > 0) {
            if (consumer == null) {
                consumer = new SimpleConsumer(leadBroker, port, 100000, 64 * 1024, clientId);
            }
            FetchRequest req = new FetchRequestBuilder().clientId(clientId)
                    .addFetch(topicAndPartition.topic(), topicAndPartition.partition(), readOffset, 100000) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                    .build();
            FetchResponse fetchResponse = consumer.fetch(req);

            if (fetchResponse.hasError()) {
                numErrors++;
                // Something went wrong!
                short code = fetchResponse.errorCode(topicAndPartition.topic(), topicAndPartition.partition());
                System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                if (numErrors > 5) break;
                if (code == ErrorMapping.OffsetOutOfRangeCode()) {
                    // We asked for an invalid offset. For simple case ask for the last element to reset
                    readOffset = getLastOffset(consumer, kafka.api.OffsetRequest.LatestTime(), clientId);
                    continue;
                }
                consumer.close();
                consumer = null;
                leadBroker = findNewLeader(leadBroker, port);
                continue;
            }
            numErrors = 0;

            long numRead = 0;
            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topicAndPartition.topic(), topicAndPartition.partition())) {
                long currentOffset = messageAndOffset.offset();
                if (currentOffset < readOffset) {
                    System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
                    continue;
                }
                readOffset = messageAndOffset.nextOffset();
                ByteBuffer payload = messageAndOffset.message().payload();

                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                System.out.println(String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, "UTF-8"));
                numRead++;
                maxReads--;
            }
            Map<TopicAndPartition, OffsetMetadataAndError> requestInfo = new HashMap<>();
            requestInfo.put(topicAndPartition, new OffsetMetadataAndError(readOffset, "", (short) 0));
            OffsetCommitRequest offsetCommitRequest = new OffsetCommitRequest("logger_kafka_group2", requestInfo, kafka.api.OffsetRequest.CurrentVersion(), (short) 0, clientId);
            consumer.commitOffsets(offsetCommitRequest);
            if (numRead == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }

        }
        if (consumer != null) consumer.close();
    }

    public static long getLastOffset(SimpleConsumer consumer,
                                     long whichTime, String clientName) {

        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<>();
        PartitionOffsetRequestInfo info = new PartitionOffsetRequestInfo(whichTime, 1);
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(
                requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);
        List<TopicAndPartition> topicAndPartitions = new LinkedList<>();
        topicAndPartitions.add(topicAndPartition);
        OffsetFetchRequest fetchRequest = new OffsetFetchRequest(groupId, topicAndPartitions, kafka.api.OffsetRequest.CurrentVersion(), (short) 0, clientName);
        OffsetFetchResponse response1 = consumer.fetchOffsets(fetchRequest);
        System.out.println("response1 = " + response1.offsets().get(topicAndPartition).offset());
        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topicAndPartition.topic(), topicAndPartition.partition()));
            return 0;
        }
        long[] offsets = response.offsets(topicAndPartition.topic(), topicAndPartition.partition());
        return offsets[0];
    }

    private String findNewLeader(String a_oldLeader, int a_port) throws Exception {
        for (int i = 0; i < 3; i++) {
            PartitionMetadata metadata = findLeader(m_replicaBrokers, a_port);
            if (metadata != null && metadata.leader() != null && (!a_oldLeader.equalsIgnoreCase(metadata.leader().host()) || i != 0)) {
                return metadata.leader().host();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        System.out.println("Unable to find new leader after Broker failure. Exiting");
        throw new Exception("Unable to find new leader after Broker failure. Exiting");
    }

    private PartitionMetadata findLeader(List<String> brokers, int a_port) {
        PartitionMetadata returnMetaData = null;
        loop:
        for (String broker : brokers) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(broker, a_port, 100000, 64 * 1024, "leaderLookup");

                List<String> topics = Collections.singletonList(topicAndPartition.topic());
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == topicAndPartition.partition()) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error communicating with Broker [" + broker + "] to find Leader for [" + topicAndPartition.topic()
                        + ", " + topicAndPartition.partition() + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        if (returnMetaData != null) {
            m_replicaBrokers.clear();
            for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
                m_replicaBrokers.add(replica.host());
            }
        }
        return returnMetaData;
    }
}