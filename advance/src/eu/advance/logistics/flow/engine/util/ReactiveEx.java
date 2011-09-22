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

package eu.advance.logistics.flow.engine.util;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension methods to the Reactive4Java operators for now. They may be moved later on into
 * the library.
 * @author karnokd, 2011.06.22.
 */
public final class ReactiveEx {

	/**
	 * Utility class.
	 */
	private ReactiveEx() {
	}
	/**
	 * Combine the incoming Ts of the various observables into a single list of Ts like
	 * using Reactive.zip() on more than two sources.
	 * @param <T> the element type
	 * @param srcs the iterable of observable sources.
	 * @return the new observable
	 */
	public static <T> Observable<List<T>> combine(final List<? extends Observable<? extends T>> srcs) {
		if (srcs.size() < 1) {
			return Reactive.never();
		} else
		if (srcs.size() == 1) {
			return Reactive.select(srcs.get(0), new Func1<T, List<T>>() {
				@Override
				public List<T> invoke(T param1) {
					List<T> result = new ArrayList<T>(1);
					result.add(param1);
					return result;
				};
			});
		}
		return new Observable<List<T>>() {
			@Override
			public Closeable register(Observer<? super List<T>> observer) {
				Observable<List<T>> res0 = Reactive.zip(srcs.get(0), srcs.get(1), new Func2<T, T, List<T>>() {
					@Override
					public java.util.List<T> invoke(T param1, T param2) {
						List<T> result = new ArrayList<T>();
						result.add(param1);
						result.add(param2);
						return result;
					};
				});
				for (int i = 2; i < srcs.size(); i++) {
					res0 = Reactive.zip(res0, srcs.get(i), new Func2<List<T>, T, List<T>>() {
						@Override
						public java.util.List<T> invoke(java.util.List<T> param1, T param2) {
							param1.add(param2);
							return param1;
						};
					});
				}
				return res0.register(observer);
			}
		};
	}
	/**
	 * Combine a stream of Ts with a constant T whenever the src fires.
	 * @param <T> the element type
	 * @param src the source of Ts
	 * @param constant the constant T to combine with
	 * @return the new observer
	 */
	public static <T> Observable<List<T>> combine(Observable<? extends T> src, final T constant) {
		return Reactive.select(src, new Func1<T, List<T>>() {
			@Override
			public List<T> invoke(T param1) {
				List<T> result = new ArrayList<T>();
				result.add(param1);
				result.add(constant);
				return result;
			};
		});
	}
	/**
	 * Combine a constant T with a stream of Ts whenever the src fires.
	 * @param <T> the element type
	 * @param constant the constant T to combine with
	 * @param src the source of Ts
	 * @return the new observer
	 */
	public static <T> Observable<List<T>> combine(final T constant, Observable<? extends T> src) {
		return Reactive.select(src, new Func1<T, List<T>>() {
			@Override
			public List<T> invoke(T param1) {
				List<T> result = new ArrayList<T>();
				result.add(constant);
				result.add(param1);
				return result;
			};
		});
	}
	/** An empty closeable. */
	private static final Closeable EMPTY_CLOSEABLE = new Closeable() {
		@Override
		public void close() throws IOException {
			// NO OP
		}
	};
	/** @return an empty, no-op closeable instance. */
	public static Closeable emptyCloseable() {
		return EMPTY_CLOSEABLE;
	}
}
