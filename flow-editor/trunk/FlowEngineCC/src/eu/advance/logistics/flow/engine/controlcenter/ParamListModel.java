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

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;

/**
 *
 * @author TTS
 */
class ParamListModel extends AbstractListModel implements ComboBoxModel {

    /** */
	private static final long serialVersionUID = -6188414966650112162L;
	private List<Entry> data = Lists.newArrayList();
    private Entry selection;
    private JComboBox comboBox;

    static ParamListModel build(JComboBox cb) {
        ParamListModel model = new ParamListModel();
        model.comboBox = cb;
        cb.setModel(model);
        return model;
    }

    private ParamListModel() {
    }

    @Override
    public Object getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    void update(AdvanceBlockRegistryEntry entry) {
        int size = data.size();
        data.clear();
        fireIntervalRemoved(this, 0, size);
        if (entry != null) {
            for (AdvanceBlockParameterDescription p : entry.inputs.values()) {
                data.add(new Entry(p, "IN"));
            }
            for (AdvanceBlockParameterDescription p : entry.outputs.values()) {
                data.add(new Entry(p, "OUT"));
            }
            if (!data.isEmpty()) {
                Collections.sort(data);
                fireIntervalAdded(this, 0, data.size());
                comboBox.setSelectedIndex(0);
            }
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (anItem instanceof Entry) ? (Entry) anItem : null;
    }

    @Override
    public Object getSelectedItem() {
        return selection;
    }

    private static class Entry implements Comparable<Entry> {

        private final AdvanceBlockParameterDescription target;
        private final String displayName;

        private Entry(AdvanceBlockParameterDescription blockParameterDescription, String kind) {
            this.target = blockParameterDescription;
            this.displayName = "[" + kind + "] " + target.displayName + " (" + target.type.typeURI + ")";
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
