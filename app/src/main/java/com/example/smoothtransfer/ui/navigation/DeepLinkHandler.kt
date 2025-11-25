package com.example.smoothtransfer.ui.navigation

import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Singleton để xử lý các sự kiện từ bên ngoài (như từ Activity).
 * Nó sẽ phát ra các Event mà ViewModel có thể lắng nghe.
 */
object DeepLinkHandler {
    // Thay vì NavRequest, chúng ta phát ra chính PhoneClone.Event
    private val _eventFlow = MutableSharedFlow<PhoneClone.Event>(replay = 0)
    val eventFlow = _eventFlow.asSharedFlow()

    suspend fun postEvent(event: PhoneClone.Event) {
        _eventFlow.emit(event)
    }
}

