// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.validation.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Checks for buildings that are not in a residential area.
 *
 * @since xxx
 */
public class BuildingOutsideResidentialArea extends Test {
  protected static final int BUILDING_OUTSIZE_RESIDENTIAL_AREA = 3801;

    /**
     * Constructs a new {@code BuildingOutsideResidentialArea} test.
     */
    public BuildingOutsideResidentialArea() {
        super(tr("Building outside a residential area"), tr("Checks for building out of a residential area."));
    }

    @Override
    public void visit(Collection<OsmPrimitive> selection) {
      double distance = Config.getPref().getDouble("validator.BuildingOutsideResidentialArea.minimalDistance", 50.0);

      List<Way> buildings = new ArrayList<Way>();
      List<Way> residentialAreas = new ArrayList<Way>();

      /*Filter out buildings and residential areas.  */
      for (OsmPrimitive p: selection) {
        if (p instanceof Way) {
          Way w = (Way)p;
          if (isBuilding(w))
            buildings.add(w);
          else if(isResidentialArea(w))
            residentialAreas.add(w);
        }
      }

      /* Find all building that are not in a residential area.  */
      List<Way> buildingCandidates = new ArrayList<Way>();

      for (Way b: buildings)
        if (!isInResidentialArea(b, residentialAreas))
          buildingCandidates.add(b);

      /* Iterate all buildings and find these that are close to an other and are not
       * in a residential areas.  */
      for (Way b1: buildingCandidates) {
        for (Way b2: buildingCandidates)
          if (b1 != b2) {
            if (getBuildingsDistance(b1, b2) < distance) {
              addError(b1);
              break;
            }
          }
      }
    }

    private boolean isInResidentialArea(Way building, List<Way> residentialAreas) {
      for (Node n: building.getNodes())
        for (Way r: residentialAreas)
          if (Geometry.nodeInsidePolygon(n, r.getNodes()))
            return true;

      return false;
    }

    private double getBuildingsDistance(Way b1, Way b2) {
      double minimalDistance = Double.MAX_VALUE;

      for (Node n1: b1.getNodes())
        for (Node n2: b2.getNodes()) {
          double distance = n1.getEastNorth().distance(n2.getEastNorth());
          if (distance < minimalDistance)
            minimalDistance = distance;
        }

      return minimalDistance;
    }

    private void addError(Way building) {
      errors.add(TestError.builder(this, Severity.WARNING, BUILDING_OUTSIZE_RESIDENTIAL_AREA)
                .message(tr("Building outside a residential area"))
                .primitives(building)
                .highlight(building)
                .build());
    }
}
