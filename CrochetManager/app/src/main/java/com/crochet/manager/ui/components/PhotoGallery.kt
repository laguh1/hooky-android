package com.crochet.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * A reusable horizontal photo gallery with pager dots and optional delete support.
 *
 * @param photos         List of absolute file paths to display
 * @param height         Height of the gallery area
 * @param onDeletePhoto  If non-null, shows a trash icon per photo; tapping it asks for
 *                       confirmation then calls this lambda with the file path.
 * @param modifier       Modifier applied to the outer Box
 */
@Composable
fun PhotoGallery(
    photos: List<String>,
    height: Dp = 220.dp,
    onDeletePhoto: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var photoToDelete by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (photos.isEmpty()) {
            // Empty state placeholder — dark grey with subtle inner border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2D2D2D)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF383838))
                )
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { photos.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val path = photos[page]

                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = java.io.File(path),
                        contentDescription = "Photo ${page + 1} of ${photos.size}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Delete button — top-right, only when onDeletePhoto is provided
                    if (onDeletePhoto != null) {
                        IconButton(
                            onClick = { photoToDelete = path },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Pager indicator dots
            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(photos.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White
                                    else Color.White.copy(alpha = 0.45f)
                                )
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for delete
    photoToDelete?.let { path ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("Delete photo?") },
            text = { Text("This photo will be removed. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePhoto?.invoke(path)
                        photoToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
