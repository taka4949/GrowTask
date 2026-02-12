package com.example.growtask

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserStats : RealmObject {
    @PrimaryKey
    var id: String = "USER_001" // ユーザーは一人だけ
    var totalPoints: Int = 0
    var currentExp: Int = 0
    var treeStage: Int = 0
}