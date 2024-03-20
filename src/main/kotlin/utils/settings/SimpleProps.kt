package utils.settings

import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private fun autogeneratedComments() =
    "Auto regenerated on ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"

interface PersistedSettings {
    val settingsGroup: String
}

class SimpleProps(path: String) {
    private val propFile = File(path)
    private val properties: Properties = Properties()

    init {
        try {
            properties.load(propFile.reader())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun <T> bind(deserializer: (String) -> T, default: T): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T =
                properties.getProperty(keyName(thisRef, property))?.let { deserializer(it) } ?: default

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                properties.setProperty(keyName(thisRef, property), value.toString())
                properties.store(propFile.writer(), autogeneratedComments())
            }

            private fun keyName(thisRef: Any?, property: KProperty<*>): String {
                val group: String = (thisRef as? PersistedSettings)?.settingsGroup ?: "global"
                return "$group.${property.name}"
            }
        }
    }
}