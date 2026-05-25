package com.example.ui

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.Booking
import com.example.data.WashProvider
import com.example.data.WashService
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UgaWashApp(
    modifier: Modifier = Modifier,
    viewModel: UgaWashViewModel = viewModel(
        factory = UgaWashViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val selectedDistrict by viewModel.selectedDistrict.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val paymentState by viewModel.paymentState.collectAsStateWithLifecycle()
    val activeBooking by viewModel.activeBookingDetails.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Find Washbays, 1 = My Bookings
    var selectedProviderForDetail by remember { mutableStateOf<WashProvider?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedServiceForCheckout by remember { mutableStateOf<WashService?>(null) }
    var verificationBookingToVerify by remember { mutableStateOf<Booking?>(null) }

    val districts = listOf("All", "Kampala Central", "Kawempe / Makerere", "Nakawa / Wakiso", "Entebbe", "Jinja", "Rubaga")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(SparkleGold, LiquidCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalCarWash,
                                contentDescription = null,
                                tint = DeepObsidian,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "UgaWash",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteClean,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = LiquidCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Makerere Hill, Kampala",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .border(1.dp, DividerDark, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Text(
                                text = "GPS Live",
                                style = MaterialTheme.typography.labelSmall,
                                color = WhiteClean,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepObsidian,
                    titleContentColor = WhiteClean
                )
            )
        },
        bottomBar = {
            Surface(
                color = DeepObsidian,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, DividerDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val activeBookingsCount = bookings.count { it.paymentStatus == "PAID" }
                    
                    BottomNavTabItem(
                        selected = activeTab == 0,
                        icon = Icons.Rounded.Search,
                        label = "Find Washbays",
                        tag = "tab_find",
                        onClick = { activeTab = 0 }
                    )
                    
                    BottomNavTabItem(
                        selected = activeTab == 1,
                        icon = Icons.Rounded.ReceiptLong,
                        label = "My Bookings",
                        badgeCount = activeBookingsCount,
                        tag = "tab_bookings",
                        onClick = { activeTab = 1 }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepObsidian)
                .padding(innerPadding)
        ) {
            if (activeTab == 0) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search & Location Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search wash bays, streets Kampala...", color = SlateGray) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null, tint = SlateGray)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Rounded.Close, contentDescription = null, tint = SlateGray)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = SparkleGold,
                                unfocusedBorderColor = DividerDark,
                                focusedTextColor = WhiteClean,
                                unfocusedTextColor = WhiteClean
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input")
                        )
                    }

                    // Districts Horizontal Scroll using Custom Chips (avoids Material Chip version conflicts)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(districts) { district ->
                            val isSelected = selectedDistrict == district
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SparkleGold else CardDark)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) SparkleGold else DividerDark,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectDistrict(district) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = district,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DeepObsidian else SlateGray
                                )
                            }
                        }
                    }

                    // List of Providers
                    if (providers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SearchOff,
                                    contentDescription = null,
                                    tint = SlateGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "No Washbays Found",
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteClean,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Try clearing your filters or search keywords.",
                                    color = SlateGray,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(providers) { provider ->
                                ProviderCard(
                                    provider = provider,
                                    distanceKm = viewModel.calculateDistance(provider.latitude, provider.longitude),
                                    onSelect = {
                                        selectedProviderForDetail = provider
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                MyBookingsSection(
                    bookings = bookings,
                    onCancelBooking = { viewModel.cancelBooking(it) },
                    onVerifyArrival = { verificationBookingToVerify = it }
                )
            }
        }
    }

    // Detail Dialog
    selectedProviderForDetail?.let { provider ->
        ProviderDetailDialog(
            provider = provider,
            distanceKm = viewModel.calculateDistance(provider.latitude, provider.longitude),
            onDismiss = { selectedProviderForDetail = null },
            onServiceSelected = { service ->
                selectedServiceForCheckout = service
                viewModel.startBooking(provider, service)
                showCheckoutDialog = true
                selectedProviderForDetail = null
            }
        )
    }

    // Booking Checkout Form
    if (showCheckoutDialog && activeBooking != null) {
        BookingCheckoutDialog(
            activeBooking = activeBooking!!,
            paymentState = paymentState,
            onDismiss = {
                showCheckoutDialog = false
                viewModel.clearActiveBooking()
            },
            onPayClick = { carPlate, carModel, telecomNum, telecomProvider, date, time ->
                viewModel.initiateMobileMoneyPayment(
                    carPlate = carPlate,
                    carModel = carModel,
                    mobileNumber = telecomNum,
                    paymentProvider = telecomProvider,
                    scheduledDate = date,
                    scheduledTime = time
                )
            }
        )
    }

    // Station checklist verifying
    verificationBookingToVerify?.let { booking ->
        AttendantVerificationDialog(
            booking = booking,
            onDismiss = { verificationBookingToVerify = null },
            onSubmitVerification = { pin ->
                val success = viewModel.verifyBayBooking(booking, pin)
                if (success) {
                    verificationBookingToVerify = null
                }
                success
            }
        )
    }
}

