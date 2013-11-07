/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.flow.engine.cc;

import hu.akarnokd.reactive4java.base.Action0;
import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.utils.xml.XNAppender;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNElement.XAttributeName;
import hu.akarnokd.utils.xml.XNElement.XRepresentationRecord;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTextUI.BasicCaret;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The value dialog.
 * @author akarnokd, 2011.10.21.
 */
public class CCValueDialog extends JFrame {
	/** */
	private static final long serialVersionUID = 2212596687655705888L;
	/** The label manager. */
	protected final LabelManager labels;
	/** The engine info. */
	public EngineInfoPanel engineInfo;
	/** The row. */
	protected final CCDebugRow row;
	/** The text. */
	protected JTextArea text;
	/** The tree. */
	protected JTree tree;
	/** The element starts. */
	final Map<XNElement, Integer> elementStarts = Maps.newHashMap();
	/** The element ends. */
	final Map<XNElement, Integer> elementEnds = Maps.newHashMap();
	/** The text starts. */
	final Map<XNElement, Integer> textStarts = Maps.newHashMap();
	/** The text ends. */
	final Map<XNElement, Integer> textEnds = Maps.newHashMap();
	/** Tree selection is under progress. */
	boolean treeSelecting;
	/** Find text. */
	protected JTextField find;
	/** Find next. */
	protected JButton next;
	/** Find previous. */
	protected JButton prev;
	/** Toggle word wrap. */
	protected JCheckBox wrap;
	/**
	 * Creates the dialog GUI.
	 * @param labels the label manager.
	 * @param row the value source
	 */
	public CCValueDialog(@NonNull final LabelManager labels, @NonNull final CCDebugRow row) {
		this.labels = labels;
		this.row = row;
		setTitle("Debug value");
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		engineInfo = new EngineInfoPanel(labels);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		text = new JTextArea(25, 70);
		text.setEditable(false);
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		tree = new JTree(new DefaultTreeModel(null));
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath p = tree.getSelectionPath();
				if (p != null) {
					treeSelecting = true;
					try {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode)p.getLastPathComponent();
						((Action0)n.getUserObject()).invoke();
					} finally {
						treeSelecting = false;
					}
				}
			}
		});
		new WrappingCaret(text);
		
		text.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				if (!treeSelecting) {
					selectNode(e.getDot());
				}
			}
		});
		
		wrap = new JCheckBox(labels.get("Word wrap"));
		wrap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				text.setLineWrap(wrap.isSelected());
				text.setWrapStyleWord(wrap.isSelected());
			}
		});
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(text));
		split.setOneTouchExpandable(true);
		
		split.setDividerLocation(250);
		
		JLabel realmLabel = new JLabel(labels.get("Realm:") + " " + row.watch.realm);
		JLabel blockLabel = new JLabel(labels.get("Block:") + " " + row.watch.block + "(" + row.watch.blockType + ")");
		JLabel portLabel = new JLabel(labels.get("Port:") + " " + row.watch.port);
		JLabel timeLabel = new JLabel(labels.get("Timestamp:") + " " + row.timestamp);
		
		JLabel findLabel = new JLabel(labels.get("Find:"));
		find = new JTextField(20);
		next = new JButton(labels.get("Next"));
		prev = new JButton(labels.get("Prev"));
		
		ActionListener nextAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFindNext();
			}
		};
		find.addActionListener(nextAction);
		next.addActionListener(nextAction);
		prev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFindPrev();
			}
		});
		
		if (Option.isError(row.value)) {
			text.setText(Option.getError(row.value).toString());
		} else 
		if (Option.isSome(row.value)) {
			String s = row.value.value().toString();
			text.setText(s);
			createTree(row.value.value());
		}
		text.addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					Font f = text.getFont();
					if (e.getUnitsToScroll() < 0) {
						text.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 1));
					} else 
					if (f.getSize() > 4) {
						text.setFont(new Font(f.getName(), f.getStyle(), f.getSize() - 1));
					}
					e.consume();
				} else {
					text.getParent().dispatchEvent(e);
				}
			}
		});
		
		JSeparator topSeparator = new JSeparator(JSeparator.HORIZONTAL);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(engineInfo)
			.addComponent(topSeparator)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(realmLabel)
				.addGap(30)
				.addComponent(blockLabel)
				.addGap(30)
				.addComponent(portLabel)
				.addGap(30)
				.addComponent(timeLabel)
				.addGap(50)
				.addComponent(findLabel)
				.addComponent(find, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(next)
				.addComponent(prev)
				.addComponent(wrap)
			)
			.addComponent(split)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(engineInfo)
			.addComponent(topSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(realmLabel)
				.addComponent(blockLabel)
				.addComponent(portLabel)
				.addComponent(timeLabel)
				.addComponent(findLabel)
				.addComponent(find)
				.addComponent(next)
				.addComponent(prev)
				.addComponent(wrap)
			)
			.addComponent(split)
		);
		pack();
	}
	/**
	 * Create tree.
	 * @param o the value
	 */
	void createTree(Object o) {
		if (!(o instanceof XNElement)) {
			return;
		}
		final XNElement e = (XNElement)o;
		
		final StringBuilder b = new StringBuilder();
		e.toStringRep("", new HashMap<String, String>(), 
				new XNAppender() {
			@Override
			public XNAppender append(Object o) {
				b.append(o);
				return this;
			}
			@Override
			public int length() {
				return b.length();
			}
		}, new Action1<XRepresentationRecord>() {
			@Override
			public void invoke(XRepresentationRecord value) {
				switch (value.state) {
				case START_ELEMENT:
					elementStarts.put(value.element, value.charOffset);
					break;
				case END_ELEMENT:
					elementEnds.put(value.element, value.charOffset);
					break;
				case START_TEXT:
					textStarts.put(value.element, value.charOffset);
					break;
				case END_TEXT:
					textEnds.put(value.element, value.charOffset);
					break;
				default:
				}
			}
		});
		
		DefaultMutableTreeNode root = createNode(e);
		tree.setModel(new DefaultTreeModel(root));
	}
	/**
	 * The tree action to invoke when selecting a node.
	 * @author akarnokd, 2011.10.21.
	 */
	protected abstract static class TreeAction implements Action0 {
		/** @return the wrapped element. */
		public abstract XNElement element();
		/** @return is this a text node? */
		public abstract boolean isText();
	}
	/**
	 * Create a node.
	 * @param e the element
	 * @return the created node
	 */
	public DefaultMutableTreeNode createNode(final XNElement e) {
		final String env = elementValue(e);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeAction() {
			@Override
			public void invoke() {
				selectChars(elementStarts.get(e), elementEnds.get(e));
			}
			@Override
			public String toString() {
				return env;
			}
			@Override
			public XNElement element() {
				return e;
			}
			@Override
			public boolean isText() {
				return false;
			}
		});
		if (e.content != null) {
			root.add(new DefaultMutableTreeNode(new TreeAction() {
				@Override
				public void invoke() {
					selectChars(textStarts.get(e), textEnds.get(e));
				}
				@Override
				public String toString() {
					return e.content;
				}
				@Override
				public XNElement element() {
					return e;
				}
				@Override
				public boolean isText() {
					return true;
				}
			}));
		}
		buildTree(root, e.children());
		return root;
	}
	/**
	 * Build subtree.
	 * @param node the parent node
	 * @param items the items
	 */
	void buildTree(DefaultMutableTreeNode node, List<XNElement> items) {
		for (final XNElement e : items) {
			node.add(createNode(e));
		}
	}
	/**
	 * Select specific character range int the text editor.
	 * @param start the start
	 * @param end the end inclusive
	 */
	void selectChars(int start, int end) {
		text.select(start, end);
		try {
			Rectangle rect = text.modelToView(start);
			rect.width = 1;
			rect.height = 1;
			text.scrollRectToVisible(rect);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Create the element title.
	 * @param e the element
	 * @return the text
	 */
	String elementValue(XNElement e) {
		StringBuilder b = new StringBuilder(e.name);
		for (Map.Entry<XAttributeName, String> me : e.attributes().entrySet()) {
			b.append(" ").append(me.getKey().name).append("=").append(me.getValue());
		}
		return b.toString();
	}
	/**
	 * A caret which shows selection when unfocused.
	 * @author akarnokd, 2011.10.21.
	 */
	public static class WrappingCaret extends DefaultCaret {
	    /**
		 * 
		 */
		private static final long serialVersionUID = -2620981667340906382L;
		/** The original caret. */
		private DefaultCaret delegate;
		/** The focused painter. */
	    private HighlightPainter focusedSelectionPainter;
	    /** The unfocused painter. */
	    private HighlightPainter unfocusedSelectionPainter;
	    /** Is focus selection visible? */
	    private boolean focusedSelectionVisible;
	    /**
	     * Registers with the target component.
	     * @param target the text component
	     */
	    public WrappingCaret(JTextComponent target) {
	        installDelegate((DefaultCaret) target.getCaret());
	        target.setCaret(this);
	    }
	    /**
	     * Install the delegate.
	     * @param delegate the delegate
	     */
	    private void installDelegate(DefaultCaret delegate) {
	        this.delegate = delegate;
	        setBlinkRate(delegate.getBlinkRate());
	    }

	    /**
	     * Install the selecetion painters.
	     */
	    private void installSelectionPainters() {
	        if (delegate instanceof BasicCaret) {
	            installDefaultPainters();
	        } else {
	            try {
	                Method method = delegate.getClass().getDeclaredMethod(
	                        "getSelectionPainter");
	                method.setAccessible(true);
	                focusedSelectionPainter = (HighlightPainter) method
	                        .invoke(delegate);
	                Constructor<?>[] constructors = focusedSelectionPainter
	                        .getClass().getDeclaredConstructors();
	                constructors[0].setAccessible(true);
	                unfocusedSelectionPainter = (HighlightPainter) constructors[0]
	                        .newInstance(getUnfocusedSelectionColor());
	            } catch (Exception e) {
	                installDefaultPainters();
	            }
	        }
	    }
	    /** @return get the unfocused selection color. */
	    private Color getUnfocusedSelectionColor() {
	        Color first = getComponent().getSelectionColor();
	        // create a reasonable unfocusedSelectionColor
	        return new Color(first.getRed(), first.getGreen(), first.getBlue(), first.getAlpha() / 2);
	    }
	    /**
	     * Install default painters.
	     */
	    private void installDefaultPainters() {
	        focusedSelectionPainter = super.getSelectionPainter();
	        unfocusedSelectionPainter = new DefaultHighlightPainter(
	                getUnfocusedSelectionColor());
	    }

	    @Override
	    public void install(JTextComponent c) {
	        super.install(c);
	        installSelectionPainters();
	        setSelectionVisible(isSelectionVisible());
	    }

	    @Override
	    public void setSelectionVisible(boolean vis) {
	        focusedSelectionVisible = vis;
	        super.setSelectionVisible(!isSelectionVisible());
	        super.setSelectionVisible(true);
	    }

	    @Override
	    protected HighlightPainter getSelectionPainter() {
	        return focusedSelectionVisible ? focusedSelectionPainter
	                : unfocusedSelectionPainter;
	    }

	}
	/**
	 * Select the node which has the specific offset.
	 * @param offset the offset
	 */
	void selectNode(int offset) {
		for (Map.Entry<XNElement, Integer> start : textStarts.entrySet()) {
			int endIdx = textEnds.get(start.getKey());
			if (start.getValue() <= offset && offset <= endIdx) {
				findNode(start.getKey(), true);
				return;
			}
		}
		int sx = Integer.MAX_VALUE;
		int sx2 = Integer.MAX_VALUE;
		XNElement e = null;
		for (Map.Entry<XNElement, Integer> start : elementStarts.entrySet()) {
			int endIdx = elementEnds.get(start.getKey());
			if (start.getValue() <= offset && offset <= endIdx) {
				int o1 = offset - start.getValue();
				int o2 = endIdx - offset;
				if (o1 <= sx && o2 <= sx2) {
					sx = o1;
					sx2 = o2;
					e = start.getKey();
				}
			}
		}
		if (e != null) {
			findNode(e, false);
		}
	}
	/**
	 * Find the node with the given element type.
	 * @param e the element object
	 * @param isText a node representing text
	 */
	void findNode(XNElement e, boolean isText) {
		Deque<DefaultMutableTreeNode> list = Lists.newLinkedList();
		list.add((DefaultMutableTreeNode)tree.getModel().getRoot());
		while (!list.isEmpty()) {
			DefaultMutableTreeNode n = list.removeFirst();
			TreeAction ta = (TreeAction)n.getUserObject();
			if (ta.element() == e && ta.isText() == isText) {
				tree.clearSelection();
				tree.setSelectionPath(new TreePath(n.getPath()));
				return;
			}
			for (int i = 0; i < n.getChildCount(); i++) {
				list.add((DefaultMutableTreeNode)n.getChildAt(i));
			}
		}
	}
	/** Find the next. */
	void doFindNext() {
		String t = find.getText().toLowerCase();
		if (!t.isEmpty()) {
			String s = text.getText().toLowerCase();
			int pos = text.getCaretPosition();
			int idx = s.indexOf(t, pos);
			if (idx >= 0) {
				selectNode(idx);
				treeSelecting = true;
				selectChars(idx, idx + t.length());
				treeSelecting = false;
				return;
			}
			// wrap around
			idx = s.indexOf(t);
			if (idx >= 0) {
				selectNode(idx);
				treeSelecting = true;
				selectChars(idx, idx + t.length());
				treeSelecting = false;
				return;
			}
		}
	}
	/** Find the previous. */
	void doFindPrev() {
		String t = find.getText().toLowerCase();
		if (!t.isEmpty()) {
			String s = text.getText().toLowerCase();
			int pos = text.getCaretPosition() - (text.getSelectionEnd() - text.getSelectionStart()) - 1;
			if (pos >= 0) {
				int idx = s.lastIndexOf(t, pos);
				if (idx >= 0) {
					selectNode(idx);
					treeSelecting = true;
					selectChars(idx, idx + t.length());
					treeSelecting = false;
					return;
				}
			}
			// wrap around
			int idx = s.lastIndexOf(t);
			if (idx >= 0) {
				selectNode(idx);
				treeSelecting = true;
				selectChars(idx, idx + t.length());
				treeSelecting = false;
				return;
			}
		}
	}
}
