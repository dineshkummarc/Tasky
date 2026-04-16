package com.thatsmanmeet.taskyapp.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.thatsmanmeet.taskyapp.datastore.SettingsStore
import com.thatsmanmeet.taskyapp.room.Todo
import com.thatsmanmeet.taskyapp.room.TodoViewModel
import com.thatsmanmeet.taskyapp.room.notes.Note
import com.thatsmanmeet.taskyapp.room.notes.NoteViewModel
import com.thatsmanmeet.taskyapp.ui.theme.TaskyTheme

sealed class Item {
    data class TaskItem(val task: Todo) : Item()
    data class NoteItem(val note: Note) : Item()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }

    val activity = LocalActivity.current as Activity
    val context = LocalContext.current
    val settingStore = SettingsStore(context)
    val savedThemeKey = settingStore.getThemeModeKey.collectAsState(initial = "")
    val savedFontKey = settingStore.getUseSystemFontKey.collectAsState(initial = false)
    val todoViewModel = viewModel<TodoViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodoViewModel(application = activity.application) as T
            }
        }
    )
    val notesViewModel = viewModel<NoteViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NoteViewModel(application = activity.application) as T
            }
        }
    )

    val allTasks by todoViewModel.getAllTodosFlow.collectAsState(initial = emptyList())
    val allNotes by notesViewModel.getAllNotesFlow.collectAsState(initial = emptyList())

    val filteredTasks = allTasks
        .filter { it.title?.contains(searchText, ignoreCase = true) == true || it.todoDescription?.contains(searchText, ignoreCase = true) == true }
        .map { Item.TaskItem(it) }

    val filteredNotes = allNotes
        .filter { it.title?.contains(searchText, ignoreCase = true) == true || it.body?.contains(searchText, ignoreCase = true) == true }
        .map { Item.NoteItem(it) }

    val combinedList = filteredTasks + filteredNotes
    Log.d("LIST", "SearchPage: $combinedList")

    TaskyTheme(
        darkTheme = when (savedThemeKey.value) {
            "0" -> isSystemInDarkTheme()
            "1" -> false
            else -> true
        },
        useSystemFont = savedFontKey.value!!
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = modifier.padding(10.dp),
                    title = {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = searchText,
                            onValueChange = { searchText = it },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            },
                            placeholder = { Text("Search") }
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                if (searchText.isNotEmpty()){
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(combinedList) { item ->
                            when (item) {
                                is Item.TaskItem -> {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp) // consistent circle size
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check, // or Icons.Default.Edit
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp) // smaller than circle to add padding
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = item.task.title ?: "",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                is Item.NoteItem -> {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                // redirect to the note screen
                                                navHostController.navigate(route = "edit_notes_screen/" + item.note.ID){
                                                    popUpTo(Screen.SearchScreen.route){
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                        ,
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Row(Modifier.padding(12.dp),verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp) // consistent circle size
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Create, // or Icons.Default.Edit
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp) // smaller than circle to add padding
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = item.note.title ?: "",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}