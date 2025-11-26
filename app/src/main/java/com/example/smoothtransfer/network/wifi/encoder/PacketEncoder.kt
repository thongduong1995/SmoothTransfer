package com.example.smoothtransfer.network.wifi.encoder

import com.example.smoothtransfer.network.wifi.protocol.Packet
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * Encoder for Packet to ByteBuf conversion
 * Format: [cmd(4 bytes)][isPath(1 byte)][totalDataLength(8 bytes)][curPos(8 bytes)][payloadLength(4 bytes)][payload]
 */
class PacketEncoder : MessageToByteEncoder<Packet>() {
    
    override fun encode(ctx: ChannelHandlerContext, packet: Packet, out: ByteBuf) {
        // Write header
        out.writeInt(packet.cmd)
        out.writeByte(if (packet.isPath) 1 else 0)
        out.writeLong(packet.totalDataLength)
        out.writeLong(packet.curPos)
        
        // Write payload length and payload
        out.writeInt(packet.payload.size)
        out.writeBytes(packet.payload)
    }
}


