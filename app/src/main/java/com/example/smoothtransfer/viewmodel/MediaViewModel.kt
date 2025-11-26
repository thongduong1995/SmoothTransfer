package com.example.smoothtransfer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private var connectionViewModel: PhoneCloneConnectionViewModel? = null

    fun setConnectionViewModel(viewModel: PhoneCloneConnectionViewModel) {
        connectionViewModel = viewModel
        viewModel.setMediaViewModel(this)

        // Initialize FileTransferSender with connection
        //fileTransferSender = FileTransferSender(getApplication(), gson, viewModel)
    }
}