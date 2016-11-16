package com.jiabo.letty.servlet.container;

import javax.servlet.http.HttpServletRequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {
	private static final Logger log = LoggerFactory
			.getLogger(HttpServerInboundHandler.class);
	private HttpRequest request;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;
		}
		if (msg instanceof HttpContent) {
			HttpServletRequest request=new LettyHttpServletRequestWrapper(request)
			HttpContent content = (HttpContent) msg;
			ByteBuf buf = content.content();
			String text = buf.toString(io.netty.util.CharsetUtil.UTF_8);
			buf.release();
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(cause.getMessage());
		ctx.close();
	}

}
