package seg.jUCMNav.importexport.reports;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;

import seg.jUCMNav.importexport.reports.utils.ReportUtils;
import seg.jUCMNav.importexport.reports.utils.jUCMNavErrorDialog;
import seg.jUCMNav.views.preferences.ReportGeneratorPreferences;
import ucm.map.EndPoint;
import ucm.map.InBinding;
import ucm.map.NodeConnection;
import ucm.map.OutBinding;
import ucm.map.PathNode;
import ucm.map.PluginBinding;
import ucm.map.RespRef;
import ucm.map.StartPoint;
import ucm.map.Stub;
import ucm.map.impl.OrForkImpl;
import urncore.Condition;
import urncore.IURNDiagram;
import urncore.IURNNode;
import urncore.Metadata;
import urncore.UCMmodelElement;
import urncore.URNmodelElement;

import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;

/**
 * implements the creation the the UCM elements description for each
 * UCM figure
 * 
 * @author dessure
 * 
 */
public class UCMDiagramSection extends PDFReportDiagram {

	int[] tableParams = { 1, 2, 0, 100 };

	public UCMDiagramSection() {

	}

	/**
	 * creates the data dictionary section in the report
	 * 
	 * @param document
	 *            the document in which the report is created
	 * @param tableParams
	 *            the parameters used for the creation of table (nbOfColumns, padding, spacing, width)
	 * @param description
	 *            the description of the ucm diagram
	 */
	private void insertUCMDiagramSectionHeader(Document document, int[] tableParams, String description) {
		try {

			Table table = ReportUtils.createTable(tableParams[0], tableParams[1], tableParams[2], tableParams[3]);

			Chunk chunk = new Chunk(description, descriptionBoldFont);
			Cell descriptionCell = new Cell(chunk);
			descriptionCell.setColspan(1);
			descriptionCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			descriptionCell.setBorderWidthBottom(1.5f);

			table.addCell(descriptionCell);

			document.add(table);
		} catch (Exception e) {
			jUCMNavErrorDialog error = new jUCMNavErrorDialog(e.getMessage());
			e.printStackTrace();

		}
	}


