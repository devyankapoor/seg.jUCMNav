package seg.jUCMNav.model.commands.delete;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.Command;

import seg.jUCMNav.model.ModelCreationFactory;
import seg.jUCMNav.model.commands.JUCMNavCommand;
import ucm.map.ComponentRef;
import ucm.map.EndPoint;
import ucm.map.Map;
import ucm.map.NodeConnection;
import ucm.map.PathNode;
import ucm.map.RespRef;
import ucm.map.StartPoint;
import urn.URNspec;
import urncore.Responsibility;

/**
 * Command to delete a node on a path.
 * 
 * Currently deletes pathnodes that have 1 in, 1 out.
 * 
 * @author Etienne Tremblay, jkealey
 *  
 */
public class DeleteNodeCommand extends Command implements JUCMNavCommand {

    private static final String DeleteCommand_Label = "DeletePathNodeCommand";

    /** the node to be removed */
    private PathNode node;

    /** the preceeding node; assuming just one */
    private PathNode previous;

    /** the next node; assuming just one */
    private PathNode next;

    /** our node's sources; right now only one */
    private Vector sources;

    /** our node's targets; right now only one */
    private Vector targets;

    /** the new connection from previous to next */
    private NodeConnection newConn;

    /** the map containing the pathnodes and node connections */
    private Map map;

    /** if we are bound to a component, this is it */
    private ComponentRef compRef;

    /** if we are a RespRef, this is our respDef */
    private Responsibility respDef;

    private boolean aborted=false;
    
    public DeleteNodeCommand(PathNode node) {
        this.node = node;
        setLabel(DeleteCommand_Label);
    }

    /**
     * Right now, can execute if we have exactly one input and one output.
     * 
     * Furthermore, we must not be start points or end points.
     */
    public boolean canExecute() {

        if (node.eContainer()==null || node instanceof StartPoint || node instanceof EndPoint)
            return false;
        else {
            if (node.getPred().size() == 1 && node.getSucc().size() == 1)
                return true;
            else
                return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        // could happen if was already deleted by other command
        if (node.eContainer() == null){
            aborted=true;
            return;
        }
        map = (Map) node.eContainer().eContainer();
        previous = ((NodeConnection) node.getPred().get(0)).getSource();
        next = ((NodeConnection) node.getSucc().get(0)).getTarget();
        compRef = node.getCompRef();
        sources = new Vector();
        targets = new Vector();
        sources.addAll(node.getPred());
        targets.addAll(node.getSucc());
        newConn = (NodeConnection) ModelCreationFactory.getNewObject((URNspec) map.eContainer().eContainer(), NodeConnection.class);

        if (node instanceof RespRef) {
            respDef = ((RespRef) node).getRespDef();
        }
        redo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        if (aborted)
            return;
        // ASSUMING ONLY FOR EMPTYNODE - 1 IN, ONE OUT.

        testPreConditions();

        node.getSucc().clear();
        node.getPred().clear();

        ((NodeConnection) sources.get(0)).setSource(null);
        ((NodeConnection) targets.get(0)).setTarget(null);

        map.getPathGraph().getNodeConnections().remove((NodeConnection) sources.get(0));
        map.getPathGraph().getNodeConnections().remove((NodeConnection) targets.get(0));
        map.getPathGraph().getNodeConnections().add(newConn);

        map.getPathGraph().getPathNodes().remove(node);

        node.setCompRef(null);
        if (node instanceof RespRef) {
            ((RespRef) node).setRespDef(null);
        }

        newConn.setSource(previous);
        newConn.setTarget(next);

        testPostConditions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        if (aborted)
            return;
        testPostConditions();

        node.getSucc().addAll(targets);
        node.getPred().addAll(sources);

        ((NodeConnection) sources.get(0)).setSource(previous);
        ((NodeConnection) targets.get(0)).setTarget(next);

        map.getPathGraph().getNodeConnections().add((NodeConnection) sources.get(0));
        map.getPathGraph().getNodeConnections().add((NodeConnection) targets.get(0));
        map.getPathGraph().getNodeConnections().remove(newConn);

        map.getPathGraph().getPathNodes().add(node);

        if (node instanceof RespRef) {
            ((RespRef) node).setRespDef(respDef);
        }
        node.setCompRef(compRef);
        newConn.setSource(null);
        newConn.setTarget(null);

        testPreConditions();

    }

    /**
     * @return Returns the PathPathNode.
     */
    public PathNode getPathNode() {
        return node;
    }

    /**
     * @param PathPathNode
     *            The PathPathNode to set.
     */
    public void setPathNode(PathNode node) {
        this.node = node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPreConditions()
     */
    public void testPreConditions() {
        assert canExecute() : "pre canExecute";
        assert previous != null && next != null && newConn != null : "pre something is null";
        assert sources.size() == node.getPred().size() && targets.size() == node.getSucc().size() : "pre source/target problem";
        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            NodeConnection nc = (NodeConnection) iter.next();
            assert node.getPred().contains(nc) : "pre missing source";
            assert map.getPathGraph().getNodeConnections().contains(nc) : "pre source not in model";
        }
        for (Iterator iter = targets.iterator(); iter.hasNext();) {
            NodeConnection nc = (NodeConnection) iter.next();
            assert node.getSucc().contains(nc) : "pre missing target";
            assert map.getPathGraph().getNodeConnections().contains(nc) : "pre target not in model";
        }

        assert compRef == node.getCompRef() : "pre parent problem";
        if (node instanceof RespRef) {
            assert respDef != null && respDef == ((RespRef) node).getRespDef() : "pre respref not linked";
        }

        assert !map.getPathGraph().getNodeConnections().contains(newConn) : "pre new conn";

    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPostConditions()
     */
    public void testPostConditions() {
        assert previous != null && next != null && newConn != null : "post something is null";
        assert node.getPred().size() == 0 && 0 == node.getSucc().size() : "post source/target problem";

        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            NodeConnection nc = (NodeConnection) iter.next();
            assert !map.getPathGraph().getNodeConnections().contains(nc) : "post source in model";
        }
        for (Iterator iter = targets.iterator(); iter.hasNext();) {
            NodeConnection nc = (NodeConnection) iter.next();
            assert !map.getPathGraph().getNodeConnections().contains(nc) : "post target in model";
        }
        assert null == node.getCompRef() : "post parent problem";
        if (node instanceof RespRef) {
            assert respDef != null && null == ((RespRef) node).getRespDef() : "post respref still linked";
        }

        assert map.getPathGraph().getNodeConnections().contains(newConn) : "post new conn";
    }
}