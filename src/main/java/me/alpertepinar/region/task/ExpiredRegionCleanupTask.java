package me.alpertepinar.region.task;

import me.alpertepinar.region.region.RegionManager;

public final class ExpiredRegionCleanupTask implements Runnable {

    private final RegionManager regionManager;

    public ExpiredRegionCleanupTask(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public void run() {
        regionManager.removeExpiredCacheRegions();
    }
}
