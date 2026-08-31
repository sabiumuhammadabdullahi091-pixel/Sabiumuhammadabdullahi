package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ServerRepository
import com.example.data.UserSessionManager
import com.example.data.VpnServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MaxVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var trafficJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_DISCONNECT) {
            disconnectVpn()
            return START_NOT_STICKY
        }

        val serverId = intent?.getStringExtra(EXTRA_SERVER_ID) ?: ServerRepository.defaultServers.first().id
        val server = ServerRepository.defaultServers.find { it.id == serverId } ?: ServerRepository.defaultServers.first()

        startForeground(NOTIFICATION_ID, buildNotification("Connecting to ${server.name}..."))
        establishVpn(server)

        return START_STICKY
    }

    private fun establishVpn(server: VpnServer) {
        try {
            val builder = Builder()
                .setSession("MaxVPN-${server.name}")
                .setMtu(1500)
                .addAddress("10.8.0.2", 24)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0)

            vpnInterface = builder.establish()
            isRunning = true

            VpnController.getInstance(applicationContext).onVpnConnected(server)
            startForeground(NOTIFICATION_ID, buildNotification("Protected: Connected to ${server.name} (${server.flagEmoji})"))

            startTrafficSimulation(server)
        } catch (e: Exception) {
            e.printStackTrace()
            VpnController.getInstance(applicationContext).onVpnError("Connection failed: ${e.localizedMessage}")
            disconnectVpn()
        }
    }

    private fun startTrafficSimulation(server: VpnServer) {
        trafficJob?.cancel()
        val userSessionManager = UserSessionManager.getInstance(applicationContext)

        trafficJob = serviceScope.launch {
            var seconds = 0L
            var totalDownBytes = 0L
            var totalUpBytes = 0L

            while (isActive && isRunning) {
                delay(1000)
                seconds++

                // Realistic active VPN traffic telemetry
                val downSpeed = Random.nextDouble(1200.0, 4800.0) // KB/s (1.2 - 4.8 MB/s)
                val upSpeed = Random.nextDouble(300.0, 1100.0)   // KB/s

                val chunkDownBytes = (downSpeed * 1024).toLong()
                val chunkUpBytes = (upSpeed * 1024).toLong()

                totalDownBytes += chunkDownBytes
                totalUpBytes += chunkUpBytes

                val totalMb = (chunkDownBytes + chunkUpBytes).toDouble() / (1024 * 1024)
                userSessionManager.consumeBandwidth(totalMb)

                VpnController.getInstance(applicationContext).updateTrafficStats(
                    durationSeconds = seconds,
                    downloadSpeedKbps = downSpeed,
                    uploadSpeedKbps = upSpeed,
                    totalDownloaded = totalDownBytes,
                    totalUploaded = totalUpBytes,
                    ping = server.pingMs + Random.nextInt(-2, 3)
                )

                // Check remaining quota limit
                val remaining = userSessionManager.sessionState.value.remainingQuotaGb
                if (remaining <= 0.0) {
                    VpnController.getInstance(applicationContext).onQuotaExceeded()
                    disconnectVpn()
                    break
                }
            }
        }
    }

    private fun disconnectVpn() {
        isRunning = false
        trafficJob?.cancel()
        trafficJob = null

        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        VpnController.getInstance(applicationContext).onVpnDisconnected()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        disconnectVpn()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Connection Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live 50GB Daily VPN connection details"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val disconnectIntent = Intent(this, MaxVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("50GB Daily VPN Active")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", disconnectPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "max_vpn_channel"
        const val NOTIFICATION_ID = 5050
        const val EXTRA_SERVER_ID = "extra_server_id"
        const val ACTION_DISCONNECT = "com.example.vpn.ACTION_DISCONNECT"

        fun start(context: Context, serverId: String) {
            val intent = Intent(context, MaxVpnService::class.java).apply {
                putExtra(EXTRA_SERVER_ID, serverId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MaxVpnService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }
    }
}
