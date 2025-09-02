package org.mtransit.parser.ca_laurentides_linter_bus;

import static org.mtransit.commons.RegexUtils.DIGITS;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class LaurentidesLInterBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new LaurentidesLInterBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_FR;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "L'Inter (TaCL)";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@SuppressWarnings("RedundantMethodOverride")
	@Override
	public boolean useRouteShortNameForRouteId() {
		return false;
	}

	@Nullable
	@Override
	public Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "ZC":
		case "ZCN":
		case "ZCS":
			return 1_003L;
		case "ZN":
		case "ZNN":
		case "ZNS":
			return 1_014L;

		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	@Override
	public boolean useRouteIdForRouteShortName() {
		return true;
	}

	@Override
	@Nullable
	public String getRouteIdCleanupRegex() {
		return "[N|S]$";
	}

	@Override
	public boolean mergeRouteLongName(@NotNull MRoute mRoute, @NotNull MRoute mRouteToMerge) {
		if (mRoute.getId() == 1_003L) {
			mRoute.setLongName("Inter Centre");
			return true;
		} else if (mRoute.getId() == 1_014L) {
			mRoute.setLongName("Inter Nord");
			return true;
		}
		return super.mergeRouteLongName(mRoute, mRouteToMerge);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR = "E76525"; // ORANGE (from web site)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public boolean directionSplitterEnabled(long routeId) {
		return true;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.POINT.matcher(tripHeadsign).replaceAll(CleanUtils.POINT_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanBounds(Locale.FRENCH, tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(Locale.FRENCH, gStopName);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		final String stopCode = getStopCode(gStop);
		if (!stopCode.isEmpty() & CharUtils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // using stop code as stop ID
		}
		//noinspection DiscouragedApi
		final String stopId1 = gStop.getStopId();
		final Matcher matcher = DIGITS.matcher(stopCode);
		if (matcher.find()) {
			final int digits = Integer.parseInt(matcher.group());
			final int stopId;
			if (stopCode.endsWith("N")) {
				stopId = 140_000;
			} else {
				if (stopId1.endsWith("S")) {
					stopId = 190_000;
				} else if (stopId1.endsWith("S-ANM")) {
					stopId = 1_190_000;
				} else if (stopId1.endsWith("ANM")) {
					stopId = 1_140_000;
				} else {
					throw new MTLog.Fatal("Stop doesn't have an ID (end with) %s!", gStop);
				}
			}
			return stopId + digits;
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	}
}
