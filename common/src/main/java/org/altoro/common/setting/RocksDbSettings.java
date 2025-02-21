package org.altoro.common.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RocksDbSettings {

  @Setter
  @Getter
  private static org.altoro.common.setting.RocksDbSettings rocksDbSettings;

  @Getter
  private int levelNumber;
  @Getter
  private int maxOpenFiles;
  @Getter
  private int compactThreads;
  @Getter
  private long blockSize;
  @Getter
  private long maxBytesForLevelBase;
  @Getter
  private double maxBytesForLevelMultiplier;
  @Getter
  private int level0FileNumCompactionTrigger;
  @Getter
  private long targetFileSizeBase;
  @Getter
  private int targetFileSizeMultiplier;
  @Getter
  private boolean enableStatistics;

  private RocksDbSettings() {

  }

  public static org.altoro.common.setting.RocksDbSettings getDefaultSettings() {
    org.altoro.common.setting.RocksDbSettings defaultSettings = new org.altoro.common.setting.RocksDbSettings();
    return defaultSettings.withLevelNumber(7).withBlockSize(64).withCompactThreads(32)
        .withTargetFileSizeBase(256).withMaxBytesForLevelMultiplier(10)
        .withTargetFileSizeMultiplier(1)
        .withMaxBytesForLevelBase(256).withMaxOpenFiles(-1).withEnableStatistics(false);
  }

  public static org.altoro.common.setting.RocksDbSettings getSettings() {
    if (rocksDbSettings == null) {
      return getDefaultSettings();
    }
    return rocksDbSettings;
  }

  public static org.altoro.common.setting.RocksDbSettings initCustomSettings(int levelNumber, int compactThreads,
                                                                           int blockSize, long maxBytesForLevelBase,
                                                                           double maxBytesForLevelMultiplier, int level0FileNumCompactionTrigger,
                                                                           long targetFileSizeBase,
                                                                           int targetFileSizeMultiplier) {
    rocksDbSettings = new org.altoro.common.setting.RocksDbSettings()
        .withMaxOpenFiles(-1)
        .withEnableStatistics(false)
        .withLevelNumber(levelNumber)
        .withCompactThreads(compactThreads)
        .withBlockSize(blockSize)
        .withMaxBytesForLevelBase(maxBytesForLevelBase)
        .withMaxBytesForLevelMultiplier(maxBytesForLevelMultiplier)
        .withLevel0FileNumCompactionTrigger(level0FileNumCompactionTrigger)
        .withTargetFileSizeBase(targetFileSizeBase)
        .withTargetFileSizeMultiplier(targetFileSizeMultiplier);
    return rocksDbSettings;
  }

  public static void loggingSettings() {
    logger.info(String.format(
        "level number: %d, CompactThreads: %d, Blocksize: %d, maxBytesForLevelBase: %d,"
            + " withMaxBytesForLevelMultiplier: %f, level0FileNumCompactionTrigger: %d, "
            + "withTargetFileSizeBase: %d, withTargetFileSizeMultiplier: %d",
        rocksDbSettings.getLevelNumber(),
        rocksDbSettings.getCompactThreads(), rocksDbSettings.getBlockSize(),
        rocksDbSettings.getMaxBytesForLevelBase(),
        rocksDbSettings.getMaxBytesForLevelMultiplier(),
        rocksDbSettings.getLevel0FileNumCompactionTrigger(),
        rocksDbSettings.getTargetFileSizeBase(), rocksDbSettings.getTargetFileSizeMultiplier()));
  }

  public org.altoro.common.setting.RocksDbSettings withMaxOpenFiles(int maxOpenFiles) {
    this.maxOpenFiles = maxOpenFiles;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withCompactThreads(int compactThreads) {
    this.compactThreads = compactThreads;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withBlockSize(long blockSize) {
    this.blockSize = blockSize * 1024;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withMaxBytesForLevelBase(long maxBytesForLevelBase) {
    this.maxBytesForLevelBase = maxBytesForLevelBase * 1024 * 1024;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withMaxBytesForLevelMultiplier(double maxBytesForLevelMultiplier) {
    this.maxBytesForLevelMultiplier = maxBytesForLevelMultiplier;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withLevel0FileNumCompactionTrigger(int level0FileNumCompactionTrigger) {
    this.level0FileNumCompactionTrigger = level0FileNumCompactionTrigger;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withEnableStatistics(boolean enable) {
    this.enableStatistics = enable;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withLevelNumber(int levelNumber) {
    this.levelNumber = levelNumber;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withTargetFileSizeBase(long targetFileSizeBase) {
    this.targetFileSizeBase = targetFileSizeBase * 1024 * 1024;
    return this;
  }

  public org.altoro.common.setting.RocksDbSettings withTargetFileSizeMultiplier(int targetFileSizeMultiplier) {
    this.targetFileSizeMultiplier = targetFileSizeMultiplier;
    return this;
  }
}
