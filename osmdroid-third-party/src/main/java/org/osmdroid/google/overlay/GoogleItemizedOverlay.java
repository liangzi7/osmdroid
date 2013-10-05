package org.osmdroid.google.overlay;

import java.util.ArrayList;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;

public class GoogleItemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public GoogleItemizedOverlay(final Drawable aDefaultMarker) {
		super(boundCenterBottom(aDefaultMarker));
	}

	public GoogleItemizedOverlay(final Drawable aDefaultMarker, final org.osmdroid.views.overlay.OverlayItem.HotspotPlace aHotspotPlace) {
		super(aHotspotPlace == org.osmdroid.views.overlay.OverlayItem.HotspotPlace.CENTER ? boundCenter(aDefaultMarker) : boundCenterBottom(aDefaultMarker));
	}

	public void addOverlay(final OverlayItem aOverlayItem) {
		mOverlays.add(aOverlayItem);
		populate();
	}

	@Override
	protected OverlayItem createItem(final int aIndex) {
		return mOverlays.get(aIndex);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void removeAllItems() {
		mOverlays.clear();
		// TODO do we need to call populate() here ???
	}
}
