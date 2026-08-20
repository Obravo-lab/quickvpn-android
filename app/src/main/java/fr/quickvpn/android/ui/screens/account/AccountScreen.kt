package fr.quickvpn.android.ui.screens.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.quickvpn.android.R
import fr.quickvpn.android.ui.navigation.viewModelFactory
import fr.quickvpn.android.ui.theme.CtaButton

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onDeleted: () -> Unit,
    vm: AccountViewModel = viewModel<AccountViewModel>(factory = viewModelFactory<AccountViewModel>())
) {
    val state by vm.ui.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (state.loggedOut) {
        onLoggedOut()
        return
    }
    if (state.deleted) {
        onDeleted()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.account_title),
            style = MaterialTheme.typography.titleLarge
        )
        state.user?.let {
            Text(
                text = it.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(20.dp))

        // --- Mot de passe ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.account_section_password),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.passwordCurrent,
                    onValueChange = vm::onPasswordCurrent,
                    label = { Text(stringResource(R.string.account_password_current)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.passwordNew,
                    onValueChange = vm::onPasswordNew,
                    label = { Text(stringResource(R.string.account_password_new)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.passwordConfirm,
                    onValueChange = vm::onPasswordConfirm,
                    label = { Text(stringResource(R.string.account_password_confirm)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                state.passwordError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = passwordErrorText(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (state.passwordSuccess) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.account_password_success),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(12.dp))
                CtaButton(
                    onClick = vm::changePassword,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.account_password_submit))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Abonnement ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.account_section_subscription),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                state.user?.let { user ->
                    val active = user.hasVpnAccess
                    Text(
                        text = stringResource(
                            if (active) R.string.account_sub_active else R.string.account_sub_inactive
                        ),
                        color = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    user.subscriptionEnd?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.account_sub_end, it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                state.cancelInfo?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.account_cancel_info),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                state.cancelError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (it == "network") {
                            stringResource(R.string.error_network)
                        } else {
                            it
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    enabled = state.user?.hasVpnAccess == true && state.cancelInfo == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.account_cancel))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Supprimer le compte ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.account_section_delete),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.account_delete_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                state.deleteError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (it == "network") {
                            stringResource(R.string.error_network)
                        } else {
                            it
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.account_delete_btn))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Text(stringResource(R.string.dashboard_logout))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Text(stringResource(R.string.plans_back))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.account_cancel_confirm_title)) },
            text = { Text(stringResource(R.string.account_cancel_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    vm.cancelSubscription()
                }) {
                    Text(stringResource(R.string.account_cancel_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.plans_back))
                }
            }
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            requiredWord = stringResource(R.string.account_delete_word),
            deleting = state.deleting,
            onConfirm = { password, word ->
                showDeleteDialog = false
                vm.deleteAccount(password, word)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun DeleteDialog(
    requiredWord: String,
    deleting: Boolean,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var word by remember { mutableStateOf("") }
    val enabled = password.isNotEmpty() && word.trim().uppercase() == requiredWord && !deleting

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.account_delete_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.account_delete_dialog_msg))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.account_password_current)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text(stringResource(R.string.account_delete_word_hint, requiredWord)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (deleting) {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password, word.trim().uppercase()) },
                enabled = enabled
            ) {
                Text(
                    stringResource(R.string.account_delete_submit),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.plans_back))
            }
        }
    )
}

@Composable
private fun passwordErrorText(key: String): String = when (key) {
    "network" -> stringResource(R.string.error_network)
    "pw_mismatch" -> stringResource(R.string.account_pw_mismatch)
    "pw_short" -> stringResource(R.string.account_pw_short)
    else -> key
}
