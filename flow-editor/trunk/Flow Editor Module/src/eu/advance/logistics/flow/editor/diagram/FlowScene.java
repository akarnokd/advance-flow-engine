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

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.util.EnumSet;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
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

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.editor.FlowDescriptionDataObject;
import eu.advance.logistics.flow.editor.actions.*;
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.undo.BindRemoved;
import eu.advance.logistics.flow.editor.undo.BlockRenamed;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.AdvanceData;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;

/**
 *
 * @author TTS
 */
public class FlowScene extends GraphPinScene<AbstractBlock, BlockBind, BlockParameter> {

    private final LayerWidget backgroundLayer = new LayerWidget(this);
    private final LayerWidget mainLayer = new LayerWidget(this);
    private final LayerWidget connectionLayer = new LayerWidget(this);
    private final LayerWidget upperLayer = new LayerWidget(this);
    final BlockConnectionProvider connectionManager;
    private Router router;
    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction();
    private WidgetAction moveAction;
    private WidgetAction connectAction;
    private SceneLayout sceneLayout;
    private ColorScheme scheme = new ColorScheme();
    private JComponent satelliteView;
    private ParamWidget inParamWidget;
    private ParamWidget outParamWidget;
    private Widget contentWidget = new Widget(this);
    private boolean adjusting;
    private FlowDescription flowDescription;
    private UndoRedoSupport undoRedoSupport;

    /**
     * Creates a FlowScene graph scene with a specific color scheme.
     * @param scheme the color scheme
     */
    private FlowScene(UndoRedoSupport urs, FlowDescription flowDesc) {
        undoRedoSupport = urs;
        flowDescription = flowDesc;
        connectionManager = new BlockConnectionProvider(this, flowDesc);
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        inParamWidget = new ParamWidget(this, scheme);
        inParamWidget.addChild(WidgetBuilder.createLabel(this, "INPUT", false));
        outParamWidget = new ParamWidget(this, scheme);
        outParamWidget.addChild(WidgetBuilder.createLabel(this, "OUTPUT", false));

        setBackground(SystemColor.control);
        setOpaque(true);
        contentWidget.setOpaque(true);
        contentWidget.setBackground(Color.WHITE);
        contentWidget.setBorder(ColorScheme.PAGE_BORDER);
        backgroundLayer.addChild(contentWidget);

        addChild(backgroundLayer);
        addChild(mainLayer);
        addChild(inParamWidget);
        addChild(outParamWidget);
        addChild(connectionLayer);
        addChild(upperLayer);

        router = RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer);

        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
        getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));

