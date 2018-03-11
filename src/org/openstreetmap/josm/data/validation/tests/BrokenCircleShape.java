// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.validation.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.HashMap;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Pair;

/**
 * Checks for buildings with angles close to right angle.
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
        if (!w.isUsable() || !w.isClosed() || !isBuilding(w)) return;

        int count = w.getNodesCount();
        if (count <= 6)
            return;

        ArrayList<ArrayList<Double>> angles = new ArrayList<ArrayList<Double>>();

        for (Pair<Double, Node> pair: w.getAngles()) {
            if(angles.isEmpty()) {
                ArrayList<Double> l = new ArrayList<>();
                l.add(pair.a);
                angles.add(l);
            }
            else {
                int i;
                for(i = 0; i < angles.size(); i++) {
                    double delta = Math.abs(angles.get(i).get(0) - pair.a);
                    if (delta < 5.0) {
                        angles.get(i).add(pair.a);
                        break;
                    }

                    if (i == angles.size() - 1) {
                        ArrayList<Double> l = new ArrayList<>();
                        l.add(pair.a);
                        angles.add(l);
                    }
                }

            }
        }

        for (int i = 0; i < angles.size(); i++) {
            int groupSize = angles.get(i).size();
            if (groupSize < w.getRealNodesCount() && groupSize >= count * 0.8) {
                errors.add(TestError.builder(this, Severity.WARNING, 3901)
                        .message(tr("Broken circular shape"))
                        .primitives(w)
                        .build());
                return;
            }
        }
    }

}
