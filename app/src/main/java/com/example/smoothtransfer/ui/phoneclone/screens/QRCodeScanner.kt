package com.example.smoothtransfer.ui.phoneclone.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.also
import kotlin.let

/**
 * QRCodeScanner - Composable function for scanning QR codes using CameraX and ML Kit
 * 
 * This composable provides a camera preview with real-time QR code scanning capabilities.
 * It uses:
 * - CameraX: For camera preview and image capture
 * - ML Kit Barcode Scanner: For QR code detection and recognition
 * 
 * Features:
 * - Automatic camera permission request
 * - Real-time QR code scanning from camera feed
 * - Stops scanning after first QR code is detected (prevents multiple callbacks)
 * - Lifecycle-aware camera management
 * 
 * Flow:
 * 1. Check camera permission
 * 2. Request permission if not granted
 * 3. Initialize CameraX with PreviewView
 * 4. Set up ImageAnalysis to process camera frames
 * 5. Use ML Kit to detect QR codes in each frame
 * 6. Call onQRCodeScanned callback when QR code is found
 * 
 * @param onQRCodeScanned Callback invoked when QR code is successfully scanned
 *                        Called only once per QR code (stops scanning after first detection)
 * @param modifier Modifier for styling and layout
 */
@Composable
fun QRCodeScanner(
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    /**
     * LocalContext: Gets Android Context for permission checks and camera operations
     */
    val context = LocalContext.current
    
    /**
     * LocalLifecycleOwner: Gets lifecycle owner for CameraX lifecycle binding
     * 
     * CameraX needs lifecycle owner to:
     * - Start camera when screen is visible
     * - Stop camera when screen is invisible
     * - Manage camera resources properly
     */
    val lifecycleOwner = LocalLifecycleOwner.current
    
    /**
     * hasCameraPermission: State variable tracking camera permission status
     * 
     * Uses remember { mutableStateOf() } to:
     * - Persist state across recompositions
     * - Trigger recomposition when permission changes
     * - Initialize with current permission status
     */
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    /**
     * cameraPermissionLauncher: ActivityResultLauncher for requesting camera permission
     * 
     * Uses rememberLauncherForActivityResult to:
     * - Remember launcher across recompositions
     * - Handle permission request result
     * - Update hasCameraPermission state when user responds
     */
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Update permission state when user responds to permission dialog
        hasCameraPermission = isGranted
    }
    
    /**
     * LaunchedEffect(Unit): Side effect that runs once when composable is created
     * 
     * Key = Unit: Runs only once
     * 
     * Purpose:
     * - Auto-request camera permission if not granted
     * - Ensures camera permission is requested before showing camera preview
     */
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            // Request camera permission automatically
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    /**
     * Conditional Rendering: Only show camera preview if permission is granted
     * 
     * If permission not granted:
     * - Nothing is displayed (user needs to grant permission first)
     * - Permission request dialog is shown automatically
     */
    if (hasCameraPermission) {
        /**
         * Capture lifecycleOwner in composable scope
         * 
         * Why capture?
         * - AndroidView factory runs in different scope
         * - Need to ensure lifecycleOwner is accessible in factory lambda
         * - Prevents potential scope issues
         */
        val owner = lifecycleOwner
        
        /**
         * AndroidView: Embeds Android View (PreviewView) in Compose UI
         * 
         * factory: Lambda that creates the Android View
         * - Called once when AndroidView is first created
         * - Returns the View to be embedded
         * - Sets up CameraX and ML Kit scanning
         * 
         * modifier: Applied to the Android View
         */
        AndroidView(
            factory = { ctx ->
                /**
                 * PreviewView: CameraX view that displays camera preview
                 * 
                 * This is the actual Android View that shows camera feed
                 * It's embedded in Compose using AndroidView
                 */
                val previewView = PreviewView(ctx)
                
                /**
                 * Executor: Main executor for running callbacks on main thread
                 * 
                 * Used for:
                 * - Camera provider listener callbacks
                 * - UI updates
                 */
                val executor = ContextCompat.getMainExecutor(ctx)
                
                /**
                 * ProcessCameraProvider: CameraX provider for camera access
                 * 
                 * getInstance(): Gets singleton instance of camera provider
                 * Returns Future that completes when provider is ready
                 */
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                
                /**
                 * Add Listener: Set up camera when provider is ready
                 * 
                 * This listener runs when ProcessCameraProvider is initialized
                 * Sets up Preview, ImageAnalysis, and binds to lifecycle
                 */
                cameraProviderFuture.addListener({
                    // Get camera provider from future
                    val cameraProvider = cameraProviderFuture.get()
                    
                    /**
                     * Preview UseCase: Displays camera preview on screen
                     * 
                     * Preview.Builder(): Creates preview configuration
                     * setSurfaceProvider(): Connects preview to PreviewView
                     * 
                     * This shows the live camera feed to user
                     */
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    /**
                     * ImageAnalysis UseCase: Processes camera frames for QR code detection
                     * 
                     * ImageAnalysis.Builder(): Creates image analysis configuration
                     * setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST):
                     *   - Keeps only the latest frame
                     *   - Drops older frames if processing is slow
                     *   - Prevents frame queue buildup
                     * 
                     * This analyzes each camera frame to detect QR codes
                     */
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    
                    /**
                     * BarcodeScanner: ML Kit scanner for detecting QR codes
                     * 
                     * getClient(): Gets singleton instance of barcode scanner
                     * This scanner processes images and detects barcodes/QR codes
                     */
                    val scanner = BarcodeScanning.getClient()
                    
                    /**
                     * scanned: Flag to prevent multiple callbacks
                     * 
                     * Purpose:
                     * - Set to true after first QR code is detected
                     * - Prevents processing more frames after detection
                     * - Stops scanning after successful detection
                     */
                    var scanned = false
                    
                    /**
                     * setAnalyzer: Set up image analyzer for QR code detection
                     * 
                     * Executors.newSingleThreadExecutor():
                     *   - Creates single-threaded executor for analysis
                     *   - Ensures frames are processed sequentially
                     *   - Prevents race conditions
                     * 
                     * Analyzer Lambda:
                     * - Called for each camera frame
                     * - Receives ImageProxy (camera frame)
                     * - Processes frame if not already scanned
                     * - Closes ImageProxy after processing
                     */
                    imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor()
                    ) { imageProxy ->
                        if (!scanned) {
                            // Process frame if QR code not yet detected
                            processImageProxy(scanner, imageProxy) { qrCode ->
                                // Mark as scanned to stop processing more frames
                                scanned = true
                                // Call callback with detected QR code
                                onQRCodeScanned(qrCode)
                            }
                        } else {
                            // If already scanned, just close the frame (don't process)
                            imageProxy.close()
                        }
                    }
                    
                    /**
                     * CameraSelector: Selects which camera to use
                     * 
                     * DEFAULT_BACK_CAMERA: Uses rear camera (back camera)
                     * Alternative: DEFAULT_FRONT_CAMERA for front camera
                     */
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    /**
                     * Bind Camera UseCases: Connect camera to lifecycle
                     * 
                     * Steps:
                     * 1. unbindAll(): Unbind any existing use cases
                     * 2. bindToLifecycle(): Bind preview and imageAnalysis to lifecycle
                     * 
                     * Lifecycle Binding:
                     * - Camera starts when lifecycle is STARTED
                     * - Camera stops when lifecycle is STOPPED
                     * - Automatically manages camera resources
                     */
                    try {
                        cameraProvider.unbindAll()
                        // Bind preview and imageAnalysis as UseCase instances
                        cameraProvider.bindToLifecycle(
                            owner,  // Lifecycle owner (Activity/Fragment)
                            cameraSelector,  // Which camera to use
                            preview as UseCase,  // Preview use case
                            imageAnalysis as UseCase  // Image analysis use case
                        )
                    } catch (e: Exception) {
                        // Log error if camera binding fails
                        e.printStackTrace()
                    }
                }, executor)
                
                // Return PreviewView to be embedded in Compose
                previewView
            },
            modifier = modifier.fillMaxSize()  // Fill available space
        )
    }
}

