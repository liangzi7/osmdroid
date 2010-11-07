// Created by plusminus on 21:31:36 - 25.09.2008
package org.andnav.osm.tileprovider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;

import org.andnav.osm.views.util.IMapTileFilenameProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OpenStreetMapTileDownloader loads tiles from a server and passes them to
 * a OpenStreetMapTileFilesystemProvider.
 * 
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 * 
 */
public class OpenStreetMapTileDownloader extends OpenStreetMapAsyncTileProvider {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory
			.getLogger(OpenStreetMapTileDownloader.class);

	// ===========================================================
	// Fields
	// ===========================================================

	private String mCloudmadeToken;

	private IOpenStreetMapTileProviderCloudmadeTokenCallback mCallback;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileDownloader(
			final IOpenStreetMapTileProviderCloudmadeTokenCallback pCallback,
			IMapTileFilenameProvider pMapTileFilenameProvider) {
		super(NUMBER_OF_TILE_DOWNLOAD_THREADS,
				TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE, pMapTileFilenameProvider);
		mCallback = pCallback;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean getShouldTilesBeSavedInCache() {
		return true;
	}

	@Override
	public boolean getUsesDataConnection() {
		return true;
	}

	@Override
	protected String threadGroupName() {
		return "downloader";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	};

	// ===========================================================
	// Methods
	// ===========================================================

	private String buildURL(final OpenStreetMapTile tile)
			throws CloudmadeException {
		return tile.getRenderer().getTileURLString(tile, mCallback);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class TileLoader extends OpenStreetMapAsyncTileProvider.TileLoader {

		@Override
		public void loadTile(final OpenStreetMapTileRequestState aState)
				throws CantContinueException {

			InputStream in = null;
			OutputStream out = null;
			OpenStreetMapTile tile = aState.getMapTile();

			try {
				final String tileURLString = buildURL(tile);

				if (DEBUGMODE)
					logger.debug("Downloading Maptile from url: "
							+ tileURLString);

				in = new BufferedInputStream(new URL(tileURLString)
						.openStream(), StreamUtils.IO_BUFFER_SIZE);

				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream,
						StreamUtils.IO_BUFFER_SIZE);
				StreamUtils.copy(in, out);
				out.flush();

				final byte[] data = dataStream.toByteArray();
				tileLoaded(aState, data);
				return;
			} catch (final UnknownHostException e) {
				// no network connection so empty the queue
				logger.warn("UnknownHostException downloading MapTile: " + tile
						+ " : " + e);
				throw new CantContinueException(e);
			} catch (final FileNotFoundException e) {
				logger.warn("Tile not found: " + tile + " : " + e);
			} catch (final IOException e) {
				logger.warn("IOException downloading MapTile: " + tile + " : "
						+ e);
			} catch (final CloudmadeException e) {
				logger.warn("CloudmadeException downloading MapTile: " + tile
						+ " : " + e);
			} catch (final Throwable e) {
				logger.error("Error downloading MapTile: " + tile, e);
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
			}

			/*
			 * Don't immediately send the tile back. If we're moving, and the
			 * internet is a bit patchy, then by the time the download has
			 * finished we don't need this tile any more. If we still do need it
			 * then the file system provider will get it again next time it's
			 * needed. That should be immediately because the view is redrawn
			 * when it receives this completion event.
			 */
			// tileLoaded(aTile, true);
			tileLoadedFailed(aState);
		}
	}
}