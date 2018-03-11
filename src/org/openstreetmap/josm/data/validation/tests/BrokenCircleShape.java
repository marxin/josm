// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.validation.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Pair;

/**
 * Checks for circular buildings that have a deformed shape.
 *
 * @since xxx
 */
public class BrokenCircleShape extends Test {
  /**
   * Constructs a new {@code RightAngleBuildingTest} test.
   */
  public BrokenCircleShape() {
    super(tr("Broken circle shape"),
        tr("Checks for circular buildings that have a deformed shape."));
  }

  @Override
  public void visit(Way w) {
    if (!w.isUsable() || !w.isClosed() || !isBuilding(w))
      return;

    int count = w.getNodesCount() - 1;
    if (count <= 6)
      return;

    /* Identify that the shape has most of nodes organized in a circle.  */
    double circleAngle = 180 * (count - 2) / count;

    int matches = 0;
    double maximalAngleDifference = Config.getPref().getDouble("validator.BrokenCircleShape.maximalAngleDifference", 1);
    for (Pair<Double, Node> pair: w.getAngles())
      if (Math.abs(pair.a - circleAngle) < maximalAngleDifference)
        matches++;

    /* Continue if most of the angles are fine.  */
    if (matches < count * 0.6)
      return;

    double minDelta = Config.getPref().getDouble("validator.BrokenCircleShape.minimalDelta", 0.00001);

    /* Remove the last (closing) node of way. **/
    List<Node> nodes = w.getNodes ();
    nodes.remove (nodes.size () - 1);

    double cx = 0;
    double cy = 0;

    for (Node n: nodes) {
      cx += n.getEastNorth().east();
      cy += n.getEastNorth().north();
    }

    Node center = new Node (new EastNorth(cx / count, cy / count));

    double maxDistance = 0;
    double minDistance = Double.MAX_VALUE;

    for (Node n: nodes) {
      double distance = center.getEastNorth ().distance (n.getEastNorth ());
      if (distance > maxDistance)
        maxDistance = distance;
      if (distance < minDistance)
        minDistance = distance;
    }

    if ((maxDistance - minDistance) > minDelta)
      errors.add(TestError.builder(this, Severity.WARNING, 3901)
          .message(tr("Broken circular shape"))
          .primitives(w)
          .build());
  }
}
