package com.example.smoothtransfer.ui.phoneclone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smoothtransfer.network.wifi.WifiAwareQrData
import com.example.smoothtransfer.ui.phoneclone.screens.ConnectedScreen
import com.example.smoothtransfer.ui.phoneclone.screens.ConnectingScreen
import com.example.smoothtransfer.ui.phoneclone.screens.PermissionRequestScreen
import com.example.smoothtransfer.ui.phoneclone.screens.PhoneCloneSelectRole
import com.example.smoothtransfer.ui.phoneclone.screens.QrCodeDisplayScreen
import com.example.smoothtransfer.ui.phoneclone.screens.QrCodeScannerScreen
import com.example.smoothtransfer.ui.phoneclone.screens.SearchingContentScreen
import com.example.smoothtransfer.ui.phoneclone.screens.SelectTransferMethodScreen
import com.example.smoothtransfer.viewmodel.MainViewModel
import com.example.smoothtransfer.viewmodel.MediaViewModel
import com.example.smoothtransfer.viewmodel.PhoneCloneConnectionViewModel
import java.util.UUID

@Composable
fun PhoneCloneFlowHostScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: PhoneCloneConnectionViewModel = viewModel(),
    mediaViewModel: MediaViewModel = viewModel()
) {
    val currentState by viewModel.state.collectAsState()

    LaunchedEffect(currentState) {
        val shouldShowBottomBar = when (currentState) {
            is PhoneClone.State.SelectRole -> true
            else -> false
        }
        mainViewModel.setBottomBarVisibility(shouldShowBottomBar)
    }

    AnimatedContent(
        // `targetState` là trạng thái mà AnimatedContent sẽ quan sát.
        // Mỗi khi `currentState` thay đổi, nó sẽ kích hoạt animation.
        targetState = currentState,
        label = "screen_transition",
        // `transitionSpec` định nghĩa hiệu ứng chuyển cảnh.
        transitionSpec = {
            // Lấy hướng di chuyển từ trạng thái đích.
            val direction = targetState.direction
            // Thời gian animation, có thể điều chỉnh để nhanh hơn hoặc chậm hơn.
            val duration = 400

            // Hiệu ứng "Shared Axis" kết hợp giữa slide một khoảng ngắn và fade.
            // Điều này tạo cảm giác màn hình di chuyển trên trục Z.
            when (direction) {
                PhoneClone.NavDirection.FORWARD -> {
                    // --- Hiệu ứng TIẾN TỚI (Phải -> Trái) ---
                    (
                            // Màn hình mới trượt vào từ phải, nhưng chỉ một khoảng ngắn.
                            slideInHorizontally(animationSpec = tween(duration)) { fullWidth ->
                                fullWidth / 4 // Trượt vào 1/5 chiều rộng
                            } + fadeIn(animationSpec = tween(duration))
                            ).togetherWith(
                            // Màn hình cũ trượt ra về trái, cũng chỉ một khoảng ngắn.
                            slideOutHorizontally(animationSpec = tween(duration)) { fullWidth ->
                                -fullWidth / 4 // Trượt ra 1/5 chiều rộng
                            } + fadeOut(animationSpec = tween(duration))
                        )
                }

                PhoneClone.NavDirection.BACKWARD -> {
                    // --- Hiệu ứng LÙI LẠI (Trái -> Phải) ---
                    (
                            // Màn hình mới trượt vào từ trái.
                            slideInHorizontally(animationSpec = tween(duration)) { fullWidth ->
                                -fullWidth / 4
                            } + fadeIn(animationSpec = tween(duration))
                            ).togetherWith(
                            // Màn hình cũ trượt ra về phải.
                            slideOutHorizontally(animationSpec = tween(duration)) { fullWidth ->
                                fullWidth / 4
                            } + fadeOut(animationSpec = tween(duration))
                        )
                }

                PhoneClone.NavDirection.NONE -> {
                    // Lần đầu tải, chỉ cần fade in nhẹ nhàng, không trượt.
                    fadeIn(animationSpec = tween(duration))
                        .togetherWith(fadeOut(animationSpec = tween(duration)))
                }
            }
        }
    ) { targetState ->

        when (targetState) {
            is PhoneClone.State.SelectRole -> {
                PhoneCloneSelectRole(action = viewModel)
            }

            is PhoneClone.State.SelectTransferMethod -> {
                SelectTransferMethodScreen(
                    action = viewModel,
                    targetState.isSender,
                    onBackClicked = {
                        viewModel.onEvent(PhoneClone.Event.BackPressed)
                    }
                )
            }

            is PhoneClone.State.ShowCameraToScanQr -> {
                QrCodeScannerScreen(
                    viewModel,
                    onBackClicked = {
                        viewModel.onEvent(PhoneClone.Event.BackPressed)
                    }
                )
            }

            is PhoneClone.State.Connecting -> {
                // Màn hình hiển thị đang kết nối...
                ConnectingScreen(
                    message = targetState.message,
                    action = viewModel
                )
            }

            is PhoneClone.State.Connected -> {
                ConnectedScreen(viewModel, targetState.deviceInfo)
            }

            is PhoneClone.State.SearchingContent -> {
                SearchingContentScreen(viewModel, targetState.deviceInfo)
            }

            is PhoneClone.State.ContentListSelection -> {
                // Màn hình để Receiver chọn nội dung muốn nhận
                // ContentListScreen(items = state.items, onStartClick = { ... })
            }

            is PhoneClone.State.Transferring -> {
                // Màn hình hiển thị tiến trình transfer
                // TransferringScreen(progress = state.progress, status = state.statusText)
            }

            is PhoneClone.State.Restoring -> {
                // Màn hình hiển thị đang khôi phục dữ liệu
                // RestoringScreen()
            }

            is PhoneClone.State.Completed -> {
                // Màn hình hoàn thành
                // CompletedScreen(onFinish = { navController.popBackStack() })
            }

            // --- Các màn hình khác của luồng Receiver sẽ được thêm vào đây ---
            is PhoneClone.State.DisplayQrCode -> {
                val qrData = WifiAwareQrData(
                    serviceName = "SmartSwitch",
                    peerId = UUID.randomUUID().toString(),
                    connectionMetadata = ""
                )
                QrCodeDisplayScreen(viewModel, qrData, onBackClicked = {
                    viewModel.onEvent(PhoneClone.Event.BackPressed)
                })
            }

            is PhoneClone.State.WaitingForContentList -> {
                // WaitingScreen(...)
            }

            is PhoneClone.State.RequestPermissions -> {
                PermissionRequestScreen(
                    action = viewModel,
                    isSender = targetState.isSender,
                    onBackClicked = {
                        viewModel.onEvent(PhoneClone.Event.BackPressed)
                    }
                )
            }

            is PhoneClone.State.Error -> TODO()
        }
    }
}
