package de.proskor.cft.model.ea
import de.proskor.cft.model.And
import de.proskor.cft.model.ea.peers.EAPeer

private class EAAnd(initialPeer: EAPeer) extends EAGate(initialPeer) with And