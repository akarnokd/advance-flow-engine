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

import com.google.common.collect.Lists;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.vmd.VMDMinimizeAbility;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author TTS
 */
public class BlockWidget extends Widget implements StateModel.Listener, VMDMinimizeAbility {

    private Widget header;
    private ImageWidget minimizeWidget;
    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;
    private IconSetWidget glyphSetWidget;
    private SeparatorWidget pinsSeparator;
    private StateModel stateModel = new StateModel(2);
    private Anchor nodeAnchor;
    private ColorScheme scheme;
    private WeakHashMap<Anchor, Anchor> proxyAnchorCache = new WeakHashMap<Anchor, Anchor>();
    private Widget inParamsWidget;
    private Widget outParamsWidget;
    private List<PinWidget> inPins = Lists.newArrayList();
    private List<PinWidget> outPins = Lists.newArrayList();

    /**
     * Creates a node widget with a specific color scheme.
     * @param scene the scene
     * @param scheme the color scheme
     */
    BlockWidget(Scene scene, ColorScheme scheme) {
        super(scene);
        assert scheme != null;
        this.scheme = scheme;

        nodeAnchor = new BlockAnchor(this, true, scheme);

        setLayout(LayoutFactory.createVerticalFlowLayout());
        setMinimumSize(new Dimension(128, 8));

        header = new Widget(scene);
        header.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 8));
        addChild(header);

        boolean right = scheme.isNodeMinimizeButtonOnRight(this);

        minimizeWidget = new ImageWidget(scene, scheme.getMinimizeWidgetImage(this));
        minimizeWidget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        minimizeWidget.getActions().addAction(new ToggleMinimizedAction());
        if (!right) {
            header.addChild(minimizeWidget);
        }

        imageWidget = new ImageWidget(scene);
        header.addChild(imageWidget);

        nameWidget = new LabelWidget(scene);
        nameWidget.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        header.addChild(nameWidget);

        typeWidget = new LabelWidget(scene);
        typeWidget.setForeground(Color.BLACK);
        header.addChild(typeWidget);
        header.setChildConstraint(typeWidget, new Float(1));

        glyphSetWidget = new IconSetWidget(scene, BorderFactory.createEmptyBorder(2));
        SeparatorWidget separator = new SeparatorWidget(scene, SeparatorWidget.Orientation.VERTICAL);
        header.addChild(separator);
        header.addChild(glyphSetWidget);

        if (right) {
            Widget widget = new Widget(scene);
            widget.setOpaque(false);
            header.addChild(widget, 1000);
            header.addChild(minimizeWidget);
        }

        pinsSeparator = new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL);
        addChild(pinsSeparator);

