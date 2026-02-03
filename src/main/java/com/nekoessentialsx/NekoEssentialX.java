package com.nekoessentialsx;

import com.nekoessentialsx.afk.AFKManager;
import com.nekoessentialsx.catstyle.CatStyleManager;
import com.nekoessentialsx.config.ConfigRecoveryManager;
import com.nekoessentialsx.database.DatabaseManager;
import com.nekoessentialsx.dailylogin.DailyLoginManager;
import com.nekoessentialsx.economy.EconomyManager;
import com.nekoessentialsx.explosion.ExplosionProtectionManager;
import com.nekoessentialsx.explosion.ExplosionProtectionListener;
import com.nekoessentialsx.gui.GUIListener;
import com.nekoessentialsx.gui.GUIManager;
import com.nekoessentialsx.kits.KitManager;
import com.nekoessentialsx.newbiegift.NewbieGiftManager;
import com.nekoessentialsx.titles.TitleManager;
import com.nekoessentialsx.tpa.TPAManager;
import com.nekoessentialsx.warp.WarpManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoEssentialX extends JavaPlugin {
    private static NekoEssentialX instance;
    private CatStyleManager catStyleManager;
    private TitleManager titleManager;
    private DatabaseManager databaseManager;
    private TPAManager tpaManager;
    private EconomyManager economyManager;
    private GUIManager guiManager;
    private ConfigRecoveryManager configRecoveryManager;
    private NewbieGiftManager newbieGiftManager;
    private DailyLoginManager dailyLoginManager;
    private AFKManager afkManager;
    private KitManager kitManager;
    private WarpManager warpManager;
    private ExplosionProtectionManager explosionProtectionManager;
    private net.kyori.adventure.platform.bukkit.BukkitAudiences audiences;
    private Object forgeListener;
    private Object forgeEventBus;
    
    /**
     * 获取防爆保护管理器实例
     * @return 防爆保护管理器实例
     */
    public ExplosionProtectionManager getExplosionProtectionManager() {
        return explosionProtectionManager;
    }

    @Override
    public void onEnable() {
        // 设置插件实例
        instance = this;
        // 加载配置
        saveDefaultConfig();
        
        // 初始化配置恢复管理器
        configRecoveryManager = new ConfigRecoveryManager(this);
        configRecoveryManager.initialize();
        
        // 执行配置检查和恢复流程
        configRecoveryManager.runRecoveryProcess();
        
        // 检查Vault插件是否安装
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("===========================================");
            getLogger().warning("Vault插件未安装！");
            getLogger().warning("经济功能将无法使用，请安装Vault以启用经济功能");
            getLogger().warning("===========================================");
        }
        
        // 初始化数据库管理器
        databaseManager = DatabaseManager.getInstance(this);
        databaseManager.initialize();
        
        // 初始化经济管理器（Vault依赖）
        economyManager = new EconomyManager(this);
        economyManager.initialize();
        
        // 初始化猫娘风格管理器
        catStyleManager = new CatStyleManager(this);
        catStyleManager.onEnable();
        
        // 初始化头衔管理器
        titleManager = new TitleManager(this);
        titleManager.onEnable();
        
        // 初始化TPA管理器
        tpaManager = new TPAManager(this);
        
        // 初始化GUI管理器
        guiManager = new GUIManager(this);
        // 初始化GUI系统
        guiManager.initialize();
        
        // 初始化新手礼包管理器
        newbieGiftManager = new NewbieGiftManager(this);
        newbieGiftManager.onEnable();
        
        // 初始化每日登录管理器
        dailyLoginManager = new DailyLoginManager(this);
        dailyLoginManager.onEnable();
        
        // 初始化AFK管理器
        afkManager = new AFKManager(this);
        
        // 初始化工具包管理器
        kitManager = new KitManager(this);
        
        // 初始化传送点管理器
        warpManager = new WarpManager(this);
        
        // 初始化防爆保护管理器
        explosionProtectionManager = new ExplosionProtectionManager(this);
        getServer().getPluginManager().registerEvents(
                new ExplosionProtectionListener(this, explosionProtectionManager), this);
        
        // 初始化Adventure Audiences
        audiences = net.kyori.adventure.platform.bukkit.BukkitAudiences.create(this);
        
        // 注册命令执行器
        registerCommands();
        
        // 尝试注册Forge事件监听器（仅在Mohist服务器上运行）
        registerForgeEventListeners();
        
        getLogger().info("NekoEssentialX已成功加载！喵~");
    }
    
    private void registerCommands() {
        // 创建命令执行器实例
        com.nekoessentialsx.commands.Commandtitle titleCmd = new com.nekoessentialsx.commands.Commandtitle(this);
        com.nekoessentialsx.commands.Commandmoney moneyCmd = new com.nekoessentialsx.commands.Commandmoney(this);
        com.nekoessentialsx.commands.Commandhelp helpCmd = new com.nekoessentialsx.commands.Commandhelp(this);
        com.nekoessentialsx.commands.Commandinfo infoCmd = new com.nekoessentialsx.commands.Commandinfo(this);
        com.nekoessentialsx.commands.Commandlist listCmd = new com.nekoessentialsx.commands.Commandlist(this);
        com.nekoessentialsx.commands.Commandhome homeCmd = new com.nekoessentialsx.commands.Commandhome(this);
        com.nekoessentialsx.commands.Commandsethome sethomeCmd = new com.nekoessentialsx.commands.Commandsethome(this);
        com.nekoessentialsx.commands.Commanddelhome delhomeCmd = new com.nekoessentialsx.commands.Commanddelhome(this);
        com.nekoessentialsx.commands.Commandmsg msgCmd = new com.nekoessentialsx.commands.Commandmsg(this);
        com.nekoessentialsx.commands.Commandr replyCmd = new com.nekoessentialsx.commands.Commandr(this);
        com.nekoessentialsx.commands.Commandafk afkCmd = new com.nekoessentialsx.commands.Commandafk(this);
        com.nekoessentialsx.commands.Commandkit kitCmd = new com.nekoessentialsx.commands.Commandkit(this);
        com.nekoessentialsx.commands.Commandwarp warpCmd = new com.nekoessentialsx.commands.Commandwarp(this);
        com.nekoessentialsx.commands.Commandsetwarp setwarpCmd = new com.nekoessentialsx.commands.Commandsetwarp(this);
        com.nekoessentialsx.commands.Commanddelwarp delwarpCmd = new com.nekoessentialsx.commands.Commanddelwarp(this);
        com.nekoessentialsx.commands.Commandmainmenu mainmenuCmd = new com.nekoessentialsx.commands.Commandmainmenu(this);
        
        // 注册etitle命令执行器和Tab补全
        getCommand("etitle").setExecutor(titleCmd);
        getCommand("etitle").setTabCompleter(titleCmd);
        
        // 注册money命令执行器和Tab补全
        getCommand("money").setExecutor(moneyCmd);
        getCommand("money").setTabCompleter(moneyCmd);
        
        // 注册help命令执行器和Tab补全
        getCommand("help").setExecutor(helpCmd);
        getCommand("help").setTabCompleter(helpCmd);
        
        // 注册info命令执行器和Tab补全
        getCommand("info").setExecutor(infoCmd);
        getCommand("info").setTabCompleter(infoCmd);
        
        // 注册list命令执行器和Tab补全
        getCommand("list").setExecutor(listCmd);
        getCommand("list").setTabCompleter(listCmd);
        
        // 注册家系统命令执行器和Tab补全
        getCommand("home").setExecutor(homeCmd);
        getCommand("home").setTabCompleter(homeCmd);
        getCommand("sethome").setExecutor(sethomeCmd);
        getCommand("sethome").setTabCompleter(sethomeCmd);
        getCommand("delhome").setExecutor(delhomeCmd);
        getCommand("delhome").setTabCompleter(delhomeCmd);
        
        // 注册聊天系统命令执行器和Tab补全
        getCommand("msg").setExecutor(msgCmd);
        getCommand("msg").setTabCompleter(msgCmd);
        getCommand("tell").setExecutor(msgCmd);
        getCommand("tell").setTabCompleter(msgCmd);
        getCommand("whisper").setExecutor(msgCmd);
        getCommand("whisper").setTabCompleter(msgCmd);
        getCommand("w").setExecutor(msgCmd);
        getCommand("w").setTabCompleter(msgCmd);
        getCommand("r").setExecutor(replyCmd);
        getCommand("r").setTabCompleter(replyCmd);
        getCommand("reply").setExecutor(replyCmd);
        getCommand("reply").setTabCompleter(replyCmd);
        
        // 注册AFK命令执行器和Tab补全
        getCommand("afk").setExecutor(afkCmd);
        getCommand("afk").setTabCompleter(afkCmd);
        
        // 注册工具包命令执行器和Tab补全
        getCommand("kit").setExecutor(kitCmd);
        getCommand("kit").setTabCompleter(kitCmd);
        
        // 注册传送点命令执行器和Tab补全
        getCommand("warp").setExecutor(warpCmd);
        getCommand("warp").setTabCompleter(warpCmd);
        getCommand("setwarp").setExecutor(setwarpCmd);
        getCommand("setwarp").setTabCompleter(setwarpCmd);
        getCommand("delwarp").setExecutor(delwarpCmd);
        getCommand("delwarp").setTabCompleter(delwarpCmd);
        
        // 注册mainmenu命令执行器和Tab补全
        getCommand("mainmenu").setExecutor(mainmenuCmd);
        getCommand("mainmenu").setTabCompleter(mainmenuCmd);
        // 注册mainmenu的别名
        getCommand("menu").setExecutor(mainmenuCmd);
        getCommand("menu").setTabCompleter(mainmenuCmd);
        getCommand("mm").setExecutor(mainmenuCmd);
        getCommand("mm").setTabCompleter(mainmenuCmd);
        getCommand("gui").setExecutor(mainmenuCmd);
        getCommand("gui").setTabCompleter(mainmenuCmd);
        getCommand("nekomenu").setExecutor(mainmenuCmd);
        getCommand("nekomenu").setTabCompleter(mainmenuCmd);
        
        // 注册titlegui命令执行器和Tab补全
        getCommand("titlegui").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("§c呜...只有玩家才可以使用这个命令的说...喵~");
                return true;
            }
            
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            guiManager.openMainGUI(player);
            return true;
        });
        getCommand("titlegui").setTabCompleter((sender, command, alias, args) -> {
            // titlegui命令没有参数，返回空列表
            return java.util.Collections.emptyList();
        });
        
        // 注册nekoessentialx命令执行器和Tab补全
        getCommand("nekoessentialx").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                // 输出所有内置命令指南
                sender.sendMessage("§6===== NekoEssentialX 命令指南 =====");
                sender.sendMessage("§a/nekoessentialx reload §7- 重载插件配置的说~");
                sender.sendMessage("§a/nekoessentialx version §7- 查看插件版本的说~");
                sender.sendMessage("§a/nekoessentialx claimgift §7- 领取新手大礼包的说~");
                sender.sendMessage("§a/nekoessentialx checkin §7- 每日签到领取奖励的说~");
                sender.sendMessage("§a/etitle <set|list|info|give|take|clear> [player] [title] §7- 管理玩家头衔的说~");
                sender.sendMessage("§a/tpa <player> §7- 请求传送到指定玩家的说~");
                sender.sendMessage("§a/tpaccept [player|*] §7- 接受传送请求的说~");
                sender.sendMessage("§a/tpdeny [player|*] §7- 拒绝传送请求的说~");
                sender.sendMessage("§a/tpacancel [player] §7- 取消已发送的传送请求的说~");
                sender.sendMessage("§a/money <balance|pay|deposit|withdraw|give|take> [player] [amount] §7- 管理玩家经济的说~");
                sender.sendMessage("§a/titlegui §7- 打开头衔GUI界面的说~");
                sender.sendMessage("§a/help [command] §7- 显示帮助信息的说~");
                sender.sendMessage("§a/info §7- 显示服务器信息的说~");
                sender.sendMessage("§a/list §7- 显示在线玩家列表的说~");
                sender.sendMessage("§a/home [home_name] §7- 传送到家的说~");
                sender.sendMessage("§a/sethome [home_name] §7- 设置家的说~");
                sender.sendMessage("§a/delhome [home_name] §7- 删除家的说~");
                sender.sendMessage("§a/msg <player> <message> §7- 发送私信的说~");
                sender.sendMessage("§a/r <message> §7- 回复私信的说~");
                sender.sendMessage("§a/afk §7- 设置AFK状态的说~");
                sender.sendMessage("§a/kit [kit_name] §7- 领取工具包的说~");
                sender.sendMessage("§a/warp [warp_name] §7- 传送到传送点的说~");
                sender.sendMessage("§a/setwarp <warp_name> §7- 设置传送点的说~");
                sender.sendMessage("§a/delwarp <warp_name> §7- 删除传送点的说~");
                sender.sendMessage("§a/mainmenu §7- 打开NekoEssentialX主菜单的说~");
                sender.sendMessage("§6==================================");
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                sender.sendMessage("§aNekoEssentialX配置已重载的说！喵~");
                return true;
            } else if (args[0].equalsIgnoreCase("version")) {
                sender.sendMessage("§aNekoEssentialX当前版本：1.0-SNAPSHOT 的说~喵~");
                return true;
            } else if (args[0].equalsIgnoreCase("claimgift")) {
                // 处理领取新手礼包命令
                if (!(sender instanceof org.bukkit.entity.Player)) {
                    sender.sendMessage("§c呜...只有玩家才可以使用这个命令的说...喵~");
                    return true;
                }
                
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
                String playerName = player.getName();
                
                // 检查玩家是否可以领取新手礼包
                if (this.getNewbieGiftManager().canClaimGift(playerName)) {
                    // 打开新手礼包GUI
                    this.getGuiManager().openNewbieGiftGUI(player);
                } else {
                    player.sendMessage("§c呜...你已经领取过新手礼包了的说...喵~");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("checkin")) {
                // 处理每日签到命令
                if (!(sender instanceof org.bukkit.entity.Player)) {
                    sender.sendMessage("§c呜...只有玩家才可以使用这个命令的说...喵~");
                    return true;
                }
                
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
                this.getDailyLoginManager().handleDailyCheckIn(player);
                return true;
            }
            return false;
        });
        getCommand("nekoessentialx").setTabCompleter((sender, command, alias, args) -> {
            java.util.List<String> completions = new java.util.ArrayList<>();
            if (args.length == 1) {
                completions.add("reload");
                completions.add("version");
                completions.add("claimgift");
                completions.add("checkin");
            }
            return completions;
        });
        
        // 完整的TPA命令实现和Tab补全
        getCommand("tpa").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("§c呜...只有玩家才可以使用这个命令的说...喵~");
                return true;
            }
            
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            if (args.length < 1) {
                player.sendMessage("§c用法: /tpa <玩家>的说~喵~");
                return true;
            }
            
            org.bukkit.entity.Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§c呜...找不到这个玩家的说...喵~");
                return true;
            }
            
            // 使用TPA管理器发送请求
            tpaManager.sendRequest(player, target);
            return true;
        });
        getCommand("tpa").setTabCompleter((sender, command, alias, args) -> {
            java.util.List<String> completions = new java.util.ArrayList<>();
            if (args.length == 1) {
                // 补全在线玩家名
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    if (player.getName().startsWith(args[0])) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        });
        
        // tpaccept命令实现和Tab补全
        getCommand("tpaccept").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("§c呜...只有玩家才可以使用这个命令的说...喵~");
                return true;
            }
            
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            String senderName = args.length > 0 ? args[0] : null;
            
            // 使用TPA管理器接受请求
            tpaManager.acceptRequest(player, senderName);
            return true;
        });
        getCommand("tpaccept").setTabCompleter((sender, command, alias, args) -> {
            java.util.List<String> completions = new java.util.ArrayList<>();
            if (args.length == 1) {
                // 补全在线玩家名和通配符*
                completions.add("*");
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    if (player.getName().startsWith(args[0])) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        });
        
        // tpdeny命令实现和Tab补全
        getCommand("tpdeny").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("§c呜...只有玩家才可以使用这个命令的说...喵~");
                return true;
            }
            
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            String senderName = args.length > 0 ? args[0] : null;
            
            // 使用TPA管理器拒绝请求
            tpaManager.denyRequest(player, senderName);
            return true;
        });
        getCommand("tpdeny").setTabCompleter((sender, command, alias, args) -> {
            java.util.List<String> completions = new java.util.ArrayList<>();
            if (args.length == 1) {
                // 补全在线玩家名和通配符*
                completions.add("*");
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    if (player.getName().startsWith(args[0])) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        });
        
        // tpacancel命令实现和Tab补全
        getCommand("tpacancel").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("§c呜...只有玩家才可以使用这个命令的说...喵~");
                return true;
            }
            
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            String targetName = args.length > 0 ? args[0] : null;
            
            // 使用TPA管理器取消请求
            tpaManager.cancelRequest(player, targetName);
            return true;
        });
        getCommand("tpacancel").setTabCompleter((sender, command, alias, args) -> {
            java.util.List<String> completions = new java.util.ArrayList<>();
            if (args.length == 1) {
                // 补全在线玩家名
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    if (player.getName().startsWith(args[0])) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        });
    }

    @Override
    public void onDisable() {
        // 卸载Forge事件监听器
        unregisterForgeEventListeners();
        
        if (catStyleManager != null) {
            catStyleManager.onDisable();
        }
        
        if (titleManager != null) {
            titleManager.onDisable();
        }
        
        if (newbieGiftManager != null) {
            newbieGiftManager.onDisable();
        }
        
        if (dailyLoginManager != null) {
            dailyLoginManager.onDisable();
        }
        
        // 关闭Adventure Audiences
        if (audiences != null) {
            audiences.close();
        }
        
        // 关闭数据库连接
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("NekoEssentialX已成功卸载！喵~");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        
        if (catStyleManager != null) {
            catStyleManager.reload();
        }
        
        if (titleManager != null) {
            titleManager.reload();
        }
        
        if (newbieGiftManager != null) {
            newbieGiftManager.reload();
        }
        
        if (explosionProtectionManager != null) {
            explosionProtectionManager.reloadConfig();
        }
    }

    public CatStyleManager getCatStyleManager() {
        return catStyleManager;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public TPAManager getTPAManager() {
        return tpaManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    public NewbieGiftManager getNewbieGiftManager() {
        return newbieGiftManager;
    }
    
    public DailyLoginManager getDailyLoginManager() {
        return dailyLoginManager;
    }
    
    /**
     * 获取AFK管理器实例
     * @return AFK管理器实例
     */
    public AFKManager getAFKManager() {
        return afkManager;
    }
    
    /**
     * 获取工具包管理器实例
     * @return 工具包管理器实例
     */
    public KitManager getKitManager() {
        return kitManager;
    }
    
    /**
     * 获取传送点管理器实例
     * @return 传送点管理器实例
     */
    public WarpManager getWarpManager() {
        return warpManager;
    }
    
    /**
     * 获取Adventure Audiences实例
     * @return Adventure Audiences实例
     */
    public net.kyori.adventure.platform.bukkit.BukkitAudiences getAudiences() {
        return audiences;
    }
    
    /**
     * 使用反射注册Forge事件监听器
     */
    private void registerForgeEventListeners() {
        try {
            // 检查是否是Mohist服务器
            Class.forName("com.mohistmc.MohistMC");
            
            // 检查Forge是否可用
            Class.forName("net.minecraftforge.common.MinecraftForge");
            Class.forName("net.minecraftforge.eventbus.api.IEventBus");
            Class.forName("net.minecraftforge.event.CommandEvent");
            
            // 加载MohistAPI类
            Class<?> mohistApiClass = Class.forName("com.mohistmc.api.MohistAPI");
            Class<?> eventBusClass = Class.forName("net.minecraftforge.eventbus.api.IEventBus");
            
            // 获取MinecraftForge类和事件总线
            Class<?> minecraftForgeClass = Class.forName("net.minecraftforge.common.MinecraftForge");
            java.lang.reflect.Method getEventBusMethod = minecraftForgeClass.getMethod("getEventBus");
            forgeEventBus = getEventBusMethod.invoke(null);
            
            // 创建ForgeCommandListener实例
            Class<?> forgeListenerClass = Class.forName("com.nekoessentialsx.mohist.ForgeCommandListener");
            java.lang.reflect.Constructor<?> constructor = forgeListenerClass.getConstructor(NekoEssentialX.class);
            forgeListener = constructor.newInstance(this);
            
            // 注册事件监听器
            java.lang.reflect.Method addListenerMethod = eventBusClass.getMethod("register", Object.class);
            addListenerMethod.invoke(forgeEventBus, forgeListener);
            
            getLogger().info("成功注册Forge事件监听器！喵~");
        } catch (ClassNotFoundException e) {
            // Mohist服务器未启用Forge，跳过注册
            getLogger().info("检测到Mohist服务器但未启用Forge，Forge事件监听器将被禁用！喵~");
        } catch (Exception e) {
            // 其他注册失败情况
            getLogger().info("Forge事件监听器注册失败：" + e.getMessage() + "，将被禁用！喵~");
        }
    }
    
    /**
     * 卸载Forge事件监听器
     */
    private void unregisterForgeEventListeners() {
        if (forgeEventBus != null && forgeListener != null) {
            try {
                Class<?> eventBusClass = Class.forName("net.minecraftforge.eventbus.api.IEventBus");
                java.lang.reflect.Method removeListenerMethod = eventBusClass.getMethod("unregister", Object.class);
                removeListenerMethod.invoke(forgeEventBus, forgeListener);
                getLogger().info("成功卸载Forge事件监听器！喵~");
            } catch (Exception e) {
                getLogger().warning("卸载Forge事件监听器失败：" + e.getMessage());
            }
        }
    }
}