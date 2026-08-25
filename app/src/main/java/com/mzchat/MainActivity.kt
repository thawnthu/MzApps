package com.mzchat.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
        setContent { MzChatProFull() }
    }
}

@Composable
fun MzChatProFull() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var myName by remember { mutableStateOf("Thawnthu") }
    var myPic by remember { mutableStateOf("") }

    if (!isLoggedIn) {
        LoginFullScreen(onDone = { name -> myName = name; isLoggedIn = true })
    } else {
        MainAppFull(myName)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginFullScreen(onDone: (String) -> Unit) {
    var step by remember { mutableStateOf(1) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Scaffold { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MzChat PRO", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF25D366))
            Spacer(Modifier.height(30.dp))
            when (step) {
                1 -> {
                    Text("Phone Number", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(phone, { phone = it }, label = { Text("+91 1234567890") }, modifier = Modifier.fillMaxWidth().padding(top=16.dp))
                    Button(onClick = { step = 2 }, Modifier.fillMaxWidth().padding(top=20.dp).height(50.dp)) { Text("SEND OTP") }
                }
                2 -> {
                    Text("OTP Verification", style = MaterialTheme.typography.titleLarge)
                    Text("OTP: ${phone} ah thawn a ni")
                    OutlinedTextField(otp, { otp = it }, label = { Text("6 Digit OTP") }, modifier = Modifier.fillMaxWidth().padding(top=16.dp))
                    Button(onClick = { step = 3 }, Modifier.fillMaxWidth().padding(top=20.dp).height(50.dp)) { Text("VERIFY OTP") }
                }
                3 -> {
                    Text("Profile Setup", style = MaterialTheme.typography.titleLarge)
                    Box(Modifier.size(100.dp).clip(CircleShape).background(Color.Gray).clickable{}, contentAlignment=Alignment.Center){ Text("Add Pic") }
                    OutlinedTextField(name, { name = it }, label = { Text("Your Name") }, modifier = Modifier.fillMaxWidth().padding(top=16.dp))
                    OutlinedTextField("", {}, label = { Text("About (Optional)") }, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                    Button(onClick = { onDone(name.ifEmpty{"Thawnthu"}) }, Modifier.fillMaxWidth().padding(top=20.dp).height(50.dp)) { Text("GO TO HOME") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppFull(myName: String) {
    var tab by remember { mutableStateOf(0) }
    var showUsers by remember { mutableStateOf(false) }
    val titles = listOf("Home", "Chat", "Status", "Online", "Profile")

    Scaffold(
        topBar = { TopAppBar(title={Text(titles[tab])}) },
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
                0 -> HomeScreenFull()
                1 -> ChatScreenFull(myName, onPlusClick={showUsers=true})
                2 -> StatusScreenFull()
                3 -> OnlineScreenFull()
                4 -> ProfileScreenFull(myName)
            }
            if(showUsers) {
                AlertDialog(onDismissRequest={showUsers=false}, title={Text("New Chat - Select User")},
                    text={ LazyColumn{ items(20){ i-> ListItem(modifier=Modifier.clickable{showUsers=false}, headlineContent={Text("User ${i+1}")}, leadingContent={Box(Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))}, supportingContent={Text("Tap to chat")}) } } },
                    confirmButton={ TextButton(onClick={showUsers=false}){Text("Close")} }
                )
            }
        }
    }
}

@Composable
fun HomeScreenFull() {
    var search by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(search, {search=it}, Modifier.fillMaxWidth().padding(8.dp), placeholder={Text("🔍 All Search...")}, singleLine=true)
        LazyColumn {
            items(20) { i->
                Card(Modifier.fillMaxWidth().padding(8.dp).clickable{}) {
                    Row(Modifier.padding(12.dp)) {
                        Box(Modifier.size(45.dp).clip(CircleShape).background(Color.Gray))
                        Column(Modifier.padding(start=12.dp)) {
                            Row { Text("Friend ${i+1}", style=MaterialTheme.typography.titleMedium); Spacer(Modifier.width(8.dp)); Text("• 2h ago", color=Color.Gray) }
                            Text("Heihi ka post a ni - pic leh thu a lang - profile click theih")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreenFull(myName: String, onPlusClick: ()->Unit) {
    var search by remember { mutableStateOf("") }
    Column {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment=Alignment.CenterVertically) {
            OutlinedTextField(search, {search=it}, Modifier.weight(1f), placeholder={Text("🔍 Chat Search highlight...")}, singleLine=true)
            IconButton(onClick=onPlusClick){ Icon(Icons.Filled.AddCircle, "Chat+", tint=Color(0xFF25D366), modifier=Modifier.size(36.dp)) }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(15) { i->
                ListItem(
                    modifier=Modifier.clickable{},
                    leadingContent={ Box(Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment=Alignment.Center){ Text("F${i+1}") } },
                    headlineContent={ Text("Friend ${i+1}") },
                    supportingContent={ 
                        when(i) {
                            0-> Text("Typing...", color=Color(0xFF25D366))
                            1-> Text("✓✓ Last message - double tick")
                            else -> Text("Last seen today at 10:30 AM")
                        }
                    },
                    trailingContent={ Text(if(i<3) "✓✓" else "10:30") }
                )
                Divider()
            }
        }
    }
}

@Composable fun StatusScreenFull() {
    var search by remember { mutableStateOf("") }
    Column(Modifier.padding(8.dp)) {
        OutlinedTextField(search, {search=it}, Modifier.fillMaxWidth(), placeholder={Text("🔍 Search Status...")})
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth().clickable{}){ Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(50.dp).clip(CircleShape).background(Color.Gray)); Column(Modifier.padding(start=12.dp)){ Text("My Status"); Text("Tap to add status update - WhatsApp ang", color=Color.Gray) } } }
        Text("Recent updates", Modifier.padding(16.dp))
    }
}

@Composable fun OnlineScreenFull() {
    var search by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(search, {search=it}, Modifier.fillMaxWidth().padding(8.dp), placeholder={Text("🔍 Search Online...")})
        LazyColumn {
            items(20){ i->
                ListItem(
                    leadingContent={ Box(Modifier.size(45.dp).clip(CircleShape).background(Color.LightGray)) },
                    headlineContent={ Row(verticalAlignment=Alignment.CenterVertically){ Text("Friend ${i+1}"); Spacer(Modifier.width(8.dp)); Box(Modifier.size(10.dp).clip(CircleShape).background(Color.Green)) } },
                    supportingContent={ Text("Online - Active now") },
                    trailingContent={ Text("Online") }
                )
            }
        }
    }
}

@Composable fun ProfileScreenFull(myName: String) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(90.dp).clip(CircleShape).background(Color(0xFF25D366)), contentAlignment=Alignment.Center){ Text(myName.take(1), style=MaterialTheme.typography.headlineLarge, color=Color.White) }
            Column(Modifier.padding(start=20.dp)) {
                Text(myName, style=MaterialTheme.typography.headlineSmall)
                Row(Modifier.padding(top=8.dp), horizontalArrangement=Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("25", style=MaterialTheme.typography.titleLarge); Text("Posts") }
                    Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("1.2k", style=MaterialTheme.typography.titleLarge); Text("Friends") }
                }
            }
        }
        Text("About: Mizo | Developer | Auto profile pic leh hming a lang", Modifier.padding(top=16.dp))
        Divider(Modifier.padding(vertical=16.dp))
        Text("Ka Posts zawng zawng:", style=MaterialTheme.typography.titleMedium)
        LazyColumn { items(10){ Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){ Text("Post ${it+1} - Profile pic mil in a lang zel ang", Modifier.padding(12.dp)) } } }
    }
}
