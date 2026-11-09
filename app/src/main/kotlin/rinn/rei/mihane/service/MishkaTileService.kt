package rinn.rei.mihane.service

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.ExternalProxyAction
import rinn.rei.mihane.platform.ExternalProxyOutcome
import rinn.rei.mihane.platform.ProxyServiceBridge
import rinn.rei.mihane.platform.ProxyServiceController
import rinn.rei.mihane.platform.ProxyState

class MishkaTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        // 系统重复绑定时会再进一次而没有配对的 onStopListening，不先取消就留下一条仍在
        // updateTile 的旧 collector
        stateJob?.cancel()
        stateJob =
            scope.launch {
                ProxyServiceBridge.state.collect { status ->
                    val tile = qsTile ?: return@collect
                    tile.state =
                        when (status.state) {
                            ProxyState.Running -> Tile.STATE_ACTIVE
                            ProxyState.Starting -> Tile.STATE_ACTIVE
                            else -> Tile.STATE_INACTIVE
                        }
                    tile.subtitle =
                        when (status.state) {
                            ProxyState.Running -> getString(R.string.tile_connected)
                            ProxyState.Starting -> getString(R.string.tile_connecting)
                            ProxyState.Error -> getString(R.string.tile_error)
                            else -> getString(R.string.tile_disconnected)
                        }
                    tile.updateTile()
                }
            }
    }

    override fun onStopListening() {
        stateJob?.cancel()
        stateJob = null
        super.onStopListening()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onClick() {
        super.onClick()
        val controller = ProxyServiceController(applicationContext)
        // Tile 不是 Activity，VPN 授权对话框拉不了，只能由这一支自己起透明跳板；其余结果
        // Controller 内已落地（过渡态忽略、订阅校验都算在内），这里不再重复判一次状态
        when (val outcome = controller.applyExternalAction(ExternalProxyAction.Toggle)) {
            is ExternalProxyOutcome.NeedVpnPermission ->
                launchVpnPermissionActivity(outcome.subscriptionId)

            else -> Unit
        }
    }

    private fun launchVpnPermissionActivity(subscriptionId: String) {
        val intent =
            Intent(this, VpnPermissionActivity::class.java).apply {
                putExtra(VpnPermissionActivity.EXTRA_SUBSCRIPTION_ID, subscriptionId)
            }
        TileServiceCompat.startActivityAndCollapse(
            this,
            PendingIntentActivityWrapper(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT, false),
        )
    }
}
