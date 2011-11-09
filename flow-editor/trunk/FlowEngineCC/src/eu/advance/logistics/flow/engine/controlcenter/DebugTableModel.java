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
import com.google.common.io.Closeables;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.model.rt.AdvanceParameterDiagnostic;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.reactive.Observer;
import java.awt.EventQueue; 
import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 *
 * @author TTS
 */
class DebugTableModel extends AbstractTableModel {

    private AdvanceEngineControl engine;
    private List<Entry> data = Lists.newArrayList();
    private List<Closeable> targets = Lists.newArrayList();

    DebugTableModel(AdvanceEngineControl engine) {
        this.engine = engine;
    }

    void debug(String realm, String blockId, String param) throws IOException, AdvanceControlException {
        DebugObserver observer = new DebugObserver(realm, blockId, param);
        observer.target = engine.debugParameter(realm, blockId, param).register(observer);
        targets.add(observer.target);
    }

    void close() {
        for (Closeable c1 : targets) {
            Closeables.closeQuietly(c1);
        }
    }

    private void append(Entry entry) {
        int index = data.size();
        data.add(entry);
        fireTableRowsInserted(index, index);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Entry entry = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return entry.realm;
            case 1:
                return entry.block;
            case 2:
                return entry.param;
            case 3:
                return entry.timestamp;
            case 4:
                return entry.value;
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Realm";
            case 1:
                return "Block";
            case 2:
                return "Param";
            case 3:
                return "Timestamp";
            case 4:
                return "Value";
            default:
                return Integer.toString(columnIndex);
        }
    }

    private static class Entry {

        String value;
        Date timestamp;
        String realm;
        String block;
        String param;
    }

    private class DebugObserver implements Observer<AdvanceParameterDiagnostic> {

        private String realm;
        private String blockId;
        private String param;
        private Closeable target;

        private DebugObserver(String realm, String blockId, String param) {
            this.realm = realm;
            this.blockId = blockId;
            this.param = param;
        }

        @Override
        public void next(final AdvanceParameterDiagnostic d) {
            final Entry entry = new Entry();
            entry.realm = realm;
            entry.block = blockId;
            entry.param = param;
            entry.timestamp = d.timestamp;
            if (Option.isError(d.value)) {
                entry.value = Option.getError(d.value).toString();
            } else {
                entry.value = d.value.value().toString();
            }
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    append(entry);
                }
            });
        }

        @Override
        public void error(Throwable ex) {
            final Entry entry = new Entry();
            entry.realm = realm;
            entry.block = blockId;
            entry.param = param;
            entry.timestamp = new Date();
            entry.value = ex.toString();
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    append(entry);
                }
            });
        }

        @Override
        public void finish() {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    targets.remove(target);
                }
            });

        }
    }
    
    void backgroundClose() {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Closing debug...");
        ph.setInitialDelay(0);
        ph.start();
        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                close();
                return null;
            }

            @Override
            protected void done() {
                ph.finish();
            }
        }.execute();
    }
}