//        getActions().addAction(ActionFactory.createRectangularSelectAction(
//                ActionFactory.createDefaultRectangularSelectDecorator(this),
//                backgroundLayer,
//                ActionFactory.createObjectSceneRectangularSelectProvider(this)));

        final GridGraphLayout<AbstractBlock, BlockBind> graphLayout = new GridGraphLayout<AbstractBlock, BlockBind>();
        graphLayout.setChecker(false);
        graphLayout.setAnimated(false);

        sceneLayout = LayoutFactory.createSceneGraphLayout(this, graphLayout);
        addSceneListener(new SceneListener() {

            @Override
            public void sceneRepaint() {
            }

            @Override
            public void sceneValidating() {
            }

            @Override
            public void sceneValidated() {
                if (!adjusting) {
                    adjusting = true;
                    updateBackground();
                    adjusting = false;
                }
            }
        });


        //moveAction = ActionFactory.createAlignWithMoveAction(mainLayer, upperLayer, null);
        AlignWithMoveDecorator decorator = ActionFactory.createDefaultAlignWithMoveDecorator();
        WidgetMoveSupport sp = new WidgetMoveSupport(this, mainLayer, upperLayer, decorator);
        moveAction = ActionFactory.createMoveAction(sp, sp);

        connectAction = ActionFactory.createConnectAction(connectionLayer, connectionManager);

    }

    public boolean isAdjusting() {
        return adjusting;
    }
    private final int border = 80;
    private final int pad = 50;
    private final int padSize = 2 * pad;
    private final int minWidth = 200;
    private final int minHeight = 150;

    private void updateBackground() {
        int xmin = Integer.MAX_VALUE;
        int ymin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymax = Integer.MIN_VALUE;
        int ok = 0;
        for (Widget w : mainLayer.getChildren()) {
            Rectangle t = w.getBounds();
            if (t != null) {
                t = w.convertLocalToScene(t);
                xmin = Math.min(xmin, t.x);
                ymin = Math.min(ymin, t.y);
                xmax = Math.max(xmax, t.x + t.width);
                ymax = Math.max(ymax, t.y + t.height);
                ok++;
            }
        }
        Rectangle r = new Rectangle();
        if (ok == 0) {
            r.setSize(minWidth, minHeight);
        } else {
            r.setFrameFromDiagonal(xmin, ymin, xmax, ymax);
            r = mainLayer.convertSceneToLocal(r);
            r.width = Math.max(r.width, minWidth);
            r.height = Math.max(r.height, minHeight);
        }
        boolean needsValidation = false;
        Rectangle c = new Rectangle(r.x - pad, r.y - pad, r.width + padSize, r.height + padSize);
        if (!c.equals(contentWidget.getPreferredBounds())) {
            contentWidget.setPreferredBounds(c);
            needsValidation = true;
        }
        Rectangle w = inParamWidget.getBounds();
        if (w != null) {
            Point loc = new Point(r.x - w.width - border, r.y + (r.height - w.height) / 2);
            if (!loc.equals(inParamWidget.getLocation())) {
                inParamWidget.setPreferredLocation(loc);
                needsValidation = true;
            }
        }
        w = outParamWidget.getBounds();
        if (w != null) {
            Point loc = new Point(r.x + r.width + border, r.y + (r.height - w.height) / 2);
            if (!loc.equals(outParamWidget.getLocation())) {
                outParamWidget.setPreferredLocation(loc);
                needsValidation = true;
            }
        }
        if (needsValidation) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    revalidate();
                }
            });
        }
    }

    public void refresh() {
        connectionLayer.revalidate();
    }

    /**
     * Implements attaching a widget to a node. The widget is BlockWidget and 
     * has object-hover, select, popup-menu and move actions.
     * @param node the node
     * @return the widget attached to the node
     */
    @Override
    protected Widget attachNodeWidget(final AbstractBlock node) {
        if (node.getFlowDiagram().getActiveBlock() == node) {
            return null;
        }
        if (node instanceof ConstantBlock) {
            final ConstantBlock cb = (ConstantBlock) node;
            ConstantBlockWidget widget = new ConstantBlockWidget(this, scheme, cb);
            mainLayer.addChild(widget);
            widget.getActions().addAction(createObjectHoverAction());
            widget.getActions().addAction(createSelectAction());
            widget.getActions().addAction(moveAction);
            widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    return createPopup(new ConstEditAction(undoRedoSupport, cb),
                            new DeleteBlockAction(undoRedoSupport, flowDescription, node));
                }
            }));
            
            
            return widget;
        }
        BlockWidget widget = new BlockWidget(this, scheme);
        mainLayer.addChild(widget);
        
        widget.setToolTipText(node.getTooltip());
        
        widget.getHeader().getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(moveAction);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

            @Override
            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                JPopupMenu popup = createPopup(new DeleteBlockAction(undoRedoSupport, flowDescription, node));
                if (node instanceof SimpleBlock) {
                    popup.addSeparator();
                    JMenuItem type = new JMenuItem("Type: " + ((SimpleBlock)node).description.id);
                    type.setEnabled(false);
                    popup.add(type);
                }
                return popup;
            }
        }));

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
                        if (text == null || text.isEmpty()) {
                            return;
                        }
                        final Widget blockWidget = ((LabelWidget) widget).getParentWidget();
                        final AbstractBlock block = (AbstractBlock) findObject(blockWidget);
                        undoRedoSupport.start();
                        String old = block.getId();
                        boolean ok = block.setId(text);
                        if (ok) {
                            undoRedoSupport.commit(new BlockRenamed(block, old, text));
                        } else {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(
                                    NbBundle.getMessage(FlowDescriptionDataObject.class,
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
    protected Widget attachPinWidget(AbstractBlock node, final BlockParameter pin) {
        if (node instanceof ConstantBlock) {
            return null; // non visual
//            PinWidget widget = new PinWidget(this, scheme, false, false);
//            ((ConstantBlockWidget) findWidget(node)).attachPinWidget(widget);
//            widget.getActions().addAction(createObjectHoverAction());
//            widget.getActions().addAction(createSelectAction());
//            widget.getActions().addAction(connectAction);
//            widget.setPinName("CONST");
//            return widget;
        }
        String name = pin.getDisplayName();
        PinWidget widget;
        if (node.getFlowDiagram().getActiveBlock() == node) {
            if (pin.type == BlockParameter.Type.OUTPUT) {
                widget = new PinWidget(this, scheme, name, true, false);
                widget.setLabelIcon(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/param.png")); // NOI18N
                outParamWidget.attachPinWidget(widget);
            } else {
                widget = new PinWidget(this, scheme, name, true, true);
                widget.setLabelIcon(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/param.png")); // NOI18N
                inParamWidget.attachPinWidget(widget);
            }
            final CompositeBlock block = (CompositeBlock) node;
            final BlockParameter.Type type = pin.type;
            widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu menu = new JPopupMenu();
                    menu.add(new ParamEditAction(undoRedoSupport, pin));
                    menu.add(new ParamRemoveAction(undoRedoSupport, pin));
                    menu.addSeparator();
                    menu.add(new ParamAddAction(undoRedoSupport, block, type));
                    // display the parameter type
                    if (pin.getDescription() != null && pin.getDescription().type != null) {
                        menu.addSeparator();
                        JMenuItem mtype = new JMenuItem();
                        AdvanceType at = pin.getDescription().type;
                        mtype.setText("Type: " + at);
                        mtype.setEnabled(false);
                        menu.add(mtype);
                    }
                    return menu;
                }
            }));
        } else {
            widget = new PinWidget(this, scheme, name, false, false);
            boolean isInput = (pin.type == BlockParameter.Type.INPUT);
            ((BlockWidget) findWidget(node)).attachPinWidget(widget, isInput);
            if (isInput) {
                widget.setLabelIcon(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/in.png")); // NOI18N
            } else {
                widget.setLabelIcon(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/out.png")); // NOI18N
            }
        }
        widget.getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(connectAction);

        if (BlockConnectionProvider.getParamType(widget, pin) == BlockParameter.Type.INPUT) {
            final CompositeBlock parent = node.getParent();
            widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu menu = new JPopupMenu();
                    
                    AdvanceType at = pin.getDescription().type;

                    String s = null;
                    
                    if (AdvanceData.BOOLEAN.equals(at.typeURI)) {
                        s = "Boolean";
                    } else
                    if (AdvanceData.INTEGER.equals(at.typeURI)) {
                        s = "Integer";
                    } else
                    if (AdvanceData.REAL.equals(at.typeURI)) {
                        s = "Real";
                    } else
                    if (AdvanceData.STRING.equals(at.typeURI)) {
                        s = "String";
                    } else
                    if (AdvanceData.TIMESTAMP.equals(at.typeURI)) {
                        s = "Timestamp";
                    } else
                    if (AdvanceData.TYPE.equals(at.typeURI)) {
                        s = "Type";
                    }

                    
                    if (s != null) {
                        menu.add(new ConstAddAction(undoRedoSupport, FlowScene.this, parent, pin, s, null));
                    } else {
                        menu.add(new ConstAddAction(undoRedoSupport, FlowScene.this, parent, pin, "Boolean", "advance:boolean"));
                        menu.add(new ConstAddAction(undoRedoSupport, FlowScene.this, parent, pin, "Integer", "advance:integer"));
                        menu.add(new ConstAddAction(undoRedoSupport, FlowScene.this, parent, pin, "Real", "advance:real"));
                        menu.add(new ConstAddAction(undoRedoSupport, FlowScene.this, parent, pin, "String", "advance:string"));
                        menu.add(new ConstAddAction(undoRedoSupport, FlowScene.this, parent, pin, "Timestamp", "advance:timestamp"));
                        menu.add(new ConstAddAction(undoRedoSupport, FlowScene.this, parent, pin, "Type", null));
                    }
                    
                    menu.addSeparator();
                    
                    JMenuItem typeInfo = new JMenuItem("Type: " + at.toString());
                    typeInfo.setEnabled(false);
                    menu.add(typeInfo);
                    
                    return menu;
                }
            }));
        }
        if (BlockConnectionProvider.getParamType(widget, pin) == BlockParameter.Type.OUTPUT) {
//            final CompositeBlock parent = node.getParent();
            widget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu menu = new JPopupMenu();
                    
                    AdvanceType at = pin.getDescription().type;
                   
                    JMenuItem typeInfo = new JMenuItem("Type: " + at.toString());
                    typeInfo.setEnabled(false);
                    menu.add(typeInfo);
                    
                    return menu;
                }
            }));
        }
        WidgetBuilder.configure(widget, pin);
        return widget;
    }

    @Override
    protected void detachPinWidget(BlockParameter pin, Widget widget) {
        super.detachPinWidget(pin, widget);
        if (widget instanceof PinWidget) {
            BlockWidget blockWidget = (BlockWidget) findWidget(pin.owner);
            if (blockWidget != null) {
                blockWidget.detachPinWidget((PinWidget) widget);
            }
        }
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
        connection.getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyReleased(Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object obj = findObject(widget);
                    if (obj instanceof BlockBind) {
                        BlockBind blockBind = (BlockBind) obj;
                        /*
                        String msg = String.format(NbBundle.getBundle(FlowScene.class).getString("DELETE_BIND"),
                                blockBind.source.getPath(), blockBind.destination.getPath());
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                                msg, NotifyDescriptor.YES_NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                         * 
                         */
                            undoRedoSupport.start();
                            CompositeBlock parent = blockBind.getParent();
                            blockBind.destroy();
                            undoRedoSupport.commit(new BindRemoved(parent, blockBind));
                            /*
                        }*/
                        return State.CONSUMED;
                    }
                }
                return super.keyReleased(widget, event);
            }
        });

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
        if (pin.owner instanceof ConstantBlock) {
            return ((ConstantBlockWidget) findWidget(pin.owner)).getNodeAnchor();
        }

        Widget pinWidget = findWidget(pin);
        final BlockWidget nodeWidget = (BlockWidget) findWidget(getPinNode(pin));
        Anchor anchor;
        if (nodeWidget == null) {
            anchor = AnchorFactory.createDirectionalAnchor(pinWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
        } else if (pinWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor(pinWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
            anchor = nodeWidget.createAnchorPin(anchor);
        } else {
            anchor = nodeWidget.getNodeAnchor();
        }
        return anchor;
    }

    public void layoutScene() {
        sceneLayout.invokeLayoutImmediately();
    }

    void clean() {
        for (BlockBind bind : Lists.newArrayList(getEdges())) {
            removeEdge(bind);
        }
        for (AbstractBlock block : Lists.newArrayList(getNodes())) {
            removeNode(block);
        }
        for (BlockParameter param : Lists.newArrayList(getPins())) {
            removePin(param);
        }
        validate();
    }

    public JComponent getSatelliteView() {
        if (satelliteView == null) {
            satelliteView = createSatelliteView();
        }
        return satelliteView;
    }

    public static FlowScene create(final UndoRedoSupport urs, final FlowDescription flowDesc) {
        final FlowScene scene = new FlowScene(urs, flowDesc);
        final GroupBlockAction groupBlockAction = new GroupBlockAction(scene, flowDesc);
        scene.contentWidget.getActions().addAction(ActionFactory.createAcceptAction(new PaletteAcceptProvider(scene, flowDesc)));
        
        scene.contentWidget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

            @Override
            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                groupBlockAction.setLocation(localLocation);
                return createPopup(
                        DeleteBlockAction.build(scene),
                        null,
                        groupBlockAction);
            }
        }));
        scene.inParamWidget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

            @Override
            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                return createPopup(
                        new ParamAddAction(urs, flowDesc.getActiveBlock(), BlockParameter.Type.INPUT));
            }
        }));
        scene.outParamWidget.getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

            @Override
            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                return createPopup(
                        new ParamAddAction(urs, flowDesc.getActiveBlock(), BlockParameter.Type.OUTPUT));
            }
        }));
        FlowSceneController flowListener = new FlowSceneController(scene);
        flowListener.init(flowDesc);
        flowDesc.addListener(flowListener);
        return scene;
    }

    private static JPopupMenu createPopup(Action... actions) {
        JPopupMenu menu = new JPopupMenu();
        boolean lastNull = true;
        for (Action a : actions) {
            if (a != null) {
                menu.add(a);
                lastNull = false;
            } else if (!lastNull) {
                menu.addSeparator();
                lastNull = true;
            }
        }
        return menu.getComponentCount() != 0 ? menu : null;
    }

    public FlowDescription getFlowDescription() {
        return flowDescription;
    }

    public UndoRedoSupport getUndoRedoSupport() {
        return undoRedoSupport;
    }
}