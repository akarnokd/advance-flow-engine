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
package eu.advance.logistics.flow.editor.actions;

import eu.advance.logistics.flow.editor.ContextSupport;
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.visual.widget.Scene.SceneListener;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author TTS
 */
public final class ZoomAction extends AbstractAction implements Presenter.Toolbar {

    private final static String ACTION_PATH = "Actions/View"; // NOI18N
    private final static String ICON_BASE_KEY = "iconBase"; // NOI18N
    private final static String ICON_BASE = "eu/advance/logistics/flow/editor/actions/zoom.png"; // NOI18N
    private FlowScene flowScene;
    private JComboBox options;
    private JPanel panel;
    private boolean adjusting;
    private ContextSupport<FlowScene> contextSupport = new ContextSupport<FlowScene>(FlowScene.class) {

        @Override
        protected void contextChanged(FlowScene context) {
            setFlowScene(context);
        }
    };
    private SceneListener sceneListener = new SceneListener() {

        @Override
        public void sceneRepaint() {
        }

        @Override
        public void sceneValidating() {
        }

        @Override
        public void sceneValidated() {
            update();
        }
    };

    public ZoomAction() {
        putValue(NAME, NbBundle.getMessage(ZoomAction.class, "CTL_ZoomAction"));
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(ICON_BASE_KEY, ICON_BASE);
        String[] items = new String[]{
            "50%", "75%", "100%", "150%", "200%", "400%"
        };
        options = new JComboBox(items);
        //options.setPrototypeDisplayValue("9999%");
        Dimension d = options.getPreferredSize();
        d.width = 70;
        options.setPreferredSize(d);
        options.setEditable(true);
        options.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    apply();
                }
            }
        });
        setFlowScene(null);
        contextSupport.activate();
    }

    private void setFlowScene(FlowScene fs) {
        if (flowScene != null) {
            flowScene.removeSceneListener(sceneListener);
        }
        setEnabled(fs != null);
        options.setEnabled(fs != null);
        if (fs != null) {
            fs.addSceneListener(sceneListener);
        }
        options.setForeground(Color.BLACK);
        flowScene = fs;
        update();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        apply();
    }

    private void update() {
        if (adjusting) {
            return;
        }
        if (flowScene != null) {
            int value = (int) (flowScene.getZoomFactor() * 100);
            options.setSelectedItem(value + "%");
        } else {
            options.setSelectedItem("100%");
        }
    }

    private void apply() {
        if (flowScene == null) {
            return;
        }
        options.setForeground(Color.BLACK);
        Object sel = options.getSelectedItem();
        if (sel != null) {
            String s = sel.toString().trim();
            int i = s.indexOf('%');
            if (i != -1) {
                s = s.substring(0, i);
            }
            try {
                apply(flowScene, Double.parseDouble(s) / 100);
            } catch (NumberFormatException ignore) {
                options.setForeground(Color.RED);
            }
        }
    }

    void apply(FlowScene flowScene, double z) {
        if (Double.compare(z, flowScene.getZoomFactor()) != 0) {
            adjusting = true;
            flowScene.setZoomFactor(z);
            flowScene.getView().repaint();
            flowScene.getSatelliteView().repaint();
            int value = (int) (flowScene.getZoomFactor() * 100);
            options.setSelectedItem(value + "%");
            adjusting = false;
        }
    }

    @Override
    public Component getToolbarPresenter() {
        if (panel == null) {
            panel = new JPanel(); // flow layout
            panel.setOpaque(false);
            panel.add(new JLabel((Icon) getValue(SMALL_ICON)));
            panel.add(options);
        }
        return panel;
    }

    static ZoomAction getInstance() {
        return Lookups.forPath(ACTION_PATH).lookup(ZoomAction.class);
    }
}
