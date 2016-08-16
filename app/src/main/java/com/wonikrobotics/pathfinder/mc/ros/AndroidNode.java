package com.wonikrobotics.pathfinder.mc.ros;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

import java.util.Vector;

/**
 *  AndroidNode
 *
 *  @author         Doryeong Park
 *  @date           29. 7. 2016
 *
 *  @description    Abstract class to provide ROS node interface
 */
public class AndroidNode extends AbstractNodeMain implements NodeMain {

    private Vector<CustomPublisher> publishers;
    private Vector<CustomSubscriber> subscribers;
    private GraphName nodeName;

    public AndroidNode(String nodeName) {

        publishers = new Vector<CustomPublisher>();
        subscribers = new Vector<CustomSubscriber>();
        this.nodeName = GraphName.of(nodeName);

    }

    @Override
    public GraphName getDefaultNodeName() {
        return nodeName;
    }

    /**
     * onStart
     * @param connectedNode
     * @description Executes all publishers & subscribers in storage
     */
    @Override
    public void onStart(final ConnectedNode connectedNode) {

        for (int i = 0; i < publishers.size(); ++i)
            publishers.elementAt(i).onStart(connectedNode);

        for (int i = 0; i < subscribers.size(); ++i)
            subscribers.elementAt(i).onStart(connectedNode);

    }

    /**
     * onShutDown
     * @param node
     * @description Method executed during node shutdown process
     */
    @Override
    public void onShutdown(Node node) {

    }

    /**
     * onShutdownComplete
     * @param node
     * @description Method executed when shutdown process is finished
     */
    @Override
    public void onShutdownComplete(Node node) {

    }

    /**
     * onError
     * @param node
     * @param throwable
     * @description Method executed when it's not able to connect with ROS master
     */
    @Override
    public void onError(Node node, Throwable throwable) {

    }

    /**
     * addPublisher
     * @param ps
     * @decription Store publisher to be executed
     */
    public void addPublisher(CustomPublisher ps) {

        publishers.add(ps);

    }

    /**
     * addSubscriber
     * @param ss
     * @description Store subscriber to be executed
     */
    public void addSubscriber(CustomSubscriber ss) {

        subscribers.add(ss);

    }

}