@Composable
fun BottomNavTabItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeCount: Int = 0,
    tag: String,
    onClick: () -> Unit
) {
    val contentColor = if (selected) SparkleGold else SlateGray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AccentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun ProviderCard(
    provider: WashProvider,
    distanceKm: Float,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_card_${provider.id}"),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        border = BorderStroke(1.dp, DividerDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (provider.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = provider.imageUrl,
                        contentDescription = provider.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CardDark, DividerDark)
                                )
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = LiquidCyan.copy(alpha = 0.08f),
                                radius = 220f,
                                center = Offset(size.width * 0.9f, size.height * 0.3f)
                            )
                            drawCircle(
                                color = SparkleGold.copy(alpha = 0.05f),
                                radius = 120f,
                                center = Offset(size.width * 0.1f, size.height * 0.8f)
                            )
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepObsidian.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Navigation,
                            contentDescription = null,
                            tint = LiquidCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f km away", distanceKm),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WhiteClean
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepObsidian.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = SparkleGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = provider.rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteClean
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhiteClean,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = provider.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (provider.isVerified) SuccessGreen.copy(alpha = 0.12f) else SlateGray.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (provider.isVerified) Icons.Rounded.VerifiedUser else Icons.Rounded.Info,
                        contentDescription = null,
                        tint = if (provider.isVerified) SuccessGreen else SlateGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (provider.isVerified) "Verified detailing agency" else "Unverified Listing",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (provider.isVerified) SuccessGreen else SlateGray
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderDetailDialog(
    provider: WashProvider,
    distanceKm: Float,
    onDismiss: () -> Unit,
    onServiceSelected: (WashService) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .offset(y = 120.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = DeepObsidian,
            border = BorderStroke(1.dp, DividerDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(DividerDark)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = WhiteClean,
                            fontSize = 20.sp
                        )
                        Text(
                            text = provider.locationName,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGray
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CardDark)
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = WhiteClean)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (provider.isVerified) SuccessGreen.copy(alpha = 0.08f)
                            else AccentRed.copy(alpha = 0.05f)
                        )
                        .border(
                            1.dp,
                            if (provider.isVerified) SuccessGreen.copy(alpha = 0.3f)
                            else AccentRed.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (provider.isVerified) Icons.Rounded.GppGood else Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = if (provider.isVerified) SuccessGreen else AccentRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (provider.isVerified) "Local Service Verification" else "Verification Standby",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (provider.isVerified) SuccessGreen else AccentRed
                            )
                            Text(
                                text = provider.verificationReason,
                                fontSize = 12.sp,
                                color = SlateGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Select Detailing Package:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SparkleGold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(provider.parseServices()) { service ->
                        ServiceItemRow(
                            service = service,
                            onBookClick = { onServiceSelected(service) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, tint = LiquidCyan)
                        Column {
                            Text("Direct Contact", fontSize = 11.sp, color = SlateGray)
                            Text(provider.phone, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WhiteClean)
                        }
                    }
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DividerDark)
                    ) {
                        Text("Call Bay", fontSize = 12.sp, color = WhiteClean)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceItemRow(
    service: WashService,
    onBookClick: () -> Unit
) {
    Card(
        onClick = onBookClick,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, DividerDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("service_row_${service.name.replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = WhiteClean
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        service.name.lowercase().contains("silver") || service.name.lowercase().contains("standard") -> "Body exterior & inside clean"
                        service.name.lowercase().contains("steam") || service.name.lowercase().contains("under") -> "Stain remove + engine steam blast"
                        else -> "Premium structural restore & high wax"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.US).format(service.priceUGX)} UGX",
                    fontWeight = FontWeight.Black,
                    color = SparkleGold,
                    fontSize = 15.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LiquidCyan)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Book",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepObsidian
                    )
                }
            }
        }
    }
}

