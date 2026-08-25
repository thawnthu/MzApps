package com.mzchat.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MzChatFullApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MzChatFullApp() {
    var tab by remember { mutableStateOf(0) }
    var showUserList by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected=tab==0, onClick={tab=0}, icon={Icon(Icons.Filled.Home,null)}, label={Text("Home")})
                NavigationBarItem(selected=tab==1, onClick={tab=1}, icon={Icon(Icons.Filled.Chat,null)}, label={Text("Chat")})
                NavigationBarItem(selected=tab==2, onClick={tab=2}, icon={Icon(Icons.Filled.Circle,null)}, label={Text("Status")})
                NavigationBarItem(selected=tab==3, onClick={tab=3}, icon={Icon(Icons.Filled.People,null)}, label={Text("Online")})
                NavigationBarItem(selected=tab==4, onClick={tab=4}, icon={Icon(Icons.Filled.Person,null)}, label={Text("Profile")})
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when(tab) {
                0 -> HomeFullScreen()
                1 -> ChatFullScreen(onPlusClick = { showUserList = true })
                2 -> StatusFullScreen()
                3 -> OnlineFullScreen()
                4 -> ProfileFullScreen()
            }
            if(showUserList) { UserListDialog { showUserList=false } }
        }
    }
}

// --- HOME - Header Search tawlh ve lo ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFullScreen() {
    var search by remember { mutableStateOf("") }
    Column {
        // HEADER - Tawlh ve lo
        TopAppBar(
            title = {
                TextField(value=search, onValueChange={search=it}, placeholder={Text("Search all...")}, modifier=Modifier.fillMaxWidth(), singleLine=true)
            }
        )
        LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
            items(20) {
                Card(Modifier.fillMaxWidth().padding(6.dp).clickable{}) {
                    Row(Modifier.padding(12.dp), verticalAlignment=Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                        Column(Modifier.padding(start=12.dp)) {
                            Text("Thawnthu", style=MaterialTheme.typography.titleMedium)
                            Text("He pic leh thu hi Home ah a lang ang - ${it+1}")
                        }
                    }
                }
            }
        }
    }
}

// --- CHAT - Search highlight + Chat+ + typing/tick/lastseen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFullScreen(onPlusClick: ()->Unit) {
    var search by remember { mutableStateOf("") }
    Column {
        TopAppBar(
            title = { TextField(value=search, onValueChange={search=it}, placeholder={Text("Search chat...")}, modifier=Modifier.fillMaxWidth()) },
            actions = { IconButton(onClick=onPlusClick){ Icon(Icons.Filled.AddCircle, "Chat+", tint=Color(0xFF25D366), modifier=Modifier.size(30.dp)) } }
        )
        LazyColumn {
            items(15) { i ->
                ListItem(
                    modifier=Modifier.clickable{},
                    leadingContent={ Box(Modifier.size(45.dp).clip(CircleShape).background(Color.LightGray), contentAlignment=Alignment.Center){ Text("F${i+1}") } },
                    headlineContent={ Text("Friend ${i+1} - Click la Profile en theih") },
                    supportingContent={ 
                        if(i==0) Text("Typing...", color=Color(0xFF25D366)) 
                        else Text("Last message • ✓✓") 
                    },
                    trailingContent={ Column(horizontalAlignment=Alignment.End){ Text("10:3${i} AM"); if(i==1) Text("✓✓", color=Color.Blue) } }
                )
                Divider()
            }
        }
    }
}

@Composable
fun UserListDialog(onClose: ()->Unit) {
    AlertDialog(onDismissRequest=onClose, title={Text("New Chat - Users")}, 
        text={ LazyColumn{ items(10){ ListItem(headlineContent={Text("User ${it+1}")}, leadingContent={Box(Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))}) } } },
        confirmButton={ TextButton(onClick=onClose){Text("Close")}} )
}

@Composable fun StatusFullScreen() {
    var search by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(search, {search=it}, Modifier.fillMaxWidth().padding(8.dp), placeholder={Text("Search status...")})
        Text("Status Update theihna - WhatsApp ang", Modifier.padding(16.dp))
    }
}
@Composable fun OnlineFullScreen() {
    var search by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(search, {search=it}, Modifier.fillMaxWidth().padding(8.dp), placeholder={Text("Search online friends...")})
        LazyColumn{ items(20){ ListItem(headlineContent={Text("Friend ${it+1} Online")}, leadingContent={ Box(Modifier.size(12.dp).clip(CircleShape).background(Color.Green)) }) } }
    }
}
@Composable fun ProfileFullScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(80.dp).clip(CircleShape).background(Color.Gray))
            Column(Modifier.padding(start=16.dp)) {
                Text("Thawnthu", style=MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement=Arrangement.spacedBy(20.dp)) {
                    Column{ Text("25"); Text("Posts") }; Column{ Text("1.2k"); Text("Friends") }
                }
            }
        }
        Text("About: Mizo Developer - Ka thil post zawng zawng hnuaiah a awm ang", Modifier.padding(top=16.dp))
        Card(Modifier.fillMaxWidth().padding(top=16.dp)){ Text("Post 1 - Profile pic upload mil in a lang zel ang", Modifier.padding(16.dp)) }
    }
}
