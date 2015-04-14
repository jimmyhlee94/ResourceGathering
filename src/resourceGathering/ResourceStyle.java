package resourceGathering;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class ResourceStyle extends DefaultStyleOGL2D {
	@Override
	public Color getColor(final Object agent) {
		if (agent instanceof Resource) {
            final Resource resource = (Resource) agent;

            final int strength = (int) Math.max(220 - 20 * resource.value, 20);
            return new Color(0xFF, strength, strength); // 0xFFFFFF - white,
                                                        // 0xFF0000 - red
	    }
	
	    return super.getColor(agent);
	}
	
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	      spatial = shapeFactory.createRectangle(16, 16);
	    }
	    return spatial;
	  }
}