	public void createUCMDiagramDescription(Document document, URNmodelElement element, IURNDiagram diagram) {

		try {
			// sections array contains a list of UCM Types we can report on
			// the order in the array will be the order in which they appear
			// in the report.
			String[] sections = { "RespRef", "Stub", "OrForkImpl", "StartPoint", "EndPoint" };

			// variables used to skip headers for multiple items
			boolean firstOrFork = true;
			boolean firstResp = true;
			boolean firstStartPoint = true;
			boolean firstEndPoint = true;
			boolean firstStub = true;

			// variable needed to decide if we print or not this node type in report
			boolean showRespRefNode = ReportGeneratorPreferences.getUCMSHOWRESPONSIBILITY();
			boolean showStubNode = ReportGeneratorPreferences.getUCMSHOWSTUB();
			boolean showOrForkImplNode = ReportGeneratorPreferences.getUCMSHOWORFORK();
			boolean showStartPointNode = ReportGeneratorPreferences.getUCMSHOWSTARTPOINT();
			boolean showEndPointNode = ReportGeneratorPreferences.getUCMSHOWENDPOINT();

			boolean sectionAlreadyChecked = false; // to see if we already checked, to print or not this section type

			int respRefNo = 1;
			int orForkImplNo = 1;
			int stubNo = 1;
			int startPointNo = 1;
			int endPointNo = 1;

			String nodeType = new String();

			HashMap diagramNodes = new HashMap(); // all of the nodes contained in the diagram
			HashMap sectionsMap = new HashMap(); // list of sections (sectionNo, sectionType)
			HashMap respRefSection = new HashMap(); // list of nodes for RespRef
			HashMap orForkImplSection = new HashMap(); // list of nodes for OrFork type
			HashMap stubSection = new HashMap(); // list of nodes for Stub type
			HashMap startPointSection = new HashMap(); // list of nodes for StartPoint type
			HashMap endPointSection = new HashMap(); // list of nodes for EndPoint type

			// Map Description title, underlined
			if (element.getDescription() != null) {
				insertDiagramDescription(document, element);
			}

			// sections contains the list of node types to report on

			// get all nodes for this type from the diagramNodes
			for (Iterator iter = diagram.getNodes().iterator(); iter.hasNext();) {

				PathNode currentNode = (PathNode) iter.next();

				if (showRespRefNode && currentNode instanceof RespRef) {
					Integer hashKey = new Integer(respRefNo);
					respRefSection.put(hashKey, currentNode);
					respRefNo++;
				} else if (showStubNode && currentNode instanceof Stub) {
					Integer hashKey = new Integer(stubNo);
					stubSection.put(hashKey, currentNode);
					stubNo++;
				} else if (showOrForkImplNode && currentNode instanceof OrForkImpl) {
					Integer hashKey = new Integer(orForkImplNo);
					orForkImplSection.put(hashKey, currentNode);
					orForkImplNo++;
				} else if (showStartPointNode && currentNode instanceof StartPoint) {
					Integer hashKey = new Integer(startPointNo);
					startPointSection.put(hashKey, currentNode);
					startPointNo++;
				} else if (showEndPointNode && currentNode instanceof EndPoint) {
					Integer hashKey = new Integer(endPointNo);
					endPointSection.put(hashKey, currentNode);
					endPointNo++;
				}

			}
			// print sections
			if ( respRefSection.size() > 0)
			{
				for (int i4 = 1; i4 <= respRefSection.size(); i4++) {

					if (firstResp == true) {
						insertUCMDiagramSectionHeader( document, tableParams, "Responsibilities" );
						firstResp = false;
					}

					Integer hashKey = new Integer(i4);
					RespRef resp = (RespRef) respRefSection.get(hashKey);
					insertResponsibility( document, resp );
					document.add(Chunk.NEWLINE);
				}				
			}
			
			if (stubSection.size() > 0)
			{
				for (int i4 = 1; i4 <= stubSection.size(); i4++) {

					if (firstStub == true) {
						insertUCMDiagramSectionHeader( document, tableParams, "Stubs" );
						firstStub = false;
					}

					Integer hashKey = new Integer(i4);
					Stub stub = (Stub) stubSection.get(hashKey);
					insertStub(document, stub);
					document.add(Chunk.NEWLINE);
				}
			}

			if (orForkImplSection.size() > 0)
			{
				for (int i4 = 1; i4 <= orForkImplSection.size(); i4++) {
					if (firstOrFork == true) {
						insertUCMDiagramSectionHeader( document, tableParams, "Or Fork Description" );
						firstOrFork = false;
					}

					Integer hashKey = new Integer(i4);
					OrForkImpl orFork = (OrForkImpl) orForkImplSection.get(hashKey);
					insertOrForkProbability(document, orFork);
					document.add(Chunk.NEWLINE);
				}
			}

			if (startPointSection.size() > 0)
			{
				for (int i4 = 1; i4 <= startPointSection.size(); i4++) {
					if (firstStartPoint == true) {
						insertUCMDiagramSectionHeader( document, tableParams, "Start Point " );
						firstStartPoint = false;
					}

					Integer hashKey = new Integer(i4);
					StartPoint startPoint = (StartPoint) startPointSection.get(hashKey);
					insertStartPoint(document, startPoint);
					document.add(Chunk.NEWLINE);
				}
			}

			if (endPointSection.size() > 0)
			{
				for (int i4 = 1; i4 <= endPointSection.size(); i4++) {
					if (firstEndPoint == true) {
						insertUCMDiagramSectionHeader( document, tableParams, "End Points" );
						firstEndPoint = false;
					}

					Integer hashKey = new Integer(i4);
					EndPoint endPoint = (EndPoint) endPointSection.get(hashKey);
					insertEndPoint(document, endPoint);
					document.add(Chunk.NEWLINE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void insertOrForkProbability(Document document, PathNode node) {
		try {

			for (Iterator iter = node.getSucc().iterator(); iter.hasNext();) {   
				//<BM> <2008-02-21> NodeConnection class - 'probability' attribute
				NodeConnection element = (NodeConnection) iter.next();

				//<BM> <2008-02-23> Extract OrFork attributes(label, expression)                                   
				Condition orCondition = element.getCondition(); //DA: Could be null... needs to be checked

				//<BM> <2008-02-23> Extract probability attribute
				double probability = element.getProbability();

				// <BM> <2008-02-24> check if the label and expression strings are empty
				if (orCondition != null) {
					if(ReportUtils.notEmpty(orCondition.getLabel())) {  

						document.add(new Chunk("[" + orCondition.getLabel() + "] ==> ", descriptionFont)); //<BM> <2008-02-23> Search for label result                

						if(ReportUtils.notEmpty(orCondition.getExpression())) {            

							document.add(new Chunk(orCondition.getExpression(), descriptionFont));         //<BM> <2008-02-23> Search for expression result

						}
					}
				}
				// <BM> <2008-02-24> no need to check probability for empty since it always has a default value of 1.0
				document.add(new Chunk(" (probability: " + probability + ")", descriptionFont));//<BM> <2008-02-21> Fixed the way probability is output
				document.add(Chunk.NEWLINE);
			}

			insertMetadata( document, node.getMetadata() );

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertResponsibility(Document document, RespRef resp)
	{
		try {
			ReportUtils.writeLineWithSeparator( document, resp.getRespDef().getName(), ": ", resp.getRespDef().getDescription(), descriptionFont, true );
			String expression = resp.getRespDef().getExpression();
			if ( ReportUtils.notEmpty( expression ) ) {
				String [] expression_lines = expression.split( "\n" );
				if ( expression_lines.length == 1 ) {
					ReportUtils.writeLineWithSeparator( document, "     Expression", ": ", expression_lines[0], descriptionFont, false );
				} else {
					ReportUtils.writeLineWithSeparator( document, "     Expression\n", null, null, descriptionFont, false );
	            for ( int i = 0; i < expression_lines.length; i++ )
	            	ReportUtils.writeLineWithSeparator( document, "          " + expression_lines[i], null, null, descriptionFont, false );
				}
				document.add(Chunk.NEWLINE);
			}

			insertMetadata( document, resp.getRespDef().getMetadata() );
			//if ( !resp.getRespDef().getMetadata().isEmpty() )
				//document.add(Chunk.NEWLINE);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertStartPoint(Document document, StartPoint start)
	{
		try {
			ReportUtils.writeLineWithSeparator(document, start.getName(), ": ", start.getDescription(), descriptionFont, true);
			if ( ReportUtils.notEmpty( start.getPrecondition().getLabel() )) {
				ReportUtils.writeLineWithSeparator( document, "     Precondition", ":  \"", start.getPrecondition().getLabel() + "\"", headerFont, true );
			}
			insertMetadata( document, start.getMetadata() );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertEndPoint(Document document, EndPoint end)
	{
		try {
			ReportUtils.writeLineWithSeparator(document, end.getName(), ": ", end.getDescription(), descriptionFont, true);
			if ( ReportUtils.notEmpty( end.getPostcondition().getLabel() )) {
				ReportUtils.writeLineWithSeparator( document, "     Postcondition", ":  \"", end.getPostcondition().getLabel() + "\"", headerFont, true );
			}
			insertMetadata( document, end.getMetadata() );

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertMetadata( Document document, EList metadata )
	{
		Metadata mdata;
		
		if ( metadata.isEmpty() )
			return;
		
		if( metadata.size() == 1) {
			mdata = (Metadata) metadata.get(0);
			ReportUtils.writeLineWithSeparator( document, "     Metadata:  \"" + mdata.getName(), "\" = \"", mdata.getValue() + "\"", headerFont, true );			
		} else
		{
			ReportUtils.writeLineWithSeparator( document, "     Metadata\n", null, null, headerFont, false );					

			for ( Iterator iter = metadata.iterator(); iter.hasNext(); ) {
				mdata = (Metadata) iter.next();
				ReportUtils.writeLineWithSeparator( document, "          \"" + mdata.getName(), "\" = \"", mdata.getValue() + "\"\n", headerFont, false );
			}
		}
	}

	private void insertSuccessorDescription(Document document, PathNode node) {
		try {
			int i = 1;
			for (Iterator iter = node.getSucc().iterator(); iter.hasNext();) {
				NodeConnection element = (NodeConnection) iter.next();

				// find source associated with predecessor
				IURNNode urnNode = element.getTarget();
				UCMmodelElement ucmElement = (UCMmodelElement) urnNode;

				document.add(new Chunk("Output " + i + ": "));
				document.add(new Chunk(ucmElement.getName()));
				document.add(Chunk.NEWLINE);

				i++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void insertStub( Document document, Stub stub ) {
		try {
			// name and description
			String stubType;

			if (stub.isDynamic()) {
				stubType = "Dynamic Stub - ";
			} else {
				stubType = "Static Stub - ";
			}
			ReportUtils.writeLineWithSeparator(document, stubType + stub.getName(), ": ", stub.getDescription(), descriptionFont, true);

			insertMetadata( document, stub.getMetadata() );

			// Plugin Bindings
			for (Iterator bindings = stub.getBindings().iterator(); bindings.hasNext();) {

				PluginBinding binding = (PluginBinding) bindings.next();

				// Plugin map
				Paragraph pluginMapPar = new Paragraph();
				pluginMapPar.setIndentationLeft(10);
				document.add(Chunk.NEWLINE);
				Chunk pluginMap1 = new Chunk("Plugin Map - " + binding.getPlugin().getName(), pluginMapTitleFont);
				pluginMapPar.add(pluginMap1);
				document.add(pluginMapPar);

				// Static Stub input bindings
				if (binding.getIn().iterator().hasNext()) {
					Paragraph par = new Paragraph();
					par.setIndentationLeft(20);
					par.add(new Chunk("Input Bindings:", bindingsHeaderFont));
					document.add(par);
				}

				for (Iterator ins = binding.getIn().iterator(); ins.hasNext();) {
					InBinding inBinding = (InBinding) ins.next();

					int stubEntryIndex = 0;
					if (stub.getSucc().indexOf(inBinding.getStubEntry()) == -1) {
						stubEntryIndex = 1;
					} else {
						stubEntryIndex = stub.getSucc().indexOf(inBinding.getStubEntry()) + 1;
					}
					Paragraph par = new Paragraph();
					par.setIndentationLeft(30);
					String inItemDescription = "IN " + stubEntryIndex + " <-> " + inBinding.getStartPoint().getName();
					par.add(new Chunk(inItemDescription, bindingsFont));
					document.add(par);
				}

				// Static Stub output bindings
				if (binding.getOut().iterator().hasNext()) {
					Paragraph par = new Paragraph();
					par.setIndentationLeft(20);
					par.add(new Chunk("Output Bindings:", bindingsHeaderFont));
					document.add(par);

				}
				for (Iterator outs = binding.getOut().iterator(); outs.hasNext();) {
					OutBinding outBinding = (OutBinding) outs.next();

					int stubExitIndex = 0;
					stubExitIndex = stub.getSucc().indexOf(outBinding.getStubExit()) + 1;

					Paragraph par = new Paragraph();
					par.setIndentationLeft(30);
					String inItemDescription = "OUT " + stubExitIndex + " <-> " + outBinding.getEndPoint().getName();
					par.add(new Chunk(inItemDescription, bindingsFont));
					document.add(par);
				}

				// Additional plugin binding information
				Paragraph addlInfo = new Paragraph();
				addlInfo.setIndentationLeft(20);
				addlInfo.add(new Chunk("Precondition:", bindingsHeaderFont));
				addlInfo.add(Chunk.NEWLINE);

				if (ReportUtils.notEmpty(binding.getPrecondition().getLabel())) {
					Chunk details = new Chunk("   Label: " + binding.getPrecondition().getLabel(), pluginMapTitleFont);
					addlInfo.add(details);
					addlInfo.add(Chunk.NEWLINE);
				}
				if (ReportUtils.notEmpty(binding.getPrecondition().getExpression())) {
					Chunk details = new Chunk("   Expression: " + binding.getPrecondition().getExpression(), pluginMapTitleFont);
					addlInfo.add(details);
					addlInfo.add(Chunk.NEWLINE);
				}
				if (ReportUtils.notEmpty(binding.getPrecondition().getDescription())) {
					Chunk details = new Chunk("   Description: " + binding.getPrecondition().getDescription(), pluginMapTitleFont);
					addlInfo.add(details);
					addlInfo.add(Chunk.NEWLINE);
				}
				Chunk details = new Chunk("Transaction: " + binding.isTransaction(), pluginMapTitleFont);
				addlInfo.add(details);
				addlInfo.add(Chunk.NEWLINE);

				details = new Chunk("Probability: " + binding.getProbability() + "", pluginMapTitleFont);
				addlInfo.add(details);

				document.add(addlInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
