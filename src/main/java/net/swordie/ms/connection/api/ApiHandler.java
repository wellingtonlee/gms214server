package net.swordie.ms.connection.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.swordie.ms.Server;
import net.swordie.ms.client.Client;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.InPacket;
import net.swordie.ms.handlers.ApiRequestHandler;
import net.swordie.ms.handlers.header.InHeader;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import static net.swordie.ms.connection.netty.NettyClient.CLIENT_KEY;

/**
 * @author Sjonnie
 * Created on 10/5/2018.
 */
public class ApiHandler extends SimpleChannelInboundHandler<InPacket> {

    private static final Logger log = Logger.getLogger(ApiHandler.class);
    private static final ConcurrentHashMap<String, long[]> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private static final long WINDOW_MS = 60_000; // 1 minute

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InPacket inPacket) throws Exception {
        // IP-based rate limiting
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        long now = System.currentTimeMillis();
        long[] window = rateLimitMap.compute(ip, (k, v) -> {
            if (v == null || now - v[1] > WINDOW_MS) {
                return new long[]{1, now};
            }
            v[0]++;
            return v;
        });
        if (window[0] > MAX_REQUESTS_PER_WINDOW) {
            log.warn("Rate limit exceeded for IP: " + ip);
            ctx.close();
            return;
        }

        Client c = (Client) ctx.channel().attr(CLIENT_KEY).get();
        short op = inPacket.decodeShort();
        ApiInHeader opHeader = ApiInHeader.getByVal(op);
        if(opHeader == null) {
            handleUnknown(inPacket, op);
            return;
        }
        if(!InHeader.isSpamHeader(InHeader.getInHeaderByOp(op))) {
            log.debug(String.format("[API In]\t| %s, %d/0x%s\t| %s", InHeader.getInHeaderByOp(op), op, Integer.toHexString(op).toUpperCase(), inPacket));
        }
        switch (opHeader) {
            case REQUEST_TOKEN:
                ApiRequestHandler.handleTokenRequest(c, inPacket);
                break;
            case CREATE_ACCOUNT_REQUEST:
                ApiRequestHandler.handleCreateAccountRequest(c, inPacket);
                break;
        }
    }

    private void handleUnknown(InPacket inPacket, short op) {
        log.error("Unknown API request " + op);
    }
}
