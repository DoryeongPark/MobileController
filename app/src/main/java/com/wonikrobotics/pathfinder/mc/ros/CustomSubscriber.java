package com.wonikrobotics.pathfinder.mc.ros;

import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

/**
 * CustomSubscriber
 *
 * @author      Doryeong Park
 * @date        29. 7. 2016
 *
 * @description Custom subscriber interface
 */
public abstract class CustomSubscriber {

    private String topicName;
    private String sensorType;
    private Subscriber subscriber;

    public CustomSubscriber(String topicName, String sensorType) {

        this.topicName = topicName;
        this.sensorType = sensorType;

    }

    /**
     * subscribingRoutine
     * @param message
     * @description User needs to define routine of subscribing data here
     */
    public abstract void subscribingRoutine(Message message);

    /**
     * onStart
     * @param connectedNode
     * @description Create subscriber and start subscribing routine
     */
    public void onStart(ConnectedNode connectedNode) {

        subscriber = connectedNode.newSubscriber(topicName, sensorType);

        subscriber.addMessageListener(new MessageListener<Message>() {
            @Override
            public void onNewMessage(Message message) {

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
