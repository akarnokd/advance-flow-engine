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

package eu.advance.logistics.live.reporter.charts;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.joda.time.DateMidnight;
import org.joda.time.LocalTime;
import org.joda.time.ReadableDateTime;

import eu.advance.logistics.live.reporter.db.MLDB;
import eu.advance.logistics.live.reporter.db.PredictionDB;
import eu.advance.logistics.live.reporter.model.ArxPredictionKey;
import eu.advance.logistics.live.reporter.model.ArxPrediction;
import eu.advance.logistics.live.reporter.model.ConsignmentSummary;
import eu.advance.logistics.live.reporter.model.ItemStatus;
import eu.advance.logistics.live.reporter.model.MlPrediction;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.set.TLongSet;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.utils.trove.TroveUtils;

/**
 * Helper class to compose the hub summary.
 * @author karnokd, 2013.04.26.
 */
public final class HubDepotSummary {
	/** */
	private HubDepotSummary() { }
	/**
	 * Sets up the given summary object from the values at the
	 * give hub.
	 * @param hub the hub identifier
	 * @param when the time
	 * @param unit the aggregation unit
	 * @param sum the summary object
	 * @param cache the data cache
	 */
	public static void setHubSummary(
			long hub, 
			ReadableDateTime when,
			UOM unit,
			SumData sum,
			HubDepotDataCache cache) {
		
		TLongObjectMap<ConsignmentSummary> consignmentStatus = cache.getHubStatus(hub, when);
		
		Map<OrientStatus, Map<ItemStatus, BarData>> o = new LinkedHashMap<>();
		Map<ItemStatus, BarData> p;
		
		Aggregates global = new Aggregates();
		EnumMap<ServiceLevel, Aggregates> perLevel = new EnumMap<>(ServiceLevel.class);
		for (ServiceLevel sl : ServiceLevel.values()) {
			perLevel.put(sl, new Aggregates());
		}
		
		for (ConsignmentSummary cs : consignmentStatus.valueCollection()) {
			double cscreated = 0;
			double csscanned = 0;
			double csdeclared = 0;
			double csathub = 0;
			double cslefthub = 0;

			switch (unit) {
			case PRICEUNIT:
				cscreated = cs.priceUnit(ItemStatus.CREATED);
				csscanned = cs.priceUnit(ItemStatus.SCANNED);
				csdeclared = cs.priceUnit(ItemStatus.DECLARED);
				csathub = cs.priceUnit(ItemStatus.AT_HUB);
				cslefthub = cs.priceUnit(ItemStatus.LEFT_HUB_TODAY);
				break;
			case FLOORSPACE:
				cscreated = cs.floorspace(ItemStatus.CREATED);
				csscanned = cs.floorspace(ItemStatus.SCANNED);
				csdeclared = cs.floorspace(ItemStatus.DECLARED);
				csathub = cs.floorspace(ItemStatus.AT_HUB);
				cslefthub = cs.floorspace(ItemStatus.LEFT_HUB_TODAY);
				break;
			case ITEMCOUNT:
				cscreated = cs.itemCount(ItemStatus.CREATED);
				csscanned = cs.itemCount(ItemStatus.SCANNED);
				csdeclared = cs.itemCount(ItemStatus.DECLARED);
				csathub = cs.itemCount(ItemStatus.AT_HUB);
				cslefthub = cs.itemCount(ItemStatus.LEFT_HUB_TODAY);
				break;
			default:
			}
			
			global.created += cscreated;
			global.scanned += csscanned;
			global.declared += csdeclared;
			global.athub += csathub;
			global.lefthub += cslefthub;
			
			perLevel.get(cs.level).created += cscreated;
			perLevel.get(cs.level).scanned += csscanned;
			perLevel.get(cs.level).declared += csdeclared;
			perLevel.get(cs.level).athub += csathub;
			perLevel.get(cs.level).lefthub += cslefthub;
		}

		Map<Long, Pair<LocalTime, List<MlPrediction>>> predictions = MLDB.getMLLatestPredictions(hub, null, when.toDateTime());
		for (Pair<LocalTime, List<MlPrediction>> pds : predictions.values()) {
			for (MlPrediction pd : pds.second) {
				if (pd.unit == unit && pd.inbound && pd.dayOffset == 0) {
					if (pd.service != ServiceLevel.ALL) {
						double predicted = /* pd.current + */ pd.remaining;
						global.predicted += predicted;
						perLevel.get(pd.service).predicted += predicted;
					}
				}
			}
		}
		p = global.create();
	    o.put(OrientStatus.SINGLE, p);
	    sum.items.put(ServiceLevel.ALL, o);

	    o = new LinkedHashMap<>();
	    p = perLevel.get(ServiceLevel.STANDARD).create();
	    o.put(OrientStatus.SINGLE, p);
	    sum.items.put(ServiceLevel.STANDARD, o);
	    
	    o = new LinkedHashMap<>();
	    p = perLevel.get(ServiceLevel.PRIORITY).create();
	    o.put(OrientStatus.SINGLE, p);
	    sum.items.put(ServiceLevel.PRIORITY, o);

	    o = new LinkedHashMap<>();
	    p = perLevel.get(ServiceLevel.SPECIAL).create();
	    o.put(OrientStatus.SINGLE, p);
	    sum.items.put(ServiceLevel.SPECIAL, o);

	}
	/**
	 * Sets up the given summary object from the values at the
	 * give hub.
	 * @param hub the hub identifier
	 * @param depot the depot identifier
	 * @param when the time
	 * @param unit the aggregation unit
	 * @param sumData the summary object
	 * @param cache the data cache
	 */
	public static void setDepotSummary(
			long hub,
			long depot,
			ReadableDateTime when,
			UOM unit,
			SumData sumData,
			HubDepotDataCache cache) {
		
		TLongSet depots = TroveUtils.singleton(depot);
		TLongObjectMap<ConsignmentSummary> consignmentStatus = cache.getHubDepotStatus(hub, depots, depots, when);

		Aggregates globalSrc = new Aggregates();
		Aggregates globalDst = new Aggregates();
		EnumMap<ServiceLevel, Aggregates> perLevelSrc = new EnumMap<>(ServiceLevel.class);
		EnumMap<ServiceLevel, Aggregates> perLevelDst = new EnumMap<>(ServiceLevel.class);
		for (ServiceLevel sl : ServiceLevel.values()) {
			perLevelSrc.put(sl, new Aggregates());
			perLevelDst.put(sl, new Aggregates());
		}
		
		for (ConsignmentSummary cs : consignmentStatus.valueCollection()) {
			double cscreated = 0;
			double csscanned = 0;
			double csdeclared = 0;
			double csathub = 0;
			double cslefthub = 0;

			switch (unit) {
			case PRICEUNIT:
				cscreated = cs.priceUnit(ItemStatus.CREATED);
				csscanned = cs.priceUnit(ItemStatus.SCANNED);
				csdeclared = cs.priceUnit(ItemStatus.DECLARED);
				csathub = cs.priceUnit(ItemStatus.AT_HUB);
				cslefthub = cs.priceUnit(ItemStatus.LEFT_HUB_TODAY);
				break;
			case FLOORSPACE:
				cscreated = cs.floorspace(ItemStatus.CREATED);
				csscanned = cs.floorspace(ItemStatus.SCANNED);
				csdeclared = cs.floorspace(ItemStatus.DECLARED);
				csathub = cs.floorspace(ItemStatus.AT_HUB);
				cslefthub = cs.floorspace(ItemStatus.LEFT_HUB_TODAY);
				break;
			case ITEMCOUNT:
				cscreated = cs.itemCount(ItemStatus.CREATED);
				csscanned = cs.itemCount(ItemStatus.SCANNED);
				csdeclared = cs.itemCount(ItemStatus.DECLARED);
				csathub = cs.itemCount(ItemStatus.AT_HUB);
				cslefthub = cs.itemCount(ItemStatus.LEFT_HUB_TODAY);
				break;
			default:
			}
			
			Aggregates global = cs.collectionDepot == depot ? globalSrc : globalDst;
			EnumMap<ServiceLevel, Aggregates> perLevel = cs.collectionDepot == depot ? perLevelSrc : perLevelDst;
			
			
			global.created += cscreated;
			global.scanned += csscanned;
			global.declared += csdeclared;
			global.athub += csathub;
			global.lefthub += cslefthub;
			
			perLevel.get(cs.level).created += cscreated;
			perLevel.get(cs.level).scanned += csscanned;
			perLevel.get(cs.level).declared += csdeclared;
			perLevel.get(cs.level).athub += csathub;
			perLevel.get(cs.level).lefthub += cslefthub;
		}
		Map<Long, Pair<LocalTime, List<MlPrediction>>> predictions = MLDB.getMLLatestPredictions(hub, null, when.toDateTime());
		Pair<LocalTime, List<MlPrediction>> pds = predictions.get(depot);
		if (pds != null) {
			for (MlPrediction pd : pds.second) {
				if (pd.unit == unit && pd.dayOffset == 0) {
					if (pd.service != ServiceLevel.ALL) {
						Aggregates global = pd.inbound ? globalSrc : globalDst;
						double predicted = /* pd.current + */ pd.remaining;
						global.predicted += predicted;
						EnumMap<ServiceLevel, Aggregates> perLevel = pd.inbound ? perLevelSrc : perLevelDst;
						perLevel.get(pd.service).predicted += predicted;
					}
				}
			}
		}

		Map<OrientStatus, Map<ItemStatus, BarData>> o;
		Map<ItemStatus, BarData> p;

		o = new LinkedHashMap<>();
		p = globalSrc.create();
		o.put(OrientStatus.ORIGIN, p);

		p = globalDst.create();
		o.put(OrientStatus.DESTIN, p);
		sumData.items.put(ServiceLevel.ALL, o);

		o = new LinkedHashMap<>();
		p = perLevelSrc.get(ServiceLevel.STANDARD).create();
		o.put(OrientStatus.ORIGIN, p);

		p = perLevelDst.get(ServiceLevel.STANDARD).create();
		o.put(OrientStatus.DESTIN, p);
		sumData.items.put(ServiceLevel.STANDARD, o);

		o = new LinkedHashMap<>();
		p = perLevelSrc.get(ServiceLevel.PRIORITY).create();
		o.put(OrientStatus.ORIGIN, p);

		p = perLevelDst.get(ServiceLevel.PRIORITY).create();
		o.put(OrientStatus.DESTIN, p);
		sumData.items.put(ServiceLevel.PRIORITY, o);    

		o = new LinkedHashMap<>();
		p = perLevelSrc.get(ServiceLevel.SPECIAL).create();
		o.put(OrientStatus.ORIGIN, p);

		p = new LinkedHashMap<ItemStatus, BarData>();
		p = perLevelDst.get(ServiceLevel.SPECIAL).create();
		o.put(OrientStatus.DESTIN, p);
		sumData.items.put(ServiceLevel.SPECIAL, o);    
	}
	/**
	 * Sets the hub level prediction record.
	 * @param hubId the target hub
	 * @param fromDay the target start day
	 * @param horizon the horizon length in days
	 * @param dayBy the output record
	 */
	public static void setHubPrediction(
			long hubId, 
			DateMidnight fromDay,
			int horizon,
			DayByData dayBy) {

		EnumMap<ServiceLevel, EnumSet<ServiceLevel>> serviceToSnip = new EnumMap<>(ServiceLevel.class);
		for (ServiceLevel sl : ServiceLevel.values()) {
			serviceToSnip.put(sl, EnumSet.noneOf(ServiceLevel.class));
		}
		serviceToSnip.get(ServiceLevel.STANDARD).addAll(Arrays.asList(ServiceLevel.ALL, ServiceLevel.STANDARD));
		serviceToSnip.get(ServiceLevel.PRIORITY).addAll(Arrays.asList(ServiceLevel.ALL, ServiceLevel.PRIORITY));
		serviceToSnip.get(ServiceLevel.SPECIAL).addAll(Arrays.asList(ServiceLevel.ALL, ServiceLevel.SPECIAL));

		Map<ArxPredictionKey, List<ArxPrediction>> out = new HashMap<>();
		PredictionDB.getArxPredictions(hubId, -hubId, fromDay, horizon, out);
		
		Map<DateMidnight, TObjectDoubleMap<ServiceLevel>> dailyStatuses = new TreeMap<>();
		
		for (Map.Entry<ArxPredictionKey, List<ArxPrediction>> e : out.entrySet()) {
			if (e.getKey().unit == dayBy.unit) {
				for (ArxPrediction p : e.getValue()) {
					if (p.inbound) {
						TObjectDoubleMap<ServiceLevel> perSL = dailyStatuses.get(p.dayPredict);
						if (perSL == null) {
							perSL = new TObjectDoubleHashMap<>();
							dailyStatuses.put(p.dayPredict, perSL);
						}
						for (ServiceLevel st : serviceToSnip.get(p.service)) {
							perSL.adjustOrPutValue(st, Math.max(0, p.value), Math.max(0, p.value));
						}
					}
				}
			}
		}
		
		ServiceLevel[] sts = { ServiceLevel.ALL, ServiceLevel.SPECIAL, ServiceLevel.PRIORITY, ServiceLevel.STANDARD };
		
		for (Map.Entry<DateMidnight, TObjectDoubleMap<ServiceLevel>> e : dailyStatuses.entrySet()) {
			final Map<OrientStatus, Map<ServiceLevel, BarData>> o = new LinkedHashMap<>();
			final Map<ServiceLevel, BarData> p = new LinkedHashMap<>();
			
			for (ServiceLevel st : sts) {
				p.put(st, new BarData(e.getValue().get(st)));
			}
			
			o.put(OrientStatus.SINGLE, p);
			dayBy.items.put(new Date(e.getKey().getMillis()), o);
		}

	}
	/**
	 * Sets the hub level prediction record.
	 * @param hubId the target hub
	 * @param depot the target depot
	 * @param fromDay the target start day
	 * @param horizon the horizon length in days
	 * @param dayBy the output record
	 */
	public static void setDepotPrediction(
			long hubId, 
			long depot,
			DateMidnight fromDay,
			int horizon,
			DayByData dayBy) {

		EnumMap<ServiceLevel, EnumSet<ServiceLevel>> serviceToSnip = new EnumMap<>(ServiceLevel.class);
		for (ServiceLevel sl : ServiceLevel.values()) {
			serviceToSnip.put(sl, EnumSet.noneOf(ServiceLevel.class));
		}
		serviceToSnip.get(ServiceLevel.STANDARD).addAll(Arrays.asList(ServiceLevel.ALL, ServiceLevel.STANDARD));
		serviceToSnip.get(ServiceLevel.PRIORITY).addAll(Arrays.asList(ServiceLevel.ALL, ServiceLevel.PRIORITY));
		serviceToSnip.get(ServiceLevel.SPECIAL).addAll(Arrays.asList(ServiceLevel.ALL, ServiceLevel.SPECIAL));

		Map<ArxPredictionKey, List<ArxPrediction>> out = new HashMap<>();
		PredictionDB.getArxPredictions(hubId, depot, fromDay, horizon, out);
		
		Map<DateMidnight, TObjectDoubleMap<ServiceLevel>> dailyIn = new TreeMap<>();
		Map<DateMidnight, TObjectDoubleMap<ServiceLevel>> dailyOut = new TreeMap<>();
		
		for (Map.Entry<ArxPredictionKey, List<ArxPrediction>> e : out.entrySet()) {
			for (ArxPrediction p : e.getValue()) {
				if (p.unit == dayBy.unit) {
					Map<DateMidnight, TObjectDoubleMap<ServiceLevel>> dailyStatuses = p.inbound ? dailyIn : dailyOut;
					
					TObjectDoubleMap<ServiceLevel> perSL = dailyStatuses.get(p.dayPredict);
					if (perSL == null) {
						perSL = new TObjectDoubleHashMap<>();
						dailyStatuses.put(p.dayPredict, perSL);
					}
					for (ServiceLevel st : serviceToSnip.get(p.service)) {
						perSL.adjustOrPutValue(st, Math.max(0, p.value), Math.max(0, p.value));
					}
				}
			}
		}
		
		ServiceLevel[] sts = { ServiceLevel.ALL, ServiceLevel.SPECIAL, ServiceLevel.PRIORITY, ServiceLevel.STANDARD };
		
		TreeSet<DateMidnight> commonDates = new TreeSet<>(dailyIn.keySet());
		commonDates.addAll(dailyOut.keySet());
		for (DateMidnight day : commonDates) {
			final Map<OrientStatus, Map<ServiceLevel, BarData>> o = new LinkedHashMap<>();
			Map<ServiceLevel, BarData> p = new LinkedHashMap<>();
			
			TObjectDoubleMap<ServiceLevel> e = dailyIn.get(day);
			for (ServiceLevel st : sts) {
				if (e != null) {
					p.put(st, new BarData(e.get(st)));
				} else {
					p.put(st, new BarData(0));
				}
			}
			
			o.put(OrientStatus.ORIGIN, p);
			
			p = new LinkedHashMap<>();
			e = dailyOut.get(day);
			for (ServiceLevel st : sts) {
				if (e != null) {
					p.put(st, new BarData(e.get(st)));
				} else {
					p.put(st, new BarData(0));
				}
			}
			o.put(OrientStatus.DESTIN, p);
			
			dayBy.items.put(new Date(day.getMillis()), o);
		}

	}
}
