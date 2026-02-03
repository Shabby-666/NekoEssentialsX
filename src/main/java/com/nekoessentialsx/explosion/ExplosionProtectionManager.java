package com.nekoessentialsx.explosion;

import com.nekoessentialsx.NekoEssentialX;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class ExplosionProtectionManager {
    private final NekoEssentialX plugin;
    private boolean enabled;
    private ExplosionConfig entityExplosionConfig;
    private ExplosionConfig tntExplosionConfig;
    private ExplosionConfig endCrystalExplosionConfig;
    private ExplosionConfig bedExplosionConfig;
    private ExplosionConfig otherExplosionConfig;
    private EntityBlockBreakConfig entityBlockBreakConfig;
    private LoggingConfig loggingConfig;

    public ExplosionProtectionManager(NekoEssentialX plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * 加载配置
     */
    public void loadConfig() {
        plugin.reloadConfig();
        org.bukkit.configuration.ConfigurationSection config = plugin.getConfig().getConfigurationSection("anti-explosion");

        // 主开关
        this.enabled = plugin.getConfig().getBoolean("anti-explosion.enabled", true);

        // 生物爆炸配置
        ConfigurationSection entityExplosionSection = plugin.getConfig().getConfigurationSection("anti-explosion.entity-explosion");
        this.entityExplosionConfig = new ExplosionConfig(entityExplosionSection);

        // 实体破坏方块配置
        ConfigurationSection entityBlockBreakSection = plugin.getConfig().getConfigurationSection("anti-explosion.entity-block-break");
        this.entityBlockBreakConfig = new EntityBlockBreakConfig(entityBlockBreakSection);

        // TNT爆炸配置
        ConfigurationSection tntExplosionSection = plugin.getConfig().getConfigurationSection("anti-explosion.tnt-explosion");
        this.tntExplosionConfig = new ExplosionConfig(tntExplosionSection);

        // 末影水晶爆炸配置
        ConfigurationSection endCrystalExplosionSection = plugin.getConfig().getConfigurationSection("anti-explosion.end-crystal-explosion");
        this.endCrystalExplosionConfig = new ExplosionConfig(endCrystalExplosionSection);

        // 床爆炸配置
        ConfigurationSection bedExplosionSection = plugin.getConfig().getConfigurationSection("anti-explosion.bed-explosion");
        this.bedExplosionConfig = new ExplosionConfig(bedExplosionSection);

        // 其他爆炸配置
        ConfigurationSection otherExplosionSection = plugin.getConfig().getConfigurationSection("anti-explosion.other-explosion");
        this.otherExplosionConfig = new ExplosionConfig(otherExplosionSection);

        // 日志配置
        ConfigurationSection loggingSection = plugin.getConfig().getConfigurationSection("anti-explosion.logging");
        this.loggingConfig = new LoggingConfig(loggingSection);

        plugin.getLogger().info("防爆系统配置已加载！喵~");
    }

    /**
     * 热加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * 检查防爆系统是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取生物爆炸配置
     */
    public ExplosionConfig getEntityExplosionConfig() {
        return entityExplosionConfig;
    }

    /**
     * 获取TNT爆炸配置
     */
    public ExplosionConfig getTntExplosionConfig() {
        return tntExplosionConfig;
    }

    /**
     * 获取末影水晶爆炸配置
     */
    public ExplosionConfig getEndCrystalExplosionConfig() {
        return endCrystalExplosionConfig;
    }

    /**
     * 获取床爆炸配置
     */
    public ExplosionConfig getBedExplosionConfig() {
        return bedExplosionConfig;
    }

    /**
     * 获取其他爆炸配置
     */
    public ExplosionConfig getOtherExplosionConfig() {
        return otherExplosionConfig;
    }

    /**
     * 获取实体破坏方块配置
     */
    public EntityBlockBreakConfig getEntityBlockBreakConfig() {
        return entityBlockBreakConfig;
    }

    /**
     * 获取日志配置
     */
    public LoggingConfig getLoggingConfig() {
        return loggingConfig;
    }

    /**
     * 记录爆炸日志
     */
    public void logExplosion(String explosionType, String eventType, boolean blocked, String details) {
        if (!loggingConfig.isEnabled()) {
            return;
        }

        if (blocked && !loggingConfig.isLogBlocked()) {
            return;
        }

        if (!blocked && !loggingConfig.isLogAllowed()) {
            return;
        }

        String message = String.format("[防爆系统] 类型: %s, 事件: %s, 处理结果: %s, 详情: %s",
                explosionType, eventType, blocked ? "拦截" : "允许", details);

        Level level = loggingConfig.isDetailed() ? Level.INFO : Level.WARNING;
        plugin.getLogger().log(level, message);
    }

    /**
     * 爆炸配置类
     */
    public static class ExplosionConfig {
        private boolean enabled;
        private List<String> types;
        private boolean breakBlocks;
        private boolean damageEntities;
        private double maxRadius;
        private double powerMultiplier;

        public ExplosionConfig(ConfigurationSection section) {
            if (section == null) {
                this.enabled = false;
                this.types = List.of();
                this.breakBlocks = false;
                this.damageEntities = true;
                this.maxRadius = 0.0;
                this.powerMultiplier = 1.0;
                return;
            }

            this.enabled = section.getBoolean("enabled", true);
            this.types = section.getStringList("types");
            this.breakBlocks = section.getBoolean("break-blocks", false);
            this.damageEntities = section.getBoolean("damage-entities", true);
            this.maxRadius = section.getDouble("max-radius", 0.0);
            this.powerMultiplier = section.getDouble("power-multiplier", 1.0);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public List<String> getTypes() {
            return types;
        }

        public boolean isBreakBlocks() {
            return breakBlocks;
        }

        public boolean isDamageEntities() {
            return damageEntities;
        }

        public double getMaxRadius() {
            return maxRadius;
        }

        public double getPowerMultiplier() {
            return powerMultiplier;
        }
    }

    /**
     * 日志配置类
     */
    public static class LoggingConfig {
        private boolean enabled;
        private String level;
        private boolean detailed;
        private boolean logBlocked;
        private boolean logAllowed;

        public LoggingConfig(ConfigurationSection section) {
            if (section == null) {
                this.enabled = true;
                this.level = "INFO";
                this.detailed = true;
                this.logBlocked = true;
                this.logAllowed = false;
                return;
            }

            this.enabled = section.getBoolean("enabled", true);
            this.level = section.getString("level", "INFO");
            this.detailed = section.getBoolean("detailed", true);
            this.logBlocked = section.getBoolean("log-blocked", true);
            this.logAllowed = section.getBoolean("log-allowed", false);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getLevel() {
            return level;
        }

        public boolean isDetailed() {
            return detailed;
        }

        public boolean isLogBlocked() {
            return logBlocked;
        }

        public boolean isLogAllowed() {
            return logAllowed;
        }
    }

    /**
     * 实体破坏方块配置类
     */
    public static class EntityBlockBreakConfig {
        private boolean enabled;
        private List<String> types;
        private boolean allowBreak;
        private List<String> allowedBlocks;
        private List<String> blockedBlocks;
        private double maxRange;
        private boolean applyToAllEntities;

        public EntityBlockBreakConfig(ConfigurationSection section) {
            if (section == null) {
                // 默认配置：禁止所有生物破坏所有方块
                this.enabled = true;
                this.types = List.of();
                this.allowBreak = false;
                this.allowedBlocks = List.of();
                this.blockedBlocks = List.of();
                this.maxRange = 0.0;
                this.applyToAllEntities = true;
                return;
            }

            this.enabled = section.getBoolean("enabled", true);
            this.types = section.getStringList("types");
            this.allowBreak = section.getBoolean("allow-break", false);
            this.allowedBlocks = section.getStringList("allowed-blocks");
            this.blockedBlocks = section.getStringList("blocked-blocks");
            this.maxRange = section.getDouble("max-range", 0.0);
            this.applyToAllEntities = section.getBoolean("apply-to-all-entities", true);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public List<String> getTypes() {
            return types;
        }

        public boolean isAllowBreak() {
            return allowBreak;
        }

        public List<String> getAllowedBlocks() {
            return allowedBlocks;
        }

        public List<String> getBlockedBlocks() {
            return blockedBlocks;
        }

        public double getMaxRange() {
            return maxRange;
        }

        public boolean isApplyToAllEntities() {
            return applyToAllEntities;
        }
    }
}