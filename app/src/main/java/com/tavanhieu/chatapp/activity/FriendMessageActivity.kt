package com.tavanhieu.chatapp.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.tavanhieu.chatapp.R
import com.tavanhieu.chatapp.adpater.AdapterListMessage
import com.tavanhieu.chatapp.fcm_notifications.MyFirebaseMessagingSend
import com.tavanhieu.chatapp.m_class.Conversations
import com.tavanhieu.chatapp.m_class.HangSo
import com.tavanhieu.chatapp.m_class.Message
import com.tavanhieu.chatapp.m_class.User
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class FriendMessageActivity : UserActiveActivity() {
    private lateinit var imgBack: ImageView
    private lateinit var imgFriend: CircleImageView
    private lateinit var txtFriend: TextView
    private lateinit var imgCall: ImageView
    private lateinit var imgVideo: ImageView
    private lateinit var imgThongTin: ImageView
    private lateinit var cameraBottom: ImageView
    private lateinit var pictureBottom: ImageView
    private lateinit var edtMessBottom: EditText
    private lateinit var sendMessBottom: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var rcvListMessage: RecyclerView
    private lateinit var uidReceiver: String
    private lateinit var hoTenReceiver: String
    private lateinit var uidSender: String
    private lateinit var mAdapter: AdapterListMessage
    private var arr = ArrayList<Message>()

    private lateinit var nguoiNhan: User
    private lateinit var nguoiGui: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_message)
        anhXa()
        try {
        //Nhận info từ màn hình homeChat:
        hoTenReceiver = intent.getStringExtra("hoTen").toString()
        //Lấy id của người gửi và người nhận:
        uidReceiver = intent.getStringExtra("receiverId")!!
        uidSender   = FirebaseAuth.getInstance().currentUser?.uid!!

        //Load ảnh toolbar người nhận - Load ảnh người nhận:
        FirebaseStorage.getInstance().reference.child(HangSo.KEY_USER)
            .child(uidReceiver).downloadUrl
            .addOnSuccessListener {
                if(it != null) {
                    Picasso.get().load(it).into(imgFriend)
                }
            }
            .addOnFailureListener {}

        //Lấy ra Người gửi/ Người nhận hiện tại:
        Firebase.database.reference.child(HangSo.KEY_USER)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(data in snapshot.children) {
                        val user = data.getValue(User::class.java)!!
                        if(user.uid.equals(uidSender))
                            nguoiGui = user

                        if(user.uid.equals(uidReceiver))
                            nguoiNhan = user
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        //Gán tên lên toolbar, đọc message:
        txtFriend.text = hoTenReceiver
        docMessage()
        //Ánh xạ adapter cho message
        mAdapter = AdapterListMessage(this)
        mAdapter.setData(arr)
        rcvListMessage.adapter = mAdapter

        mOnClick()} catch (e: Exception)
        {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun anhXa() {
        imgBack     = findViewById(R.id.imgArrowBackToolBarMessage)
        imgFriend   = findViewById(R.id.imgIconFriendToolbarMessage)
        txtFriend   = findViewById(R.id.txtNameFriendToolbarMessage)
        imgCall     = findViewById(R.id.imgCallActionBarMessage)
        imgVideo    = findViewById(R.id.imgVideoActionBarMessage)
        imgThongTin = findViewById(R.id.imgInformationActionBarMessage)
        cameraBottom   = findViewById(R.id.cameraMenuBottomMessage)
        pictureBottom  = findViewById(R.id.pictureMenuBottomMessage)
        edtMessBottom  = findViewById(R.id.inputMessMenuBottonMessage)
        sendMessBottom = findViewById(R.id.sendMenuBottomMessage)
        progressBar    = findViewById(R.id.progessBarFriendMessage)
        rcvListMessage = findViewById(R.id.rcvListMessFriendMessage)
    }

    private fun mOnClick() {
        imgBack.setOnClickListener { onBackPressed() }
        sendMessBottom.setOnClickListener { sendMessage() }
    }
    private fun sendMessage() {
        val mess = edtMessBottom.text.trim().toString()
        val date = Date()

        //Kiểm tra nếu tin nhắn không rỗng thì cho phép thực hiện:
        if(mess.isNotEmpty() || mess != "") {
            //Add tin nhắn cho người gửi:
            Firebase.database.reference.child(HangSo.KEY_CHATS_TTCN)
                .child(uidSender+uidReceiver)
                .child(HangSo.KEY_MESSAGE).push()
                .setValue(Message(date, mess, uidSender, null))
                .addOnSuccessListener {

                    //Add tin nhắn cho người nhận:
                    Firebase.database.reference.child(HangSo.KEY_CHATS_TTCN)
                        .child(uidReceiver+uidSender)
                        .child(HangSo.KEY_MESSAGE).push()
                        .setValue(Message(date, mess, uidSender, null))

                    //Add người đã nhắn:
                    Firebase.database.reference.child(HangSo.KEY_CONVERSATIONS)
                        .child(uidSender)
                        .child(uidReceiver)
                        .setValue(Conversations(nguoiNhan.hoTen!!, "Bạn: $mess", date, uidReceiver, nguoiNhan.anh))

                    //Add người nhắn cho người nhận:
                    Firebase.database.reference.child(HangSo.KEY_CONVERSATIONS)
                        .child(uidReceiver)
                        .child(uidSender)
                        .setValue(Conversations(nguoiGui.hoTen!!, mess, date, uidSender, nguoiGui.anh))
                }
            //Gửi thông báo:
            MyFirebaseMessagingSend.pushNotifications(this, nguoiNhan.token!!, "${nguoiGui.hoTen!!} gửi tới ${nguoiNhan.hoTen!!}", mess)
        }
        //Sau khi gửi xong xóa nội dung nhắn trước đó:
        edtMessBottom.text = null
    }

    private fun docMessage() {
        progressBar.visibility = View.VISIBLE

        //Đọc message từ firebase và gán vào mảng:
        Firebase.database.reference.child(HangSo.KEY_CHATS_TTCN)
            .child(uidSender+uidReceiver)
            .child(HangSo.KEY_MESSAGE)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    arr.clear()
                    //Thêm dữ liệu tin nhắn vào mảng:
                    for(data in snapshot.children) {
                        val mess = data.getValue(Message::class.java)
                        arr.add(mess!!)
                    }
                    mAdapter.notifyDataSetChanged()
                    rcvListMessage.scrollToPosition(arr.size - 1)
                    progressBar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}