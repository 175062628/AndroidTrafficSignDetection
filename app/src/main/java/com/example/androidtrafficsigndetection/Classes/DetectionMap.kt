package com.example.androidtrafficsigndetection.Classes

import com.example.androidtrafficsigndetection.DataModel.ArgumentPair

class DetectionMap {
    private val kvMap = hashMapOf<Any, Any>()
    fun setMap(listKey: List<Any>, listValue: List<Any>) {
        for(i in listKey.indices){
            kvMap[listKey[i]] = listValue[i]
        }
    }
    fun setMap(kvList: MutableList<ArgumentPair>) {
        for(argumentPair in kvList){
            kvMap[argumentPair.key] = argumentPair.value
        }
    }
    fun getMap(): HashMap<Any, Any> {
        return kvMap
    }
}