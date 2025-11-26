package com.example.smoothtransfer.network.protocol

/**
 * Command constants for communication between sender and receiver
 */
object Commands {
    // Request content list from sender
    const val CMD_DEVICE_INFO = 1
    const val CMD_CONTENT_LIST = 2
    
    // Response with content list from sender
    const val RSP_CONTENT_LIST = 3
    
    // Request thumbnail from sender
    const val CMD_THUMBNAIL_REQUEST = 4
    
    // Response with thumbnail bitmap from sender
    const val RSP_THUMBNAIL = 5
    
    // Request to transfer selected files (from receiver to sender)
    const val CMD_TOTAL_CONTENT_LIST_REQUEST = 6
    
    // Sender notifies receiver about file type being transferred
    const val CMD_SEND_FILE_INFO = 7
    
    // File data packets (both file info and file data use this command)
    const val CMD_FILE_DATA_SEND = 8
    
    // PC notifies Android to start restore (media scan) after all files transferred
    const val CMD_PC_RESTORE_START = 9
    
    // Device information exchange (both sender and receiver)

    
    // Receiver responds with file transfer status (success/failure)
    const val RSP_FILE_DATA_SEND = 10
    
    // Peer disconnect notification (sent before closing connection)
    const val CMD_PEER_DISCONNECT = 11

    const val CMD_HEARTBEAT = 12
    
    /**
     * getCommandName - Get command name string from command number
     * 
     * Converts command number to readable string for logging.
     * 
     * @param cmd Command number
     * @return Command name string (e.g., "CMD_THUMBNAIL_REQUEST") or "UNKNOWN_CMD_$cmd" if not found
     */
    fun getCommandName(cmd: Int): String {
        return when (cmd) {
            CMD_CONTENT_LIST -> "CMD_CONTENT_LIST"
            RSP_CONTENT_LIST -> "RSP_CONTENT_LIST"
            CMD_THUMBNAIL_REQUEST -> "CMD_THUMBNAIL_REQUEST"
            RSP_THUMBNAIL -> "RSP_THUMBNAIL"
            CMD_TOTAL_CONTENT_LIST_REQUEST -> "CMD_TOTAL_CONTENT_LIST_REQUEST"
            CMD_SEND_FILE_INFO -> "CMD_SEND_FILE_INFO"
            CMD_FILE_DATA_SEND -> "CMD_FILE_DATA_SEND"
            CMD_PC_RESTORE_START -> "CMD_PC_RESTORE_START"
            CMD_DEVICE_INFO -> "CMD_DEVICE_INFO"
            RSP_FILE_DATA_SEND -> "RSP_FILE_DATA_SEND"
            CMD_PEER_DISCONNECT -> "CMD_PEER_DISCONNECT"
            else -> "UNKNOWN_CMD_$cmd"
        }
    }
}


