package seg.jUCMNav.importexport.z151.marshal;

//  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
//  <!--  Timer  -->
//  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
//  <xsd:complexType name="Timer">
//    <xsd:complexContent>
//      <xsd:extension base="WaitingPlace">
//        <xsd:sequence>
//          <xsd:element minOccurs="0" name="timeoutPath" type="xsd:IDREF"/> <!-- NodeConnection -->
//        </xsd:sequence>
//      </xsd:extension>
//    </xsd:complexContent>
//  </xsd:complexType>

import seg.jUCMNav.importexport.z151.generated.*;

public class TimerMHandler extends WaitingPlaceMHandler {
	public Object handle(Object o, Object target, boolean isFullConstruction) {
		ucm.map.Timer elem = (ucm.map.Timer) o;
		String objId = elem.getId();
		Timer elemZ = (Timer) id2object.get(objId);
		if (null == elemZ) {
			if (null == target)
				elemZ = of.createTimer();
			else
				elemZ = (Timer) target;
			id2object.put(objId, elemZ);
		}
		if (isFullConstruction) {
			elemZ = (Timer) super.handle(elem, elemZ, true);
			if (elem.getTimeoutPath() != null) {
				NodeConnection nodeConnection = (NodeConnection) process(elem.getTimeoutPath(), null, false);
				elemZ.setTimeoutPath(nodeConnection);
				nodeConnection.setTimer(elemZ);
			}
			// elemZ.setWaitType();
			// elemZ.setContRef();
			// elemZ.setLabel();
			// elemZ.setPos();
			// elemZ.setId();
			// elemZ.setDesc();
			// elemZ.setConcern();
			// elemZ.setName();
			//
			// elemZ.getTimeoutPath();
			// elemZ.getWaitType();
			// elemZ.getPred();
			// elemZ.getSucc();
			// elemZ.getContRef();
			// elemZ.getLabel();
			// elemZ.getPos();
			// elemZ.getMetadata();
			// elemZ.getToLinks();
			// elemZ.getFromLinks();
			// elemZ.getConcern();
			// elemZ.getName();
			// elemZ.getId();
			// elemZ.getDesc();
			// elemZ.getClass();
		}
		return elemZ;
	}
}