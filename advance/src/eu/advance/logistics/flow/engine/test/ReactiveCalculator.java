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

package eu.advance.logistics.flow.engine.test;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.reactive.DefaultObservable;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.reactive4java.swing.SwingObservables;

import java.awt.Container;
import java.io.Closeable;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Example reactive calculator.
 * @author akarnokd, 2011.10.06.
 */
public class ReactiveCalculator extends JFrame {
	/** */
	private static final long serialVersionUID = -4588013477615627689L;
	/** A reactive variable. */
	static class RInteger implements Observable<Integer>, Observer<Integer> {
		/** The observable helper. */
		DefaultObservable<Integer> notifier = new DefaultObservable<Integer>();
		/** The current value. */
		Integer value;
		/** @return the current value. */
		public Integer get() { return value; };
		/**
		 * Set the current value and notify observers.
		 * @param value the new value
		 */
		public void set(Integer value) {
			if (!(this.value == value || (this.value != null && this.value.equals(value)))) {
				this.value = value;
				notifier.next(value);
			}
		}
		@Override
		public Closeable register(Observer<? super Integer> o) {
			return notifier.register(o);
		}
		@Override
		public void next(Integer value) {
			set(value);
		}
		@Override
		public void finish() {

		}
		@Override
		public void error(Throwable t) {
			t.printStackTrace();
		}
	}
	/** Initialize the GUI. */
	void init() {
		setTitle("Reactive calc");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		RInteger v1 = new RInteger();
		RInteger v2 = new RInteger();
		RInteger v3 = new RInteger();
		RInteger v4 = new RInteger();
		
		JLabel l1 = new JLabel("V1:");
		final JTextField t1 = new JTextField();
		t1.setEditable(false);
		JLabel l2 = new JLabel("V2:");
		JTextField t2 = new JTextField();
		JLabel l3 = new JLabel("V3:");
		JTextField t3 = new JTextField();
		JLabel l4 = new JLabel("V4:");
		final JTextField t4 = new JTextField();
		t4.setEditable(false);
		
		Container c = getContentPane();
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(l1)
				.addComponent(l2)
				.addComponent(l3)
				.addComponent(l4)
			)
			.addGroup(
					gl.createParallelGroup()
					.addComponent(t1)
					.addComponent(t2)
					.addComponent(t3)
					.addComponent(t4)
				)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(l1)
				.addComponent(t1)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(l2)
				.addComponent(t2)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(l3)
				.addComponent(t3)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(l4)
				.addComponent(t4)
			)
		);
		
		pack();
		
		Observable<DocumentEvent> o2 = SwingObservables.ObservableDocumentListener.register(t2.getDocument());
		Observable<DocumentEvent> o3 = SwingObservables.ObservableDocumentListener.register(t3.getDocument());
		
		Func1<DocumentEvent, String> theNewText = new Func1<DocumentEvent, String>() {
			@Override
			public String invoke(DocumentEvent param1) {
				Document doc = param1.getDocument();
				try {
					return doc.getText(0, doc.getLength());
				} catch (BadLocationException ex) {
					return "";
				}
			}
		};
		Func1<String, Boolean> nonEmpty = new Func1<String, Boolean>() {
			@Override
			public Boolean invoke(String param1) {
				return !param1.isEmpty();
			}
		};
		Func1<String, Integer> parseInt = new Func1<String, Integer>() {
			@Override
			public Integer invoke(String param1) {
				return Integer.parseInt(param1);
			}
		};
		Func1<Object, Boolean> nonNull = new Func1<Object, Boolean>() {
			@Override
			public Boolean invoke(Object param1) {
				return param1 != null;
			}
		};
		
		Observable<Integer> int2 = Reactive.select(Reactive.where(Reactive.select(o2, theNewText), nonEmpty), parseInt);
		Observable<Integer> int3 = Reactive.select(Reactive.where(Reactive.select(o3, theNewText), nonEmpty), parseInt);
		
		int2.register(v2);
		int3.register(v3);
		
		Observable<Integer> v2xv3 = Reactive.where(Reactive.combineLatest(v3, v2, new Func2<Integer, Integer, Integer>() {
			@Override
			public Integer invoke(Integer param1, Integer param2) {
				if (param1 != null && param2 != null) {
					return param1 * param2;
				}
				return null;
			}
		}), nonNull);
		
		v2xv3.register(v1);
		
		Observable<Integer> v1plusv3 = Reactive.where(Reactive.combineLatest(v1, v3, new Func2<Integer, Integer, Integer>() {
			@Override
			public Integer invoke(Integer param1, Integer param2) {
				if (param1 != null && param2 != null) {
					return param1 + param2;
				}
				return null;
			}
		}), nonNull);
		
		v1plusv3.register(v4);
		
		v1.register(Reactive.toObserver(new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				t1.setText(value.toString());
			}
		}));
		v4.register(Reactive.toObserver(new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				t4.setText(value.toString());
			}
		}));
	}
	/**
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ReactiveCalculator f = new ReactiveCalculator();
				f.init();
				f.setVisible(true);
				f.setLocationRelativeTo(null);
			} 
		});

	}

}