/**
 * processImageProxy - Process camera frame to detect QR codes using ML Kit
 * 
 * This function:
 * 1. Extracts MediaImage from ImageProxy
 * 2. Creates InputImage for ML Kit (with rotation info)
 * 3. Processes image with ML Kit BarcodeScanner
 * 4. Filters results to find QR codes
 * 5. Calls callback with QR code value
 * 6. Closes ImageProxy to release frame
 * 
 * @OptIn(ExperimentalGetImage::class): Required for accessing ImageProxy.image
 * 
 * @param scanner ML Kit BarcodeScanner instance for detecting QR codes
 * @param imageProxy Camera frame from CameraX ImageAnalysis
 * @param onQRCodeFound Callback invoked when QR code is detected
 *                      Receives the QR code string value
 */
@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onQRCodeFound: (String) -> Unit
) {
    /**
     * Extract MediaImage from ImageProxy
     * 
     * ImageProxy wraps MediaImage with metadata
     * MediaImage is the actual image data needed for ML Kit
     */
    val mediaImage = imageProxy.image
    
    if (mediaImage != null) {
        /**
         * Create InputImage for ML Kit
         * 
         * InputImage.fromMediaImage():
         *   - Converts MediaImage to InputImage format
         *   - Includes rotation degrees for correct orientation
         *   - ML Kit needs rotation info to scan QR codes correctly
         * 
         * imageProxy.imageInfo.rotationDegrees:
         *   - Rotation of camera frame (0, 90, 180, 270)
         *   - Needed because camera frames may be rotated
         */
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        /**
         * Process image with ML Kit BarcodeScanner
         * 
         * scanner.process(image): Asynchronously processes image
         * Returns Task that completes when processing is done
         * 
         * Callbacks:
         * - addOnSuccessListener: Called when barcodes are detected
         * - addOnFailureListener: Called if processing fails
         * - addOnCompleteListener: Always called when processing completes
         */
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                /**
                 * Success: Barcodes detected (may include QR codes and other barcodes)
                 * 
                 * Iterate through detected barcodes
                 * Filter for QR codes only (FORMAT_QR_CODE)
                 * Extract raw value (QR code string)
                 * Call callback with QR code value
                 */
                for (barcode in barcodes) {
                    // Check if barcode is QR code format
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        // Extract QR code string value
                        barcode.rawValue?.let { qrCode ->
                            // Call callback with detected QR code
                            onQRCodeFound(qrCode)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                /**
                 * Failure: Error during image processing
                 * 
                 * Log error for debugging
                 * ImageProxy will still be closed in onCompleteListener
                 */
                e.printStackTrace()
            }
            .addOnCompleteListener {
                /**
                 * Complete: Always called when processing finishes (success or failure)
                 * 
                 * Close ImageProxy to release camera frame
                 * Important: Must close to prevent memory leaks
                 * CameraX reuses ImageProxy objects, so closing releases them back
                 */
                imageProxy.close()
            }
    } else {
        /**
         * No MediaImage: ImageProxy doesn't contain image data
         * 
         * This shouldn't happen normally, but handle gracefully
         * Close ImageProxy to release frame
         */
        imageProxy.close()
    }
}

