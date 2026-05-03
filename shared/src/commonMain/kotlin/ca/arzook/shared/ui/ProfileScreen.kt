package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.AuthenticatedData
import ca.arzook.shared.model.UpdateProfileRequest

private val PBrown = Color(0xFF8B4513)

@Composable
fun ProfileScreen(
    user: AuthenticatedData?,
    isLoggedIn: Boolean = false,
    authViewModel: AuthViewModel? = null,
    onUploadPhotoId: () -> Unit = {},
    onUploadUtilityBill: () -> Unit = {},
) {
    if (user == null) {
        Box(Modifier.fillMaxSize().background(Cream40), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        modifier = Modifier.background(Cream40).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.AccountCircle, null, Modifier.size(80.dp), tint = PBrown)
        Spacer(Modifier.height(8.dp))
        Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Email, null, tint = PBrown, modifier = Modifier.size(18.dp))
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
            Text("✓ Profile updated", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }

        Button(
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
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PBrown),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (saving) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Save Changes", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        // Document upload section
        PhotoIdSection(
            user = user,
            onUploadPhotoId = onUploadPhotoId,
            onUploadUtilityBill = onUploadUtilityBill
        )

        // Change password (local accounts only)
        if (user.provider == "local") {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { showChangePassword = !showChangePassword },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow40, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (showChangePassword) "Cancel" else "Change Password") }
            AnimatedVisibility(visible = showChangePassword) { ChangePasswordForm() }
        }
    }
}

@Composable
private fun EditableProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = !editing,
        trailingIcon = {
            IconButton(onClick = { editing = !editing }) {
                Icon(
                    if (editing) Icons.Filled.Check else Icons.Filled.Edit,
                    contentDescription = if (editing) "Done" else "Edit",
                    tint = PBrown
                )
            }
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
private fun PhotoIdSection(
    user: AuthenticatedData,
    onUploadPhotoId: () -> Unit,
    onUploadUtilityBill: () -> Unit,
) {
    val verified = user.photoIdVerified == true
    val attached = user.photoIdAttached == true

    if (verified) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Verified, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Your photo ID has been verified", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF3E0)).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Documents Required", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        }
        Spacer(Modifier.height(6.dp))

        if (attached && !verified) {
            if (user.photoIdRejected != null) {
                Text("Your ID was rejected: ${user.photoIdRejectedNote ?: "Please upload a new valid ID."}",
                    color = Color(0xFFB71C1C), fontSize = 13.sp)
            } else {
                Text("Your documents are under review.", color = Color(0xFF5D4037), fontSize = 13.sp)
                return
            }
        } else {
            Text(
                "Please upload the following two documents:",
                fontSize = 13.sp, color = Color(0xFF5D4037), fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("• Government-issued photo ID (passport, driver's license)", fontSize = 13.sp, color = Color(0xFF5D4037))
            Text("• Proof of address (utility bill, bank statement)", fontSize = 13.sp, color = Color(0xFF5D4037))
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onUploadPhotoId,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow40, contentColor = Color.Black)
            ) { Text(if (user.photoIdRejected != null) "New Photo ID" else "Upload Photo ID", fontSize = 12.sp) }
            Button(
                onClick = onUploadUtilityBill,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PBrown)
            ) { Text("Upload Utility Bill", color = Color.White, fontSize = 12.sp) }
        }
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
        if (successMsg.isNotEmpty()) Text(successMsg, color = Color(0xFF4CAF50))
        Spacer(Modifier.height(8.dp))
        PasswordField("Old Password", oldPassword, oldVisible, { oldPassword = it }, { oldVisible = !oldVisible })
        Spacer(Modifier.height(8.dp))
        PasswordField("New Password", newPassword, newVisible, { newPassword = it }, { newVisible = !newVisible })
        Spacer(Modifier.height(8.dp))
        PasswordField("Confirm Password", confirmPassword, confirmVisible, { confirmPassword = it }, { confirmVisible = !confirmVisible })
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                errorMsg = when {
                    oldPassword.isEmpty() -> "Enter your old password."
                    newPassword.length < 6 -> "New password must be at least 6 characters."
                    newPassword != confirmPassword -> "Passwords don't match."
                    else -> { successMsg = "Password updated."; "" }
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PBrown),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Update Password", color = Color.White) }
    }
}

@Composable
private fun PasswordField(label: String, value: String, visible: Boolean, onChange: (String) -> Unit, onToggle: () -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
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
