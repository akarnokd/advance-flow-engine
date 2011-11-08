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

import com.google.common.collect.Maps;
import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.engine.AdvanceBlockResolver;
import eu.advance.logistics.flow.engine.AdvanceCompiler;
import eu.advance.logistics.flow.engine.AdvanceLocalSchemaResolver;
import eu.advance.logistics.flow.engine.AdvanceSchemaResolver;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.cc.CCDebugRow;
import eu.advance.logistics.flow.engine.cc.CCValueDialog;
import eu.advance.logistics.flow.engine.cc.CCWatchSettings;
import eu.advance.logistics.flow.engine.cc.LabelManager;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.base.Scheduler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;

@ActionID(category = "RemoteFlowEngine",
id = "eu.advance.logistics.flow.engine.controlcenter.VerifyFlowAction")
@ActionRegistration(iconBase = "eu/advance/logistics/flow/engine/controlcenter/buildProject.png",
displayName = "#CTL_VerifyFlowAction",
surviveFocusChange = true)
public final class VerifyFlowAction implements ActionListener {

    private FlowDescription context;

    public VerifyFlowAction(FlowDescription ctx) {
        this.context = ctx;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AdvanceEngineControl engine = EngineController.getInstance().getEngine();
        new VerifyWorker(engine, context).execute();
    }

    private static class VerifyWorker extends SwingWorker {

        private AdvanceEngineControl engine;
        private FlowDescription flowDescription;
        private ProgressHandle ph;

        public VerifyWorker(AdvanceEngineControl engine, FlowDescription fd) {
            this.engine = engine;
            this.flowDescription = fd;
            this.ph = ProgressHandleFactory.createHandle("Verifying flow...");
            ph.setInitialDelay(0);
            ph.start();
        }

        @Override
        protected Object doInBackground() throws Exception {
            AdvanceCompositeBlock flow = flowDescription.build();
            if (engine != null) {
                return engine.verifyFlow(flow);
            }

            return BlockRegistry.getInstance().verify(flow);
        }

        @Override
        protected void done() {
            try {
                AdvanceCompilationResult result = (AdvanceCompilationResult) get();
                flowDescription.setCompilationResult(result);
                if (result != null) {
                    boolean ok = result.success();
                    String text = "Verification result: " + (ok ? "OK" : "FAILED");
                    NotificationDisplayer.getDefault().notify("Verify flow",
                            Commons.getNotificationIcon(ok), text, null);
                    StatusDisplayer.getDefault().setStatusText(text);

//                    if (!result.success()) {
//                        showResultDialog(result);
//                    }
                }
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex.getCause());
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                ph.finish();
            }
        }

        /**
         * Show the results in the debug value dialog.
         * @param result the result
         */
        protected void showResultDialog(AdvanceCompilationResult result) {
            CCDebugRow dr = new CCDebugRow();
            dr.watch = new CCWatchSettings();
            dr.timestamp = new Date();
            XElement e = new XElement("advance-compilation-result");
            result.save(e);
            dr.value = Option.some(e);
            dr.watch.block = "";
            dr.watch.blockType = "";
            dr.watch.port = "";
            dr.watch.realm = "";

            CCValueDialog dlg = new CCValueDialog(new LabelManager() {

                @Override
                public String get(String key) {
                    return key;
                }

                @Override
                public String format(String key, Object... values) {
                    return String.format(key, values);
                }
            }, dr);
            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        }
    }
}
