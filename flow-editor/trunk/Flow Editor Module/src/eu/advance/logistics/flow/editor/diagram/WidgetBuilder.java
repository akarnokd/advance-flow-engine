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
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

/**
 *
 * @author TTS
 */
class WidgetBuilder {

    private static Font labelFont = new Font("Arial", Font.PLAIN, 10);
    private FlowScene scene;

    WidgetBuilder(FlowScene scene) {
        this.scene = scene;
    }

    void configure(final BlockWidget widget, AbstractBlock block) {
        widget.setNodeName(block.getId());

        if (block instanceof SimpleBlock) {
            final BlockCategory cat = BlockRegistry.getInstance().findByType(((SimpleBlock) block).description);
            final Image catImg = cat.getImageObject();
            widget.setNodeImage(catImg);

//            widget.addGlyph(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/config.png"),
//                    new Runnable() {
//
//                        @Override
//                        public void run() {
//                            NotifyDescriptor nd = new NotifyDescriptor.Message(widget.getNodeName());
//                            DialogDisplayer.getDefault().notify(nd);
//                        }
//                    });
        } else if (block instanceof CompositeBlock) {
            final CompositeBlock compositeBlock = (CompositeBlock) block;
            widget.setNodeImage(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/palette/images/block.png"));

//            widget.addGlyph(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/database.png"),
//                    new Runnable() {
//
//                        @Override
//                        public void run() {
//                            // TODO
//                            NotifyDescriptor nd = new NotifyDescriptor.Message(widget.getNodeName());
//                            DialogDisplayer.getDefault().notify(nd);
//                        }
//                    });
            widget.getActions().addAction(new WidgetAction.Adapter() {

                @Override
                public State mouseClicked(Widget widget, WidgetMouseEvent event) {
                    if (event.getClickCount() == 2) {
                        compositeBlock.getFlowDiagram().setActiveBlock(compositeBlock);
                        return State.CONSUMED;
                    }
                    return super.mouseClicked(widget, event);
                }
            });
        } else if (block instanceof ConstantBlock) {
            widget.getActions().addAction(new WidgetAction.Adapter() {

                @Override
                public State mouseClicked(Widget widget, WidgetMouseEvent event) {
                    if (event.getClickCount() == 2) {
                        // TODO edit const value
                        return State.CONSUMED;
                    }
                    return super.mouseClicked(widget, event);
                }
            });
        }

//        if (Math.random() > 0.5) {
//            widget.addGlyph(
//                    ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/alert_16.png"),
//                    null);
//        }
//        widget.addGlyph(
//                ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/deployment_in_test.png"),
//                null);

        for (BlockParameter param : sort(block.getInputs())) {
            scene.addPin(block, param);
        }

        for (BlockParameter param : sort(block.getOutputs())) {
            scene.addPin(block, param);
        }
    }

    void configure(final ConstantBlockWidget widget, ConstantBlock block) {
        widget.getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State mouseClicked(Widget widget, WidgetMouseEvent event) {
                if (event.getClickCount() == 2) {
                    // TODO edit const value
                    return State.CONSUMED;
                }
                return super.mouseClicked(widget, event);
            }
        });
        scene.addPin(block, block.getParameter());
    }

    static List<BlockParameter> sort(Collection<BlockParameter> params) {
        List<BlockParameter> list = new ArrayList<BlockParameter>(params);
        Collections.sort(list);
        return list;
    }

    static Widget createLabel(Scene scene, String name, boolean opaque) {
        final LabelWidget labelWidget = new LabelWidget(scene, name);
        labelWidget.setFont(labelFont);
        if (opaque) {
            labelWidget.setOpaque(true);
            labelWidget.setBackground(Color.BLACK);
            labelWidget.setForeground(Color.WHITE);
        }
        labelWidget.setAlignment(LabelWidget.Alignment.CENTER);
        labelWidget.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return labelWidget;
    }

    static void configure(PinWidget w, BlockParameter param) {
        w.setPinName(param.getDisplayName());
        AdvanceBlockParameterDescription d = param.getDescription();
        if (d.displayName != null || d.documentation != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            if (d.displayName != null) {
                sb.append("<b>").append(d.displayName).append("</b><br>");
            }
            if (d.documentation != null) {
                sb.append(d.documentation);
            }
            sb.append("</html>");
            w.setToolTipText(sb.toString());
        }
    }
}
