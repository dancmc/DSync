package io.dancmc.dsync

interface IndexServiceComm{
    fun messageReceived(message:Int, obj:Any?)
}