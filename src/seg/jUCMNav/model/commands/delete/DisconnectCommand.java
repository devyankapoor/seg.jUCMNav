package seg.jUCMNav.model.commands.delete;

import java.util.Iterator;

import org.eclipse.gef.commands.Command;

import seg.jUCMNav.model.commands.JUCMNavCommand;
import seg.jUCMNav.model.util.ParentFinder;
import ucm.map.Connect;
import ucm.map.EndPoint;
import ucm.map.NodeConnection;
import ucm.map.PathNode;
import ucm.map.StartPoint;
import urn.URNspec;

/**
 * Given a PathNode connected to a Connect, remove the connect from the map and break the connections.
 * 
 * @author jkealey
 *  
 */
public class DisconnectCommand extends Command implements JUCMNavCommand {
    private Connect connect;

    private PathNode left, right;
    private NodeConnection ncLeft, ncRight;
    private URNspec urn;

    /**
     * 
     * @param pn
     *            PathNode that is either to the left or right of a connect.
     */
    public DisconnectCommand(PathNode pn) {
        // pn to the left of a connect?
        for (Iterator iter = pn.getSucc().iterator(); iter.hasNext();) {
            NodeConnection nc = (NodeConnection) iter.next();
            if (nc.getTarget() instanceof Connect) {
                this.left = pn;
                this.ncLeft = nc;
                this.connect = (Connect) nc.getTarget();
                this.ncRight = (NodeConnection) this.connect.getSucc().get(0);
                this.right = (PathNode)this.ncRight.getTarget();
                break;
            }

        }

        // no? pn to the right of a connect?
        if (left == null) {
            for (Iterator iter = pn.getPred().iterator(); iter.hasNext();) {
                NodeConnection nc = (NodeConnection) iter.next();
                if (nc.getSource() instanceof Connect) {
                    this.right = pn;
                    this.ncRight = nc;
                    this.connect = (Connect) nc.getSource();
                    this.ncLeft = (NodeConnection) this.connect.getPred().get(0);
                    this.left = (PathNode)this.ncLeft.getSource();
                    break;
                }

            }
        }

        if (left != null && left.getSpecDiagram() != null) {
            this.urn = this.left.getSpecDiagram().getUrndefinition().getUrnspec();
        }

    }

    /**
     * 
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() {
        return left != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        redo();
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void redo() {
        if (connect.getSpecDiagram() == null)
            return; // already disconnected

        testPreConditions();

        ncLeft.setSource(null);
        ncRight.setTarget(null);

        left.getSpecDiagram().getConnections().remove(ncLeft);
        left.getSpecDiagram().getConnections().remove(ncRight);
        left.getSpecDiagram().getNodes().remove(connect);

        left.setX(right.getX());
        left.setY(right.getY() - 50);
        left.setCompRef(ParentFinder.getPossibleParent(left));

        testPostConditions();
    }

    /**
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPostConditions()
     */
    public void testPostConditions() {
        assert left != null && right != null && ncLeft != null && ncRight != null && connect != null && urn != null : "post something is null"; //$NON-NLS-1$

        if (left instanceof EndPoint)
            assert left.getSucc().size() == 0 : "post left already connected"; //$NON-NLS-1$
        else
            assert left.getSucc().size() == 1 : "post left already connected"; //$NON-NLS-1$

        if (right instanceof StartPoint)
            assert right.getPred().size() == 0 : "post right already connected"; //$NON-NLS-1$
        else
            assert right.getPred().size() == 1 : "post right already connected"; //$NON-NLS-1$

    }

    /**
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPreConditions()
     */
    public void testPreConditions() {
        assert left != null && right != null && ncLeft != null && ncRight != null && connect != null && urn != null : "pre something is null"; //$NON-NLS-1$

        if (left instanceof EndPoint)
            assert left.getSucc().size() == 1 : "pre left not connected"; //$NON-NLS-1$
        else
            assert left.getSucc().size() == 2 : "pre left not connected"; //$NON-NLS-1$

        if (right instanceof StartPoint)
            assert right.getPred().size() == 1 : "pre right not connected"; //$NON-NLS-1$
        else
            assert right.getPred().size() == 2 : "pre right not connected"; //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void undo() {
        if (connect.getSpecDiagram() != null)
            return; // already re-connected

        testPostConditions();

        ncLeft.setSource(left);
        ncRight.setTarget(right);

        left.getSpecDiagram().getConnections().add(ncLeft);
        left.getSpecDiagram().getConnections().add(ncRight);
        right.getSpecDiagram().getNodes().add(connect);

        left.setX(right.getX());
        left.setY(right.getY());

        left.setCompRef(right.getCompRef());

        testPreConditions();
    }

}