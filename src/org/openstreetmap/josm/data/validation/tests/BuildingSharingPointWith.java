// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.validation.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

/**
 * Checks for buildings that have a points shared with a different objec
 */
public class BuildingSharingPointWith extends Test {
    static final String HIGHWAY = "highway";

    private Set<Way> visitedWays;

    /**
     * Constructs a new {@code BuildingSharingPointWith} test.
     */
    public BuildingSharingPointWith() {
        super(tr("Building sharing points"), tr("Checks for building nodes being shared."));
    }

    @Override
    public void visit(Way w) {
        if (!w.isUsable() || !w.isClosed() || !isBuilding(w)) return;

        visitedWays.add(w);

        Set<OsmPrimitive> visitedPrimitives = new HashSet<>();
        for (Node n: w.getNodes()) {
            List<OsmPrimitive> r = n.getReferrers();

            for (OsmPrimitive p : r) {
                if (p != w && !visitedWays.contains(p)
                        && !visitedPrimitives.contains(p)
                        && isConflictCandidate(p))
                {
                    addError(w, p, n);
                    visitedPrimitives.add(p);
                }
            }
        }
    }

    @Override
    public void startTest(ProgressMonitor monitor) {
        super.startTest(monitor);

        visitedWays = new HashSet<>();
    }

    @Override
    public void endTest() {
        super.endTest();

        visitedWays = null;
    }

    private boolean isConflictCandidate(OsmPrimitive p) {
      return isBuilding(p) || isResidentialArea(p) || p.hasTag(HIGHWAY);
    }

    private String getMessage(OsmPrimitive p) {
      if (isBuilding(p))
        return tr("Building sharing point with a building");
      else if(isResidentialArea(p))
        return tr("Building sharing point with a residential area");
      else if (p.hasTag(HIGHWAY))
        return tr("Building sharing point with a highway");
      else
        throw new IllegalArgumentException();
    }

    private void addError(Way building, OsmPrimitive conflicting, Node sharedPoint) {
        errors.add(TestError.builder(this, Severity.WARNING, 3801)
                .message(getMessage(conflicting))
                .primitives(building, sharedPoint)
                .highlight(building)
                .build());
    }
}
