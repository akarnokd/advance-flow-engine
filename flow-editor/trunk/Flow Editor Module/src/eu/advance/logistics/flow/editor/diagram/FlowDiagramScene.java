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

import eu.advance.logistics.flow.editor.FlowDiagramController;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import java.beans.PropertyVetoException;
import java.util.EnumSet;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class FlowDiagramScene extends GraphPinScene<SimpleBlock, BlockBind, BlockParameter> {

    public static final String PIN_ID_DEFAULT_SUFFIX = "#default"; // NOI18N
    private final LayerWidget backgroundLayer = new LayerWidget(this);
    private final LayerWidget mainLayer = new LayerWidget(this);
    private final LayerWidget connectionLayer = new LayerWidget(this);
    private final LayerWidget upperLayer = new LayerWidget(this);
    final BlockConnectionProvider connectionManager = new BlockConnectionProvider(this);
    private Router router;
    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction();
    private WidgetAction moveAction;
    private WidgetAction connectAction;
    private SceneLayout sceneLayout;
    private ColorScheme scheme;
    private FlowDiagramController controller;

    /**
     * Creates a FlowDiagramScene graph scene.
     */
    public FlowDiagramScene(FlowDiagramController c) {
        this(c, new ColorScheme());
    }

    /**
     * Creates a FlowDiagramScene graph scene with a specific color scheme.
     * @param scheme the color scheme
     */
    public FlowDiagramScene(FlowDiagramController c, ColorScheme scheme) {
        this.controller = c;
        this.scheme = scheme;
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        addChild(backgroundLayer);
        addChild(mainLayer);
        addChild(connectionLayer);
        addChild(upperLayer);

        router = RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer);

        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
        getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));

        sceneLayout = LayoutFactory.createSceneGraphLayout(this, new GridGraphLayout<SimpleBlock, BlockBind>().setChecker(false));

        moveAction = ActionFactory.createAlignWithMoveAction(mainLayer, upperLayer, null);
        connectAction = ActionFactory.createConnectAction(connectionLayer, connectionManager);
    }

    /**
     * Implements attaching a widget to a node. The widget is BlockWidget and 
     * has object-hover, select, popup-menu and move actions.
     * @param node the node
     * @return the widget attached to the node
     */
    @Override
    protected Widget attachNodeWidget(SimpleBlock node) {
        BlockWidget widget = new BlockWidget(this, scheme);
        mainLayer.addChild(widget);

        widget.getHeader().getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(moveAction);


        //possibilità di editare i nomi
        final LabelWidget nameWidget = widget.getNodeNameWidget();
        nameWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(
                new TextFieldInplaceEditor() {

                    @Override
                    public boolean isEnabled(Widget widget) {
                        return true;
                    }

                    @Override
                    public String getText(Widget widget) {
                        return ((LabelWidget) widget).getLabel();
                    }

                    @Override
                    public void setText(Widget widget, String text) {
                        final Widget blockWidget = ((LabelWidget) widget).getParentWidget();
                        final SimpleBlock block = (SimpleBlock) findObject(blockWidget);
                        if (!controller.setBlockId(block, text)) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(
                                    NbBundle.getMessage(FlowDiagramController.class,
                                    "ID_ALREADY_EXISTS", text),
                                    NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);
                        }
                    }
                }, EnumSet.<InplaceEditorProvider.ExpansionDirection>of(InplaceEditorProvider.ExpansionDirection.RIGHT)));

        return widget;
    }

    /**
     * Implements attaching a widget to a pin. The widget is PinWidget and has object-hover and select action.
     * The the node id ends with "#default" then the pin is the default pin of a node and therefore it is non-visual.
     * @param node the node
     * @param pin the pin
     * @return the widget attached to the pin, null, if it is a default pin
     */
    @Override
    protected Widget attachPinWidget(SimpleBlock node, BlockParameter pin) {
        final PinWidget widget = new PinWidget(this, scheme);
        ((BlockWidget) findWidget(node)).attachPinWidget(widget);
        widget.getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(connectAction);

        if (pin.type == BlockParameter.Type.OUTPUT) {
            widget.setLabelIcon(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/out.png")); // NOI18N
        } else if (pin.type == BlockParameter.Type.INPUT) {
            widget.setLabelIcon(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/in.png")); // NOI18N
        }
        widget.setPinName(WidgetHelper.getDisplayName(pin.description));

//        final LabelWidget nameLabel = (LabelWidget) widget.getPinNameWidget();
//        nameLabel.getActions().addAction(ActionFactory.createInplaceEditorAction(
//                new TextFieldInplaceEditor() {
//
//                    @Override
//                    public boolean isEnabled(Widget widget) {
//                        return true;
//                    }
//
//                    @Override
//                    public String getText(Widget widget) {
//                        return ((LabelWidget) widget).getLabel();
//                    }
//
//                    @Override
//                    public void setText(Widget widget, String text) {
//                        ((LabelWidget) widget).setLabel(text);
//                        final BlockParameter pin = (BlockParameter) findObject(widget);
//                        pin.setName(text);
//                    }
//                },
//                EnumSet.<InplaceEditorProvider.ExpansionDirection>of(InplaceEditorProvider.ExpansionDirection.RIGHT)));
        return widget;
    }

    /**
     * Implements attaching a widget to an edge. the widget is ConnectionWidget and has object-hover, select and move-control-point actions.
     * @param edge the edge
     * @return the widget attached to the edge
     */
    @Override
    protected Widget attachEdgeWidget(BlockBind edge) {
        final BlockConnectionWidget connection = new BlockConnectionWidget(this, scheme);
        connection.setRouter(router);
        connectionLayer.addChild(connection);

        connection.getActions().addAction(createObjectHoverAction());
        connection.getActions().addAction(createSelectAction());
        connection.getActions().addAction(moveControlPointAction);

        return connection;
    }

    /**
     * Attaches an anchor of a source pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldSourcePin the old source pin
     * @param sourcePin the new source pin
     */
    @Override
    protected void attachEdgeSourceAnchor(BlockBind edge, BlockParameter oldSourcePin, BlockParameter sourcePin) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(getPinAnchor(sourcePin));
    }

    /**
     * Attaches an anchor of a target pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldTargetPin the old target pin
     * @param targetPin the new target pin
     */
    @Override
    protected void attachEdgeTargetAnchor(BlockBind edge, BlockParameter oldTargetPin, BlockParameter targetPin) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }

    private Anchor getPinAnchor(BlockParameter pin) {
        if (pin == null) {
            return null;
        }

        return getPinAnchor(findWidget(pin));
    }

    private Anchor getPinAnchor(Widget pinWidget) {
        final BlockWidget nodeWidget = (BlockWidget) findWidget(getPinNode((BlockParameter) findObject(pinWidget)));
        Anchor anchor;
        if (pinWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor(pinWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
            anchor = nodeWidget.createAnchorPin(anchor);
        } else {
            anchor = nodeWidget.getNodeAnchor();
        }
        return anchor;
    }

    public void layoutScene() {
        sceneLayout.invokeLayout();
    }

    public void addBlock(SimpleBlock block) {
        BlockWidget widget = (BlockWidget) addNode(block);
        WidgetHelper.configure(this, widget, block);
    }

    public void addConnection(BlockBind c) {
        addEdge(c);
        setEdgeSource(c, c.source);
        setEdgeTarget(c, c.destination);
    }

    public FlowDiagramController getController() {
        return controller;
    }
}