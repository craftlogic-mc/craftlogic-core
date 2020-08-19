package ru.craftlogic.mixin;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerHangWatchdog;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftAPI;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;

@Mixin(ServerHangWatchdog.class)
public abstract class MixinServerHangWatchdog implements Runnable {
    @Shadow
    @Final
    private DedicatedServer server;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private long maxTickTime;

    @Shadow protected abstract void scheduleHalt();

    /**
     * @author Radviger
     * @reason Better hang cause detection
     */
    @Override
    @Overwrite
    public void run() {
        while (this.server.isServerRunning()) {
            long serverTime = this.server.getCurrentTime();
            long currentTime = MinecraftServer.getCurrentTimeMillis();
            long tickTime = currentTime - serverTime;
            if (tickTime > this.maxTickTime) {
                LOGGER.fatal("A single server tick took {} seconds (should be max {})", String.format("%.2f", (float) tickTime / 1000.0F), String.format("%.2f", 0.05F));
                LOGGER.fatal("Considering it to be crashed, server will forcibly shutdown.");
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                ThreadInfo[] info = bean.dumpAllThreads(true, true);
                StringBuilder stacktrace = new StringBuilder();
                Error error = new Error(String.format("ServerHangWatchdog detected that a single server tick took %.2f seconds (should be max 0.05)", (float) tickTime / 1000.0F));

                for (ThreadInfo i : info) {
                    if (i.getThreadId() == this.server.getServerThread().getId()) {
                        error.setStackTrace(i.getStackTrace());
                    }

                    stacktrace.append(i);
                    stacktrace.append("\n");
                }

                CrashReport report = new CrashReport("Watching Server", error);
                this.server.addServerInfoToCrashReport(report);
                CrashReportCategory category = report.makeCategory("Thread Dump");
                category.addCrashSection("Threads", stacktrace);
                category.addCrashSection("Active Mod Container", CraftAPI.getActiveModId("unknown"));
                File file1 = new File(new File(this.server.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
                if (report.saveToFile(file1)) {
                    LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }

                this.scheduleHalt();
            }

            try {
                Thread.sleep(serverTime + this.maxTickTime - currentTime);
            } catch (InterruptedException ignored) { }
        }
    }
}
