package com.example.smartswitchpc.network.encoder

import com.example.smoothtransfer.network.protocol.Packet
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
 * Decoder for ByteBuf to Packet conversion
 * Format: [cmd(4 bytes)][isPath(1 byte)][totalDataLength(8 bytes)][curPos(8 bytes)][payloadLength(4 bytes)][payload]
 */
class PacketDecoder : ByteToMessageDecoder() {
    
    override fun decode(ctx: ChannelHandlerContext, inBuf: ByteBuf, out: MutableList<Any>) {
        // Check if we have enough bytes for header (4 + 1 + 8 + 8 + 4 = 25 bytes minimum)
        if (inBuf.readableBytes() < 25) {
            return
        }
        
        // Mark reader index
        inBuf.markReaderIndex()
        
        // Read header
        val cmd = inBuf.readInt()
        val isPath = inBuf.readByte() == 1.toByte()
        val totalDataLength = inBuf.readLong()
        val curPos = inBuf.readLong()
        val payloadLength = inBuf.readInt()
        
        // Check if we have enough bytes for payload
        if (inBuf.readableBytes() < payloadLength) {
            // Not enough data, reset reader index
            inBuf.resetReaderIndex()
            return
        }
        
        // Read payload
        val payload = ByteArray(payloadLength)
        inBuf.readBytes(payload)
        
        // Create and add packet to output
        val packet = Packet(cmd, isPath, totalDataLength, curPos, payload)
        out.add(packet)
    }
}


