package jp.techacademy.thion.maikeru.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var mQuestion:Question
    private lateinit var mAdapter:QuestionDetailListAdapter
    private lateinit var mAnswerRef:DatabaseReference

    private val mEventListener=object:ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val extras=intent.extras
        mQuestion=extras!!.get("question") as Question
        title=mQuestion.title

        mAdapter= QuestionDetailListAdapter(this,mQuestion)
        listView.adapter=mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener{
            val user= FirebaseAuth.getInstance().currentUser

            if(user==null){
                val intent= Intent(applicationContext,LoginActivity::class.java)
                startActivity(intent)
            }else{
                val intent=Intent(applicationContext,AnswerSendActivity::class.java)
                intent.putExtra("question",mQuestion)
                startActivity(intent)

            }

            val databaseReference=FirebaseDatabase.getInstance().reference
            mAnswerRef=databaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
                AnswersPATH)
            mAnswerRef.addChildEventListener(mEventListener)
        }
    }
}