package rinn.rei.mihane.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import rinn.rei.mihane.domain.model.ConnectionsResponse
import rinn.rei.mihane.domain.model.DelayResult
import rinn.rei.mihane.domain.model.DnsQueryResponse
import rinn.rei.mihane.domain.model.GroupsResponse
import rinn.rei.mihane.domain.model.LogMessage
import rinn.rei.mihane.domain.model.MemoryData
import rinn.rei.mihane.domain.model.MihomoConfig
import rinn.rei.mihane.domain.model.MihomoVersion
import rinn.rei.mihane.domain.model.ProvidersResponse
import rinn.rei.mihane.domain.model.ProxiesResponse
import rinn.rei.mihane.domain.model.RuleProvidersResponse
import rinn.rei.mihane.domain.model.RulesResponse
import rinn.rei.mihane.domain.model.TrafficData

/**
 * mihomo runtime 客户端门面：REST + WebSocket 流。实现见 data 层 `MihomoRepositoryImpl`。
 */
interface MihomoRepository {

    val connectionState: StateFlow<Boolean>

    fun trafficFlow(): Flow<TrafficData>
    fun logsFlow(level: String = "info"): Flow<LogMessage>
    fun memoryFlow(): Flow<MemoryData>
    fun connectionsFlow(): Flow<ConnectionsResponse>

    suspend fun getVersion(): Result<MihomoVersion>
    suspend fun getConfig(): Result<MihomoConfig>
    suspend fun getProxies(): Result<ProxiesResponse>
    suspend fun getGroups(): Result<GroupsResponse>
    suspend fun selectProxy(group: String, name: String): Result<Unit>
    suspend fun unfixProxy(group: String): Result<Unit>
    suspend fun getGroupDelay(
        name: String,
        testUrl: String = "http://www.gstatic.com/generate_204",
        timeout: Int = 5000,
    ): Result<Map<String, Int>>

    suspend fun getProxyDelay(
        name: String,
        testUrl: String = "http://www.gstatic.com/generate_204",
        timeout: Int = 5000,
    ): Result<DelayResult>

    suspend fun getProviderProxyDelay(
        provider: String,
        name: String,
        testUrl: String = "http://www.gstatic.com/generate_204",
        timeout: Int = 5000,
    ): Result<DelayResult>

    suspend fun getRules(): Result<RulesResponse>
    suspend fun getConnections(): Result<ConnectionsResponse>
    suspend fun closeAllConnections(): Result<Unit>
    suspend fun closeConnection(id: String): Result<Unit>
    suspend fun getProviders(): Result<ProvidersResponse>
    suspend fun updateProvider(name: String): Result<Unit>
    suspend fun getRuleProviders(): Result<RuleProvidersResponse>
    suspend fun updateRuleProvider(name: String): Result<Unit>
    suspend fun queryDns(name: String, type: String = "A"): Result<DnsQueryResponse>
    suspend fun updateSystemDns(servers: List<String>): Result<Unit>
    suspend fun flushFakeIp(): Result<Unit>
    suspend fun flushDnsCache(): Result<Unit>

    fun close()
}
