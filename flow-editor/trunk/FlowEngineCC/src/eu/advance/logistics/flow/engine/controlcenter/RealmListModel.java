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

import com.google.common.collect.Lists;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author TTS
 */
class RealmListModel extends AbstractListModel implements ComboBoxModel {

    private List<Entry> data = Lists.newArrayList();
    private Entry selection;
    private JComboBox comboBox;

    static RealmListModel build(JComboBox cb) {
        RealmListModel model = new RealmListModel();
        model.comboBox = cb;
        cb.setModel(model);
        return model;
    }

    private RealmListModel() {
    }

    @Override
    public Object getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    private void update(List<AdvanceRealm> list) {
        int size = data.size();
        data.clear();
        fireIntervalRemoved(this, 0, size);
        for (AdvanceRealm r : list) {
            data.add(new Entry(r));
        }
        fireIntervalAdded(this, 0, data.size());
    }

    void refresh() {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Retrieving realms list...");
        ph.setInitialDelay(0);
        ph.start();
        new SwingWorker<List<AdvanceRealm>, Void>() {

            @Override
            protected List<AdvanceRealm> doInBackground() throws Exception {
                return EngineController.getInstance().getEngine().datastore().queryRealms();
            }

            @Override
            protected void done() {
                try {
                    update(get());
                    if (!data.isEmpty()) {
                        comboBox.setSelectedIndex(0);
                    }
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

    private static class Entry {

        AdvanceRealm realm;

        private Entry(AdvanceRealm realm) {
            this.realm = realm;
        }

        @Override
        public String toString() {
            return realm.name + " (" + realm.status + ")";
        }
    }
}
