package com.wonikrobotics.ros;

import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

/**
 * Created by Felix on 2016-07-29.
 */
public abstract class CustomSubscriber {

    private String topicName;
    private String sensorType;
    private Subscriber subscriber;

    public CustomSubscriber(String topicName, String sensorType){

        this.topicName = topicName;
        this.sensorType = sensorType;

    }

    public abstract void subscribingRoutine(Message message);

    public void onStart(ConnectedNode connectedNode) {

        subscriber = connectedNode.newSubscriber(topicName, sensorType);

        subscriber.addMessageListener(new MessageListener<sensor_msgs.Range>() {
            @Override
            public void onNewMessage(sensor_msgs.Range message) {

                subscribingRoutine(message);

            }

        });
    }

    public String getTopicName() {

        return topicName;

    }

    public String getSensorType() {

        return sensorType;

    }

}
