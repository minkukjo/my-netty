import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.CharsetUtil
import java.net.InetSocketAddress

@ChannelHandler.Sharable
class EchoClientHandler: SimpleChannelInboundHandler<ByteBuf>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ByteBuf?) {
        println("Client received: ${msg?.toString(CharsetUtil.UTF_8)}")
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8))
    }
}

class Client(
    private val host: String,
    private val port: Int,
) {
    fun start() {
        val group = NioEventLoopGroup()
        val bootStrap = Bootstrap().apply {
            group(group)
            channel(NioSocketChannel::class.java)
            remoteAddress(InetSocketAddress(host,port))
            handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(EchoClientHandler())
                }
            })
        }
        val future = bootStrap.connect().sync()
        future.channel().closeFuture().sync()
        group.shutdownGracefully().sync()
    }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        error("Usage: ${Client::class.simpleName} <host> <port>")
    }
    val host = args[0]
    val port = args[1].toInt()

    Client(host, port).start()
}
