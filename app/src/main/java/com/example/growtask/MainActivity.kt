package com.example.growtask

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageTree: ImageView
    private lateinit var textLevel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        imageTree = findViewById(R.id.imageTree)
        textLevel = findViewById(R.id.textLevel)
        val fab: FloatingActionButton = findViewById(R.id.fabAdd)

        val config = RealmConfiguration.Builder(
            schema = setOf(Task::class, UserStats::class)
        ).build()
        realm = Realm.open(config)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(emptyList()) { task ->
            toggleTaskStatus(task)
        }
        recyclerView.adapter = adapter

        loadData()

        fab.setOnClickListener {
            showAddTaskDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    // ■ データを読み込んで画面を更新する
    private fun loadData() {
        // ★今日の「yyyy-MM-dd」を作る
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.Main).launch {
            // ★変更：「targetDate」が「today」と同じものだけを探す
            val tasks = realm.query<Task>("targetDate == $0", today).sort("createdAt").find()
            adapter.updateData(tasks)

            var stats = realm.query<UserStats>().first().find()
            if (stats == null) {
                realm.write { copyToRealm(UserStats()) }
                stats = realm.query<UserStats>().first().find()
            }
            updateTreeUI(stats!!)
        }
    }

    private fun showAddTaskDialog() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("今日のタスクを追加")
            .setView(editText)
            .setPositiveButton("追加") { _, _ ->
                val title = editText.text.toString()
                if (title.isNotEmpty()) addTask(title)
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun addTask(title: String) {
        // 今日の日付を取得
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            realm.write {
                // タスクを作る時に、日付もセットされる（Task.ktの初期値で自動的に今日になる）
                copyToRealm(Task().apply {
                    this.title = title
                    this.targetDate = today // 念のため明示的に入れる
                })
            }
            withContext(Dispatchers.Main) { loadData() }
        }
    }

    private fun toggleTaskStatus(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            var message = "" // 表示するメッセージを入れる箱

            realm.write {
                val liveTask = findLatest(task) ?: return@write
                liveTask.isDone = !liveTask.isDone

                // ユーザーデータを探す（なければ作る！）
                var stats = query<UserStats>().first().find()
                if (stats == null) {
                    stats = copyToRealm(UserStats())
                }

                if (liveTask.isDone) {
                    // ■ チェックを入れた時
                    stats.currentExp += 20
                    message = "経験値 +20 ゲット！" // メッセージセット

                    if (stats.currentExp >= 100) {
                        stats.currentExp = 0
                        stats.totalPoints += 1
                        message = "レベルアップ！！" // 上書き
                        if (stats.treeStage < 3) stats.treeStage += 1
                    }
                } else {
                    // ■ チェックを外した時
                    stats.currentExp = maxOf(0, stats.currentExp - 20)
                    message = "経験値が戻りました" // メッセージセット
                }
            }

            // 画面更新とメッセージ表示
            withContext(Dispatchers.Main) {
                loadData()
                // メッセージがあれば必ず表示する
                if (message.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }

                // レベルアップの時だけダイヤログも出す
                if (message == "レベルアップ！！") {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("おめでとう！")
                        .setMessage("木が成長しました！")
                        .setPositiveButton("やったね", null)
                        .show()
                }
            }
        }
    }

    private fun updateTreeUI(stats: UserStats) {
        // ★日付を表示に追加
        val dateLabel = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date())
        textLevel.text = "$dateLabel | Lv.${stats.totalPoints + 1} (${stats.currentExp}%)"

        val imageRes = when (stats.treeStage) {
            0 -> R.drawable.stage_0
            1 -> R.drawable.stage_1
            2 -> R.drawable.stage_2
            3 -> R.drawable.stage_3
            else -> R.drawable.stage_0
        }
        imageTree.setImageResource(imageRes)
    }
}