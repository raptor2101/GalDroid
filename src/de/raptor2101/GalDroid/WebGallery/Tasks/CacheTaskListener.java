package de.raptor2101.GalDroid.WebGallery.Tasks;

public interface CacheTaskListener {
  void onCacheOperationStart(int elementCount);

  void onCacheOperationProgress(int elementCount);

  void onCacheOperationDone();
}
