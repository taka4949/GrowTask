package com.example.growtask

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Task : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var title: String = ""
    var isDone: Boolean = false
    var createdAt: String = System.currentTimeMillis().toString()

    // ★追加：タスクを実行する日（yyyy-MM-dd 形式の文字で管理）
    // デフォルトは「今日」になる
    var targetDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}