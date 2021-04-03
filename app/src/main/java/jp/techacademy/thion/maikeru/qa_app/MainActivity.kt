package jp.techacademy.thion.maikeru.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.get
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar  // ← 追加
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
// findViewById()を呼び出さずに該当Viewを取得するために必要となるインポート宣言
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {    // ← 修正

    private var mGenre = 0    // ← 追加

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList:ArrayList<Question>
    private lateinit var mAdapter:QuestionsListAdapter

    private var snapshotListener:ListenerRegistration?=null

/*    private var mGenreRef:DatabaseReference?=null

    private val mEventListener=object:ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map=dataSnapshot.value as Map<String,String>
            val title=map["title"]?:""
            val body =map["body"]?:""
            val name=map["name"]?:""
            val uid=map["uid"]?:""
            val imageString=map["image"]?:""
            val bytes=
                if(imageString.isNotEmpty()){
                    Base64.decode(imageString, Base64.DEFAULT)
                }else{
                    byteArrayOf()
                }
            val answerArrayList=ArrayList<Answer>()
            val answerMap=map["answers"] as Map<String,String>?
            if (answerMap!=null){
                for (key in answerMap.keys){
                    val temp=answerMap[key] as Map<String,String>
                    val answerBody=temp["body"]?:""
                    val answerName=temp["name"]?:""
                    val answerUid=temp["uid"]?:""
                    val answer=Answer(answerBody,answerName,answerUid,key)
                    answerArrayList.add(answer)
                }
            }
            val question=Question(title,body,name,uid,dataSnapshot.key?:"",mGenre,bytes,answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map=dataSnapshot.value as Map<String,String>

            for (question in mQuestionArrayList){
                if (dataSnapshot.key.equals(question.questionUid)){
                    question.answers.clear()
                    val answerMap=map["answers"] as Map<String,String>?
                    if(answerMap!=null){
                        for(key in answerMap.keys){
                            val temp=answerMap[key] as Map<String,String>
                            val answerBody=temp["body"]?:""
                            val answerName=temp["name"]?:""
                            val answerUid=temp["uid"]?:""
                            val answer=Answer(answerBody,answerName,answerUid,key)
                            question.answers.add(answer)
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onCancelled(p0: DatabaseError) {
        }

    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // idがtoolbarがインポート宣言により取得されているので
        // id名でActionBarのサポートを依頼
        setSupportActionBar(toolbar)

        // fabにClickリスナーを登録
        // fabにClickリスナーを登録
        // --- ここから ---
        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(view, getString(R.string.question_no_select_genre), Snackbar.LENGTH_LONG).show()
            } else {

            }
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }
        // --- ここまで修正 ---

        // ～～ ここから
        // ナビゲーションドロワーの設定
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // ～～ ここまで
        mDatabaseReference=FirebaseDatabase.getInstance().reference

        mAdapter= QuestionsListAdapter(this)
        mQuestionArrayList=ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener{parent,view,position,id->

            val intent=Intent(applicationContext,QuestionDetailListAdapter::class.java)
            intent.putExtra("question",mQuestionArrayList[position])
            startActivity(intent)

        }
    }

    override fun onResume() {
        super.onResume()
        if(mGenre==0){
            onNavigationItemSelected(nav_view.menu.getItem(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id=item.itemId
        if(id==R.id.action_settings){
            val intent=Intent(applicationContext,SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
/*        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }*/
    }

    // ～～ ここから
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        } else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            toolbar.title = getString(R.string.menu_compter_label)
            mGenre = 4
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter=mAdapter

        snapshotListener?.remove()

        snapshotListener=FirebaseFirestore.getInstance()
            .collection(ContentsPATH)
            .whereEqualTo("genre",mGenre)
            .addSnapshotListener{querySnapshot,firebaseFirestoreException->
                if(firebaseFirestoreException!=null){
                    return@addSnapshotListener
                }
                var questions=listOf<Question>()
                val results=querySnapshot?.toObjects(FirestoreQuestion::class.java)
                results?.also{
                    questions=it.map{firestoreQuestion ->
                        val bytes=
                            if(firestoreQuestion.image.isNotEmpty()){
                                Base64.decode(firestoreQuestion.image,Base64.DEFAULT)
                            }else{
                                byteArrayOf()
                            }
                        Question(firestoreQuestion.title,firestoreQuestion.body,firestoreQuestion.name,firestoreQuestion.uid,
                        firestoreQuestion.id,firestoreQuestion.genre ,bytes,firestoreQuestion.answers)

                    }
                }
                mQuestionArrayList.clear()
                mQuestionArrayList.addAll(questions)
                mAdapter.notifyDataSetChanged()

            }

        return true
    }
    // ～～ ここまで
}