@Composable
fun BookingCheckoutDialog(
    activeBooking: ActiveBooking,
    paymentState: PaymentState,
    onDismiss: () -> Unit,
    onPayClick: (carPlate: String, carModel: String, num: String, telecom: String, date: String, time: String) -> Unit
) {
    var carPlate by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var telecomNum by remember { mutableStateOf("") }
    var telecomProvider by remember { mutableStateOf("MTN") }
    var selectedDate by remember { mutableStateOf("Today, 25 May") }
    var selectedTime by remember { mutableStateOf("10:00 AM") }

    val dateOptions = listOf("Today, 25 May", "Tomorrow, 26 May", "Wed, 27 May")
    val timeOptions = listOf("09:00 AM", "11:00 AM", "01:30 PM", "04:00 PM")

    val isNumValid = telecomNum.length >= 10 && (
        telecomNum.startsWith("077") || telecomNum.startsWith("078") || telecomNum.startsWith("076") || telecomNum.startsWith("039") ||
        telecomNum.startsWith("075") || telecomNum.startsWith("070") || telecomNum.startsWith("074")
    )
    val isPlateValid = carPlate.isNotEmpty() && carPlate.length >= 5
    val canSubmit = isNumValid && isPlateValid && carModel.trim().isNotEmpty()

    Dialog(
        onDismissRequest = {
            if (paymentState is PaymentState.Idle || paymentState is PaymentState.Success) {
                onDismiss()
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidian,
            border = BorderStroke(1.dp, DividerDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (paymentState is PaymentState.Idle) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Confirm Booking", fontWeight = FontWeight.Black, fontSize = 20.sp, color = WhiteClean)
                            Text("Direct mobile money settlement", fontSize = 11.sp, color = SlateGray)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = WhiteClean)
                        }
                    }

                    Divider(color = DividerDark)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(activeBooking.provider.name, fontSize = 11.sp, color = SlateGray)
                                Text(activeBooking.service.name, fontWeight = FontWeight.Bold, color = WhiteClean, fontSize = 15.sp)
                            }
                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.US).format(activeBooking.service.priceUGX)} UGX",
                                fontWeight = FontWeight.Black,
                                color = SparkleGold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = carModel,
                            onValueChange = { carModel = it },
                            label = { Text("Car Model (e.g., Harrier)", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteClean,
                                unfocusedTextColor = WhiteClean,
                                focusedBorderColor = SparkleGold,
                                unfocusedBorderColor = DividerDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f).testTag("car_model_input")
                        )
                        
                        OutlinedTextField(
                            value = carPlate,
                            onValueChange = { carPlate = it },
                            label = { Text("Plate No.", fontSize = 12.sp) },
                            placeholder = { Text("UAI 019X", color = SlateGray.copy(alpha = 0.5f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteClean,
                                unfocusedTextColor = WhiteClean,
                                focusedBorderColor = SparkleGold,
                                unfocusedBorderColor = DividerDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("plate_input")
                        )
                    }

                    Column {
                        Text("Reservation Date:", fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            dateOptions.forEach { date ->
                                val dateSelected = selectedDate == date
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (dateSelected) SparkleGold else CardDark)
                                        .border(BorderStroke(1.dp, if (dateSelected) SparkleGold else DividerDark), RoundedCornerShape(8.dp))
                                        .clickable { selectedDate = date }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = date,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dateSelected) DeepObsidian else SlateGray
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Text("Session Slot:", fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            timeOptions.forEach { time ->
                                val timeSelected = selectedTime == time
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (timeSelected) SparkleGold else CardDark)
                                        .border(BorderStroke(1.dp, if (timeSelected) SparkleGold else DividerDark), RoundedCornerShape(8.dp))
                                        .clickable { selectedTime = time }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timeSelected) DeepObsidian else SlateGray
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = DividerDark)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Uganda Mobile Money Number:",
                            fontSize = 12.sp,
                            color = SlateGray,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (telecomProvider == "MTN") AccentYellow.copy(alpha = 0.15f) else CardDark)
                                    .border(
                                        width = 1.dp,
                                        color = if (telecomProvider == "MTN") AccentYellow else DividerDark,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { telecomProvider = "MTN" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentYellow))
                                    Text("MTN MoMo", color = WhiteClean, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (telecomProvider == "Airtel") AccentRed.copy(alpha = 0.15f) else CardDark)
                                    .border(
                                        width = 1.dp,
                                        color = if (telecomProvider == "Airtel") AccentRed else DividerDark,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { telecomProvider = "Airtel" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentRed))
                                    Text("Airtel Money", color = WhiteClean, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = telecomNum,
                            onValueChange = { if (it.length <= 10) telecomNum = it },
                            placeholder = { Text("e.g. 0771234567 or 0751234567", color = SlateGray.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Rounded.Smartphone, contentDescription = null, tint = SlateGray)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteClean,
                                unfocusedTextColor = WhiteClean,
                                focusedBorderColor = if (telecomProvider == "MTN") AccentYellow else AccentRed,
                                unfocusedBorderColor = DividerDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("phone_input")
                        )

                        if (telecomNum.isNotEmpty() && !isNumValid) {
                            Text(
                                text = "Enter valid Uganda telecom mobile number (077/078/076/075/070...)",
                                color = AccentRed,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (canSubmit) {
                                onPayClick(carPlate, carModel, telecomNum, telecomProvider, selectedDate, selectedTime)
                            }
                        },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (telecomProvider == "MTN") AccentYellow else AccentRed,
                            contentColor = DeepObsidian,
                            disabledContainerColor = DividerDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("pay_button")
                    ) {
                        Text(
                            text = "Authorize MoMo Settlement",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = if (canSubmit) DeepObsidian else SlateGray
                        )
                    }
                } else {
                    MobileMoneyProcessingView(
                        state = paymentState,
                        priceUGX = activeBooking.service.priceUGX,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun MobileMoneyProcessingView(
    state: PaymentState,
    priceUGX: Int,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (state) {
            PaymentState.Initiating -> {
                CircularProgressIndicator(color = SparkleGold, modifier = Modifier.size(48.dp))
                Text(
                    text = "Initiating Secure Handshake...",
                    fontWeight = FontWeight.Bold,
                    color = WhiteClean,
                    fontSize = 16.sp
                )
                Text(
                    text = "Connecting to telecom gateway server...",
                    color = SlateGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            is PaymentState.PromptSent -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (state.telecom == "MTN") Color(0xFFFFCC00) else Color(0xFFD32F2F))
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = if (state.telecom == "MTN") Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.telecom == "MTN") "MTN MoMo Push" else "Airtel Secure Prompt",
                                fontWeight = FontWeight.Bold,
                                color = if (state.telecom == "MTN") Color.Black else Color.White,
                                fontSize = 13.sp
                            )
                        }
                        
                        Divider(color = if (state.telecom == "MTN") Color.Black.copy(0.15f) else Color.White.copy(0.15f))
                        
                        Text(
                            text = "Uganda Telecom prompt sent to ${state.number}.\nPlease check phone screen to authorize charge of UGX ${NumberFormat.getNumberInstance(Locale.US).format(priceUGX)} to UgaWash Bookings.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = if (state.telecom == "MTN") Color.Black else Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (state.telecom == "MTN") Color.Black else Color.White)
                            )
                            Text(
                                text = "Awaiting PIN entry popup...",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.telecom == "MTN") Color.Black.copy(0.6f) else Color.White.copy(0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            PaymentState.Processing -> {
                CircularProgressIndicator(color = LiquidCyan, modifier = Modifier.size(48.dp))
                Text(
                    text = "Confirming Settlement...",
                    fontWeight = FontWeight.Bold,
                    color = WhiteClean,
                    fontSize = 16.sp
                )
                Text(
                    text = "Waiting for telecom callback confirmation...",
                    color = SlateGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            is PaymentState.Success -> {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "Payment Approved!",
                    fontWeight = FontWeight.Black,
                    color = WhiteClean,
                    fontSize = 20.sp
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(BorderStroke(1.dp, SuccessGreen.copy(0.5f)), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "STATION CHECK-IN TICKET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = state.pin,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = SparkleGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Show this PIN code to the attendant when you arrive.",
                            fontSize = 11.sp,
                            color = SlateGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Got It, Go To Bookings", color = DeepObsidian, fontWeight = FontWeight.Bold)
                }
            }

            is PaymentState.Failed -> {
                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Settlement Declined",
                    fontWeight = FontWeight.Bold,
                    color = WhiteClean,
                    fontSize = 18.sp
                )
                Text(
                    text = state.error,
                    color = SlateGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Try Again", color = WhiteClean)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun MyBookingsSection(
    bookings: List<Booking>,
    onCancelBooking: (Int) -> Unit,
    onVerifyArrival: (Booking) -> Unit
) {
    if (bookings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = SlateGray,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Bookings Yet",
                    fontWeight = FontWeight.Bold,
                    color = WhiteClean,
                    fontSize = 18.sp
                )
                Text(
                    text = "Schedule standard car washes & premium detailing detailing around Kampala instantly.",
                    color = SlateGray,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(bookings) { booking ->
                BookingCard(
                    booking = booking,
                    onCancelClick = { onCancelBooking(booking.id) },
                    onVerifyClick = { onVerifyArrival(booking) }
                )
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onCancelClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    val isUsed = booking.paymentStatus == "USED/VERIFIED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_card_${booking.id}"),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, DividerDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DividerDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isUsed) Icons.Rounded.CheckCircle else Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = if (isUsed) SuccessGreen else SparkleGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isUsed) "Session Verified (Redeemed)" else "Active Scheduled Wash",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUsed) SuccessGreen else SparkleGold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isUsed) SuccessGreen.copy(0.12f)
                            else LiquidCyan.copy(0.12f)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isUsed) "VERIFIED" else "PAID (MoMo)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isUsed) SuccessGreen else LiquidCyan
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Text(
                        text = booking.providerName,
                        fontWeight = FontWeight.ExtraBold,
                        color = WhiteClean,
                        fontSize = 16.sp
                    )
                    Text(
                        text = booking.providerLocation,
                        color = SlateGray,
                        fontSize = 11.sp
                    )
                }

                Divider(color = DividerDark.copy(alpha = 0.5f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Service Scheduled", fontSize = 10.sp, color = SlateGray)
                        Text(booking.serviceName, color = WhiteClean, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Vehicle Details", fontSize = 10.sp, color = SlateGray)
                        Text("${booking.carModel} • ${booking.carPlate}", color = WhiteClean, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Arrival Date & Time", fontSize = 10.sp, color = SlateGray)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = LiquidCyan, modifier = Modifier.size(12.dp))
                            Text("${booking.scheduledDate} at ${booking.scheduledTime}", color = WhiteClean, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Amount Rendered", fontSize = 10.sp, color = SlateGray)
                        Text("${NumberFormat.getNumberInstance(Locale.US).format(booking.priceUGX)} UGX", color = SparkleGold, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }

                // Barcode/ticket dashed split
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    drawLine(
                        color = DividerDark,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepObsidian)
                        .border(1.dp, DividerDark, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("STATION BAY REDEEM CODE", fontSize = 9.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                        Text(
                            text = booking.verificationCode,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (isUsed) SlateGray else SparkleGold
                        )
                    }
                    
                    if (!isUsed) {
                        Button(
                            onClick = onVerifyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SparkleGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Verify Arrival",
                                color = DeepObsidian,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Text("Service Completed", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                if (!isUsed) {
                    Text(
                        text = "Cancel Appointment",
                        color = AccentRed.copy(0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCancelClick() }
                            .padding(top = 4.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendantVerificationDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmitVerification: (String) -> Boolean
) {
    var code1 by remember { mutableStateOf("") }
    var code2 by remember { mutableStateOf("") }
    var code3 by remember { mutableStateOf("") }
    var code4 by remember { mutableStateOf("") }
    
    var isFailedAttempt by remember { mutableStateOf(false) }

    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidian,
            border = BorderStroke(1.dp, DividerDark)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = SparkleGold,
                    modifier = Modifier.size(48.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Station Provider Verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WhiteClean
                    )
                    Text(
                        text = "Enter the user's 4-digit check-in PIN to complete and verify their car wash session:",
                        color = SlateGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tfColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteClean,
                        unfocusedTextColor = WhiteClean,
                        focusedBorderColor = SparkleGold,
                        unfocusedBorderColor = DividerDark,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    )

                    SingleDigitInputField(
                        value = code1,
                        onValueChange = {
                            if (it.length <= 1) {
                                code1 = it
                                if (it.isNotEmpty()) focusRequester2.requestFocus()
                            }
                        },
                        focusRequester = focusRequester1,
                        colors = tfColors,
                        modifier = Modifier.weight(1f).testTag("verification_digit_1")
                    )

                    SingleDigitInputField(
                        value = code2,
                        onValueChange = {
                            if (it.length <= 1) {
                                code2 = it
                                if (it.isNotEmpty()) {
                                    focusRequester3.requestFocus()
                                } else {
                                    focusRequester1.requestFocus()
                                }
                            }
                        },
                        focusRequester = focusRequester2,
                        colors = tfColors,
                        modifier = Modifier.weight(1f).testTag("verification_digit_2")
                    )

                    SingleDigitInputField(
                        value = code3,
                        onValueChange = {
                            if (it.length <= 1) {
                                code3 = it
                                if (it.isNotEmpty()) {
                                    focusRequester4.requestFocus()
                                } else {
                                    focusRequester2.requestFocus()
                                }
                            }
                        },
                        focusRequester = focusRequester3,
                        colors = tfColors,
                        modifier = Modifier.weight(1f).testTag("verification_digit_3")
                    )

                    SingleDigitInputField(
                        value = code4,
                        onValueChange = {
                            if (it.length <= 1) {
                                code4 = it
                                if (it.isEmpty()) {
                                    focusRequester3.requestFocus()
                                }
                            }
                        },
                        focusRequester = focusRequester4,
                        colors = tfColors,
                        modifier = Modifier.weight(1f).testTag("verification_digit_4")
                    )
                }

                if (isFailedAttempt) {
                    Text(
                        text = "Invalid check-in security code. Please check again.",
                        color = AccentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = DividerDark)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, DividerDark)
                    ) {
                        Text("Cancel", color = SlateGray)
                    }

                    Button(
                        onClick = {
                            val code = "$code1$code2$code3$code4".trim()
                            val isOk = onSubmitVerification(code)
                            if (!isOk) {
                                isFailedAttempt = true
                                code1 = ""
                                code2 = ""
                                code3 = ""
                                code4 = ""
                                focusRequester1.requestFocus()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_verification_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Redeem", color = DeepObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SingleDigitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    colors: TextFieldColors,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = colors,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .focusRequester(focusRequester)
            .aspectRatio(1f)
    )
}
