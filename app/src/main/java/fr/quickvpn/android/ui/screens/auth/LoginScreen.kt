package fr.quickvpn.android.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.quickvpn.android.R
import fr.quickvpn.android.ui.navigation.viewModelFactory
import fr.quickvpn.android.ui.theme.AuthBorder
import fr.quickvpn.android.ui.theme.AuthMuted
import fr.quickvpn.android.ui.theme.AuthPrimary
import fr.quickvpn.android.ui.theme.AuthText

@Composable
fun authFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = AuthText,
        unfocusedTextColor = AuthText,
        focusedBorderColor = AuthPrimary,
        unfocusedBorderColor = AuthBorder,
        cursorColor = AuthPrimary,
        focusedLabelColor = AuthMuted,
        unfocusedLabelColor = AuthMuted
    )
}

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoRegister: () -> Unit,
    onGoForgot: () -> Unit,
    vm: AuthViewModel = viewModel<AuthViewModel>(factory = viewModelFactory<AuthViewModel>())
) {
    val state by vm.ui.collectAsState()

    if (state.loggedIn) {
        onLoggedIn()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AuthText
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::onEmail,
                    label = { Text(stringResource(R.string.login_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPassword,
                    label = { Text(stringResource(R.string.login_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors()
                )

                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorText(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = vm::login,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.login_submit))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.login_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.login_signup),
                    color = AuthPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable(onClick = onGoRegister)
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onGoLogin: () -> Unit,
    vm: AuthViewModel = viewModel<AuthViewModel>(factory = viewModelFactory<AuthViewModel>())
) {
    val state by vm.ui.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AuthText
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.name,
                    onValueChange = vm::onName,
                    label = { Text(stringResource(R.string.register_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::onEmail,
                    label = { Text(stringResource(R.string.register_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPassword,
                    label = { Text(stringResource(R.string.register_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.confirm,
                    onValueChange = vm::onConfirm,
                    label = { Text(stringResource(R.string.register_confirm)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = authFieldColors()
                )

                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorText(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                state.info?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.register_check_email),
                        color = AuthPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = vm::register,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuthPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.register_submit))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.register_has_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.register_login),
                    color = AuthPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable(onClick = onGoLogin)
                )
            }
        }
    }
}

@Composable
private fun errorText(key: String): String =
    if (key == "network") stringResource(R.string.error_network) else key
