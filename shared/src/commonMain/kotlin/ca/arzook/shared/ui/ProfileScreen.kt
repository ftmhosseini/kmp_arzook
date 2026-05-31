package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.Result
import ca.arzook.shared.model.AuthenticatedData
import ca.arzook.shared.model.UpdateProfileRequest
import ca.arzook.shared.repository.ArzookRepositoryImpl
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(
    user: AuthenticatedData?,
    isLoggedIn: Boolean = false,
    authViewModel: AuthViewModel? = null,
    onBack: () -> Unit,
    token: String = "",
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "Back")
        }
        Text("Profile", style = MaterialTheme.typography.titleMedium)
    }
    HorizontalDivider()
    if (user == null) {
        Box(Modifier.fillMaxSize().background(Cream40), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)) {
                Icon(Icons.Filled.AccountCircle, null, Modifier.size(64.dp), tint = Color.Gray)
                Spacer(Modifier.height(12.dp))
                if (isLoggedIn) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading profile...", color = Color.Gray)
                } else {
                    Text("Please log in to view your profile", color = Color.Gray)
                }
            }
        }
        return
    }

    val repo = remember { ArzookRepositoryImpl(baseUrl = "https://api.arzook.ca") }
    val scope = rememberCoroutineScope()
    var uploadMsg by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var photoIdUploaded by remember { mutableStateOf(false) }
    var utilityBillUploaded by remember { mutableStateOf(false) }

    val photoIdPicker = rememberFilePicker { bytes, name ->
        uploading = true; uploadMsg = null
        scope.launch {
            val r = repo.uploadPhotoId(token, bytes, name)
            uploadMsg = if (r is Result.Success) "Photo ID uploaded ✓" else "Upload failed"
            uploading = false
            if (r is Result.Success) {
                photoIdUploaded = true
                kotlinx.coroutines.delay(2000)
                authViewModel?.loadUserDetails(token)
            }
        }
    }

    val utilityBillPicker = rememberFilePicker { bytes, name ->
        uploading = true; uploadMsg = null
        scope.launch {
            val r = repo.uploadUtilityBill(token, bytes, name)
            uploadMsg = if (r is Result.Success) "Utility bill uploaded ✓" else "Upload failed"
            uploading = false
            if (r is Result.Success) {
                utilityBillUploaded = true
                kotlinx.coroutines.delay(2000)
                authViewModel?.loadUserDetails(token)
            }
        }
    }

    var showChangePassword by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    // Editable fields
    var phone by remember(user) { mutableStateOf(user.phoneNumber ?: "") }
    var birthday by remember(user) { mutableStateOf(user.birthday ?: "") }
    var occupation by remember(user) { mutableStateOf(user.occupationStr ?: "") }
    var address by remember(user) { mutableStateOf(user.addressStr ?: "") }
    var city by remember(user) { mutableStateOf(user.cityStr ?: "") }
    var postalCode by remember(user) { mutableStateOf(user.postalCodeStr ?: "") }

    Column(
        modifier = Modifier.padding(16.dp).background(Cream40).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!user.pictureUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(user.pictureUrl),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(CircleShape)
            )
        } else {
            Icon(Icons.Filled.AccountCircle, null, Modifier.size(80.dp), tint = Brown)
        }
        Spacer(Modifier.height(8.dp))
        Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Email, null, tint = Brown, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(user.email, color = Color.DarkGray)
        }
        user.customerDepositId?.let {
            Spacer(Modifier.height(4.dp))
            Text("Customer ID: $it", color = Color.DarkGray, fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        // Editable fields
        EditableProfileField("Phone", phone) { phone = it }
        EditableProfileField("Date of Birth (YYYY-MM-DD)", birthday) { birthday = it }
        EditableProfileField("Occupation", occupation) { occupation = it }
        EditableProfileField("Address", address) { address = it }
        EditableProfileField("City", city) { city = it }
        EditableProfileField("Postal Code", postalCode) { postalCode = it }

        Spacer(Modifier.height(12.dp))

        if (saveSuccess) {
            Text("✓ Profile updated", color = GreenSuccess, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }

        ArzookButton(
            onClick = {
                saving = true
                saveSuccess = false
                authViewModel?.updateProfile(
                    UpdateProfileRequest(
                        phoneNumber = phone.ifEmpty { null },
                        birthday = birthday.ifEmpty { null },
                        occupation = occupation.ifEmpty { null },
                        address = address.ifEmpty { null },
                        city = city.ifEmpty { null },
                        postalCode = postalCode.ifEmpty { null },
                    )
                ) { success -> saving = false; saveSuccess = success }
            },
            enabled = !saving,
            containerColor = Brown,
            contentColor = Color.White
        ) {
            if (saving) CircularProgressIndicator(
                Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            else Text("Save Changes", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        // Document upload section - only show if profile info is filled
        val profileComplete =
            phone.isNotBlank() && birthday.isNotBlank() && address.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()
        if (profileComplete || user.photoIdAttached == true) {
            PhotoIdSection(
                user = user,
                onUploadPhotoId = { photoIdPicker.launch() },
                onUploadUtilityBill = { utilityBillPicker.launch() },
                photoIdUploaded = photoIdUploaded,
                bothUploaded = user.utilityBillFileName != null && user.photoIdFileName != null
            )
        } else {
            Text(
                "Please fill in your profile information above before uploading documents.",
                color = OrangeDark,
                fontSize = 13.sp
            )
        }
        if (uploading) {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator(Modifier.size(24.dp))
        }
        uploadMsg?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                it,
                color = if ("✓" in it) GreenSuccess else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }

        // Change password (local accounts only)
        if (user.provider == "local") {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            ArzookButton(
                onClick = { showChangePassword = !showChangePassword }
            ) { Text(if (showChangePassword) "Cancel" else "Change Password") }
            AnimatedVisibility(visible = showChangePassword) { ChangePasswordForm() }
        }
    }
}

@Composable
private fun EditableProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp) },
        readOnly = !editing,
        trailingIcon = {
            IconButton(onClick = {
                editing = !editing
            }) {
                Icon(
                    if (editing) Icons.Filled.Check else Icons.Filled.Edit,
                    contentDescription = if (editing) "Done" else "Edit",
                    tint = Brown
                )
            }
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .focusRequester(focusRequester)
    )
    LaunchedEffect(editing) {
        if (editing) focusRequester.requestFocus()
    }
}

