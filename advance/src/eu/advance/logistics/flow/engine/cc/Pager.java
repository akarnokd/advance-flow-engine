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

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Func1;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.core.Identifiable;

/**
 * Displays a combobox with items where the user can navigate via buttons.
 * @author akarnokd, 2011.10.11.
 * @param <T> the element type
 */
public class Pager<T extends Identifiable<?>> extends JPanel {
	/** */
	private static final long serialVersionUID = 6394893026674959221L;
	/** The items to display. */
	protected final List<T> items = Lists.newArrayList();
	/** A function to get an item name. */
	protected Func1<? super T, String> onItemName;
	/** The action to invoke when the selection changes. */
	protected Action1<? super T> onSelect;
	/** Go to first element. */
	protected JButton first;
	/** Go back one element. */
	protected JButton back;
	/** Go to next element. */
	protected JButton next;
	/** Go to last element. */
	protected JButton last;
	/** The combobox with options. */
	protected JComboBox options;
	/** Constructs the GUI. */
	public Pager() {
		options = new JComboBox();
		options.addActionListener(GUIUtils.createFromMethod(this, "doSelect"));
		first = createButton("First24", GUIUtils.createFromMethod(this, "doFirst"));
		back = createButton("Back24", GUIUtils.createFromMethod(this, "doBack"));
		next = createButton("Forward24", GUIUtils.createFromMethod(this, "doNext"));
		last = createButton("Last24", GUIUtils.createFromMethod(this, "doLast"));
		
		int h = first.getPreferredSize().height;
		
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(first, h, h, h)
			.addComponent(back, h, h, h)
			.addComponent(options)
			.addComponent(next, h, h, h)
			.addComponent(last, h, h, h)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(first)
			.addComponent(back)
			.addComponent(options, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(next)
			.addComponent(last)
		);
	}
	/**
	 * Create a button with the given named image and given action.
	 * @param name the button name
	 * @param action the action to invoke
	 * @return the button
	 */
	private JButton createButton(String name, ActionListener action) {
		JButton button = new JButton();
		button.setIcon(new ImageIcon(getClass().getResource(name + ".gif")));
		button.setDisabledIcon(new ImageIcon(getClass().getResource(name + "_Disabled.gif")));
		button.addActionListener(action);
		return button;
	}
	/** Adjust the navigation buttons based on the current combobox location. */
	protected void adjustButtons() {
		first.setEnabled(options.getSelectedIndex() > 0);
		back.setEnabled(options.getSelectedIndex() > 0);
		next.setEnabled(options.getSelectedIndex() < items.size() - 1);
		last.setEnabled(options.getSelectedIndex() < items.size() - 1);
	}
	/**
	 * Set the items.
	 * @param newItems the new items
	 */
	public void setItems(Iterable<? extends T> newItems) {
		items.clear();
		Iterables.addAll(items, newItems);
		String[] optionItems = new String[items.size()];
		int i = 0;
		for (T t : newItems) {
			optionItems[i] = onItemName.invoke(t);
			i++;
		}
		options.setModel(new DefaultComboBoxModel(optionItems));
		adjustButtons();
	}
	/** Go to the first element. */
	void doFirst() {
		options.setSelectedIndex(0);
		adjustButtons();
	}
	/** Go to the previous element. */
	void doBack() {
		options.setSelectedIndex(options.getSelectedIndex() - 1);
		adjustButtons();
	}
	/** Go to the next element. */
	void doNext() {
		options.setSelectedIndex(options.getSelectedIndex() + 1);
		adjustButtons();
	}
	/** Go to the last element. */
	void doLast() {
		options.setSelectedIndex(items.size() - 1);
		adjustButtons();
	}
	/**
	 * If the selection changed.
	 */
	void doSelect() {
		if (onSelect != null) {
			int idx = options.getSelectedIndex();
			onSelect.invoke(idx < 0 ? null : items.get(idx));
		}
		adjustButtons();
	}
	/**
	 * Set the item name function.
	 * @param onItemName the function
	 */
	void setItemName(@NonNull Func1<? super T, String> onItemName) {
		this.onItemName = onItemName;
	}
	/**
	 * The action to invoke when an item is selected.
	 * @param onSelect the action
	 */
	void setSelect(@NonNull Action1<? super T> onSelect) {
		this.onSelect = onSelect;
	}
	/**
	 * @return the currently selected index
	 */
	public int getSelectedIndex() {
		return options.getSelectedIndex();
	}
	/** @return the currently selected object or null. */
	@Nullable
	public T getSelectedItem() {
		int idx = options.getSelectedIndex();
		return idx < 0 ? null : items.get(idx);
	}
	/**
	 * Set the selected item index.
	 * @param index the index
	 */
	public void setSelectedIndex(int index) {
		options.setSelectedIndex(index);
	}
	/**
	 * Set the selection based on the supplied item.
	 * @param item the item
	 */
	public void setSelectedItem(T item) {
		int idx = 0;
		for (T e : items) {
			if (e.id().equals(item.id())) {
				options.setSelectedIndex(idx);
				return;
			}
			idx++;
		}
		options.setSelectedIndex(-1);
	}
	/** @return the list of items. */
	public List<T> getItems() {
		return Lists.newArrayList(items);
	}
	@Override
	public void setEnabled(boolean enabled) {
		first.setEnabled(enabled);
		back.setEnabled(enabled);
		options.setEnabled(enabled);
		next.setEnabled(enabled);
		last.setEnabled(enabled);
		if (enabled) {
			adjustButtons();
		}
	}
}
