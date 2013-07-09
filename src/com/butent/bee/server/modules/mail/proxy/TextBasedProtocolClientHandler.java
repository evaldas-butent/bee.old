package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public abstract class TextBasedProtocolClientHandler extends SimpleChannelHandler {
  /**
   * Closes the specified channel after all queued write requests are flushed.
   */
  static void closeOnFlush(Channel ch) {
    if (ch.isConnected()) {
      ch.write("").addListener(ChannelFutureListener.CLOSE);
    }
  }

  private final MailProxy proxy;

  final BeeLogger logger = LogUtils.getLogger(getClass());

  private final Channel serverInChannel;
  private final Object trafficLock;

  public TextBasedProtocolClientHandler(Channel inboundChannel, Object tl, MailProxy proxy) {
    serverInChannel = inboundChannel;
    trafficLock = tl;
    this.proxy = proxy;
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
    try {
      closeOnFlush(serverInChannel);
    } catch (Exception ex) {
      logger.warning(ex);
    }
  }

  @Override
  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) {
    synchronized (trafficLock) {
      if (e.getChannel().isWritable()) {
        serverInChannel.setReadable(true);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    logger.debug("Logged client exception", e.getCause());
    closeOnFlush(serverInChannel);
  }

  public MailProxy getProxy() {
    return proxy;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    String msg = (String) e.getMessage();

    synchronized (trafficLock) {
      serverInChannel.write(msg + "\r\n");
      // If inboundChannel is saturated, do not read until notified in
      // HexDumpProxyInboundHandler.channelInterestChanged().
      if (!serverInChannel.isWritable()) {
        e.getChannel().setReadable(false);
      }
    }
  }
}
