import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.CharsetUtil
import java.net.InetAddress
import java.net.InetSocketAddress

@ChannelHandler.Sharable
class EchoServerHandler : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val inBuf = msg as ByteBuf
        println("Server received: ${inBuf.toString(CharsetUtil.UTF_8)}")
        ctx.write(inBuf)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx?.writeAndFlush(Unpooled.EMPTY_BUFFER) // 대기중인 메시지를 원격 피어로 플러시하고 채널 종료
            ?.addListener(ChannelFutureListener.CLOSE)
    }
}

class Server(
    private val port: Int
) {

    fun start() {
        val serverHandler = EchoServerHandler()
        val group: EventLoopGroup = NioEventLoopGroup()
        val bootStrap = ServerBootstrap().apply {
            group(group)
            channel(NioServerSocketChannel::class.java)
            localAddress(InetSocketAddress(port))
            childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(serverHandler)
                }
            })
        }
        val channelFuture = bootStrap.bind().sync()
        channelFuture.channel().closeFuture().sync()
        group.shutdownGracefully().sync()
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        error("Usage: ${Server::class.simpleName} <port>")
    }
    val port = args[0].toInt()
    println("Server Start with port number $port")
    Server(port).start()
}
