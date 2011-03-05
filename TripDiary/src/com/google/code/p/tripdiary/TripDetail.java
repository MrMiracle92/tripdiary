package com.google.code.p.tripdiary;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Holds the Metadata for a trip (Name, time etc).
 * 
 * @author Arunabha Ghosh
 * @author Ankan Mukherjee
 */
public class TripDetail implements Parcelable {
	/** The name of the trip. */
	public String name;

	/** The trip's ID. */
	public long tripId;

	/** The time the trip was started, in seconds since epoch. */
	public long createTime;

	/** Text description for the trip. */
	public String tripDescription;

	/** If true, trace route is enabled for this trip. */
	public boolean traceRouteEnabled;

	/** The location of the default thumbnail of the trip */
	public String defaultThumbnail;

	// public boolean isCurrent = false;

	public TripDetail(String name, long tripId, long createTime,
			String tripDescription, boolean traceRouteEnabled,
			String defaultThumbnail) {
		this.name = name;
		this.tripId = tripId;
		this.createTime = createTime;
		this.tripDescription = tripDescription;
		this.traceRouteEnabled = traceRouteEnabled;
		this.defaultThumbnail = defaultThumbnail;
	}

	public TripDetail() {
	}

	@Override
	public String toString() {
		return String.format("%s,%d,%d,%s,%s,%s", name, tripId, createTime,
				tripDescription, traceRouteEnabled, defaultThumbnail);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTripId() {
		return tripId;
	}

	public void setTripId(long tripId) {
		this.tripId = tripId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getTripDescription() {
		return tripDescription;
	}

	public void setTripDescription(String tripDescription) {
		this.tripDescription = tripDescription;
	}

	public boolean isTraceRouteEnabled() {
		return traceRouteEnabled;
	}

	public void setTraceRouteEnabled(boolean traceRouteEnabled) {
		this.traceRouteEnabled = traceRouteEnabled;
	}

	public String getDefaultThumbnail() {
		return defaultThumbnail;
	}

	public void setDefaultThumbnail(String defaultThumbnail) {
		this.defaultThumbnail = defaultThumbnail;
	}

	// public boolean isCurrent() {
	// return isCurrent;
	// }
	//
	// public void setCurrent(boolean isCurrent) {
	// this.isCurrent = isCurrent;
	// }

	// Parcelable impl follows

	@Override
	public int describeContents() {
		return TripDetail.class.getCanonicalName().hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(tripId);
		dest.writeLong(createTime);
		dest.writeString(name);
		dest.writeString(tripDescription);
		dest.writeString(defaultThumbnail);
		dest.writeBooleanArray(new boolean[] { traceRouteEnabled });
	}

	private TripDetail(Parcel in) {
		tripId = in.readLong();
		createTime = in.readLong();
		name = in.readString();
		tripDescription = in.readString();
		defaultThumbnail = in.readString();
		boolean bool[] = new boolean[1];
		in.readBooleanArray(bool);
		traceRouteEnabled = bool[0];
	}

	public static final Parcelable.Creator<TripDetail> CREATOR = new Parcelable.Creator<TripDetail>() {

		@Override
		public TripDetail createFromParcel(Parcel source) {
			return new TripDetail(source);
		}

		@Override
		public TripDetail[] newArray(int size) {
			return new TripDetail[size];
		}
	};
}
