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
package eu.advance.logistics.flow.engine.controlcenter;

import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;

/**
 *
 * @author TTS
 */
class BlockListModel extends AbstractListModel implements ComboBoxModel {

    /** */
	private static final long serialVersionUID = 8495996829389013675L;
	private List<Entry> data = Lists.newArrayList();
    private Entry selection;
    private JComboBox comboBox;

    static BlockListModel build(JComboBox cb) {
        BlockListModel model = new BlockListModel();
        model.comboBox = cb;
        cb.setModel(model);
        return model;
    }

    private BlockListModel() {
    }

    @Override
    public Object getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    private void update(List<AdvanceBlockRegistryEntry> list) {
        int size = data.size();
        data.clear();
        fireIntervalRemoved(this, 0, size);
        if (list != null) {
            for (AdvanceBlockRegistryEntry entry : list) {
                data.add(new Entry(entry));
            }
            if (!data.isEmpty()) {
                Collections.sort(data);
                fireIntervalAdded(this, 0, data.size());
                comboBox.setSelectedIndex(0);
            }
        }
    }

    void refresh() {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Retrieving block list...");
        ph.setInitialDelay(0);
        ph.start();
        new SwingWorker<List<AdvanceBlockRegistryEntry>, Void>() {

            @Override
            protected List<AdvanceBlockRegistryEntry> doInBackground() throws Exception {
                return EngineController.getInstance().getEngine().queryBlocks();
            }

            @Override
            protected void done() {
                try {
                    update(get());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    ph.finish();
                }
            }
        }.execute();
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (anItem instanceof Entry) ? (Entry) anItem : null;
    }

    @Override
    public Object getSelectedItem() {
        return selection;
    }
    
    public AdvanceBlockRegistryEntry getAdvanceBlockRegistryEntry() {
        return (selection != null) ? selection.target : null;
    }

    private static class Entry implements Comparable<Entry> {

        private final AdvanceBlockRegistryEntry target;
        private final String displayName;

        private Entry(AdvanceBlockRegistryEntry blockRegistryEntry) {
            this.target = blockRegistryEntry;
            this.displayName = target.displayName + " (" + target.clazz + ")";
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public int compareTo(Entry o) {
            return displayName.compareTo(o.displayName);
        }
    }
}
