package com.example.smoothtransfer.network.wifi.protocol

import java.io.Serializable

/**
 * Packet structure for TCP communication
 * Contains header (cmd, isPath, totalDataLength, curPos) and payload
 */
data class Packet(
    val cmd: Int,                    // Command type
    val isPath: Boolean,              // True if payload contains file path, false if data
    val totalDataLength: Long,        // Total length of data
    val curPos: Long,                 // Current position/offset for data writing
    val payload: ByteArray            // Payload data (file path or actual data)
) : Serializable {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Packet
        
        if (cmd != other.cmd) return false
        if (isPath != other.isPath) return false
        if (totalDataLength != other.totalDataLength) return false
        if (curPos != other.curPos) return false
        if (!payload.contentEquals(other.payload)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = cmd
        result = 31 * result + isPath.hashCode()
        result = 31 * result + totalDataLength.hashCode()
        result = 31 * result + curPos.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}


