package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ServerCategory
import com.example.data.ServerRepository
import com.example.data.VpnServer
import com.example.ui.components.ServerCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ServerListScreen(
    currentServer: VpnServer?,
    onSelectServer: (VpnServer) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ServerCategory?>(null) }

    val allServers = ServerRepository.defaultServers
    val filteredServers = allServers.filter { server ->
        val matchesQuery = searchQuery.isBlank() ||
                server.name.contains(searchQuery, ignoreCase = true) ||
                server.country.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == null || server.category == selectedCategory
        matchesQuery && matchesCategory
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header Title
        Text(
            text = "GLOBAL SERVERS",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Connect to high-speed low-latency VPN locations",
            fontSize = 12.sp,
            color = TextSecondaryDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search country, city, or node...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMutedDark)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = CyberSurfaceDark,
                unfocusedContainerColor = CyberSurfaceDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("server_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All (${allServers.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberCyan,
                    selectedLabelColor = Color(0xFF00272B),
                    containerColor = CyberSurfaceDark,
                    labelColor = TextSecondaryDark
                )
            )

            FilterChip(
                selected = selectedCategory == ServerCategory.AFRICA_EXPRESS,
                onClick = {
                    selectedCategory = if (selectedCategory == ServerCategory.AFRICA_EXPRESS) null else ServerCategory.AFRICA_EXPRESS
                },
                label = { Text("🇳🇬 Africa Express") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberGreen,
                    selectedLabelColor = Color(0xFF003919),
                    containerColor = CyberSurfaceDark,
                    labelColor = TextSecondaryDark
                )
            )

            FilterChip(
                selected = selectedCategory == ServerCategory.FASTEST,
                onClick = {
                    selectedCategory = if (selectedCategory == ServerCategory.FASTEST) null else ServerCategory.FASTEST
                },
                label = { Text("⚡ Fastest Ping") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberCyan,
                    selectedLabelColor = Color(0xFF00272B),
                    containerColor = CyberSurfaceDark,
                    labelColor = TextSecondaryDark
                )
            )

            FilterChip(
                selected = selectedCategory == ServerCategory.STREAMING,
                onClick = {
                    selectedCategory = if (selectedCategory == ServerCategory.STREAMING) null else ServerCategory.STREAMING
                },
                label = { Text("🎬 Streaming") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberCyan,
                    selectedLabelColor = Color(0xFF00272B),
                    containerColor = CyberSurfaceDark,
                    labelColor = TextSecondaryDark
                )
            )

            FilterChip(
                selected = selectedCategory == ServerCategory.GAMING,
                onClick = {
                    selectedCategory = if (selectedCategory == ServerCategory.GAMING) null else ServerCategory.GAMING
                },
                label = { Text("🎮 Gaming") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberCyan,
                    selectedLabelColor = Color(0xFF00272B),
                    containerColor = CyberSurfaceDark,
                    labelColor = TextSecondaryDark
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // List of Servers
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredServers, key = { it.id }) { server ->
                ServerCard(
                    server = server,
                    isSelected = currentServer?.id == server.id,
                    onClick = { onSelectServer(server) }
                )
            }
        }
    }
}
