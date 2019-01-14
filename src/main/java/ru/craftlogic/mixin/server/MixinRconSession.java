package ru.craftlogic.mixin.server;

import net.minecraft.network.rcon.IServer;
import net.minecraft.network.rcon.RConThreadBase;
import net.minecraft.network.rcon.RConThreadClient;
import net.minecraft.network.rcon.RConUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

@Mixin(RConThreadClient.class)
public abstract class MixinRconSession extends RConThreadBase {
    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private byte[] buffer;

    @Shadow @Final private String rconPassword;

    @Shadow private Socket clientSocket;

    @Shadow private boolean loggedIn;

    @Shadow protected abstract void sendResponse(int code, int action, String message) throws IOException;

    @Shadow protected abstract void sendLoginFailedResponse() throws IOException;

    @Shadow protected abstract void sendMultipacketResponse(int code, String message) throws IOException;

    @Shadow protected abstract void closeSocket();

    protected MixinRconSession(IServer server, String name) {
        super(server, name);
    }

    /**
     * @author Radviger
     * @reason RCon commands debug
     */
    @Overwrite
    public void run() {
        while (this.running && this.clientSocket != null && !this.clientSocket.isClosed()) {
            try {
                BufferedInputStream is = new BufferedInputStream(this.clientSocket.getInputStream());
                int realLength = is.read(this.buffer, 0, 1460);
                if (10 > realLength) {
                    break;
                }

                int expectedLength = RConUtils.getBytesAsLEInt(this.buffer, 0, realLength);
                
                if (expectedLength == realLength - 4) {
                    int offset = 4;
                    int code = RConUtils.getBytesAsLEInt(this.buffer, offset, realLength);
                    offset += 4;
                    int action = RConUtils.getRemainingBytesAsLEInt(this.buffer, offset);
                    offset += 4;
                    switch(action) {
                        case 2:
                            if (this.loggedIn) {
                                String command = RConUtils.getBytesAsString(this.buffer, offset, realLength);

                                try {
                                    LOGGER.info("Executing command: " + command);
                                    String result = this.server.handleRConCommand(command);
                                    LOGGER.info("Result: " + result);
                                    this.sendMultipacketResponse(code, result);
                                } catch (Exception exc) {
                                    this.sendMultipacketResponse(code, "Error executing: " + command + " (" + exc.getMessage() + ")");
                                }
                            } else {
                                this.sendLoginFailedResponse();
                            }
                            break;
                        case 3:
                            String password = RConUtils.getBytesAsString(this.buffer, offset, realLength);
                            Objects.requireNonNull(password, "received password is null");
                            if (!password.isEmpty() && password.equals(this.rconPassword)) {
                                this.loggedIn = true;
                                this.sendResponse(code, 2, "");
                                LOGGER.info("Login successful");
                            } else {
                                LOGGER.info("Login failure");
                                this.loggedIn = false;
                                this.sendLoginFailedResponse();
                            }
                            break;
                        default:
                            LOGGER.info("Unknown request: " + action);
                            this.sendMultipacketResponse(code, String.format("Unknown request %s", Integer.toHexString(action)));
                    }
                }
            } catch (IOException exc) {
                return;
            } catch (Exception exc) {
                LOGGER.error("Exception whilst parsing RCON input", exc);
                return;
            }
        }
        LOGGER.info("Closing socket");
        this.closeSocket();
    }
}
