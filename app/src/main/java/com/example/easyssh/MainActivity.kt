package com.example.easyssh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.easyssh.data.Server
import com.example.easyssh.ui.theme.EasysshTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Dummy data to see your app in action
        val dummyServers = listOf(
            Server(name = "Production Web", ip = "192.168.1.10", username = "admin", environment = "Prod"),
            Server(name = "Staging DB", ip = "192.168.1.20", username = "root", environment = "Staging"),
            Server(name = "Development", ip = "localhost", username = "miko", environment = "Dev")
        )

        setContent {
            EasysshTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(title = { Text("My Servers") })
                    }
                ) { innerPadding ->
                    ServerList(
                        servers = dummyServers,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ServerList(servers: List<Server>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        items(servers) { server ->
            ServerCard(server)
        }
    }
}

@Composable
fun ServerCard(server: Server) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = server.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "${server.username}@${server.ip}", style = MaterialTheme.typography.bodyMedium)
            
            // Replaced Badge with a simple Surface/Text for broader compatibility
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = server.environment,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
