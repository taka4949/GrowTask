package com.example.growtask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 1. クラス定義：データ(tasks)と、クリック時の動作(onItemClicked)を受け取る
class TaskAdapter(
    private var tasks: List<Task>,
    private val onItemClicked: (Task) -> Unit // ← これが「ラムダ式」。クリックされたらメイン画面に知らせる通知機能。
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // 2. ViewHolder：画面の部品（View）を保持しておく箱
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textTitle)
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxDone)
    }

    // 3. 画面（レイアウト）を作る場所
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // 4. データと画面を結びつける場所（Bind）
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // データをセット
        holder.title.text = task.title
        holder.checkBox.isChecked = task.isDone

        // チェックボックスが押された時の動作
        holder.checkBox.setOnClickListener {
            // メイン画面に「このタスクが押されたよ！」と伝える
            onItemClicked(task)
        }
    }

    // 5. データの数を返す
    override fun getItemCount(): Int {
        return tasks.size
    }

    // 6. データを更新するメソッド（あとでメイン画面から呼ぶ）
    fun updateData(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged() // 「データ変わったから再描画して！」という命令
    }
}