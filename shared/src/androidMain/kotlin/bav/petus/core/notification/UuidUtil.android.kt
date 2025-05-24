package bav.petus.core.notification

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
actual fun getUUID(): String {
    return Uuid.random().toString()
}