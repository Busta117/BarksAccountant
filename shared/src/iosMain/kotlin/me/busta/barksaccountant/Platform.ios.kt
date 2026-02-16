package me.busta.barksaccountant

import platform.UIKit.UIDevice

actual fun platformName(): String =
    "${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}"
