package com.wonikrobotics.ros;

import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;

/**
 * Created by Felix on 2016-07-29.
 */
public abstract class CustomPublisher {

    public abstract void publishingRoutine(ConnectedNode connectedNode);
    public abstract void onLoopClear();

    private String topicName;
    private String sensorType;
    private int interval;
    private Publisher publisher;

    public CustomPublisher(String topicName, String sensorType, int interval){

        this.topicName = topicName;
        this.sensorType = sensorType;
        this.interval = interval;

    }

    public void onStart(final ConnectedNode connectedNode){

        publisher =
                connectedNode.newPublisher(topicName, sensorType);

        publisher.addListener(new PublisherListener() {
            @Override
            public void onNewSubscriber(Publisher publisher, SubscriberIdentifier subscriberIdentifier) {

                CustomPublisher.this.onNewSubscriber(publisher, subscriberIdentifier);

            }
            @Override
            public void onShutdown(Publisher publisher) {

                CustomPublisher.this.onShutdown(publisher);

            }

            @Override
            public void onMasterRegistrationSuccess(Object o) {

                CustomPublisher.this.onMasterRegistrationSuccess(o);

            }

            @Override
            public void onMasterRegistrationFailure(Object o) {

                CustomPublisher.this.onMasterRegistrationFailure(o);

            }

            @Override
            public void onMasterUnregistrationSuccess(Object o) {

                CustomPublisher.this.onMasterUnregistrationSuccess(o);

            }

            @Override
            public void onMasterUnregistrationFailure(Object o) {

                CustomPublisher.this.onMasterUnregistrationFailure(o);

            }

        });

        CustomCancellableLoop loop = new CustomCancellableLoop(){

            protected void loop() throws InterruptedException{

                publishingRoutine(connectedNode);

                Thread.sleep(interval);
            }
            @Override
            protected void onPreCancel(){

                onLoopClear();

            }

        };

        connectedNode.executeCancellableLoop(loop);

    }

    public String getTopicName(){

        return topicName;

    }

    public String getSensorType(){

        return sensorType;

    }

    //Functions for overriding

    public void onNewSubscriber(Publisher publisher, SubscriberIdentifier subscriberIdentifier){}
    public void onShutdown(Publisher publisher){}
    public void onMasterRegistrationSuccess(Object o){}
    public void onMasterRegistrationFailure(Object o){}
    public void onMasterUnregistrationSuccess(Object o){}
    public void onMasterUnregistrationFailure(Object o){}

}