//        Widget topLayer = new Widget(scene);
//        addChild(topLayer);

        inParamsWidget = new Widget(scene);
        inParamsWidget.setCheckClipping(true);
        inParamsWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        outParamsWidget = new Widget(scene);
        outParamsWidget.setCheckClipping(true);
        outParamsWidget.setLayout(LayoutFactory.createVerticalFlowLayout());

        addChild(WidgetBuilder.createLabel(scene, "INPUT", true));
        addChild(inParamsWidget);
        addChild(WidgetBuilder.createLabel(scene, "OUTPUT", true));
        addChild(outParamsWidget);

        stateModel = new StateModel();
        stateModel.addListener(this);

        scheme.installUI(this);
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    /**
     * Called to check whether a particular widget is minimizable. By default it returns true.
     * The result have to be the same for whole life-time of the widget. If not, then the revalidation has to be invoked manually.
     * An anchor (created by <code>BlockWidget.createPinAnchor</code> is not affected by this method.
     * @param widget the widget
     * @return true, if the widget is minimizable; false, if the widget is not minimizable
     */
    protected boolean isMinimizableWidget(Widget widget) {
        return true;
    }

    /**
     * Check the minimized state.
     * @return true, if minimized
     */
    public boolean isMinimized() {
        return stateModel.getBooleanState();
    }

    /**
     * Set the minimized state. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     * @param minimized if true, then the widget is going to be minimized
     */
    public void setMinimized(boolean minimized) {
        stateModel.setBooleanState(minimized);
    }

    /**
     * Toggles the minimized state. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     */
    public void toggleMinimized() {
        stateModel.toggleBooleanState();
    }

    /**
     * Called when a minimized state is changed. This method will show/hide child widgets of this Widget and switches anchors between
     * node and pin widgets.
     */
    @Override
    public void stateChanged() {
        boolean minimized = stateModel.getBooleanState();
        Rectangle rectangle = minimized ? new Rectangle() : null;
        for (Widget widget : getChildren()) {
            if (widget != header && widget != pinsSeparator) {
                getScene().getSceneAnimator().animatePreferredBounds(widget, minimized && isMinimizableWidget(widget) ? rectangle : null);
            }
        }
        minimizeWidget.setImage(scheme.getMinimizeWidgetImage(this));
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        scheme.updateUI(this, previousState, state);
    }

    /**
     * Sets a node image.
     * @param image the image
     */
    public void setNodeImage(Image image) {
        imageWidget.setImage(image);
        revalidate();
    }

    /**
     * Returns a node name.
     * @return the node name
     */
    public String getNodeName() {
        return nameWidget.getLabel();
    }

    /**
     * Sets a node name.
     * @param nodeName the node name
     */
    public void setNodeName(String nodeName) {
        nameWidget.setLabel(nodeName);
    }

    /**
     * Sets a node type (secondary name).
     * @param nodeType the node type
     */
    public void setNodeType(String nodeType) {
        typeWidget.setLabel(nodeType != null ? "[" + nodeType + "]" : null);
    }

    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget(PinWidget widget, boolean input) {
        if (input) {
            attachPinWidget(widget, inPins, inParamsWidget);
        } else {
            attachPinWidget(widget, outPins, outParamsWidget);
        }
    }

    private void attachPinWidget(PinWidget widget, List<PinWidget> params, Widget parent) {
        widget.setCheckClipping(true);
        if (stateModel.getBooleanState() && isMinimizableWidget(widget)) {
            widget.setPreferredBounds(new Rectangle());
        }
        
        params.add(widget);
        Collections.sort(params);
        
        parent.removeChildren();
        for (Widget w : params) {
            parent.addChild(w);
        }
    }

    void detachPinWidget(PinWidget pinWidget) {
        if (!(inPins.remove(pinWidget) || outPins.remove(pinWidget))) {
            System.err.println("Pin widget not removed!");
        }
    }

    /**
     * Add a node glyph.
     * @param image the images to add
     * @param action the images to run when the image is clicked (can be null)
     */
    public void addGlyph(Image image, Runnable action) {
        glyphSetWidget.add(image, action);
    }

    /**
     * Returns a node name widget.
     * @return the node name widget
     */
    public LabelWidget getNodeNameWidget() {
        return nameWidget;
    }

    /**
     * Returns a node anchor.
     * @return the node anchor
     */
    public Anchor getNodeAnchor() {
        return nodeAnchor;
    }

    /**
     * Creates an extended pin anchor with an ability of reconnecting to the node anchor when the node is minimized.
     * @param anchor the original pin anchor from which the extended anchor is created
     * @return the extended pin anchor, the returned anchor is cached and returns a single extended pin anchor instance of each original pin anchor
     */
    public Anchor createAnchorPin(Anchor anchor) {
        Anchor proxyAnchor = proxyAnchorCache.get(anchor);
        if (proxyAnchor == null) {
            proxyAnchor = AnchorFactory.createProxyAnchor(stateModel, anchor, nodeAnchor);
            proxyAnchorCache.put(anchor, proxyAnchor);
        }
        return proxyAnchor;
    }

    /**
     * Collapses the widget.
     */
    @Override
    public void collapseWidget() {
        stateModel.setBooleanState(true);
    }

    /**
     * Expands the widget.
     */
    @Override
    public void expandWidget() {
        stateModel.setBooleanState(false);
    }

    /**
     * Returns a header widget.
     * @return the header widget
     */
    public Widget getHeader() {
        return header;
    }

    /**
     * Returns a minimize button widget.
     * @return the miminize button widget
     */
    public Widget getMinimizeButton() {
        return minimizeWidget;
    }

    /**
     * Returns a pins separator.
     * @return the pins separator
     */
    public Widget getPinsSeparator() {
        return pinsSeparator;
    }

    private final class ToggleMinimizedAction extends WidgetAction.Adapter {

        @Override
        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
                stateModel.toggleBooleanState();
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
    }
}
