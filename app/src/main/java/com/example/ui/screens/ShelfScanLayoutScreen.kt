package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import com.example.R
import com.example.data.model.Product
import com.example.data.model.ShelfItem
import com.example.ui.theme.AppBackground
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ShelfScanLayoutScreen(
    selectedShelfNumber: Int = 1,
    products: List<Product>,
    shelfItems: List<ShelfItem>,
    selectedIndex: Int,
    onSelectShelf: (Int) -> Unit,
    onSelectProductIndex: (Int) -> Unit,
    onQuantityChanged: (String, Int) -> Unit,
    onAddMissingProduct: () -> Unit,
    onRescan: () -> Unit,
    onSaveShelf: () -> Unit,
    onBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shelf $selectedShelfNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (!cameraPermissionState.status.isGranted) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                        onRescan()
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = TealPrimaryVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rescan",
                                color = TealPrimaryVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!cameraPermissionState.status.isGranted) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                            onSelectShelf((selectedShelfNumber % 4) + 1)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("scan_next_shelf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = TealPrimaryVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan next shelf",
                            color = TealPrimaryVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onSaveShelf,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00B4D8), TealPrimary)
                                )
                            )
                            .testTag("save_shelf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save shelf",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        containerColor = AppBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Shelf Selector Pills Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..4) {
                        val isSelected = selectedShelfNumber == i
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) TealPrimary else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) TealPrimary else Color(0xFFD6E2E8)
                            ),
                            modifier = Modifier
                                .clickable { onSelectShelf(i) }
                                .testTag("shelf_pill_$i")
                        ) {
                            Text(
                                text = "Shelf $i",
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Add Shelf Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6E2E8)),
                        modifier = Modifier
                            .clickable { /* Add shelf */ }
                            .testTag("add_shelf_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Shelf",
                            tint = TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Shelf Layout Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Shelf layout",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE0F7FA)
                            ) {
                                Text(
                                    text = "${shelfItems.size} products",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimaryVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual Physical Shelf Graphic (2 Tiers)
                        VisualShelfGraphic(
                            selectedIndex = selectedIndex,
                            onSlotSelected = onSelectProductIndex,
                            onAddSlotClicked = onAddMissingProduct
                        )
                    }
                }
            }

            // Category Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Maggi / Instant Noodles",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${shelfItems.size} items",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = "Tap a product to see where it sits on the shelf",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Product List
            itemsIndexed(shelfItems) { index, item ->
                val product = products.find { it.id == item.productId }
                val slotIndex = index + 1
                val isHighlighted = selectedIndex == slotIndex

                ProductRowCard(
                    slotIndex = slotIndex,
                    product = product,
                    shelfItem = item,
                    isHighlighted = isHighlighted,
                    onSelect = { onSelectProductIndex(slotIndex) },
                    onQuantityChanged = { delta -> onQuantityChanged(item.id, delta) },
                    onEdit = onAddMissingProduct
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Add Missing Product Dashed Button
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp, bottom = 24.dp)
                        .clickable { onAddMissingProduct() }
                        .testTag("add_missing_product_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TealPrimaryVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add missing product",
                            color = TealPrimaryVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisualShelfGraphic(
    selectedIndex: Int,
    onSlotSelected: (Int) -> Unit,
    onAddSlotClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp)
    ) {
        // ROW 1 (TOP)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Row 1\n(top)",
                fontSize = 10.sp,
                color = TextMuted,
                lineHeight = 12.sp,
                modifier = Modifier.width(42.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShelfSlotItem(
                    index = 1,
                    iconRes = R.drawable.ic_product_maggi_noodles,
                    isSelected = selectedIndex == 1,
                    onClick = { onSlotSelected(1) }
                )
                ShelfSlotItem(
                    index = 2,
                    iconRes = R.drawable.ic_product_maggi_atta,
                    isSelected = selectedIndex == 2,
                    onClick = { onSlotSelected(2) }
                )
                ShelfSlotItem(
                    index = 3,
                    iconRes = R.drawable.ic_product_maggi_cup,
                    isSelected = selectedIndex == 3,
                    onClick = { onSlotSelected(3) }
                )
            }
        }

        // Metal Shelf Plank Bar (Top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
                    )
                )
        )

        // ROW 2 (BOTTOM)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Row 2\n(bottom)",
                fontSize = 10.sp,
                color = TextMuted,
                lineHeight = 12.sp,
                modifier = Modifier.width(42.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShelfSlotItem(
                    index = 4,
                    iconRes = R.drawable.ic_product_maggi_pazzta,
                    isSelected = selectedIndex == 4,
                    onClick = { onSlotSelected(4) }
                )
                ShelfSlotItem(
                    index = 5,
                    iconRes = R.drawable.ic_product_maggi_magic,
                    isSelected = selectedIndex == 5,
                    onClick = { onSlotSelected(5) }
                )
                // Empty slot with +
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .size(68.dp)
                        .clickable { onAddSlotClicked() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add slot",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Metal Shelf Plank Bar (Bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFCBD5E1), Color(0xFF64748B))
                    )
                )
        )
    }
}

@Composable
fun ShelfSlotItem(
    index: Int,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) TealPrimary else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .testTag("shelf_slot_$index"),
        contentAlignment = Alignment.Center
    ) {
        // Product Thumbnail
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Product $index",
            modifier = Modifier.size(48.dp)
        )

        // Slot Badge
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(18.dp)
                .background(if (isSelected) TealPrimary else Color(0xFF00B4D8), CircleShape)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProductRowCard(
    slotIndex: Int,
    product: Product?,
    shelfItem: ShelfItem,
    isHighlighted: Boolean,
    onSelect: () -> Unit,
    onQuantityChanged: (Int) -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFFF0FDF4) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.dp,
            color = if (isHighlighted) TealPrimary else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("product_row_$slotIndex")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge number circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF00B4D8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$slotIndex",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Thumbnail
            val iconRes = when (product?.iconResName) {
                "ic_product_maggi_atta" -> R.drawable.ic_product_maggi_atta
                "ic_product_maggi_cup" -> R.drawable.ic_product_maggi_cup
                "ic_product_maggi_pazzta" -> R.drawable.ic_product_maggi_pazzta
                "ic_product_maggi_magic" -> R.drawable.ic_product_maggi_magic
                else -> R.drawable.ic_product_maggi_noodles
            }

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Title & Location & Confidence
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product?.name ?: "Product $slotIndex",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Row ${shelfItem.rowNumber} · Slot ${shelfItem.slotNumber}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F7FA)
                    ) {
                        Text(
                            text = "✦ ${(shelfItem.confidence * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimaryVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Edit Pencil
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp).testTag("edit_product_$slotIndex")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Quantity Stepper: - 24 +
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onQuantityChanged(-1) }
                        .testTag("qty_minus_$slotIndex")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Minus",
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = "${shelfItem.quantity}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onQuantityChanged(1) }
                        .testTag("qty_plus_$slotIndex")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Plus",
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
