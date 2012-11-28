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
package eu.advance.logistics.flow.editor;

import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class FlowSceneClipboard {

    private static FlowSceneClipboard instance = new FlowSceneClipboard();
    private List<FlowSceneSelectionListener> listeners = new ArrayList<FlowSceneSelectionListener>();
    private List selection = new ArrayList();

    public static FlowSceneClipboard getInstance() {
        return instance;
    }

    private FlowSceneClipboard() {
    }

    public void setSelection(Set sel) {
        selection.clear();

        // if selection is valid, do a copy to hold
        int count_blocks = 0;
        int count_binds = 0;
        if (sel != null) {

            // first insert blocks
            for (Object s : sel) {
                if (s instanceof AbstractBlock) {
                    count_blocks++;
                    selection.add(s);
                }
            }

            // second insert connections between selected blocks
            for (Object s : sel) {

                if (s instanceof BlockBind) {

                    // verify source and destination
                    final BlockBind bind = (BlockBind) s;
                    final AbstractBlock end = bind.destination.owner;
                    final AbstractBlock start = bind.source.owner;

                    if (selection.contains(end) && (selection.contains(start))) {

                        count_binds++;
                        selection.add(s);
                    }
                }
            }
        }

        // message in status bar
        if (selection.isEmpty()) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(FlowSceneClipboard.class, "CLIPBOARD_INVALID"));
        } else {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(FlowSceneClipboard.class, "CLIPBOARD_STATUS",
                    count_blocks, count_binds));
        }

        // notify all
        for (FlowSceneSelectionListener l : listeners) {
            l.SelectionChanged();
        }
    }

    public List getSelection() {
        return selection;
    }

    public boolean isValidSelection() {
        return !selection.isEmpty();
    }

    public void addSceneSelectionListener(FlowSceneSelectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeSceneSelectionListener(FlowSceneSelectionListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }
}
