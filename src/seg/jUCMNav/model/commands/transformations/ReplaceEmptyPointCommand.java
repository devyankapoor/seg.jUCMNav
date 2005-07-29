package seg.jUCMNav.model.commands.transformations;

import org.eclipse.gef.commands.CompoundCommand;

import seg.jUCMNav.model.commands.delete.DeletePathNodeCommand;
import ucm.map.DirectionArrow;
import ucm.map.EmptyPoint;
import ucm.map.NodeConnection;
import ucm.map.PathNode;

/**
 * CompoundCommand that replaces an unconnected EmptyPoint or DirectionArrow with another path node.
 * 
 * @author jkealey
 * 
 */
public class ReplaceEmptyPointCommand extends CompoundCommand {

    private PathNode empty;
    private PathNode newNode;

    /**
     * @param empty
     *            the empty node or direction arrow to remove
     * @param newNode
     *            the new node to replace it with
     */
    public ReplaceEmptyPointCommand(PathNode empty, PathNode newNode) {
        this.empty = empty;
        this.newNode = newNode;
    }

    public boolean canExecute() {
        if (getCommands().size() == 0)
            return true;
        else
            return super.canExecute();
    }

    public void execute() {
        // delay until execution
        build();
        super.execute();
    }

    private void build() {
        assert empty instanceof EmptyPoint || empty instanceof DirectionArrow : "invalid empty point"; //$NON-NLS-1$
        if (empty.getPred().size() == 1 && empty.getSucc().size() == 1) {
            int x = empty.getX();
            int y = empty.getY();
            NodeConnection previous = (NodeConnection) empty.getPred().get(0);
            // I know we won't be using the editpartregistry to replace the empty point or direction arrow.
            add(new DeletePathNodeCommand(empty, null));
            add(new SplitLinkCommand(empty.getPathGraph(), newNode, previous, x, y));
        }
    }
}