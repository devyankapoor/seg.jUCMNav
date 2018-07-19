package seg.jUCMNav.model.commands.create;

import org.eclipse.gef.commands.Command;

import asd.ASDiagram;
import asd.Community;
import grl.GRLGraph;
import seg.jUCMNav.Messages;
import seg.jUCMNav.model.commands.JUCMNavCommand;
import urn.URNspec;
/**
 * 
 * Adds a belief to a {@link GRLGraph}
 * 
 * @author Jean-Fran�ois Roy
 * 
 */
public class AddCommunityCommand extends Command implements JUCMNavCommand {

    private Community comm1;

    // Graph where the element has been added.
    private URNspec spec;
    
    private ASDiagram asDiagram;

    /**
     * @param asDiagram 
     * @param graph
     *            graph where to add the belief
     * @param belief
     *            belief to add
     */
    public AddCommunityCommand(URNspec spec, Community comm, ASDiagram asDiagram) {
        super();
        this.spec = spec;
        this.comm1 = comm;
        this.asDiagram = asDiagram;
        setLabel(Messages.getString("AddCommunityCommand.createCommunity")); //$NON-NLS-1$
    }

    /**
     * 
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        redo();
    }

    /**
     * 
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        testPreConditions();

     //   graph.getNodes().add(community);
   
        spec.getAsdspec().getCommunities().add(comm1);
        comm1.getDiagrams().add(asDiagram);

        testPostConditions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPreConditions()
     */
    public void testPreConditions() {
        assert comm1 != null : "pre belief"; //$NON-NLS-1$
        assert spec != null : "pre spec"; //$NON-NLS-1$

       assert !spec.getAsdspec().getCommunities().contains(comm1) : "pre comm in spec"; //$NON-NLS-1$
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPostConditions()
     */
    public void testPostConditions() {
        assert comm1 != null : "post community"; //$NON-NLS-1$
        assert spec != null : "post spec"; //$NON-NLS-1$

        assert spec.getAsdspec().getCommunities().contains(comm1) : "pre community in spec"; //$NON-NLS-1$
    }

    /**
     * 
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        testPostConditions();
        spec.getAsdspec().getCommunities().remove(comm1);
        comm1.getDiagrams().remove(asDiagram);
        

        testPreConditions();
    }
}