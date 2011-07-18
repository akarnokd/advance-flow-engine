/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package eu.advance.logistics.flow.editor.diagram;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author TTS
 */
class BlockAnchor extends Anchor {

    private boolean requiresRecalculation = true;
    private HashMap<Entry, Result> results = new HashMap<Entry, Result>();
    private final boolean vertical;
    private ColorScheme scheme;

    /**
     * Creates a node anchor.
     * @param widget the node widget where the anchor is attached to
     * @param vertical if true, then anchors are placed vertically; if false, then anchors are placed horizontally
     * @param scheme color scheme
     * @since 2.5
     */
    BlockAnchor(Widget widget, boolean vertical, ColorScheme scheme) {
        super(widget);
        assert widget != null;
        assert scheme != null;
        this.vertical = vertical;
        this.scheme = scheme;
    }

    /**
     * Notifies when an entry is registered
     * @param entry the registered entry
     */
    @Override
    protected void notifyEntryAdded(Entry entry) {
        requiresRecalculation = true;
    }

    /**
     * Notifies when an entry is unregistered
     * @param entry the unregistered entry
     */
    @Override
    protected void notifyEntryRemoved(Entry entry) {
        results.remove(entry);
        requiresRecalculation = true;
    }

    /**
     * Notifies when the anchor is going to be revalidated.
     */
    @Override
    protected void notifyRevalidate() {
        requiresRecalculation = true;
    }

    private void recalculate() {
        if (!requiresRecalculation) {
            return;
        }

        Widget widget = getRelatedWidget();
        Point relatedLocation = getRelatedSceneLocation();

        Rectangle bounds = widget.convertLocalToScene(widget.getBounds());

        HashMap<Entry, Float> topmap = new HashMap<Entry, Float>();
        HashMap<Entry, Float> bottommap = new HashMap<Entry, Float>();

        for (Entry entry : getEntries()) {
            Point oppositeLocation = getOppositeSceneLocation(entry);
            if (oppositeLocation == null || relatedLocation == null) {
                results.put(entry, new Result(new Point(bounds.x, bounds.y), DIRECTION_ANY));
                continue;
            }

            int dy = oppositeLocation.y - relatedLocation.y;
            int dx = oppositeLocation.x - relatedLocation.x;

            if (vertical) {
                if (dy > 0) {
                    bottommap.put(entry, (float) dx / (float) dy);
                } else if (dy < 0) {
                    topmap.put(entry, (float) -dx / (float) dy);
                } else {
                    topmap.put(entry, dx < 0 ? Float.MAX_VALUE : Float.MIN_VALUE);
                }
            } else {
                if (dx > 0) {
                    bottommap.put(entry, (float) dy / (float) dx);
                } else if (dy < 0) {
                    topmap.put(entry, (float) -dy / (float) dx);
                } else {
                    topmap.put(entry, dy < 0 ? Float.MAX_VALUE : Float.MIN_VALUE);
                }
            }
        }

        Entry[] topList = toArray(topmap);
        Entry[] bottomList = toArray(bottommap);

        int pinGap = scheme.getNodeAnchorGap(this);
        int y = bounds.y - pinGap;
        int x = bounds.x - pinGap;
        int len = topList.length;

        for (int a = 0; a < len; a++) {
            Entry entry = topList[a];
            if (vertical) {
                x = bounds.x + (a + 1) * bounds.width / (len + 1);
            } else {
                y = bounds.y + (a + 1) * bounds.height / (len + 1);
            }
            results.put(entry, new Result(new Point(x, y), vertical ? Direction.TOP : Direction.LEFT));
        }

        y = bounds.y + bounds.height + pinGap;
        x = bounds.x + bounds.width + pinGap;
        len = bottomList.length;

        for (int a = 0; a < len; a++) {
            Entry entry = bottomList[a];
            if (vertical) {
                x = bounds.x + (a + 1) * bounds.width / (len + 1);
            } else {
                y = bounds.y + (a + 1) * bounds.height / (len + 1);
            }
            results.put(entry, new Result(new Point(x, y), vertical ? Direction.BOTTOM : Direction.RIGHT));
        }

        requiresRecalculation = false;
    }

    private Entry[] toArray(final HashMap<Entry, Float> map) {
        Set<Entry> keys = map.keySet();
        Entry[] entries = keys.toArray(new Entry[keys.size()]);
        Arrays.sort(entries, new Comparator<Entry>() {

            @Override
            public int compare(Entry o1, Entry o2) {
                float f = map.get(o1) - map.get(o2);
                if (f > 0.0f) {
                    return 1;
                } else if (f < 0.0f) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        return entries;
    }

    /**
     * Computes a result (position and direction) for a specific entry.
     * @param entry the entry
     * @return the calculated result
     */
    @Override
    public Result compute(Entry entry) {
        recalculate();
        return results.get(entry);
    }
}