@Composable
private fun PhotoIdSection(
    user: AuthenticatedData,
    onUploadPhotoId: () -> Unit,
    onUploadUtilityBill: () -> Unit,
    photoIdUploaded: Boolean = false,
    bothUploaded: Boolean = false,
) {
    val verified = user.photoIdVerified == true
    val expired = remember(user.photoIdExpiryDate) {
        user.photoIdExpiryDate?.take(10)?.let { expiry ->
            currentDateString() > expiry
        } ?: false
    }

    if (verified && !expired) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(GreenLight).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Verified, null, tint = GreenDark, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Your photo ID has been verified", color = GreenDark, fontWeight = FontWeight.Bold)
        }
        return
    }

    if (expired) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(RedLight).padding(12.dp)
        ) {
            if (photoIdUploaded) {
                Text("Your documents are under review.", color = BrownText, fontSize = 13.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Warning,
                        null,
                        tint = RedDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Photo ID Expired", fontWeight = FontWeight.Bold, color = RedDark)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your photo ID expired on ${user.photoIdExpiryDate?.take(10)}. Please upload a new valid government-issued photo ID.",
                    fontSize = 13.sp, color = RedDark
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onUploadPhotoId,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedDark)
                ) { Text("Upload New Photo ID", color = Color.White) }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(OrangeLight).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, tint = OrangeDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Documents Required", fontWeight = FontWeight.Bold, color = OrangeDark)
        }
        Spacer(Modifier.height(6.dp))
        if (bothUploaded) {
            Text("Your documents are under review.", color = BrownText, fontSize = 13.sp)
            return
        } else {
            if (user.photoIdRejected != null) {
                Text(
                    "Your ID was rejected: ${user.photoIdRejectedNote ?: "Please upload a new valid ID."}",
                    color = RedDark, fontSize = 13.sp
                )
            } else if(user.utilityBillFileName != null && user.photoIdFileName != null){
                Text("Your documents are under review.", color = BrownText, fontSize = 13.sp)
                return
            }
        }//else {
            Column {
                Text(
                    "Please upload the following two documents:",
                    fontSize = 13.sp, color = BrownText, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "• Government-issued photo ID (passport, driver's license)",
                    fontSize = 13.sp,
                    color = BrownText
                )
                Text(
                    "• Proof of address (utility bill, bank statement)",
                    fontSize = 13.sp,
                    color = BrownText
                )
            }

        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onUploadPhotoId,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow40,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    if (user.photoIdRejected != null) "New Photo ID" else "Upload Photo ID",
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onUploadUtilityBill,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brown)
            ) { Text("Upload Utility Bill", color = Color.White, fontSize = 12.sp) }
        }
     //   }
    }
}

@Composable
private fun ChangePasswordForm() {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color.White).padding(16.dp)
    ) {
        if (errorMsg.isNotEmpty()) Text(errorMsg, color = MaterialTheme.colorScheme.error)
        if (successMsg.isNotEmpty()) Text(successMsg, color = GreenSuccess)
        Spacer(Modifier.height(8.dp))
        PasswordField(
            "Old Password",
            oldPassword,
            oldVisible,
            { oldPassword = it },
            { oldVisible = !oldVisible })
        Spacer(Modifier.height(8.dp))
        PasswordField(
            "New Password",
            newPassword,
            newVisible,
            { newPassword = it },
            { newVisible = !newVisible })
        Spacer(Modifier.height(8.dp))
        PasswordField(
            "Confirm Password",
            confirmPassword,
            confirmVisible,
            { confirmPassword = it },
            { confirmVisible = !confirmVisible })
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                errorMsg = when {
                    oldPassword.isEmpty() -> "Enter your old password."
                    newPassword.length < 6 -> "New password must be at least 6 characters."
                    newPassword != confirmPassword -> "Passwords don't match."
                    else -> {
                        successMsg = "Password updated."; ""
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brown),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Update Password", color = Color.White) }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label, fontSize = 10.sp) }, singleLine = true,
        visualTransformation = if (visible) androidx.compose.ui.text.input.VisualTransformation.None
        else androidx.compose.ui.text.input.PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
