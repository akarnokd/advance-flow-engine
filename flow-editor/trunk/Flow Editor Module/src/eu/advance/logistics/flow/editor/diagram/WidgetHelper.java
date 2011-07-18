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

import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

/**
 * <b>WidgetHelper</b>
 *
 * @author TTS
 */
class WidgetHelper {

    private static Font labelFont = new Font("Arial", Font.PLAIN, 10);

    private WidgetHelper() {
    }

    static void configure(FlowDiagramScene scene, final BlockWidget widget, SimpleBlock block) {
        final BlockCategory cat = BlockRegistry.getInstance().findByType(block.description);
        final Image catImg = cat.getImageObject();
        widget.setNodeImage(catImg);

        widget.setNodeName(block.id);
        //widget.setNodeType(null);
        if (Math.random() > 0.5) {
            widget.addGlyph(
                    ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/alert_16.png"),
                    null);
        }
        widget.addGlyph(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/config.png"),
                new Runnable() {

                    @Override
                    public void run() {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(widget.getNodeName());
                        DialogDisplayer.getDefault().notify(nd);
                    }
                });
        widget.addGlyph(
                ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/deployment_in_test.png"),
                null);


        widget.addChild(createLabel(scene, "INPUT"));
        for (BlockParameter param : sort(block.inputParameters)) {
            scene.addPin(block, param);
        }

        widget.addChild(createLabel(scene, "OUTPUT"));
        for (BlockParameter param : sort(block.outputParameters)) {
            scene.addPin(block, param);
        }
    }

    private static List<BlockParameter> sort(Map<String, BlockParameter> params) {
        List<BlockParameter> list = new ArrayList<BlockParameter>(params.values());
        Collections.sort(list);
        return list;
    }

    private static Widget createLabel(FlowDiagramScene scene, String name) {
        final LabelWidget labelWidget = new LabelWidget(scene, name);
        labelWidget.setOpaque(true);
        labelWidget.setFont(labelFont);
        labelWidget.setBackground(Color.BLACK);
        labelWidget.setForeground(Color.WHITE);
        labelWidget.setAlignment(LabelWidget.Alignment.CENTER);
        labelWidget.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        return labelWidget;
    }

    static String getDisplayName(AdvanceBlockParameterDescription description) {
        String name;
        if (description.displayName != null) {
            name = description.displayName;
        } else {
            name = description.id;
        }
        return name;
    }
}
