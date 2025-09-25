package io.gitee.jesse205.activitylauncher.utils

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

open class PropertyChangeBean {
    protected val propertyChangeSupport = PropertyChangeSupport(this)

    fun addPropertyChangeListener(listener: ()->Unit) {
        propertyChangeSupport.addPropertyChangeListener{

        }
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(listener)
    }
}
