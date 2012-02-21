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
package eu.advance.logistics.flow.engine.block.prediction;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Closeables;
import hu.akarnokd.reactive4java.reactive.Observer;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.block.file.MappedColumn;
import eu.advance.logistics.flow.engine.comm.LocalConnection;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.runtime.ConstantPort;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Save the data into a local file.
 * Signature: LocalFileSave(trigger, localfiledatasource, string) -> boolean
 * @author szmarcell
 */
@Block(id = "ConsignmentMMSource", category = "file", scheduler = "IO", description = "Stream the columns of a memory-mapped binary consignment representation.")
public class ConsignmentMMSource extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(ConsignmentMMSource .class.getName());
    /** The base directory of the columns. */
    @Input("advance:string")
    protected static final String DIRECTORY = "directory";
    /** The collection of column name enums, see ConsignmentColumn. */
    @Input("advance:collection<advance:string>")
    protected static final String COLUMNS = "columns";
    /** The boolean trigger to open the source and start streaming. */
    @Input("advance:boolean")
    protected static final String TRIGGER = "trigger";
    /** Out. */
    @Output("advance:map<advance:string,advance:object>")
    protected static final String OUT = "out";
    /** The base directory for the columns. */
    protected AtomicReference<String> directory = new AtomicReference<String>();
    /** The list of columns enum, see ConsignmentColumn. */
    protected AtomicReference<EnumSet<ConsignmentColumn>> columns = new AtomicReference<EnumSet<ConsignmentColumn>>();
    @Override
    public Observer<Void> run() {
		observeInput(DIRECTORY, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				directory.set(resolver().getString(value));
			}
		});

		observeInput(COLUMNS, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				EnumSet<ConsignmentColumn> result = EnumSet.noneOf(ConsignmentColumn.class);
				for (XElement e : resolver().getItems(value)) {
					result.add(ConsignmentColumn.valueOf(resolver().getString(e)));
				}
				columns.set(result);
			}
		});

		if (getInput(TRIGGER) instanceof ConstantPort<?, ?>) {
			final boolean runNow = getBoolean(TRIGGER);
			if (runNow) {
				return new RunObserver() {
					@Override
					public void next(Void value) {
						addCloseable(scheduler().schedule(new Runnable() {
							@Override
							public void run() {
								invoke();
							}
						}));
					}
				};
			}
			return new RunObserver();
		}
    	observeInput(TRIGGER, new Action1<XElement>() {
    		@Override
    		public void invoke(XElement value) {
    			if (resolver().getBoolean(value)) {
    				ConsignmentMMSource.this.invoke();
    			}
    		}
    	});
    	return new RunObserver();
    }
    @Override
    protected void invoke() {
    	EnumSet<ConsignmentColumn> ccs = columns.get();
    	if (ccs == null || ccs.isEmpty()) {
    		return;
    	}
    	String dir = directory.get();
    	if (dir == null) {
    		return;
    	}
    	try {
	    	Pool<LocalConnection> lc = settings.context.pools.get(LocalConnection.class, dir);
	    	LocalConnection conn = lc.get();
	    	try {
	    		File f = conn.file();
		    	stream(ccs, f, new Action1<Map<XElement, XElement>>() {
		    		@Override
		    		public void invoke(Map<XElement, XElement> value) {
		        		dispatch(OUT, resolver().create(value));
		    		}
		    	});
	    	} finally {
	    		lc.put(conn);
	    	}
    	} catch (Exception ex) {
    		log(ex);
    	}
    }
	/**
	 * Stream the rows and columns of the given mapped columns.
	 * @param ccs the set of columns
	 * @param f the base directory 
	 * @param output the output action
	 * @throws IOException on error
	 */
	void stream(EnumSet<ConsignmentColumn> ccs, File f, Action1<Map<XElement, XElement>> output) throws IOException {
		List<MappedColumn> mcs = Lists.newArrayList();
		List<ConsignmentColumn> cts = Lists.newArrayList();
		try {
			for (ConsignmentColumn column : ccs) {
				MappedColumn mc = new MappedColumn(new File(f, "consignment_" + column + ".dat"), column.type.size);
				mcs.add(mc);
				cts.add(column);
			}
			stream(mcs, cts, output);
		} catch (IOException ex) {
			log(ex);
		} finally {
			Closeables.closeAll(mcs).close();
		}
	}
    /**
     * Stream all rows of the column list.
     * @param cols the column list
     * @param types the types
	 * @param output the output action
     */
    void stream(List<MappedColumn> cols, List<ConsignmentColumn> types, Action1<Map<XElement, XElement>> output) {
    	int rows = cols.get(0).rows();
    	List<XElement> columnNames = Lists.newArrayList();
    	for (ConsignmentColumn c : types) {
    		columnNames.add(resolver().create(c.toString()));
    	}
		Map<XElement, XElement> row = Maps.newHashMap();
    	for (int i = 0; i < rows; i++) {
    		row.clear();
    		for (int j = 0; j < cols.size(); j++) {
    			MappedColumn mm = cols.get(j);
    			XElement n = columnNames.get(j);
    			XElement v = null;
    			switch (types.get(j).type) {
    			case BYTE:
    				v = resolver().create(mm.getByte(i));
    				break;
    			case SHORT:
    				v = resolver().create(mm.getShort(i));
   				break;
    			case INT:
    				v = resolver().create(mm.getInt(i));
    				break;
    			case MINUTES:
    				v = resolver().create(mm.getInt(i) * 60000L);
    				break;
    			case DAYS:
    				v = resolver().create(mm.getInt(i) * (24L * 60 * 60 * 1000));
    				break;
    			case LONG:
    				v = resolver().create(mm.getLong(i));
    				break;
    			case GPS:
    				long ll = mm.getLong(i);
    				int x = (int)(ll & 0xFFFFFFFF);
    				int y = (int)((ll >> 32) & 0xFFFFFFFF);
    				v = resolver().create(x + ";" + y);
    				break;
    			default:
    				v = resolver().createObject();
    			}
    			row.put(n, v);
    		}
    		output.invoke(row);
    	}
    }
    /**
     * Test the block offline.
     * @param args the arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
		EnumSet<ConsignmentColumn> cols = EnumSet.of(
				ConsignmentColumn.MANIFESTED,
				ConsignmentColumn.DELIVERY_DEPOT,
				ConsignmentColumn.DELIVERY_POSTCODE,
				ConsignmentColumn.COLLECTING_DEPOT,
				ConsignmentColumn.COLLECTION_POSTCODE,
				ConsignmentColumn.FLAGS,
				ConsignmentColumn.PAYING_DEPOT,
				ConsignmentColumn.CONSIGNMENT_WEIGHT,
				ConsignmentColumn.H,
				ConsignmentColumn.Q,
				ConsignmentColumn.F
		);
    	
		Stopwatch sw = new Stopwatch();
		sw.start();
		ConsignmentMMSource block = new ConsignmentMMSource();
		block.settings = new BlockSettings<XElement, AdvanceRuntimeContext>();
		block.settings.resolver = new AdvanceData();
		
		final AtomicInteger cnt = new AtomicInteger();
		block.stream(cols, new File("c:/temp/hubs"), new Action1<Map<XElement, XElement>>() {
			@Override
			public void invoke(Map<XElement, XElement> value) {
				cnt.incrementAndGet();
			}
		});
		sw.stop();
		System.out.printf("Rows: %d, Time: %d%n", cnt.get(), sw.elapsedTime(TimeUnit.SECONDS));
	}
}