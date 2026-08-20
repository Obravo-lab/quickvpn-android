package fr.quickvpn.android.ui.screens.auth

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.quickvpn.android.R
import fr.quickvpn.android.ui.theme.CtaButton
import fr.quickvpn.android.ui.navigation.viewModelFactory

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
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmail,
            label = { Text(stringResource(R.string.login_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPassword,
            label = { Text(stringResource(R.string.login_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
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
        CtaButton(
            onClick = vm::login,
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.login_submit))
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onGoForgot,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(stringResource(R.string.login_forgot))
        }
        Spacer(Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.login_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.login_signup),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.login_signup))
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
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = vm::onName,
            label = { Text(stringResource(R.string.register_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmail,
            label = { Text(stringResource(R.string.register_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPassword,
            label = { Text(stringResource(R.string.register_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.confirm,
            onValueChange = vm::onConfirm,
            label = { Text(stringResource(R.string.register_confirm)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
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
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))
        CtaButton(
            onClick = vm::register,
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.register_login))
        }
    }
}

@Composable
private fun errorText(key: String): String =
    if (key == "network") stringResource(R.string.error_network) else key
