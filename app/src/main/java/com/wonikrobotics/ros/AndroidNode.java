package com.wonikrobotics.ros;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

import java.util.Vector;

/**
 * Created by Felix on 2016-07-29.
 */
public class AndroidNode extends AbstractNodeMain implements NodeMain{

    private Vector<CustomPublisher> publishers;
    private Vector<CustomSubscriber> subscribers;

    public AndroidNode(){}
    @Override
    public GraphName getDefaultNodeName(){

        return GraphName.of("AndroidNode");

    }
    @Override
    public void onStart(final ConnectedNode connectedNode){

        for(int i = 0; i < publishers.size(); ++i)
            publishers.elementAt(i).onStart(connectedNode);

        for(int i = 0; i < subscribers.size(); ++i)
            subscribers.elementAt(i).onStart(connectedNode);

    }
    @Override
    public void onShutdown(Node node) {

    }
    @Override
    public void onShutdownComplete(Node node) {

    }
    @Override
    public void onError(Node node, Throwable throwable) {

    }

    public void addPublisher(CustomPublisher ps){

        publishers.add(ps);

    }

    public void addSubscriber(CustomSubscriber ss){

        subscribers.add(ss);

    }

}
