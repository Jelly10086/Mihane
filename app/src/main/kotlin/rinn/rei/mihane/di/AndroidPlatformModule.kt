package rinn.rei.mihane.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import rinn.rei.mihane.data.backup.BackupManager
import rinn.rei.mihane.data.database.getAppDatabase
import rinn.rei.mihane.platform.AppListProvider
import rinn.rei.mihane.platform.BootStartManager
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.PlatformSystemInfo
import rinn.rei.mihane.platform.ProxyServiceController
import rinn.rei.mihane.platform.WifiPolicyController
import rinn.rei.mihane.service.ProfileUpdateScheduler

/**
 * Android 平台单例：均绑定 application Context（`androidContext()`）。
 * 需 Activity 上下文的资源（FilePicker / VPN 授权 launcher）由 MainActivity 直接持有，不入 Koin。
 */
val androidPlatformModule: Module = module {
    single { getAppDatabase(androidContext()) }
    single { PlatformStorage(androidContext()) }
    single { PlatformSystemInfo(androidContext()) }
    single { ProxyServiceController(androidContext()) }
    single { AppListProvider(androidContext()) }
    single { WifiPolicyController(androidContext()) }
    single { BootStartManager(androidContext()) }
    single { BackupManager(androidContext(), get(), get(), get(), get()) }
    single { ProfileUpdateScheduler(androidContext(), get(), get()) }
}
