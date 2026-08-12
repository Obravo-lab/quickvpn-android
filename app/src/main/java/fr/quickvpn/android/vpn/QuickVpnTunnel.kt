package fr.quickvpn.android.vpn

import com.wireguard.android.backend.Tunnel

class QuickVpnTunnel(private val name: String) : Tunnel {

    var state: Tunnel.State = Tunnel.State.DOWN
        private set

    override fun getName(): String = name

    override fun onStateChange(newState: Tunnel.State) {
        state = newState
    }
}
