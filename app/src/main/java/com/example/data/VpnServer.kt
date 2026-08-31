package com.example.data

data class VpnServer(
    val id: String,
    val name: String,
    val country: String,
    val flagEmoji: String,
    val ipAddress: String,
    val pingMs: Int,
    val loadPercent: Int,
    val category: ServerCategory = ServerCategory.FASTEST,
    val isVip: Boolean = false,
    val protocol: String = "WireGuard"
)

enum class ServerCategory {
    FASTEST,
    STREAMING,
    GAMING,
    P2P,
    AFRICA_EXPRESS
}

object ServerRepository {
    val defaultServers = listOf(
        VpnServer(
            id = "ng_lagos_1",
            name = "Nigeria - Lagos Express",
            country = "Nigeria",
            flagEmoji = "🇳🇬",
            ipAddress = "102.164.12.8",
            pingMs = 18,
            loadPercent = 32,
            category = ServerCategory.AFRICA_EXPRESS
        ),
        VpnServer(
            id = "us_ny_1",
            name = "United States - New York",
            country = "United States",
            flagEmoji = "🇺🇸",
            ipAddress = "198.51.100.24",
            pingMs = 34,
            loadPercent = 45,
            category = ServerCategory.STREAMING
        ),
        VpnServer(
            id = "uk_london_1",
            name = "United Kingdom - London",
            country = "United Kingdom",
            flagEmoji = "🇬🇧",
            ipAddress = "185.220.101.5",
            pingMs = 28,
            loadPercent = 38,
            category = ServerCategory.FASTEST
        ),
        VpnServer(
            id = "de_frankfurt_1",
            name = "Germany - Frankfurt Turbo",
            country = "Germany",
            flagEmoji = "🇩🇪",
            ipAddress = "194.187.249.12",
            pingMs = 24,
            loadPercent = 29,
            category = ServerCategory.GAMING
        ),
        VpnServer(
            id = "sg_singapore_1",
            name = "Singapore - Fast Route",
            country = "Singapore",
            flagEmoji = "🇸🇬",
            ipAddress = "139.180.201.88",
            pingMs = 42,
            loadPercent = 51,
            category = ServerCategory.P2P
        ),
        VpnServer(
            id = "jp_tokyo_1",
            name = "Japan - Tokyo Neon",
            country = "Japan",
            flagEmoji = "🇯🇵",
            ipAddress = "133.242.18.99",
            pingMs = 58,
            loadPercent = 40,
            category = ServerCategory.STREAMING
        ),
        VpnServer(
            id = "ca_toronto_1",
            name = "Canada - Toronto Shield",
            country = "Canada",
            flagEmoji = "🇨🇦",
            ipAddress = "192.99.148.60",
            pingMs = 39,
            loadPercent = 35,
            category = ServerCategory.FASTEST
        ),
        VpnServer(
            id = "nl_amsterdam_1",
            name = "Netherlands - Privacy Hub",
            country = "Netherlands",
            flagEmoji = "🇳🇱",
            ipAddress = "185.107.56.32",
            pingMs = 22,
            loadPercent = 25,
            category = ServerCategory.P2P
        )
    )
}
